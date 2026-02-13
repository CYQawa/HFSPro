package com.cyq.awa.hfspro.activities;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.PaperGridAdapter;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.cyq.awa.hfspro.tools.MyModel.MyExamList;
import com.google.android.material.card.MaterialCardView;
import android.content.Context;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

public class ExamActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_exam);

    MyExamList exam = (MyExamList) getIntent().getSerializableExtra("myexam");
    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    CollapsingToolbarLayout tooltitle = findViewById(R.id.tooltitle);
    CircularProgressIndicator progressIndicator = findViewById(R.id.btn_ProgressIndicator);
    MaterialTextView scoretext = findViewById(R.id.scoretext);
    MaterialTextView manfent = findViewById(R.id.manfen);
    MaterialCardView top_card = findViewById(R.id.top_card);
    MaterialCardView bottom_card = findViewById(R.id.bottom_card);
    RecyclerView paperRecyclerView = findViewById(R.id.paperRecyclerView);

//    scoretext.setText(exam.getScore());
//    manfent.setText("/" + exam.getManfen());
//    setCustomCardCorners(top_card, 16, 16, 0, 0);
//    setCustomCardCorners(bottom_card, 0, 0, 16, 16);
//
//    toolbar.setNavigationOnClickListener(
//        v -> {
//          finish();
//        });
//    tooltitle.setTitle(exam.getName());
//    progressIndicator.setIndeterminate(false);
//
//    try {
//      double score = Double.parseDouble(exam.getScore());
//      int manfen = exam.getManfen();
//
//      if (manfen > 0) {
//        int progress = (int) Math.round((score / manfen) * 100);
//        progressIndicator.setProgress(progress);
//      } else {
//        progressIndicator.setProgress(0);
//      }
//    } catch (NumberFormatException e) {
//      e.printStackTrace();
//      // 分数格式非法时，进度归零
//      progressIndicator.setProgress(0);
//    }
//    
//    GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
//    paperRecyclerView.setLayoutManager(layoutManager);
//    PaperGridAdapter adapter = new PaperGridAdapter(exam.getPapers());
//    paperRecyclerView.setAdapter(adapter);
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
}
