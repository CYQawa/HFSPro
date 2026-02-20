package com.cyq.awa.hfspro.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.cyq.awa.hfspro.adapter.PaperGridAdapter;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamActivity extends AppCompatActivity {
  private RetrofitTools.ApiService apiService;
  private MyExamListItem exam;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_exam);
        
        DatabaseManager dbm = DatabaseManager.getInstance();

    exam = (MyExamListItem) getIntent().getSerializableExtra("myexam");
    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    CollapsingToolbarLayout tooltitle = findViewById(R.id.tooltitle);
    CircularProgressIndicator progressIndicator = findViewById(R.id.btn_ProgressIndicator);
    MaterialTextView scoretext = findViewById(R.id.scoretext);
    MaterialTextView manfent = findViewById(R.id.manfen);
    MaterialCardView top_card = findViewById(R.id.top_card);
    MaterialCardView bottom_card = findViewById(R.id.bottom_card);
    RecyclerView paperRecyclerView = findViewById(R.id.paperRecyclerView);

    setSupportActionBar(toolbar);
    apiService = RetrofitTools.RetrofitClient.getAuthService();
    Call<ApiResponse<ExamOverviewData>> call = apiService.getExamOverview(exam.getExamId());
    showLoading();
    call.enqueue(
        new Callback<ApiResponse<ExamOverviewData>>() {
          @Override
          public void onResponse(
              Call<ApiResponse<ExamOverviewData>> call,
              Response<ApiResponse<ExamOverviewData>> response) {
            ApiResponse<ExamOverviewData> body = response.body();
            if (response.isSuccessful() && response.body() != null) {
              ExamOverviewData data = body.getData();
              if (body.isSuccess()) {
                MyExamListItem newMyexamlist =
                    new MyExamListItem(exam.getExamId(), data.getName(), data.getTime());
                    dbm.insertOrUpdateExam(newMyexamlist);
                scoretext.setText("" + data.getScore());
                manfent.setText("/" + data.getManfen());
                tooltitle.setTitle(data.getName());

                try {
                  double score = data.getScore();
                  int manfen = data.getManfen();

                  if (manfen > 0) {
                    int progress = (int) Math.round((score / manfen) * 100);
                    progressIndicator.setProgress(progress);
                  } else {
                    progressIndicator.setProgress(0);
                  }
                } catch (NumberFormatException e) {
                  e.printStackTrace();
                  // 分数格式非法时，进度归零
                  progressIndicator.setProgress(0);
                }

                GridLayoutManager layoutManager = new GridLayoutManager(ExamActivity.this, 3);
                paperRecyclerView.setLayoutManager(layoutManager);

                List<MyPaperOverview> dataList = new ArrayList<>();
                List<PaperOverview> paperGson = data.getPapers();

                for (int i = 0; i < paperGson.size(); i++) {
                  PaperOverview e = paperGson.get(i);
                  dataList.add(new MyPaperOverview(e));
                }

                PaperGridAdapter adapter = new PaperGridAdapter(dataList);
                paperRecyclerView.setAdapter(adapter);

                hideLoading();
              } else {
                String errorMsg = body.getMsg();
                hideLoading();
                showDialog("请求失败", String.format("请求失败: %s\ncode: %d", errorMsg, body.getCode()));
              }
            } else {
              // HTTP错误（如404, 500等）
              showDialog("请求失败", "服务器错误: " + response.code());
              hideLoading();
            }
          }

          @Override
          public void onFailure(Call<ApiResponse<ExamOverviewData>> call, Throwable t) {
            hideLoading();
            showDialog("请求失败", "网络请求失败！");
          }
        });

    setCustomCardCorners(top_card, 16, 16, 0, 0);
    setCustomCardCorners(bottom_card, 0, 0, 16, 16);

    toolbar.setNavigationOnClickListener(
        v -> {
          finish();
        });
    progressIndicator.setIndeterminate(false);
  }

  public void setCustomCardCorners(MaterialCardView cardView, int tl, int tr, int br, int bl) {
    // 将 dp 转换为像素（根据需求设置不同圆角值）
    float topLeftPx = dpToPx(cardView.getContext(), tl);
    float topRightPx = dpToPx(cardView.getContext(), tr);
    float bottomRightPx = dpToPx(cardView.getContext(), br);
    float bottomLeftPx = dpToPx(cardView.getContext(), bl);

    // 创建 ShapeAppearanceModel，分别设置四个角
    ShapeAppearanceModel shapeAppearanceModel =
        new ShapeAppearanceModel.Builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, topLeftPx)
            .setTopRightCorner(CornerFamily.ROUNDED, topRightPx)
            .setBottomRightCorner(CornerFamily.ROUNDED, bottomRightPx)
            .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeftPx)
            .build();

    // 将 ShapeAppearanceModel 应用到 MaterialCardView
    cardView.setShapeAppearanceModel(shapeAppearanceModel);
  }

  // dp 转像素工具方法
  private float dpToPx(Context context, float dp) {
    return dp * context.getResources().getDisplayMetrics().density;
  }

  private AlertDialog createLoadingDialog() {
    // 创建ProgressBar

    // 创建对话框
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setTitle("请稍候");
    builder.setMessage("正在加载中...");
    builder.setCancelable(false); // 禁止点击外部取消

    return builder.create();
  }

  // 使用示例
  private AlertDialog loadingDialog;

  public void showLoading() {
    if (loadingDialog == null) {
      loadingDialog = createLoadingDialog();
    }
    loadingDialog.show();
  }

  public void hideLoading() {
    if (loadingDialog != null && loadingDialog.isShowing()) {
      loadingDialog.dismiss();
    }
  }

  private void showDialog(String title, String message) {
    runOnUiThread(
        () -> {
          MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
          builder.setTitle(title).setMessage(message).setPositiveButton("确定", null).show();
        });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // 加载菜单资源文件
    getMenuInflater().inflate(R.menu.exam_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.rank) {
      Call<ApiResponse<CompareRankData>> call = apiService.getCompareRank(exam.getExamId());
      showLoading();
      call.enqueue(
          new Callback<ApiResponse<CompareRankData>>() {
            @Override
            public void onResponse(
                Call<ApiResponse<CompareRankData>> call,
                Response<ApiResponse<CompareRankData>> response) {
              ApiResponse<CompareRankData> body = response.body();
              if (response.isSuccessful() && response.body() != null) {
                CompareRankData data = body.getData();
                if (body.isSuccess()) {

                  if (data.getCompare() != null) {
                    if (data.getCompare().getCurGradeRank() != null) {
                      hideLoading();
                      showDialog(
                          "获取成功！！", "总分年段排名：" + data.getCompare().getCurGradeRank() + " 名！！！");
                    } else {
                      hideLoading();
                      showDialog("获取失败~", "没有curGradeRank字段哦~");
                    }
                  } else {
                    hideLoading();
                    showDialog("获取失败~", "没有compare字段哦~");
                  }

                  hideLoading();
                } else {
                  String errorMsg = body.getMsg();
                  hideLoading();
                  showDialog("请求失败", String.format("请求失败: %s\ncode: %d", errorMsg, body.getCode()));
                }
              } else {
                // HTTP错误（如404, 500等）
                showDialog("请求失败", "服务器错误: " + response.code());
                hideLoading();
              }
            }

            @Override
            public void onFailure(Call<ApiResponse<CompareRankData>> call, Throwable t) {
              hideLoading();
              showDialog("请求失败", "网络请求失败！");
            }
          });
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
