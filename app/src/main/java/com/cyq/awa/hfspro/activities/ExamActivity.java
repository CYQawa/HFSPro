package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.cyq.awa.hfspro.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.cyq.awa.hfspro.tools.MyModel.MyExam;
public class ExamActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);
        
        MyExam exam = (MyExam) getIntent().getSerializableExtra("myexam");
        
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        CollapsingToolbarLayout tooltitle = findViewById(R.id.tooltitle);
        
        toolbar.setNavigationOnClickListener(v ->{
            finish();
        });
        tooltitle.setTitle(exam.getName());
    }
}
