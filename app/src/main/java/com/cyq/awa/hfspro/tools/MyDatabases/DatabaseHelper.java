package com.cyq.awa.hfspro.tools.MyDatabases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "HFSPro.db";
  private static final int DATABASE_VERSION = 3;  // 3: 新增 last_exam_rank 排名快照表

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

  // 最近考试排名快照表（字段常量统一定义在 Daos 中，避免多处维护）
  private static final String CREATE_TABLE_RANK =
      "CREATE TABLE IF NOT EXISTS " + Daos.TABLE_RANK + " (" +
          Daos.COLUMN_RANK_EXAM_ID + " INTEGER PRIMARY KEY, " +
          Daos.COLUMN_RANK_NAME + " TEXT, " +
          Daos.COLUMN_RANK_TIME + " INTEGER, " +
          Daos.COLUMN_RANK_SCORE + " REAL, " +
          Daos.COLUMN_RANK_MANFEN + " REAL, " +
          Daos.COLUMN_RANK_CLASS_RANK + " INTEGER, " +
          Daos.COLUMN_RANK_CLASS_NUM + " INTEGER, " +
          Daos.COLUMN_RANK_GRADE_RANK + " INTEGER, " +
          Daos.COLUMN_RANK_GRADE_NUM + " INTEGER, " +
          Daos.COLUMN_RANK_CLASS_RATIO + " REAL, " +
          Daos.COLUMN_RANK_GRADE_RATIO + " REAL, " +
          Daos.COLUMN_RANK_SCORE_RAISE + " REAL, " +
          Daos.COLUMN_RANK_RANK_RAISE + " INTEGER, " +
          Daos.COLUMN_RANK_SIMPLE_LOST + " REAL, " +
          Daos.COLUMN_RANK_MIDDLE_LOST + " REAL, " +
          Daos.COLUMN_RANK_HARD_LOST + " REAL, " +
          Daos.COLUMN_RANK_WORST + " TEXT, " +
          Daos.COLUMN_RANK_PAPERS + " TEXT, " +
          Daos.COLUMN_RANK_SAVED_AT + " INTEGER" +
      ");";

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_TABLE_USER);
    db.execSQL(CREATE_TABLE_EXAM);  // 创建新表
    db.execSQL(CREATE_TABLE_RANK);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // 从版本1升级到版本2：新增exam表
    if (oldVersion < 2) {
      db.execSQL(CREATE_TABLE_EXAM);
    }
    // 版本3：新增最近考试排名快照表
    if (oldVersion < 3) {
      db.execSQL(CREATE_TABLE_RANK);
    }
    // 后续版本升级可继续添加 else if 分支
  }
}