// MyApplication.java
package com.cyq.awa.hfspro;

import android.app.Application;

import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseHelper;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;

public class App extends Application {
  private static App instance;
  private static DatabaseHelper dbhelper;
  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    // 在这里初始化一次
    RetrofitTools.init(this);

    dbhelper = new DatabaseHelper(this);
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
}
