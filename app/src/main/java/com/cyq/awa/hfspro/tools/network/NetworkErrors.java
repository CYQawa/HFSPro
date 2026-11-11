package com.cyq.awa.hfspro.tools.network;

import com.google.gson.JsonParseException;

import java.io.IOException;

/**
 * 把网络回调里的 Throwable 翻译成用户能看懂的提示。
 *
 * <p>之前所有异常都显示"网络请求失败！"，解析失败与断网无法区分。现在分开提示，
 * 方便用户判断是重试还是等版本更新。
 */
public final class NetworkErrors {

  private NetworkErrors() {}

  public static String describe(Throwable t) {
    if (t == null) return "未知错误，请稍后重试";

    if (t instanceof JsonParseException || t instanceof NumberFormatException) {
      return "数据解析失败，请稍后重试";
    }
    if (t instanceof IllegalStateException && t.getCause() != null) {
      return "数据解析失败，请稍后重试";
    }
    if (t instanceof IOException) {
      return "网络连接失败，请检查网络后重试";
    }
    String message = t.getMessage();
    if (message != null && !message.trim().isEmpty()) {
      return "请求失败：" + message;
    }
    return "请求失败，请稍后重试";
  }
}
