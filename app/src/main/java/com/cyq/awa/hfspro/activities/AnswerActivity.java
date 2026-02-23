package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import androidx.appcompat.app.AppCompatActivity;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.cyq.awa.hfspro.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Callback;

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
    TabLayout tabLayout = findViewById(R.id.tabLayout);
    ViewPager2 viewPager2 = findViewById(R.id.viewPager);

    toolbar.setNavigationOnClickListener(
        v -> {
          finish();
        });
    tooltitle.setTitle(paper.getSubject() + "：原卷/答题卡");

    

    //    ViewPagerAdapter adapter = new ViewPagerAdapter(this, i);
    //    viewPager2.setAdapter(adapter);
    // 使用 TabLayoutMediator 连接 TabLayout 和 ViewPager2
    //    new TabLayoutMediator(
    //            tabLayout,
    //            viewPager2,
    //            new TabLayoutMediator.TabConfigurationStrategy() {
    //              @Override
    //              public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
    //                // 为每个选项卡设置标题（也可以设置图标等）
    //                switch (position) {
    //                  case 0:
    //                    tab.setText("原卷");
    //                    break;
    //                  case 1:
    //                    tab.setText("答题卡");
    //                    break;
    //                }
    //              }
    //            })
    //        .attach();
showLoading();

    RetrofitTools.ApiService service = RetrofitTools.RetrofitClient.getAuthService();
    Call<ApiResponse<AnswerPictureData>> call =
        service.getAnswerPicture(exam.getExamId(), paper.getPaperId(), paper.getPid());
    call.enqueue(
        new Callback<ApiResponse<AnswerPictureData>>() {
          @Override
          public void onResponse(
              Call<ApiResponse<AnswerPictureData>> call,
              Response<ApiResponse<AnswerPictureData>> response) {
            if (response.isSuccessful()) {
              ApiResponse<AnswerPictureData> apiResponse = response.body();
              if (apiResponse != null && apiResponse.isSuccess()) {
                AnswerPictureData data = apiResponse.getData();
                
                List<String> paperPics = data.getPaperPic();
                if (paperPics == null) {
                  paperPics = new ArrayList<>();
                  showDialog("提示", "没有原卷");
                }
                // 创建适配器并设置给 ViewPager2
                // 在 AnswerActivity 中获取数据成功后
ViewPagerAdapter adapter = new ViewPagerAdapter(AnswerActivity.this, paperPics, data);
viewPager2.setAdapter(adapter);

                // 关联 TabLayout 和 ViewPager2，并根据实际页面数设置标题
                new TabLayoutMediator(
                        tabLayout,
                        viewPager2,
                        (tab, position) -> {
                          int itemCount = viewPager2.getAdapter().getItemCount();
                          if (itemCount == 1) {
                            tab.setText("答题卡");
                          } else {
                            tab.setText(position == 0 ? "原卷" : "答题卡");
                          }
                        })
                    .attach();
                hideLoading();
              }
            } else {
              showDialog("请求失败", "服务器错误: " + response.code());
              hideLoading();
            }
          }

          @Override
          public void onFailure(Call<ApiResponse<AnswerPictureData>> call, Throwable t) {
            showDialog("错误", "处理失败");
            hideLoading();
          }
        });
  }

  private AlertDialog createLoadingDialog() {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder.setTitle("请稍候");
    builder.setMessage("正在加载中...");
    builder.setCancelable(false);

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
}
