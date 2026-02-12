package com.cyq.awa.hfspro.activities;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import androidx.appcompat.app.AppCompatActivity;
import com.cyq.awa.hfspro.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.cyq.awa.hfspro.tools.MyModel.MyExam;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;

public class ExamActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_exam);

    MyExam exam = (MyExam) getIntent().getSerializableExtra("myexam");
    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    CollapsingToolbarLayout tooltitle = findViewById(R.id.tooltitle);
    CircularProgressIndicator progressIndicator = findViewById(R.id.btn_ProgressIndicator);
    MaterialTextView scoretext = findViewById(R.id.scoretext);
    MaterialTextView manfent = findViewById(R.id.manfen);

    scoretext.setText(exam.getScore());
    manfent.setText("/" + exam.getManfen());

    toolbar.setNavigationOnClickListener(
        v -> {
          finish();
        });
    tooltitle.setTitle(exam.getName());
    progressIndicator.setIndeterminate(false);
    try {
      double score = Double.parseDouble(exam.getScore());
      int manfen = exam.getManfen();

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
  }
}
