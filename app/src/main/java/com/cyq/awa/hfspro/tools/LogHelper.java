package com.cyq.awa.hfspro.tools;

import java.io.File;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.FileOutputStream;
import android.util.Log;
import java.io.IOException;

public class LogHelper {
  private static final String TAG = "LogcatReader";

  public static void dumpLogToFile(File outputFile) {
    int pid = android.os.Process.myPid();
    java.lang.Process process = null;
    BufferedReader reader = null;
    OutputStreamWriter writer = null;

    try {
      // 构建 logcat 命令：dump 当前缓冲区，带时间
      ProcessBuilder builder = new ProcessBuilder("logcat", "-d", "-v", "time");
      process = builder.start();

      // 读取命令输出
      reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

      // 准备写入文件
      writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);

      String line;
      while ((line = reader.readLine()) != null) {
        writer.write(line);
        writer.write('\n');
      }

      // 等待命令执行完毕
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        Log.e(TAG, "logcat exited with error code: " + exitCode);
      }

    } catch (IOException | InterruptedException e) {
      Log.e(TAG, "Failed to dump logcat", e);
    } finally {
      try {
        if (reader != null) reader.close();
        if (writer != null) writer.close();
      } catch (IOException ignored) {
      }
      if (process != null) {
        process.destroy();
      }
    }
  }

 
  public static void dumpCrashLogToFile(File outputFile) {
    java.lang.Process process = null;
    BufferedReader reader = null;
    OutputStreamWriter writer = null;

    try {
      // 构建 logcat 命令：从 crash 缓冲区 dump，带时间
      ProcessBuilder builder = new ProcessBuilder("logcat", "-b", "crash", "-d", "-v", "time");
      process = builder.start();

      // 读取命令输出
      reader =
          new BufferedReader(
              new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

      // 准备写入文件
      writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8);

      String line;
      while ((line = reader.readLine()) != null) {
        writer.write(line);
        writer.write('\n');
      }

      // 等待命令执行完毕
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        Log.e(TAG, "logcat -b crash exited with error code: " + exitCode);
      }

    } catch (IOException | InterruptedException e) {
      Log.e(TAG, "Failed to dump crash log", e);
    } finally {
      try {
        if (reader != null) reader.close();
        if (writer != null) writer.close();
      } catch (IOException ignored) {
      }
      if (process != null) {
        process.destroy();
      }
    }
  }
}
