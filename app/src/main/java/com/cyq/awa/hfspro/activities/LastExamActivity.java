package com.cyq.awa.hfspro.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import com.cyq.awa.hfspro.tools.DialogHelp;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.adapter.PaperGridAdapter;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.cyq.awa.hfspro.tools.network.GsonModel;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LastExamActivity extends AppCompatActivity {
  private RetrofitTools.ApiService apiService;
  private View contentContainer;
  private View errorContainer;

  // 新增控件
  private MaterialTextView classDefeatText;
  private LinearProgressIndicator classDefeatProgress;
  private MaterialTextView gradeDefeatText;
  private LinearProgressIndicator gradeDefeatProgress;
  private MaterialTextView scoreRaiseText;
  private MaterialTextView rankRaiseText;
  private MaterialTextView simpleLostText;
  private MaterialTextView middleLostText;
  private MaterialTextView hardLostText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_last_exam);
    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    CollapsingToolbarLayout tooltitle = findViewById(R.id.tooltitle);
    CircularProgressIndicator progressIndicator = findViewById(R.id.btn_ProgressIndicator);
    MaterialTextView scoretext = findViewById(R.id.scoretext);
    MaterialTextView manfent = findViewById(R.id.manfen);
    MaterialTextView nametext = findViewById(R.id.nametext);
    MaterialTextView idtext = findViewById(R.id.idtext);
    MaterialTextView classrank = findViewById(R.id.classrank);
    MaterialTextView classstunum = findViewById(R.id.classstunum);
    MaterialTextView graderank = findViewById(R.id.graderank);
    MaterialTextView worstsubjecttext = findViewById(R.id.worstsubjecttext);
    MaterialTextView gradestunum = findViewById(R.id.gradestunum);
    MaterialCardView top_card = findViewById(R.id.top_card);
    MaterialCardView middle_card = findViewById(R.id.middle_card);
    MaterialCardView bottom_card = findViewById(R.id.bottom_card);
    MaterialCardView bottom2_card = findViewById(R.id.bottom2_card);
    RecyclerView paperRecyclerView = findViewById(R.id.paperRecyclerView);

    // 新增绑定
    classDefeatText = findViewById(R.id.class_defeat_text);
    classDefeatProgress = findViewById(R.id.class_defeat_progress);
    gradeDefeatText = findViewById(R.id.grade_defeat_text);
    gradeDefeatProgress = findViewById(R.id.grade_defeat_progress);
    scoreRaiseText = findViewById(R.id.score_raise_text);
    rankRaiseText = findViewById(R.id.rank_raise_text);
    simpleLostText = findViewById(R.id.simple_lost_text);
    middleLostText = findViewById(R.id.middle_lost_text);
    hardLostText = findViewById(R.id.hard_lost_text);

    contentContainer = findViewById(R.id.content_container);
    errorContainer = findViewById(R.id.error_container);
    setSupportActionBar(toolbar);
    apiService = RetrofitTools.RetrofitClient.getAuthService();

    Call<ApiResponse<LastExamData>> call = apiService.getLastExam();
    DialogHelp.show();
    call.enqueue(new Callback<ApiResponse<LastExamData>>() {
      @Override
      public void onResponse(Call<ApiResponse<LastExamData>> call,
                             Response<ApiResponse<LastExamData>> response) {
        if (response.isSuccessful()) {
          ApiResponse<LastExamData> apiResponse = response.body();
          if (apiResponse != null && apiResponse.isSuccess()) {
            LastExamData data = apiResponse.getData();
            if (data != null) {
              int examId = data.getExamId();
              String worstSubject = data.getWorstSubjectText();
              double scoreRaise = data.getScoreRaise();
              int rankRaise = data.getRankRaise();
              int simpleLost = data.getSimpleQuestionLostScores();
              int middleLost = data.getMiddleQuestionLostScores();
              int hardLost = data.getHardQuestionLostScores();

              LastExamData.Extend extend = data.getExtend();
              if (extend != null) {
                int classRank = extend.getClassRank();
                int classnum = extend.getClassStuNum();
                int gradeRank = extend.getGradeRank();
                int gradenum = extend.getGradeStuNum();
                double classDefeatRatio = extend.getClassDefeatRatio();
                double gradeDefeatRatio = extend.getGradeDefeatRatio();

                classrank.setText("班级排名：" + classRank);
                classstunum.setText("班级人数：" + classnum);
                graderank.setText("年段排名：" + gradeRank);
                gradestunum.setText("年段人数：" + gradenum);
                worstsubjecttext.setText("最差的科目：" + worstSubject);

                // 击败比例文字 + 进度条
                classDefeatText.setText("击败了 " + String.format("%.1f", classDefeatRatio) + "% 的同学");
                classDefeatProgress.setProgressCompat((int) Math.round(classDefeatRatio), true);
                gradeDefeatText.setText("击败了 " + String.format("%.1f", gradeDefeatRatio) + "% 的同学");
                gradeDefeatProgress.setProgressCompat((int) Math.round(gradeDefeatRatio), true);

                // 进步/退步处理（对齐网页版正负号逻辑）
                if (scoreRaise > 0) {
                  scoreRaiseText.setText("分数提升：+" + scoreRaise + " 分");
                  scoreRaiseText.setTextColor(Color.parseColor("#4CAF50"));
                } else if (scoreRaise < 0) {
                  scoreRaiseText.setText("分数下降：" + scoreRaise + " 分"); // 自带负号
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

              // 失分分布
              simpleLostText.setText("简单题失分：" + simpleLost + " 分");
              middleLostText.setText("中等题失分：" + middleLost + " 分");
              hardLostText.setText("难题失分：" + hardLost + " 分");

              idtext.setText("examId：" + examId);

              // 请求考试详情（分数、各科）
              Call<ApiResponse<ExamOverviewData>> call2 = apiService.getExamOverview(examId);
              call2.enqueue(new Callback<ApiResponse<ExamOverviewData>>() {
                @Override
                public void onResponse(Call<ApiResponse<ExamOverviewData>> call2,
                                       Response<ApiResponse<ExamOverviewData>> response) {
                  ApiResponse<ExamOverviewData> body = response.body();
                  if (response.isSuccessful() && body != null && body.isSuccess()) {
                    ExamOverviewData data = body.getData();
                    scoretext.setText("" + data.getScore());
                    manfent.setText("/" + data.getManfen());
                    nametext.setText(data.getName());

                    // 环形进度
                    if (data.getManfen() > 0) {
                      int progress = (int) Math.round((data.getScore() / data.getManfen()) * 100);
                      progressIndicator.setProgress(progress);
                    } else {
                      progressIndicator.setProgress(0);
                    }

                    // 各科网格
                    GridLayoutManager layoutManager = new GridLayoutManager(LastExamActivity.this, 3);
                    paperRecyclerView.setLayoutManager(layoutManager);
                    List<MyPaperOverview> dataList = new ArrayList<>();
                    for (PaperOverview e : data.getPapers()) {
                      dataList.add(new MyPaperOverview(e));
                    }
                    PaperGridAdapter adapter = new PaperGridAdapter(dataList);
                    adapter.setOnItemClickListener((position, item) -> {
                      Intent intent = new Intent(LastExamActivity.this, AnswerActivity.class);
                      intent.putExtra("paper", item);
                      intent.putExtra("myexam", new MyExamListItem(examId, "不知道", 91L));
                      startActivity(intent);
                    });
                    paperRecyclerView.setAdapter(adapter);

                    DialogHelp.dismiss();
                  } else {
                    DialogHelp.dismiss();
                    showDialog("请求失败", String.format("请求失败: %s\ncode: %d",
                        body != null ? body.getMsg() : "", body != null ? body.getCode() : -1));
                  }
                }

                @Override
                public void onFailure(Call<ApiResponse<ExamOverviewData>> call2, Throwable t) {
                  DialogHelp.dismiss();
                  showDialog("请求失败", "网络请求失败！");
                }
              });
            }
          } else {
            DialogHelp.dismiss();
            showError();
          }
        } else {
          DialogHelp.dismiss();
          showDialog("请求失败", "服务器错误: " + response.code());
        }
      }

      @Override
      public void onFailure(Call<ApiResponse<LastExamData>> call, Throwable t) {
        DialogHelp.dismiss();
        showDialog("请求失败", "网络请求失败！");
      }
    });

    // 圆角设置
    setCustomCardCorners(top_card, 16, 16, 0, 0);
    setCustomCardCorners(middle_card, 1, 1, 1, 1);
    setCustomCardCorners(bottom_card, 1, 1, 1, 1);
    setCustomCardCorners(bottom2_card, 1, 1, 1, 1);
    setCustomCardCorners(findViewById(R.id.bottom3_card), 1, 1, 16, 16);

    toolbar.setNavigationOnClickListener(v -> finish());
    progressIndicator.setIndeterminate(false);
  }

  private void setCustomCardCorners(MaterialCardView cardView, int tl, int tr, int br, int bl) {
    float topLeftPx = dpToPx(cardView.getContext(), tl);
    float topRightPx = dpToPx(cardView.getContext(), tr);
    float bottomRightPx = dpToPx(cardView.getContext(), br);
    float bottomLeftPx = dpToPx(cardView.getContext(), bl);
    ShapeAppearanceModel model = new ShapeAppearanceModel.Builder()
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

  private void showDialog(String title, String message) {
    runOnUiThread(() ->
        new MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    );
  }

  private void showContent() {
    contentContainer.setVisibility(View.VISIBLE);
    errorContainer.setVisibility(View.GONE);
  }

  private void showError() {
    contentContainer.setVisibility(View.GONE);
    errorContainer.setVisibility(View.VISIBLE);
  }
}