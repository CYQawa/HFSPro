package com.cyq.awa.hfspro;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter; // 正确导入
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;

@GlideModule
public class MyGlideModule extends AppGlideModule {

  @Override
  public void applyOptions(Context context, GlideBuilder builder) {
    long unlimitedSize = 10L * 1024L * 1024L;
    builder.setDiskCache(new InternalCacheDiskCacheFactory(context, unlimitedSize));
  }

  @Override
  public boolean isManifestParsingEnabled() {
    return false;
  }
}
