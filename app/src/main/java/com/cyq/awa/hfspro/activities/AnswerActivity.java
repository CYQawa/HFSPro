package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class AnswerActivity extends AppCompatActivity {
  private MyPaperOverview paper;
  private MyExamListItem exam;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_answer);

    paper = (MyPaperOverview) getIntent().getSerializableExtra("paper");
    exam = (MyExamListItem) getIntent().getSerializableExtra("myexam");

    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    CollapsingToolbarLayout tooltitle = findViewById(R.id.tooltitle);

    toolbar.setNavigationOnClickListener(
        v -> {
          finish();
        });
    tooltitle.setTitle(paper.getSubject() + "：原卷/答题卡");
  }
}
