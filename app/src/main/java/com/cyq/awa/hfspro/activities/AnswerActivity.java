package com.cyq.awa.hfspro.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.ViewPagerAdapter;
import com.cyq.awa.hfspro.tools.DialogHelp;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnswerActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 1001;

    private MyPaperOverview paper;
    private MyExamListItem exam;
    private AnswerPictureData answerData;

    private final ExecutorService downloadExecutor = Executors.newFixedThreadPool(3);
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

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

        toolbar.setNavigationOnClickListener(v -> finish());
        tooltitle.setTitle(paper.getSubject() + "：原卷/答题卡");

        DialogHelp.show();

        RetrofitTools.ApiService service = RetrofitTools.RetrofitClient.getAuthService();
        Call<ApiResponse<AnswerPictureData>> call =
                service.getAnswerPicture(exam.getExamId(), paper.getPaperId(), paper.getPid());
        call.enqueue(new Callback<ApiResponse<AnswerPictureData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AnswerPictureData>> call,
                                   @NonNull Response<ApiResponse<AnswerPictureData>> response) {
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

                        ViewPagerAdapter adapter = new ViewPagerAdapter(AnswerActivity.this, paperPics, data);
                        viewPager2.setAdapter(adapter);

                        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
                            int itemCount = viewPager2.getAdapter().getItemCount();
                            if (itemCount == 1) {
                                tab.setText("答题卡");
                            } else {
                                tab.setText(position == 0 ? "原卷" : "答题卡");
                            }
                        }).attach();

                        DialogHelp.dismiss();
                        Snackbar.make(findViewById(R.id.coordinator), "请耐心等待加载", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    showDialog("请求失败", "服务器错误: " + response.code());
                    DialogHelp.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AnswerPictureData>> call, @NonNull Throwable t) {
                showDialog("错误", "处理失败");
                DialogHelp.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download, menu);
        return true;
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

        // 确定图片列表和标题
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        int currentTab = viewPager.getCurrentItem();
        int itemCount = viewPager.getAdapter() != null ? viewPager.getAdapter().getItemCount() : 0;

        List<String> picList = null;
        String title = "";

        if (itemCount == 1) {
            picList = answerData.getUrl();
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

        // 权限检查（仅 Android 9 及以下需要 WRITE_EXTERNAL_STORAGE）
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {//未测试
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                
                showPermissionExplanationDialog(picList, title);
                return;
            }
        }

        // 已有权限，直接显示多选下载对话框
        showDownloadSelectionDialog(picList, title);
    }

   
    private void showPermissionExplanationDialog(List<String> picList, String title) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("需要存储权限")
                .setMessage("为了将图片保存到相册，需要获取存储空间权限。")
                .setPositiveButton("确定", (dialog, which) -> {
                    
                    ActivityCompat.requestPermissions(
                            AnswerActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                })
                .setNegativeButton("取消", null)
                .show();
    }

   
    private void showDownloadSelectionDialog(List<String> picList, String title) {
        String[] itemNames = new String[picList.size()];
        for (int i = 0; i < picList.size(); i++) {
            itemNames[i] = "图 " + (i + 1);
    }
    boolean[] checkedItems = new boolean[picList.size()];

    new MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMultiChoiceItems(
            itemNames,
            checkedItems,
            (dialog, which, isChecked) -> {
              checkedItems[which] = isChecked;
            })
        .setPositiveButton(
            "下载所选",
            (dialog, which) -> {
              boolean hasSelection = false;
              for (boolean b : checkedItems) {
                if (b) {
                  hasSelection = true;
                  break;
                }
              }
              if (!hasSelection) {
                Snackbar.make(findViewById(R.id.coordinator), "请至少选择一张图片", Snackbar.LENGTH_SHORT)
                    .show();
                return;
              }
              for (int i = 0; i < checkedItems.length; i++) {
                if (checkedItems[i]) downloadImage(picList.get(i));
              }
            })
        .setNegativeButton("取消", null)
        .show();
  }

 
  private void downloadImage(String imageUrl) {
        downloadExecutor.execute(() -> {
            Request request = new Request.Builder().url(imageUrl).build();
            
try (okhttp3.Response response = okHttpClient.newCall(request).execute()) {
    // ...

                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("服务器响应错误: " + response.code());
                }
                saveImageToGallery(response.body().byteStream());
            } catch (Exception e) {
                Log.e("DownloadImage", "下载失败", e);
                runOnUiThread(() ->
                        Snackbar.make(findViewById(R.id.coordinator),
                                "下载失败: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
            }
        });
    }

 
    private void saveImageToGallery(InputStream input) throws IOException {
        String fileName = "HFSPro_" + System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HFSPro");
        } else {
            File dir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "HFSPro");
            if (!dir.exists()) dir.mkdirs();
            values.put(MediaStore.Images.Media.DATA, new File(dir, fileName).getAbsolutePath());
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IOException("无法创建媒体文件条目");

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        runOnUiThread(() ->
                Snackbar.make(findViewById(R.id.coordinator), "已保存到相册", Snackbar.LENGTH_SHORT).show());
    }

   
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                
                handleDownload();
            } else {
                Snackbar.make(findViewById(R.id.coordinator), "需要存储权限才能保存图片", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadExecutor.shutdown();
    }

  
    private void showDialog(String title, String message) {
        runOnUiThread(() -> new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show());
    }
}