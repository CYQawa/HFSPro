// MyApplication.java
package com.cyq.awa.hfspro;

import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.widget.Toast;

import com.cyq.awa.hfspro.activities.CrashActivity;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseHelper;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class App extends Application {
  private static App instance;
  private static DatabaseHelper dbhelper;
  private Thread.UncaughtExceptionHandler defaultHandler;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;

    RetrofitTools.init(this);
    dbhelper = new DatabaseHelper(this);

    initCrashHandler();
  }

  public static App getInstance() {
    return instance;
  }

  public static DatabaseHelper getDatabaseHelper() {
    return dbhelper;
  }

  @Override
  public void onTerminate() {
    if (dbhelper != null) {
      dbhelper.close();
    }
    super.onTerminate();
  }

  // ==================== 崩溃处理相关代码 ====================

  private void initCrashHandler() {
    defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

    // 设置自定义异常处理器
    Thread.setDefaultUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread thread, Throwable throwable) {
            // 处理崩溃
            handleUncaughtException(thread, throwable);
          }
        });
  }

  private void handleUncaughtException(Thread thread, Throwable throwable) {
    try {
      // 收集崩溃信息
      String crashInfo = collectCrashInfo(throwable);

      
      Intent intent = new Intent(this, CrashActivity.class);
      intent.putExtra("crash_info", crashInfo);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(intent);

      
      new Thread(
              () -> {
                try {
                  Thread.sleep(2000); 
                } catch (InterruptedException ignored) {
                }
                Process.killProcess(Process.myPid());
                System.exit(1);
              })
          .start();

    } catch (Exception e) {
      // 如果自定义处理失败，回退到默认处理器
      if (defaultHandler != null) {
        defaultHandler.uncaughtException(thread, throwable);
      }
    }
  }

  private String collectCrashInfo(Throwable throwable) {
    StringBuilder sb = new StringBuilder();

    // 收集崩溃时间
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    String time = sdf.format(new Date());
    sb.append("=== 崩溃时间 ===\n");
    sb.append(time).append("\n\n");

    // 收集应用信息
    sb.append("=== 应用信息 ===\n");
    try {
      String packageName = getPackageName();
      sb.append("包名: ").append(packageName).append("\n");

      // 应用版本
      String versionName = getPackageManager().getPackageInfo(packageName, 0).versionName;
      int versionCode =
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
              ? (int) getPackageManager().getPackageInfo(packageName, 0).getLongVersionCode()
              : getPackageManager().getPackageInfo(packageName, 0).versionCode;
      sb.append("版本: ").append(versionName).append(" (").append(versionCode).append(")\n");
    } catch (Exception e) {
      sb.append("获取应用信息失败: ").append(e.getMessage()).append("\n");
    }

    // 收集设备信息
    sb.append("\n=== 设备信息 ===\n");
    sb.append("设备型号: ").append(Build.MODEL).append("\n");
    sb.append("设备品牌: ").append(Build.BRAND).append("\n");
    sb.append("Android版本: ").append(Build.VERSION.RELEASE).append("\n");
    sb.append("SDK版本: ").append(Build.VERSION.SDK_INT).append("\n");
    sb.append("CPU架构: ").append(Build.SUPPORTED_ABIS[0]).append("\n");

    // 收集运行时信息
    sb.append("\n=== 运行时信息 ===\n");
    Runtime runtime = Runtime.getRuntime();
    long totalMemory = runtime.totalMemory() / 1024 / 1024;
    long freeMemory = runtime.freeMemory() / 1024 / 1024;
    long maxMemory = runtime.maxMemory() / 1024 / 1024;
    sb.append("总内存: ").append(totalMemory).append("MB\n");
    sb.append("空闲内存: ").append(freeMemory).append("MB\n");
    sb.append("最大内存: ").append(maxMemory).append("MB\n");

    // 收集异常堆栈
    sb.append("\n=== 异常堆栈 ===\n");
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    throwable.printStackTrace(pw);
    sb.append(sw.toString());

    // 收集根异常原因
    Throwable rootCause = throwable;
    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
      rootCause = rootCause.getCause();
    }
    if (rootCause != throwable) {
      sb.append("\n=== 根本原因 ===\n");
      rootCause.printStackTrace(pw);
      sb.append(sw.toString());
    }

    return sb.toString();
  }
}
