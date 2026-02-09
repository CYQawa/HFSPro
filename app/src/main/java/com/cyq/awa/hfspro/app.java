// MyApplication.java
package com.cyq.awa.hfspro;

import android.app.Application;

import com.cyq.awa.hfspro.tools.RetrofitTools;

public class app extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 在这里初始化一次
        RetrofitTools.init(this);
    }
}