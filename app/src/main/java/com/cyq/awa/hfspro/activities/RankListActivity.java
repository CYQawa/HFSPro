package com.cyq.awa.hfspro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.RankListAdapter;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.cyq.awa.hfspro.tools.RankSnapshot;
import com.cyq.awa.hfspro.tools.network.GsonModel.ApiResponse;
import com.cyq.awa.hfspro.tools.network.GsonModel.ExamOverviewData;
import com.cyq.awa.hfspro.tools.network.GsonModel.LastExamData;
import com.cyq.awa.hfspro.tools.network.NetworkErrors;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 最近考试排名历史记录。
 *
 * <p>接口只提供 7 天内的排名数据。本页每次成功获取到排名后都会把快照写入本地，
 * 过期后仍可离线查看；打开页面时先渲染本地记录，再静默刷新。
 */
public class RankListActivity extends AppCompatActivity {

  private static final String TAG = "RankListActivity";

  private SwipeRefreshLayout swipeRefresh;
  private RecyclerView recyclerView;
  private View emptyContainer;
  private RankListAdapter adapter;
  private final List<RankSnapshot> items = new ArrayList<>();

  private RetrofitTools.ApiService apiService;
  private boolean refreshing = false;
  private boolean firstLoadStarted = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_rank_list);

    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setNavigationOnClickListener(v -> finish());

    apiService = RetrofitTools.RetrofitClient.getAuthService();

    recyclerView = findViewById(R.id.recycler);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter = new RankListAdapter(items, this::openDetail);
    recyclerView.setAdapter(adapter);

    swipeRefresh = findViewById(R.id.swipe_refresh);
    swipeRefresh.setColorSchemeResources(R.color.md_theme_primary);
    // SwipeRefreshLayout 的唯一子布局是 FrameLayout，默认无法判断列表是否还能上滑，
    // 这里显式委托给 RecyclerView，避免列表滚动时误触发下拉刷新。
    swipeRefresh.setOnChildScrollUpCallback(
        (parent, child) -> recyclerView.canScrollVertically(-1));
    swipeRefresh.setOnRefreshListener(this::refreshFromNetwork);

    emptyContainer = findViewById(R.id.empty_container);
    findViewById(R.id.btn_empty_refresh).setOnClickListener(v -> refreshFromNetwork());

    loadFromLocal(true);
  }

  @Override
  protected void onResume() {
    super.onResume();
    // 从详情页返回（可能删除了记录）后，用本地数据刷新列表，不重复请求网络
    if (firstLoadStarted) {
      loadFromLocal(false);
    }
  }

  private void loadFromLocal(boolean triggerNetworkRefresh) {
    new Thread(
            () -> {
              List<RankSnapshot> saved = DatabaseManager.getInstance().getAllRankSnapshots();
              runOnUiThread(
                  () -> {
                    items.clear();
                    items.addAll(saved);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    if (triggerNetworkRefresh && !firstLoadStarted) {
                      firstLoadStarted = true;
                      refreshFromNetwork();
                    }
                  });
            })
        .start();
  }

  private void refreshFromNetwork() {
    if (refreshing) return;
    refreshing = true;
    swipeRefresh.setRefreshing(true);
    // 刷新中且没有本地数据时先隐藏空态，避免"暂无记录"和刷新动画同时出现
    if (items.isEmpty()) {
      emptyContainer.setVisibility(View.GONE);
      recyclerView.setVisibility(View.VISIBLE);
    }

    apiService
        .getLastExam()
        .enqueue(
            new Callback<ApiResponse<LastExamData>>() {
              @Override
              public void onResponse(
                  Call<ApiResponse<LastExamData>> call,
                  Response<ApiResponse<LastExamData>> response) {
                ApiResponse<LastExamData> body = response.body();
                if (response.isSuccessful()
                    && body != null
                    && body.isSuccess()
                    && body.getData() != null) {
                  fetchOverview(body.getData());
                } else {
                  finishRefresh(false, "在线数据不可用，已显示本地记录");
                }
              }

              @Override
              public void onFailure(Call<ApiResponse<LastExamData>> call, Throwable t) {
                Log.e(TAG, "获取最近考试失败", t);
                finishRefresh(false, "刷新失败：" + NetworkErrors.describe(t));
              }
            });
  }

  private void fetchOverview(LastExamData lastExam) {
    apiService
        .getExamOverview(lastExam.getExamId())
        .enqueue(
            new Callback<ApiResponse<ExamOverviewData>>() {
              @Override
              public void onResponse(
                  Call<ApiResponse<ExamOverviewData>> call,
                  Response<ApiResponse<ExamOverviewData>> response) {
                ApiResponse<ExamOverviewData> body = response.body();
                if (response.isSuccessful()
                    && body != null
                    && body.isSuccess()
                    && body.getData() != null) {
                  saveSnapshot(buildSnapshot(lastExam, body.getData()));
                } else {
                  finishRefresh(false, "在线数据不可用，已显示本地记录");
                }
              }

              @Override
              public void onFailure(Call<ApiResponse<ExamOverviewData>> call, Throwable t) {
                Log.e(TAG, "获取考试详情失败", t);
                finishRefresh(false, "刷新失败：" + NetworkErrors.describe(t));
              }
            });
  }

  private RankSnapshot buildSnapshot(LastExamData lastExam, ExamOverviewData overview) {
    RankSnapshot snapshot = new RankSnapshot();
    snapshot.setExamId(lastExam.getExamId());
    String name = overview.getName();
    snapshot.setExamName(name == null || name.trim().isEmpty() ? "考试 " + lastExam.getExamId() : name);
    snapshot.setExamTime(overview.getTimeInMillis());
    snapshot.setScore(overview.getScore());
    snapshot.setManfen(overview.getManfen());

    LastExamData.Extend extend = lastExam.getExtend();
    if (extend != null) {
      snapshot.setClassRank(extend.getClassRank());
      snapshot.setClassStuNum(extend.getClassStuNum());
      snapshot.setGradeRank(extend.getGradeRank());
      snapshot.setGradeStuNum(extend.getGradeStuNum());
      snapshot.setClassDefeatRatio(extend.getClassDefeatRatio());
      snapshot.setGradeDefeatRatio(extend.getGradeDefeatRatio());
    }

    snapshot.setScoreRaise(lastExam.getScoreRaise());
    snapshot.setRankRaise(lastExam.getRankRaise());
    snapshot.setSimpleLost(lastExam.getSimpleQuestionLostScores());
    snapshot.setMiddleLost(lastExam.getMiddleQuestionLostScores());
    snapshot.setHardLost(lastExam.getHardQuestionLostScores());
    snapshot.setWorstSubject(lastExam.getWorstSubjectText());
    snapshot.setPapers(overview.getPapers());
    snapshot.setSavedAt(System.currentTimeMillis());
    return snapshot;
  }

  private void saveSnapshot(RankSnapshot snapshot) {
    new Thread(
            () -> {
              try {
                DatabaseManager.getInstance().saveRankSnapshot(snapshot);
                List<RankSnapshot> saved = DatabaseManager.getInstance().getAllRankSnapshots();
                runOnUiThread(
                    () -> {
                      items.clear();
                      items.addAll(saved);
                      adapter.notifyDataSetChanged();
                      refreshing = false;
                      swipeRefresh.setRefreshing(false);
                      updateEmptyState();
                    });
              } catch (Exception e) {
                Log.e(TAG, "保存排名记录失败", e);
                runOnUiThread(() -> finishRefresh(false, "保存记录失败"));
              }
            })
        .start();
  }

  private void finishRefresh(boolean success, String message) {
    refreshing = false;
    swipeRefresh.setRefreshing(false);
    if (!success && !items.isEmpty() && message != null) {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    updateEmptyState();
  }

  private void updateEmptyState() {
    boolean empty = items.isEmpty();
    emptyContainer.setVisibility(empty ? View.VISIBLE : View.GONE);
    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
  }

  private void openDetail(RankSnapshot item) {
    Intent intent = new Intent(this, LastExamActivity.class);
    intent.putExtra("examId", item.getExamId());
    startActivity(intent);
  }
}
