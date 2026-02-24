package com.cyq.awa.hfspro.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.net.Uri;
import android.os.Environment;
import android.app.DownloadManager;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import retrofit2.Callback;
import android.content.pm.PackageInfo;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.network.GsonModel.GitHubRelease;
import com.cyq.awa.hfspro.fragments.main.HomeFragment;
import com.cyq.awa.hfspro.fragments.main.MineFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
  private static final String PREFS_NAME = "update_prefs";
  private static final String KEY_IGNORED_VERSION = "ignored_version";
  private SharedPreferences prefs;
  private FragmentContainerView fragmentContainerView;
  private HomeFragment homefragment;
  private MineFragment mineFragment;
  private FragmentManager fragmentManager;
  private Fragment nowFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    DatabaseManager dbm = DatabaseManager.getInstance();

    if (!dbm.hasToken()) {
      startActivity(new Intent(MainActivity.this, GuideActivity.class));
      finish();
    }
    fragmentContainerView = findViewById(R.id.fragment_container);
    BottomNavigationView bottomNavigationView = findViewById(R.id.main_nav_view);

    homefragment = new HomeFragment();
    mineFragment = new MineFragment();

    fragmentManager = getSupportFragmentManager();
    fragmentManager
        .beginTransaction()
        .add(R.id.fragment_container, homefragment)
        .add(R.id.fragment_container, mineFragment)
        .hide(mineFragment)
        .commit();

    showFragment(homefragment);

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
              public void onResponse(Call<GitHubRelease> call, Response<GitHubRelease> response) {
                if (response.isSuccessful() && response.body() != null) {
                  GitHubRelease release = response.body();
                  String latest = release.getTagName();
                  String current = getCurrentVersionName();
                  String ignored = prefs.getString(KEY_IGNORED_VERSION, "");

                  // 如果最新版本大于当前版本，且（没有忽略版本 或 最新版本大于忽略版本）
                  if (compareVersions(latest, current) > 0
                      && (ignored.isEmpty() || compareVersions(latest, ignored) > 0)) {
                    showUpdateDialog(MainActivity.this, release);
                  }
                }
              }

              @Override
              public void onFailure(Call<GitHubRelease> call, Throwable t) {
                // 静默失败
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
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
    builder
        .setTitle("发现新版本 " + release.getTagName())
        .setMessage(release.getBody())
        .setPositiveButton(
            "前往下载更新",
            (dialog, which) -> {
              // 原有下载逻辑
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
            })
        .setNegativeButton("稍后", null)
        .setNeutralButton(
            "忽略本次更新",
            (dialog, which) -> {
              // 保存忽略的版本号
              prefs.edit().putString(KEY_IGNORED_VERSION, release.getTagName()).apply();
              Toast.makeText(context, "已忽略版本 " + release.getTagName(), Toast.LENGTH_SHORT).show();
            })
        .show();
  }
}
