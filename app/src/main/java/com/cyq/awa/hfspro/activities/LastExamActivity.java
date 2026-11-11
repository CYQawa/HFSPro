package com.cyq.awa.hfspro.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.PaperGridAdapter;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.cyq.awa.hfspro.tools.RankSnapshot;
import com.cyq.awa.hfspro.tools.ScoreFormat;
import com.cyq.awa.hfspro.tools.network.GsonModel.PaperOverview;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 考试排名详情。
 *
 * <p>数据来自本地保存的排名快照（见 {@link RankSnapshot}），不再请求网络，
 * 因此 7 天有效期外也能查看；记录可通过右上角菜单删除。
 */
public class LastExamActivity extends AppCompatActivity {

  private static final String EXTRA_EXAM_ID = "examId";

  private long examId;
  private RankSnapshot snapshot;

  private CollapsingToolbarLayout tooltitle;
  private CircularProgressIndicator progressIndicator;
  private MaterialTextView scoretext;
  private MaterialTextView manfent;
  private MaterialTextView nametext;
  private MaterialTextView idtext;
  private MaterialTextView classrank;
  private MaterialTextView classstunum;
  private MaterialTextView graderank;
  private MaterialTextView gradestunum;
  private MaterialTextView worstsubjecttext;
  private MaterialTextView classDefeatText;
  private MaterialTextView gradeDefeatText;
  private MaterialTextView scoreRaiseText;
  private MaterialTextView rankRaiseText;
  private MaterialTextView simpleLostText;
  private MaterialTextView middleLostText;
  private MaterialTextView hardLostText;
  private LinearProgressIndicator classDefeatProgress;
  private LinearProgressIndicator gradeDefeatProgress;
  private RecyclerView paperRecyclerView;
  private View contentContainer;
  private View errorContainer;
  private MaterialTextView errorText;

