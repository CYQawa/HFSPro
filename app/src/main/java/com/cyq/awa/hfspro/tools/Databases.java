package com.cyq.awa.hfspro.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class Databases {
  private static final String PERF_NAME = "token_perf";
  private static final String KEY_TOKEN = "access_token";

  private static Databases instance;
  private SharedPreferences sharedPreferences;

  private Databases(Context context) {
    sharedPreferences = context.getSharedPreferences(PERF_NAME, Context.MODE_PRIVATE);
  }

  public static synchronized Databases getInstance(Context context) {
    if (instance == null) {
      instance = new Databases(context);
    }
    return instance;
  }

  public void saveToken(String token) {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(KEY_TOKEN, token);
    editor.apply();
  }

  public String getToken() {
    return sharedPreferences.getString(KEY_TOKEN, null);
  }

  public void clearTokens() {
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.remove(KEY_TOKEN);
    editor.apply();
  }

  // 检查是否有token
  public boolean hasToken() {
    return getToken() != null;
  }
}
