package com.cyq.awa.hfspro.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.ViewPagerAdapter;
import com.cyq.awa.hfspro.tools.DialogHelp;
import com.cyq.awa.hfspro.tools.MyModel.MarkInfo;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.cyq.awa.hfspro.transform.AnswerSheetTransformation;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    // 新增：缓存每张答题卡图片的标记列表，仅当是答题卡时使用
    private List<List<MarkInfo>> marksPerSheet;

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

                        // 提前构建答题卡标记数据，供后续下载绘制图使用
                        buildMarksPerSheetFromData(data);

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

    // ---------- 构建每张答题卡的标注数据 ----------
    private void buildMarksPerSheetFromData(AnswerPictureData data) {
        marksPerSheet = null;
        if (data == null) return;
        List<String> urls = data.getUrl();
        if (urls == null || urls.isEmpty()) return;

        int sheetCount = urls.size();
        marksPerSheet = new ArrayList<>(sheetCount);
        for (int i = 0; i < sheetCount; i++) {
            marksPerSheet.add(new ArrayList<>());
        }

        // 区域映射
        List<Map<String, List<QuestionItem>>> regionQuestionsPerSheet = new ArrayList<>(sheetCount);
        for (int i = 0; i < sheetCount; i++) {
            regionQuestionsPerSheet.add(new HashMap<>());
        }

        List<QuestionItem> questions = data.getQuestions();
        if (questions == null) return;

        // 总分信息放到第一张
        double totalScore = data.getScore();
        double subjectiveScore = 0;
        double objectiveScore = 0;
        for (QuestionItem q : questions) {
            if (q.getType() == 1) subjectiveScore += q.getScore();
            else if (q.getType() == 2) objectiveScore += q.getScore();
        }
        if (!marksPerSheet.isEmpty()) {
            List<MarkInfo> first = marksPerSheet.get(0);
            float x = 20, y = 100, lineHeight = 80;
            first.add(new MarkInfo(x, y, 0, 0, 0, null, 2, "总分:" + formatScore(totalScore)));
            first.add(new MarkInfo(x, y + lineHeight, 0, 0, 0, null, 2, "主观:" + formatScore(subjectiveScore)));
            first.add(new MarkInfo(x, y + 2 * lineHeight, 0, 0, 0, null, 2, "客观:" + formatScore(objectiveScore)));
        }

        for (QuestionItem q : questions) {
            int type = q.getType();
            if (type == 2) { // 客观题
                List<AnswerOptionItem> options = q.getAnswerOption();
                if (options != null) {
                    for (AnswerOptionItem opt : options) {
                        int idx = opt.getIndex();
                        if (idx >= 0 && idx < sheetCount) {
                            marksPerSheet.get(idx).add(new MarkInfo(
                                    Float.parseFloat(opt.getX()),
                                    Float.parseFloat(opt.getY()),
                                    Float.parseFloat(opt.getW()),
                                    Float.parseFloat(opt.getH()),
                                    opt.getRight(),
                                    opt.getOption()));
                        }
                    }
                }
            } else if (type == 1) { // 主观题
                List<String> qUrls = q.getUrl();
                if (qUrls == null || qUrls.isEmpty()) continue;
                for (String qUrl : qUrls) {
                    String baseUrl = extractBaseUrl(qUrl);
                    int idx = findSheetIndex(baseUrl, urls);
                    if (idx < 0) continue;
                    float[] coords = parseCoordinates(qUrl);
                    if (coords == null) continue;
                    float x = coords[0], y = coords[1], w = coords[2], h = coords[3];
                    String key = String.format(Locale.US, "%.2f_%.2f_%.2f_%.2f", x, y, w, h);
                    Map<String, List<QuestionItem>> map = regionQuestionsPerSheet.get(idx);
                    List<QuestionItem> list = map.computeIfAbsent(key, k -> new ArrayList<>());
                    list.add(q);
                }
            }
        }

        // 添加主观题区域框与得分文本
        for (int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++) {
            Map<String, List<QuestionItem>> sheetMap = regionQuestionsPerSheet.get(sheetIdx);
            for (Map.Entry<String, List<QuestionItem>> entry : sheetMap.entrySet()) {
                List<QuestionItem> qList = entry.getValue();
                if (qList.isEmpty()) continue;
                QuestionItem firstQ = qList.get(0);
                float x = 0, y = 0, w = 0, h = 0;
                List<String> firstUrls = firstQ.getUrl();
                if (firstUrls != null && !firstUrls.isEmpty()) {
                    float[] coords = parseCoordinates(firstUrls.get(0));
                    if (coords != null) {
                        x = coords[0]; y = coords[1]; w = coords[2]; h = coords[3];
                    }
                }
                if (w == 0 || h == 0) continue;

                marksPerSheet.get(sheetIdx).add(new MarkInfo(x, y, w, h, 0, null, 1, null));

                double totalScoreRegion = 0, totalManfenRegion = 0;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < qList.size(); i++) {
                    QuestionItem qi = qList.get(i);
                    totalScoreRegion += qi.getScore();
                    totalManfenRegion += qi.getManfen();
                    if (i > 0) sb.append(",");
                    sb.append(formatScore(qi.getScore()));
                }
                String text;
                if (qList.size() == 1) {
                    text = formatScore(totalScoreRegion) + "分/" + formatScore(totalManfenRegion) + "分";
                } else {
                    text = String.format("%s分/%s分 (小题分:%s)",
                            formatScore(totalScoreRegion), formatScore(totalManfenRegion), sb.toString());
                }
                float textX = x + 10, textY = y + 35;
                marksPerSheet.get(sheetIdx).add(new MarkInfo(textX, textY, 0, 0, 0, null, 2, text));
            }
        }
    }

    // 辅助方法：提取baseUrl
    private String extractBaseUrl(String fullUrl) {
        int queryIdx = fullUrl.indexOf('?');
        String noQuery = queryIdx > 0 ? fullUrl.substring(0, queryIdx) : fullUrl;
        int atIdx = noQuery.indexOf("%40");
        return atIdx > 0 ? noQuery.substring(0, atIdx) : noQuery;
    }

    private float[] parseCoordinates(String fullUrl) {
        try {
            int atIdx = fullUrl.indexOf("%40");
            if (atIdx < 0) return null;
            int queryIdx = fullUrl.indexOf('?');
            String cropPart = (queryIdx > atIdx) ? fullUrl.substring(atIdx, queryIdx) : fullUrl.substring(atIdx);
            cropPart = URLDecoder.decode(cropPart, "UTF-8");
            Pattern pattern = Pattern.compile("x_(\\d+),y_(\\d+),w_(\\d+),h_(\\d+)");
            Matcher matcher = pattern.matcher(cropPart);
            if (matcher.find()) {
                return new float[]{
                        Float.parseFloat(matcher.group(1)),
                        Float.parseFloat(matcher.group(2)),
                        Float.parseFloat(matcher.group(3)),
                        Float.parseFloat(matcher.group(4))
                };
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int findSheetIndex(String baseUrl, List<String> sheetUrls) {
        for (int i = 0; i < sheetUrls.size(); i++) {
            String s = sheetUrls.get(i);
            String sBase = s.contains("?") ? s.substring(0, s.indexOf('?')) : s;
            if (sBase.equals(baseUrl)) return i;
        }
        return -1;
    }

    private String formatScore(double score) {
        if (score == (long) score) return String.valueOf((long) score);
        return String.valueOf(score);
    }

    // ---------- 菜单与下载 ----------
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

        // Android 9 及以下存储权限
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                showPermissionExplanationDialog(picList, title);
                return;
            }
        }

        showDownloadSelectionDialog(picList, title);
    }

    private void showPermissionExplanationDialog(List<String> picList, String title) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("需要存储权限")
                .setMessage("为了将图片保存到相册，需要获取存储空间权限。")
                .setPositiveButton("确定", (d, w) ->
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_STORAGE_PERMISSION))
                .setNegativeButton("取消", null)
                .show();
    }

    // 修改后的多选下载对话框，答题卡会额外弹出保存模式选择
    private void showDownloadSelectionDialog(List<String> picList, String title) {
        String[] itemNames = new String[picList.size()];
        for (int i = 0; i < picList.size(); i++) {
            itemNames[i] = "图 " + (i + 1);
        }
        boolean[] checkedItems = new boolean[picList.size()];

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMultiChoiceItems(itemNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("下载所选", (dialog, which) -> {
                    List<Integer> selectedIndices = new ArrayList<>();
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) selectedIndices.add(i);
                    }
                    if (selectedIndices.isEmpty()) {
                        Snackbar.make(findViewById(R.id.coordinator), "请至少选择一张图片", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    // 判断是否为答题卡（答题卡才有 marksPerSheet）
                    boolean isAnswerSheet = (picList == answerData.getUrl());
                    if (isAnswerSheet) {
                        // 弹出单选：保存原图 / 保存绘制分数图
                        final int[] selectedMode = {0}; // 0=原图, 1=绘制图
                        new MaterialAlertDialogBuilder(this)
                                .setTitle("保存模式")
                                .setSingleChoiceItems(new String[]{"保存原图", "保存绘制分数图"}, 0,
                                        (d, w) -> selectedMode[0] = w)
                                .setPositiveButton("确定", (d, w) -> {
                                    boolean drawMarks = (selectedMode[0] == 1);
                                    for (int index : selectedIndices) {
                                        if (drawMarks && marksPerSheet != null && index < marksPerSheet.size()) {
                                            downloadMarkedImage(picList.get(index), marksPerSheet.get(index));
                                        } else {
                                            downloadImage(picList.get(index));
                                        }
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    } else {
                        // 原卷直接下载原图
                        for (int index : selectedIndices) {
                            downloadImage(picList.get(index));
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 原有下载原图
    private void downloadImage(String imageUrl) {
        downloadExecutor.execute(() -> {
            Request request = new Request.Builder().url(imageUrl).build();
            try (okhttp3.Response response = okHttpClient.newCall(request).execute()) {
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

    // 新增：下载带分数标记的图片
    private void downloadMarkedImage(String imageUrl, List<MarkInfo> marks) {
        downloadExecutor.execute(() -> {
            try {
                Bitmap bitmap = Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(imageUrl)
                        .override(Target.SIZE_ORIGINAL)
                        .transform(new AnswerSheetTransformation(marks))
                        .submit()
                        .get();
                saveBitmapToGallery(bitmap);
            } catch (Exception e) {
                Log.e("DownloadMarked", "合成失败", e);
                runOnUiThread(() ->
                        Snackbar.make(findViewById(R.id.coordinator),
                                "绘制图下载失败: " + e.getMessage(), Snackbar.LENGTH_LONG).show());
            }
        });
    }

    // 保存 Bitmap 到相册
    private void saveBitmapToGallery(Bitmap bitmap) throws IOException {
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

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        }

        runOnUiThread(() ->
                Snackbar.make(findViewById(R.id.coordinator), "已保存到相册", Snackbar.LENGTH_SHORT).show());
    }

    // 原 saveImageToGallery 保留，用于下载原图流
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