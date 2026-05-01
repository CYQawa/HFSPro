package com.cyq.awa.hfspro.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.cyq.awa.hfspro.tools.DialogHelp;
import com.google.android.material.button.MaterialButton;
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
  private MaterialTextView errorText; 
   
  // 新增控件
  private MaterialTextView scoreRaiseText;
  private MaterialTextView rankRaiseText;
  private MaterialTextView simpleLostText;
  private MaterialTextView middleLostText;
  private MaterialTextView hardLostText;
  private MaterialTextView classDefeatRatioText;
  private MaterialTextView gradeDefeatRatioText;

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
    MaterialTextView worstsubjecttext =findViewById(R.id.worstsubjecttext);
    MaterialTextView gradestunum = findViewById(R.id.gradestunum);
    MaterialCardView top_card = findViewById(R.id.top_card);
    MaterialCardView middle_card = findViewById(R.id.middle_card);
    MaterialCardView bottom_card = findViewById(R.id.bottom_card);
    MaterialCardView bottom2_card = findViewById(R.id.bottom2_card);
    RecyclerView paperRecyclerView = findViewById(R.id.paperRecyclerView);

    // 新增控件绑定
    scoreRaiseText = findViewById(R.id.score_raise_text);
    rankRaiseText = findViewById(R.id.rank_raise_text);
    simpleLostText = findViewById(R.id.simple_lost_text);
    middleLostText = findViewById(R.id.middle_lost_text);
    hardLostText = findViewById(R.id.hard_lost_text);
    classDefeatRatioText = findViewById(R.id.class_defeat_ratio_text);
    gradeDefeatRatioText = findViewById(R.id.grade_defeat_ratio_text);

    contentContainer = findViewById(R.id.content_container);
    errorContainer = findViewById(R.id.error_container);

    setSupportActionBar(toolbar);
    apiService = RetrofitTools.RetrofitClient.getAuthService();

    Call<GsonModel.ApiResponse<GsonModel.LastExamData>> call = apiService.getLastExam();
    DialogHelp.show();
    // 异步请求
    call.enqueue(
        new Callback<GsonModel.ApiResponse<GsonModel.LastExamData>>() {
          @Override
          public void onResponse(
              Call<GsonModel.ApiResponse<GsonModel.LastExamData>> call,
              Response<GsonModel.ApiResponse<GsonModel.LastExamData>> response) {
            if (response.isSuccessful()) {
              GsonModel.ApiResponse<GsonModel.LastExamData> apiResponse = response.body();
              if (apiResponse != null && apiResponse.isSuccess()) {
                GsonModel.LastExamData data = apiResponse.getData();
                if (data != null) {
                  int examId = data.getExamId();
                  int subjectNumber = data.getSubjectNumber();
                  boolean isManfen = data.getIsManfen();
                  String worstSubject = data.getWorstSubjectText();
                  int scoreRaise = data.getScoreRaise();
                  int rankRaise = data.getRankRaise();
                  int simpleLost = data.getSimpleQuestionLostScores();
                  int middleLost = data.getMiddleQuestionLostScores();
                  int hardLost = data.getHardQuestionLostScores();

                  GsonModel.LastExamData.Extend extend = data.getExtend();
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

                    // 填充新增数据
                    scoreRaiseText.setText("分数提升：+" + scoreRaise + " 分");
                    rankRaiseText.setText("排名提升：+" + rankRaise + " 名");
                    simpleLostText.setText("简单题失分：" + simpleLost + " 分");
                    middleLostText.setText("中等题失分：" + middleLost + " 分");
                    hardLostText.setText("难题失分：" + hardLost + " 分");
                    classDefeatRatioText.setText("班级击败：" + String.format("%.1f%%", classDefeatRatio));
                    gradeDefeatRatioText.setText("年段击败：" + String.format("%.1f%%", gradeDefeatRatio));
                  }

                  idtext.setText("examId：" + examId);

                  Call<ApiResponse<ExamOverviewData>> call2 = apiService.getExamOverview(examId);

                  call2.enqueue(
                      new Callback<ApiResponse<ExamOverviewData>>() {
                        @Override
                        public void onResponse(
                            Call<ApiResponse<ExamOverviewData>> call2,
                            Response<ApiResponse<ExamOverviewData>> response) {
                          ApiResponse<ExamOverviewData> body = response.body();
                          if (response.isSuccessful() && response.body() != null) {
                            ExamOverviewData data = body.getData();
                            if (body.isSuccess()) {
                              scoretext.setText("" + data.getScore());
                              manfent.setText("/" + data.getManfen());
                              nametext.setText(data.getName());

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
                                progressIndicator.setProgress(0);
                              }

                              GridLayoutManager layoutManager =
                                  new GridLayoutManager(LastExamActivity.this, 3);
                              paperRecyclerView.setLayoutManager(layoutManager);

                              List<MyPaperOverview> dataList = new ArrayList<>();
                              List<PaperOverview> paperGson = data.getPapers();

                              for (int i = 0; i < paperGson.size(); i++) {
                                PaperOverview e = paperGson.get(i);
                                dataList.add(new MyPaperOverview(e));
                              }

                              PaperGridAdapter adapter = new PaperGridAdapter(dataList);
                              adapter.setOnItemClickListener(
                                  (position, item) -> {
                                    Intent intent =
                                        new Intent(LastExamActivity.this, AnswerActivity.class);
                                    intent.putExtra("paper", item);
                                    intent.putExtra("myexam", new MyExamListItem(examId,"不知道",91L));
                                    startActivity(intent);
                                  });
                              paperRecyclerView.setAdapter(adapter);

                              DialogHelp.dismiss();
                            } else {
                              String errorMsg = body.getMsg();
                              DialogHelp.dismiss();

                              showDialog(
                                  "请求失败",
                                  String.format("请求失败: %s\ncode: %d", errorMsg, body.getCode()));
                            }
                          } else {
                            showDialog("请求失败", "服务器错误: " + response.code());
                            DialogHelp.dismiss();
                          }
                        }

                        @Override
                        public void onFailure(
                            Call<ApiResponse<ExamOverviewData>> call2, Throwable t) {
                          DialogHelp.dismiss();
                          showDialog("请求失败", "网络请求失败！");
                        }
                      });
                }
              } else {
                String errorMsg = apiResponse.getMsg();
                DialogHelp.dismiss();
                showError();
              }
            } else {
              showDialog("请求失败", "服务器错误: " + response.code());
              DialogHelp.dismiss();
            }
          }

          @Override
          public void onFailure(
              Call<GsonModel.ApiResponse<GsonModel.LastExamData>> call, Throwable t) {
            DialogHelp.dismiss();
            showDialog("请求失败", "网络请求失败！");
          }
        });

    setCustomCardCorners(top_card, 16, 16, 0, 0);
    setCustomCardCorners(middle_card, 1, 1, 1, 1);
    setCustomCardCorners(bottom_card, 1, 1, 1, 1);
    setCustomCardCorners(bottom2_card, 1, 1, 1, 1);
    // 为新增卡片也设置圆角（底部圆角）
    setCustomCardCorners(findViewById(R.id.bottom3_card), 1, 1, 16, 16);

    toolbar.setNavigationOnClickListener(
        v -> {
          finish();
        });
    progressIndicator.setIndeterminate(false);
  }

  public void setCustomCardCorners(MaterialCardView cardView, int tl, int tr, int br, int bl) {
    float topLeftPx = dpToPx(cardView.getContext(), tl);
    float topRightPx = dpToPx(cardView.getContext(), tr);
    float bottomRightPx = dpToPx(cardView.getContext(), br);
    float bottomLeftPx = dpToPx(cardView.getContext(), bl);

    ShapeAppearanceModel shapeAppearanceModel =
        new ShapeAppearanceModel.Builder()
            .setTopLeftCorner(CornerFamily.ROUNDED, topLeftPx)
            .setTopRightCorner(CornerFamily.ROUNDED, topRightPx)
            .setBottomRightCorner(CornerFamily.ROUNDED, bottomRightPx)
            .setBottomLeftCorner(CornerFamily.ROUNDED, bottomLeftPx)
            .build();

    cardView.setShapeAppearanceModel(shapeAppearanceModel);
  }

  private float dpToPx(Context context, float dp) {
    return dp * context.getResources().getDisplayMetrics().density;
  }

  private AlertDialog createLoadingDialog() {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setTitle("请稍候");
    builder.setMessage("正在加载中...");
    builder.setCancelable(false);
    return builder.create();
  }

  private void showDialog(String title, String message) {
    runOnUiThread(
        () -> {
          MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
          builder.setTitle(title).setMessage(message).setPositiveButton("确定", null).show();
        });
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