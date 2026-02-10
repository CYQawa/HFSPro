package com.cyq.awa.hfspro.tools.MyDatabases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "HFSPro.db";
  private static final int DATABASE_VERSION = 1;

  // user表
  public static final String TABLE_USER = "user";
  public static final String COLUMN_TOKEN = "token";

  private static final String CREATE_TABLE_USER =
      "CREATE TABLE " + TABLE_USER + " (" + COLUMN_TOKEN + " TEXT PRIMARY KEY" + ");";

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_TABLE_USER);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // 删除旧表，创建新表
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
    onCreate(db);
  }
}
