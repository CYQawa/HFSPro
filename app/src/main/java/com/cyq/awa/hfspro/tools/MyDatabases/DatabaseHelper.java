package com.cyq.awa.hfspro.tools.MyDatabases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "HFSPro.db";
  private static final int DATABASE_VERSION = 2;  // 升级至2，新增exam表

  // user表
  public static final String TABLE_USER = "user";
  public static final String COLUMN_TOKEN = "token";

  // exam表
  public static final String TABLE_EXAM = "exam";
  public static final String COLUMN_EXAM_ID = "examId";
  public static final String COLUMN_EXAM_NAME = "name";
  public static final String COLUMN_EXAM_TIME = "time";

  private static final String CREATE_TABLE_USER =
      "CREATE TABLE " + TABLE_USER + " (" + COLUMN_TOKEN + " TEXT PRIMARY KEY" + ");";

  private static final String CREATE_TABLE_EXAM =
      "CREATE TABLE " + TABLE_EXAM + " (" +
          COLUMN_EXAM_ID + " INTEGER PRIMARY KEY, " +
          COLUMN_EXAM_NAME + " TEXT, " +
          COLUMN_EXAM_TIME + " INTEGER" +
      ");";

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_TABLE_USER);
    db.execSQL(CREATE_TABLE_EXAM);  // 创建新表
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // 从版本1升级到版本2：新增exam表
    if (oldVersion < 2) {
      db.execSQL(CREATE_TABLE_EXAM);
    }
    // 后续版本升级可继续添加 else if 分支
  }
}