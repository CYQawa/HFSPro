package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.cyq.awa.hfspro.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayoutMediator;

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
    
    
    TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager2 = findViewById(R.id.viewPager);

        // 设置适配器
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);

        // 使用 TabLayoutMediator 连接 TabLayout 和 ViewPager2
        new TabLayoutMediator(tabLayout, viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        // 为每个选项卡设置标题（也可以设置图标等）
                        switch (position) {
                            case 0:
                                tab.setText("原卷");
                                // 如果需要图标：tab.setIcon(R.drawable.ic_home);
                                break;
                            case 1:
                                tab.setText("答题卡");
                                break;
                        }
                    }
                }).attach(); 
  }
}
