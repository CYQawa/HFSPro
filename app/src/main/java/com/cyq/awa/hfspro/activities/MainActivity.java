package com.cyq.awa.hfspro.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.fragments.main.HomeFragment;
import com.cyq.awa.hfspro.fragments.main.MineFragment;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.cyq.awa.hfspro.tools.network.GsonModel.GitHubRelease;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "update_prefs";
    private static final String KEY_IGNORED_VERSION = "ignored_version";
    private SharedPreferences prefs;
    private FragmentContainerView fragmentContainerView;
    private HomeFragment homefragment;
    private MineFragment mineFragment;
    private FragmentManager fragmentManager;
    private Fragment nowFragment;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            
            DatabaseManager dbm = DatabaseManager.getInstance();
            boolean hasToken = dbm.hasToken();

            
            mainHandler.post(() -> {
                if (isDestroyed() || isFinishing()) return; // Activity 已销毁则不操作

                if (!hasToken) {
                    // 无 token：直接跳转引导页
                    startActivity(new Intent(MainActivity.this, GuideActivity.class));
                    finish();
                } else {
                    // 有 token：加载真正的主界面
                    setContentView(R.layout.activity_main);
                    initMainUI(savedInstanceState);  
                }
            });
        }).start();
    }

   
    private void initMainUI(Bundle savedInstanceState) {
        fragmentContainerView = findViewById(R.id.fragment_container);
        BottomNavigationView bottomNavigationView = findViewById(R.id.main_nav_view);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            homefragment = new HomeFragment();
            mineFragment = new MineFragment();
            fragmentManager
                    .beginTransaction()
                    .add(R.id.fragment_container, homefragment, "home")
                    .add(R.id.fragment_container, mineFragment, "mine")
                    .hide(mineFragment)
                    .commit();
            nowFragment = homefragment;
        } else {
            homefragment = (HomeFragment) fragmentManager.findFragmentByTag("home");
            mineFragment = (MineFragment) fragmentManager.findFragmentByTag("mine");
            nowFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        }

        bottomNavigationView.setOnItemSelectedListener(
                item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_home) {
                        showFragment(homefragment);
                        return true;
                    } else if (id == R.id.nav_profilye) {
                        showFragment(mineFragment);
                        return true;
                    }
                    return true;
                });

        checkUpdate(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void showFragment(Fragment aimfragment) {
        if (aimfragment == nowFragment) return;
        FragmentTransaction fragmentt = fragmentManager.beginTransaction();
        fragmentt.setCustomAnimations(
                R.anim.mtrl_bottom_sheet_slide_in, R.anim.mtrl_bottom_sheet_slide_out);
        if (nowFragment != null) {
            fragmentt.hide(nowFragment);
        }
        fragmentt.show(aimfragment);
        fragmentt.commit();
        nowFragment = aimfragment;
    }

    public void checkUpdate(Context context) {
        RetrofitTools.ApiService gitHubService = RetrofitTools.RetrofitClient.getGitHubService();
        gitHubService
                .checkLatestRelease("CYQawa", "HFSPro")
                .enqueue(
                        new Callback<GitHubRelease>() {
                            @Override
                            public void onResponse(
                                    Call<GitHubRelease> call, Response<GitHubRelease> response) {
                                boolean hasUpdate = false;
                                String latestVer = "";
                                GitHubRelease release = null;
                                if (response.isSuccessful() && response.body() != null) {
                                    release = response.body();
                                    latestVer = release.getTagName();
                                    String current = getCurrentVersionName();
                                    String ignored = prefs.getString(KEY_IGNORED_VERSION, "");

                                    // 如果最新版本大于当前版本，且（没有忽略版本 或 最新版本大于忽略版本）
                                    if (compareVersions(latestVer, current) > 0
                                            && (ignored.isEmpty()
                                                    || compareVersions(latestVer, ignored) > 0)) {
                                        hasUpdate = true;
                                        showUpdateDialog(MainActivity.this, release);
                                    }
                                }
                                // 通知 HomeFragment 更新卡片显隐、版本信息，并传入点击弹窗回调
                                if (homefragment != null) {
                                    GitHubRelease finalRelease = release;
                                    homefragment.setUpdateAvailable(hasUpdate, getCurrentVersionName(), latestVer,
                                            hasUpdate ? () -> showUpdateDialog(MainActivity.this, finalRelease) : null);
                                }
                            }

                            @Override
                            public void onFailure(Call<GitHubRelease> call, Throwable t) {
                                // 网络失败时隐藏更新卡片
                                if (homefragment != null) {
                                    homefragment.setUpdateAvailable(false, "", "", null);
                                }
                            }
                        });
    }

    // 获取当前应用的 versionName
    private String getCurrentVersionName() {
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int compareVersions(String v1, String v2) {

        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        return 0;
    }

    private void showUpdateDialog(Context context, GitHubRelease release) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.dialog_update, null);
        dialog.setContentView(sheetView);

        // 圆角背景
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(R.drawable.bg_bottom_sheet_rounded_top);
        }

        // 设置标题与内容
        TextView titleView = sheetView.findViewById(R.id.dialog_title);
        titleView.setText("发现新版本 " + release.getTagName());

        TextView bodyView = sheetView.findViewById(R.id.dialog_body);
        bodyView.setText(release.getBody() != null ? release.getBody() : "暂无更新说明");

        // 前往下载更新
        MaterialButton btnDownload = sheetView.findViewById(R.id.btn_download);
        btnDownload.setOnClickListener(v -> {
            String apkUrl = null;
            for (GitHubRelease.Asset asset : release.getAssets()) {
                if (asset.getName().endsWith(".apk")) {
                    apkUrl = asset.getDownloadUrl();
                    break;
                }
            }
            if (apkUrl != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
                startActivity(intent);
            } else {
                Toast.makeText(context, "未找到 APK 下载链接", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        // 稍后
        MaterialButton btnLater = sheetView.findViewById(R.id.btn_later);
        btnLater.setOnClickListener(v -> dialog.dismiss());

        // 忽略本次更新
        MaterialButton btnIgnore = sheetView.findViewById(R.id.btn_ignore);
        btnIgnore.setOnClickListener(v -> {
            prefs.edit()
                    .putString(KEY_IGNORED_VERSION, release.getTagName())
                    .apply();
            Toast.makeText(context, "已忽略版本 " + release.getTagName(), Toast.LENGTH_SHORT).show();
            // 忽略后隐藏更新卡片
            if (homefragment != null) {
                homefragment.setUpdateAvailable(false, "", "", null);
            }
            dialog.dismiss();
        });

        dialog.show();
    }
}
