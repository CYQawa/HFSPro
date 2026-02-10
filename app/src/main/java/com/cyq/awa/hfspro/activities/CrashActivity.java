package com.cyq.awa.hfspro.activities;

import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.Toast;
import android.content.Intent;
import com.cyq.awa.hfspro.App;
import com.cyq.awa.hfspro.R;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

public class CrashActivity extends AppCompatActivity {
  private String crashInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_crash);
    crashInfo = getIntent().getStringExtra("crash_info");
    if (crashInfo == null) {
      crashInfo = "未获取到崩溃信息";
    }

    // 显示崩溃信息
    MaterialTextView tvCrashInfo = findViewById(R.id.tv_crash_info);
    tvCrashInfo.setText(crashInfo);

    ExtendedFloatingActionButton copy = findViewById(R.id.copy);
    copy.setOnClickListener(v -> copyToClipboard(this, crashInfo));
  }

  private void restartApp() {
    // 重启应用
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();

    // 结束当前进程
    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(0);
  }

  private void finishAndExit() {
    finish();
    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(0);
  }

  @Override
  public void onBackPressed() {
    // 禁用返回键，防止误操作
    // super.onBackPressed(); // 注释掉这行以禁用返回键
  }

  public static void copyToClipboard(Context context, String text) {
    ClipboardManager clipboard =
        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard != null) {
      ClipData clip = ClipData.newPlainText("崩溃信息", text);
      clipboard.setPrimaryClip(clip);
      Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
    }
  }
}
