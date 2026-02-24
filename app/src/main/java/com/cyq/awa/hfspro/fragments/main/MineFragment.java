package com.cyq.awa.hfspro.fragments.main;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.LogHelper;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;

public class MineFragment extends Fragment {

  public MineFragment() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_mine, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    MaterialButton checktoken = view.findViewById(R.id.btn_checktoken);
    MaterialButton logout = view.findViewById(R.id.btn_logout);
    MaterialButton dumpLog = view.findViewById(R.id.btn_dumpLog);

    ImageView i = view.findViewById(R.id.github);
    i.setOnClickListener(
        v -> {
          String url = "https://github.com/CYQawa/HFSPro";
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          startActivity(intent);
        });

    DatabaseManager db = DatabaseManager.getInstance();

    checktoken.setOnClickListener(
        v -> {
            
            
          MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
          builder
              .setTitle("你的token")
              .setMessage(db.getToken())
              .setPositiveButton("确定", null)
              .setNeutralButton(
                  "复制(请不要发给别人)",
                  (dialog, which) -> {
                    copyToClipboard(requireContext(), db.getToken());
                  })
              .show();
            //  throw new RuntimeException("手动触发的崩溃");//测试
        });
    logout.setOnClickListener(
        v -> {
          MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
          builder
              .setTitle("确定退出登录？")
              .setMessage("点击确定退出登录")
              .setPositiveButton("取消", null)
              .setNeutralButton(
                  "确定",
                  (dialog, which) -> {
                    db.clearToken();
                    restartActivity(requireContext());
                  })
              .show();
        });

    dumpLog.setOnClickListener(
        v -> {
          //Log.e("dump日志", "测试");

          File logFile = new File(requireContext().getExternalFilesDir(null), "logcat.txt");
          LogHelper.dumpLogToFile(logFile);
          
          File logFile2 = new File(requireContext().getExternalFilesDir(null), "CrashLog.txt");
          LogHelper.dumpCrashLogToFile(logFile2);
          
          Toast.makeText(requireContext(),"已尝试dump至/storage/emulated/0/Android/data/com.cyq.awa.hfspro/files",Toast.LENGTH_LONG).show();
          
         // throw new RuntimeException("手动触发的崩溃");
        });
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

  private void restartActivity(Context context) {
    if (context instanceof Activity) {
      Activity activity = (Activity) context;
      Intent intent = activity.getIntent();
      activity.finish(); // 结束当前 Activity
      activity.startActivity(intent); // 用相同的 Intent 重新启动
      activity.overridePendingTransition(0, 0); // 取消过渡动画，让重启无感知
    }
  }
}
