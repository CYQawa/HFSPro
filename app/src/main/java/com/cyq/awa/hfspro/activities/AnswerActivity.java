package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.cyq.awa.hfspro.tools.DialogHelp;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import androidx.appcompat.app.AppCompatActivity;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
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
  private AnswerPictureData answerData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_answer);

    paper = (MyPaperOverview) getIntent().getSerializableExtra("paper");
    exam = (MyExamListItem) getIntent().getSerializableExtra("myexam");

    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    CollapsingToolbarLayout tooltitle = findViewById(R.id.tooltitle);
    TabLayout tabLayout = findViewById(R.id.tabLayout);
    ViewPager2 viewPager2 = findViewById(R.id.viewPager);

    toolbar.setNavigationOnClickListener(
        v -> {
          finish();
        });
    tooltitle.setTitle(paper.getSubject() + "：原卷/答题卡");

    DialogHelp.show();

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
                answerData = apiResponse.getData();
                AnswerPictureData data = apiResponse.getData();

                List<String> paperPics = data.getPaperPic();
                if (paperPics == null) {
                  paperPics = new ArrayList<>();
                  showDialog("提示", "没有原卷");
                }
                // 创建适配器并设置给 ViewPager2
                // 在 AnswerActivity 中获取数据成功后
                ViewPagerAdapter adapter =
                    new ViewPagerAdapter(AnswerActivity.this, paperPics, data);
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
                DialogHelp.dismiss();

                Snackbar.make(findViewById(R.id.coordinator), "请耐心等待加载", Snackbar.LENGTH_SHORT)
                    .show();
              }
            } else {
              showDialog("请求失败", "服务器错误: " + response.code());
              DialogHelp.dismiss();
            }
          }

          @Override
          public void onFailure(Call<ApiResponse<AnswerPictureData>> call, Throwable t) {
            showDialog("错误", "处理失败");
            DialogHelp.dismiss();
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

  private void showDialog(String title, String message) {
    runOnUiThread(
        () -> {
          MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
          builder.setTitle(title).setMessage(message).setPositiveButton("确定", null).show();
        });
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == R.id.download) {
      handleDownload();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void handleDownload() {
    if (answerData == null) {
      Snackbar.make(findViewById(R.id.coordinator), "数据还未加载", Snackbar.LENGTH_SHORT).show();
      return;
    }

    // 根据当前选中的 Tab 确定是原卷还是答题卡
    ViewPager2 viewPager = findViewById(R.id.viewPager);
    int currentTab = viewPager.getCurrentItem();
    int itemCount = viewPager.getAdapter() != null ? viewPager.getAdapter().getItemCount() : 0;

    List<String> picList = null;
    String title = "";

    if (itemCount == 1) {
      // 只有答题卡
      picList = answerData.getUrl(); // 根据你的实际字段名调整
      title = "选择答题卡下载";
    } else if (itemCount == 2) {
      if (currentTab == 0) {
        picList = answerData.getPaperPic();
        title = "选择原卷下载";
      } else {
        picList = answerData.getUrl();
        title = "选择答题卡下载";
      }
    }

    if (picList == null || picList.isEmpty()) {
      Snackbar.make(findViewById(R.id.coordinator), "没有可下载的图片", Snackbar.LENGTH_SHORT).show();
      return;
    }

    
      // 多选对话框
      String[] itemNames = new String[picList.size()];
      for (int i = 0; i < picList.size(); i++) {
        itemNames[i] = "图 " + (i + 1);
      }

      // 记录选中状态
      boolean[] checkedItems = new boolean[picList.size()];

      new MaterialAlertDialogBuilder(this)
          .setTitle(title)
          .setMultiChoiceItems(
              itemNames,
              checkedItems,
              (dialog, which, isChecked) -> {
                // 这里保存选中状态，checkedItems[which] 会自动更新吗？
                // MaterialAlertDialogBuilder 的 setMultiChoiceItems 会自动维护 checkedItems 数组
              })
          .setPositiveButton(
              "下载所选",
              (dialog, which) -> {
                // 遍历所有选项，下载选中的图片
                for (int i = 0; i < checkedItems.length; i++) {
                  if (checkedItems[i]) {
                   // downloadImage(picList.get(i));
                   
                  }
                }
              })
          .setNegativeButton("取消", null)
          .show();
    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_download, menu);
    return true;
  }
}
