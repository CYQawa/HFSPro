package com.cyq.awa.hfspro.activities;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.AAChartModel.AAChartCore.AAOptionsModel.AAPlotOptions;
import com.github.AAChartModel.AAChartCore.AAOptionsModel.AASeries;
import com.github.AAChartModel.AAChartCore.AAOptionsModel.AADataLabels;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.PaperGridAdapter;
import com.cyq.awa.hfspro.tools.DialogHelp;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartModel;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AAChartView;
import com.github.AAChartModel.AAChartCore.AAChartCreator.AASeriesElement;
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartLineDashStyleType;
import com.github.AAChartModel.AAChartCore.AAChartEnum.AAChartType;
import com.github.AAChartModel.AAChartCore.AAOptionsModel.AAOptions;
import com.github.AAChartModel.AAChartCore.AAOptionsModel.AAXAxis;
import com.github.AAChartModel.AAChartCore.AAOptionsModel.AAYAxis;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExamActivity extends AppCompatActivity {
  private RetrofitTools.ApiService apiService;
  private MyExamListItem exam;
  private AAChartView aaChartView;
  private AAChartView aaChartView2;

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
    aaChartView = findViewById(R.id.AAChartView);
    aaChartView2 = findViewById(R.id.AAChartView2);
    MaterialCardView rankCard = findViewById(R.id.rank);
    LinearLayout contentLayout = findViewById(R.id.content_layout);
    ImageView rankclose = findViewById(R.id.rankclose);

    LayoutTransition transition = new LayoutTransition();
    transition.setDuration(500);

    contentLayout.setLayoutTransition(transition);

    setSupportActionBar(toolbar);

    rankclose.setOnClickListener(v -> rankCard.setVisibility(View.GONE));

    apiService = RetrofitTools.RetrofitClient.getAuthService();
    Call<ApiResponse<ExamOverviewData>> call = apiService.getExamOverview(exam.getExamId());
    DialogHelp.show();
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
                    new MyExamListItem(exam.getExamId(), data.getName(), data.getTimeInMillis());
                dbm.insertOrUpdateExam(newMyexamlist);
                scoretext.setText("" + data.getScore());
                manfent.setText("/" + data.getManfen());
                tooltitle.setTitle(data.getName());

                Integer rankinfo = data.getCurGradeRank();
                if (rankinfo != null) {
                  rankCard.setCardBackgroundColor(
                      ContextCompat.getColor(
                          ExamActivity.this, R.color.md_theme_onPrimaryContainer));
                  ((TextView) findViewById(R.id.ranktx)).setText("年段排名：第 " + rankinfo + " 名");
                  rankCard.setVisibility(View.VISIBLE);
                } else {
                  rankCard.setCardBackgroundColor(
                      ContextCompat.getColor(ExamActivity.this, R.color.md_theme_error));
                  ((TextView) findViewById(R.id.ranktx)).setText("本场考试无法获取排名！");
                  rankCard.setVisibility(View.VISIBLE);
                }

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
                adapter.setOnItemClickListener(
                    (position, item) -> {
                      Intent intent = new Intent(ExamActivity.this, AnswerActivity.class);
                      intent.putExtra("paper", item);
                      intent.putExtra("myexam", exam);
                      startActivity(intent);
                    });
                paperRecyclerView.setAdapter(adapter);

                // 构建饼图数据（各学科得分）
                // 获取主题色数组
                int[] colorResIds = getThemeColorResIds();
                List<Map<String, Object>> pieData = new ArrayList<>();
                if (paperGson != null && !paperGson.isEmpty()) {
                  for (int i = 0; i < paperGson.size(); i++) {
                    PaperOverview paper = paperGson.get(i);
                    Map<String, Object> dataPoint = new HashMap<>();
                    dataPoint.put("name", paper.getSubject());
                    dataPoint.put("y", paper.getScore());
                    // 分配颜色，循环使用颜色数组
                    int colorResId = colorResIds[i % colorResIds.length];
                    dataPoint.put("color", getColorString(colorResId));
                    pieData.add(dataPoint);
                  }
                } else {
                  aaChartView.setVisibility(View.GONE);
                }

                int dataCount = pieData.size(); // 数据点数量
                int baseHeightPerItem = 50; // 每个数据点期望的高度（单位 dp），可根据实际效果调整
                int minHeight = 200; // 最小高度
                int calculatedHeight = Math.max(minHeight, dataCount * baseHeightPerItem);

                // 将 dp 转换为像素
                float density = getResources().getDisplayMetrics().density;
                int heightPx = (int) (calculatedHeight * density);

                // 设置 AAChartView 的高度
                ViewGroup.LayoutParams params = aaChartView.getLayoutParams();
                params.height = heightPx;
                aaChartView.setLayoutParams(params);
                // 获取主题动态颜色
                int bgColor =
                    ContextCompat.getColor(ExamActivity.this, R.color.md_theme_background);
                int textColor =
                    ContextCompat.getColor(ExamActivity.this, R.color.md_theme_onBackground);
                String bgHex = String.format("#%06X", (0xFFFFFF & bgColor));
                String textHex = String.format("#%06X", (0xFFFFFF & textColor));

                AAChartModel aaChartModel =
                    new AAChartModel()
                        .chartType(AAChartType.Pie)
                        .title("各学科得分分布")
                        .subtitle(data.getName()) // 副标题显示考试名称
                        .dataLabelsEnabled(true)
                        .yAxisTitle("得分")
                        .backgroundColor(bgHex)
                        .axesTextColor(textHex)
                        .series(
                            new AASeriesElement[] {
                              new AASeriesElement().name("得分").data(pieData.toArray())
                            });

                // 绘制图表
                aaChartView.aa_drawChartWithChartModel(aaChartModel);

                String[] categories = new String[paperGson.size()];
                Object[] percentData = new Object[paperGson.size()];
                for (int i = 0; i < paperGson.size(); i++) {
                  PaperOverview paper = paperGson.get(i);
                  categories[i] = paper.getSubject();
                  double score = paper.getScore();
                  double fullScore = paper.getManfen(); // 假设方法名为 getManfen()，请根据实际类调整
                  double percent = (fullScore > 0) ? (score / fullScore) * 100 : 0;
                  percentData[i] = percent;
                }

                // 创建雷达图模型
                // 创建雷达图模型（基础配置）
                AAChartModel radarChartModel =
                    new AAChartModel()
                        .chartType(AAChartType.Area)
                        .polar(true)
                        .title("各学科得分百分比")
                        .subtitle(data.getName())
                        .categories(categories)
                        .yAxisTitle("百分比(%)")
                        .dataLabelsEnabled(true)
                        .backgroundColor(bgHex)
                        .axesTextColor(textHex)
                        .series(
                            new AASeriesElement[] {
                              new AASeriesElement()
                                  .name("百分比")
                                  .data(percentData)
                                  .color(getColorString(R.color.md_theme_primary))
                                  .fillOpacity(0.3)
                            });

                AAOptions aaOptions = radarChartModel.aa_toAAOptions();

                // 1. 设置 Y 轴
                AAYAxis yAxis = new AAYAxis();
                yAxis.min(0);
                yAxis.max(100);
                yAxis.tickInterval(10);
                aaOptions.yAxis(yAxis);

                // 2. 设置 X 轴放射线宽度
                AAXAxis xAxis = aaOptions.xAxis;
                if (xAxis == null) {
                  xAxis = new AAXAxis();
                }
                xAxis.gridLineWidth(1f);
                aaOptions.xAxis(xAxis);

                // 3. 设置数据标签格式，添加百分号
                AAPlotOptions plotOptions = new AAPlotOptions();
                AASeries seriesOptions = new AASeries();
                AADataLabels dataLabels = new AADataLabels();
                dataLabels.format("{y}%"); // 显示数值加百分号
                dataLabels.enabled(true); // 确保数据标签开启
                dataLabels.allowOverlap(true);
                seriesOptions.dataLabels(dataLabels);
                plotOptions.series(seriesOptions);
                aaOptions.plotOptions(plotOptions);

                // 4. 绘制雷达图
                aaChartView2.aa_drawChartWithChartOptions(aaOptions);
                // 动态调整图表高度
                int dataCount2 = categories.length;
                int baseHeightPerItem2 = 50;
                int minHeight2 = 200;
                int calculatedHeight2 = Math.max(minHeight2, dataCount2 * baseHeightPerItem2);
                float density2 = getResources().getDisplayMetrics().density;
                int heightPx2 = (int) (calculatedHeight2 * density2);
                ViewGroup.LayoutParams params2 = aaChartView2.getLayoutParams();
                params2.height = heightPx2;
                aaChartView2.setLayoutParams(params2);

                DialogHelp.dismiss();
              } else {
                String errorMsg = body.getMsg();
                DialogHelp.dismiss();
                showDialog("请求失败", String.format("请求失败: %s\ncode: %d", errorMsg, body.getCode()));
              }
            } else {
              // HTTP错误（如404, 500等）
              showDialog("请求失败", "服务器错误: " + response.code());
              DialogHelp.dismiss();
            }
          }

          @Override
          public void onFailure(Call<ApiResponse<ExamOverviewData>> call, Throwable t) {
            DialogHelp.dismiss();
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

  private void showDialog(String title, String message) {
    runOnUiThread(
        () -> {
          MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
          builder.setTitle(title).setMessage(message).setPositiveButton("确定", null).show();
        });
  }

  private String getColorString(int colorResId) {
    int color = ContextCompat.getColor(this, colorResId);
    return String.format("#%06X", (0xFFFFFF & color));
  }

  private int[] getThemeColorResIds() {
    return new int[] {
      R.color.md_theme_primary,
      R.color.md_theme_onPrimary,
      R.color.md_theme_primaryContainer,
      R.color.md_theme_onPrimaryContainer,
      R.color.md_theme_inversePrimary,
      R.color.md_theme_primaryFixed_mediumContrast,
      R.color.md_theme_onPrimaryFixed,
      R.color.md_theme_primaryFixedDim,
      R.color.md_theme_inversePrimary_highContrast
      // 饼图主题颜色
    };
  }
}