  private final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_last_exam);

    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setNavigationOnClickListener(v -> finish());

    tooltitle = findViewById(R.id.tooltitle);
    progressIndicator = findViewById(R.id.btn_ProgressIndicator);
    scoretext = findViewById(R.id.scoretext);
    manfent = findViewById(R.id.manfen);
    nametext = findViewById(R.id.nametext);
    idtext = findViewById(R.id.idtext);
    classrank = findViewById(R.id.classrank);
    classstunum = findViewById(R.id.classstunum);
    graderank = findViewById(R.id.graderank);
    gradestunum = findViewById(R.id.gradestunum);
    worstsubjecttext = findViewById(R.id.worstsubjecttext);
    classDefeatText = findViewById(R.id.class_defeat_text);
    classDefeatProgress = findViewById(R.id.class_defeat_progress);
    gradeDefeatText = findViewById(R.id.grade_defeat_text);
    gradeDefeatProgress = findViewById(R.id.grade_defeat_progress);
    scoreRaiseText = findViewById(R.id.score_raise_text);
    rankRaiseText = findViewById(R.id.rank_raise_text);
    simpleLostText = findViewById(R.id.simple_lost_text);
    middleLostText = findViewById(R.id.middle_lost_text);
    hardLostText = findViewById(R.id.hard_lost_text);
    paperRecyclerView = findViewById(R.id.paperRecyclerView);
    contentContainer = findViewById(R.id.content_container);
    errorContainer = findViewById(R.id.error_container);
    errorText = findViewById(R.id.error_text);

    progressIndicator.setIndeterminate(false);

    examId = getIntent().getLongExtra(EXTRA_EXAM_ID, -1L);
    if (examId <= 0) {
      showError("数据异常：缺少考试记录 ID");
      return;
    }
    loadSnapshot();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.rank_detail_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.action_delete) {
      confirmDelete();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void loadSnapshot() {
    new Thread(
            () -> {
              RankSnapshot saved = DatabaseManager.getInstance().getRankSnapshot(examId);
              runOnUiThread(
                  () -> {
                    if (saved == null) {
                      showError("该记录不存在或已被删除");
                      return;
                    }
                    snapshot = saved;
                    render(saved);
                    showContent();
                  });
            })
        .start();
  }

  private void render(RankSnapshot data) {
    tooltitle.setTitle(data.getExamName());
    nametext.setText(data.getExamName());
    idtext.setText("考试时间：" + formatDate(data.getExamTime()));
    scoretext.setText(ScoreFormat.format(data.getScore()));
    manfent.setText("/" + ScoreFormat.format(data.getManfen()));

    if (data.getManfen() > 0) {
      int progress = (int) Math.round((data.getScore() / data.getManfen()) * 100);
      progressIndicator.setProgress(progress);
    } else {
      progressIndicator.setProgress(0);
    }

    classrank.setText("班级排名：" + rankText(data.getClassRank()));
    classstunum.setText("班级人数：" + countText(data.getClassStuNum()));
    graderank.setText("年段排名：" + rankText(data.getGradeRank()));
    gradestunum.setText("年段人数：" + countText(data.getGradeStuNum()));

    String worst = data.getWorstSubject();
    worstsubjecttext.setText("最差的科目：" + (isBlank(worst) ? "—" : worst));

    classDefeatText.setText(
        "击败了 " + String.format(Locale.getDefault(), "%.1f", data.getClassDefeatRatio()) + "% 的同学");
    classDefeatProgress.setProgressCompat((int) Math.round(data.getClassDefeatRatio()), true);
    gradeDefeatText.setText(
        "击败了 " + String.format(Locale.getDefault(), "%.1f", data.getGradeDefeatRatio()) + "% 的同学");
    gradeDefeatProgress.setProgressCompat((int) Math.round(data.getGradeDefeatRatio()), true);

    applyRaiseText(data.getScoreRaise(), data.getRankRaise());
    simpleLostText.setText("简单题失分：" + ScoreFormat.format(data.getSimpleLost()) + " 分");
    middleLostText.setText("中等题失分：" + ScoreFormat.format(data.getMiddleLost()) + " 分");
    hardLostText.setText("难题失分：" + ScoreFormat.format(data.getHardLost()) + " 分");

    bindPapers(data);
    setCustomCardCorners(
        (MaterialCardView) findViewById(R.id.top_card), 16, 16, 0, 0);
    setCustomCardCorners(
        (MaterialCardView) findViewById(R.id.middle_card), 1, 1, 1, 1);
    setCustomCardCorners(
        (MaterialCardView) findViewById(R.id.bottom_card), 1, 1, 1, 1);
    setCustomCardCorners(
        (MaterialCardView) findViewById(R.id.bottom2_card), 1, 1, 1, 1);
    setCustomCardCorners(
        (MaterialCardView) findViewById(R.id.bottom3_card), 1, 1, 16, 16);
  }

  private void bindPapers(RankSnapshot data) {
    List<MyPaperOverview> dataList = new ArrayList<>();
    if (data.getPapers() != null) {
      for (PaperOverview paper : data.getPapers()) {
        if (paper != null) {
          dataList.add(new MyPaperOverview(paper));
        }
      }
    }
    paperRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    PaperGridAdapter adapter = new PaperGridAdapter(dataList);
    adapter.setOnItemClickListener(
        (position, item) -> {
          Intent intent = new Intent(LastExamActivity.this, AnswerActivity.class);
          intent.putExtra("paper", item);
          intent.putExtra(
              "myexam",
              new MyExamListItem(data.getExamId(), data.getExamName(), data.getExamTime()));
          startActivity(intent);
        });
    paperRecyclerView.setAdapter(adapter);
  }

  private void applyRaiseText(double scoreRaise, int rankRaise) {
    if (scoreRaise > 0) {
      scoreRaiseText.setText("分数提升：+" + ScoreFormat.format(scoreRaise) + " 分");
      scoreRaiseText.setTextColor(Color.parseColor("#4CAF50"));
    } else if (scoreRaise < 0) {
      scoreRaiseText.setText("分数下降：" + ScoreFormat.format(scoreRaise) + " 分");
      scoreRaiseText.setTextColor(Color.parseColor("#F44336"));
    } else {
      scoreRaiseText.setText("分数变化：0 分");
      scoreRaiseText.setTextColor(Color.parseColor("#757575"));
    }

    if (rankRaise > 0) {
      rankRaiseText.setText("排名提升：+" + rankRaise + " 名");
      rankRaiseText.setTextColor(Color.parseColor("#4CAF50"));
    } else if (rankRaise < 0) {
      rankRaiseText.setText("排名下降：" + rankRaise + " 名");
      rankRaiseText.setTextColor(Color.parseColor("#F44336"));
    } else {
      rankRaiseText.setText("排名不变");
      rankRaiseText.setTextColor(Color.parseColor("#757575"));
    }
  }

  private void confirmDelete() {
    if (snapshot == null) return;
    new MaterialAlertDialogBuilder(this)
        .setTitle("删除这条记录？")
        .setMessage("删除后将无法恢复，历史排名不会再次从服务器获取。")
        .setNegativeButton("取消", null)
        .setPositiveButton("删除", (dialog, which) -> deleteRecord())
        .show();
  }

  private void deleteRecord() {
    final long id = examId;
    new Thread(
            () -> {
              DatabaseManager.getInstance().deleteRankSnapshot(id);
              runOnUiThread(
                  () -> {
                    Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                    finish();
                  });
            })
        .start();
  }

  private String formatDate(long time) {
    if (time <= 0) return "—";
    return dateFormat.format(new Date(time));
  }

  private String rankText(int rank) {
    return rank > 0 ? String.valueOf(rank) : "—";
  }

  private String countText(int count) {
    return count > 0 ? String.valueOf(count) : "—";
  }

  private boolean isBlank(String text) {
    return text == null || text.trim().isEmpty();
  }

  private void showContent() {
    contentContainer.setVisibility(View.VISIBLE);
    errorContainer.setVisibility(View.GONE);
  }

  private void showError(String message) {
    if (errorText != null) errorText.setText(message);
    contentContainer.setVisibility(View.GONE);
    errorContainer.setVisibility(View.VISIBLE);
  }

  private void setCustomCardCorners(MaterialCardView cardView, int tl, int tr, int br, int bl) {
    float topLeftPx = dpToPx(cardView.getContext(), tl);
    float topRightPx = dpToPx(cardView.getContext(), tr);
    float bottomRightPx = dpToPx(cardView.getContext(), br);
    float bottomLeftPx = dpToPx(cardView.getContext(), bl);
    ShapeAppearanceModel model =
        new ShapeAppearanceModel.Builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, topLeftPx)
            .setTopRightCorner(CornerFamily.ROUNDED, topRightPx)
            .setBottomRightCorner(CornerFamily.ROUNDED, bottomRightPx)
            .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeftPx)
            .build();
    cardView.setShapeAppearanceModel(model);
  }

  private float dpToPx(Context context, float dp) {
    return dp * context.getResources().getDisplayMetrics().density;
  }
}
