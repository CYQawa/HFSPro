package com.cyq.awa.hfspro.tools.MyDatabases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.cyq.awa.hfspro.tools.MyModel;
import java.util.ArrayList;
import java.util.List;

public class Daos {
  public static final String TABLE_USER = "user";
  public static final String COLUMN_TOKEN = "token";

  public static final String TABLE_EXAM = "exam";
  public static final String COLUMN_EXAM_ID = "examId";
  public static final String COLUMN_EXAM_NAME = "name";
  public static final String COLUMN_EXAM_TIME = "time";

  public static class TokenDao {

    private final DatabaseHelper dbHelper;

    public TokenDao(DatabaseHelper helper) {
      this.dbHelper = helper;
    }

    public void saveToken(String token) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();

      db.delete(TABLE_USER, null, null);

      // 插入新token
      ContentValues values = new ContentValues();
      values.put(COLUMN_TOKEN, token);
      db.insert(TABLE_USER, null, values);

      db.close();
    }

    public String getToken() {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      String token = null;

      Cursor cursor =
          db.query(TABLE_USER, new String[] {COLUMN_TOKEN}, null, null, null, null, null);

      if (cursor.moveToFirst()) {
        token = cursor.getString(0);
      }

      cursor.close();
      db.close();
      return token;
    }

    public boolean hasToken() {
      return getToken() != null;
    }

    /** 清除 token */
    public void clearToken() {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      db.delete(TABLE_USER, null, null);
      db.close();
    }
  }

  public static class ExamDao {

    private final DatabaseHelper dbHelper;

    public ExamDao(DatabaseHelper helper) {
      this.dbHelper = helper;
    }

    /** 获取所有考试的 examId 列表 */
    public List<Long> getAllExamIds() {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      List<Long> ids = new ArrayList<>();
      Cursor cursor =
          db.query(TABLE_EXAM, new String[] {COLUMN_EXAM_ID}, null, null, null, null, null);
      while (cursor.moveToNext()) {
        ids.add(cursor.getLong(0));
      }
      cursor.close();
      db.close();
      return ids;
    }

    public void insertOrUpdateExam(MyModel.MyExamListItem exam) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put(COLUMN_EXAM_ID, exam.getExamId());
      values.put(COLUMN_EXAM_NAME, exam.getName());
      values.put(COLUMN_EXAM_TIME, exam.getTime());
      db.replace(TABLE_EXAM, null, values);
      db.close();
    }

    public void insertOrUpdateExams(List<MyModel.MyExamListItem> examList) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      db.beginTransaction();
      try {
        for (MyModel.MyExamListItem exam : examList) {
          ContentValues values = new ContentValues();
          values.put(COLUMN_EXAM_ID, exam.getExamId());
          values.put(COLUMN_EXAM_NAME, exam.getName());
          values.put(COLUMN_EXAM_TIME, exam.getTime());
          db.replace(TABLE_EXAM, null, values);
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
        db.close();
      }
    }

    /** 根据 examId 删除考试记录 */
    public void deleteExamById(long examId) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      db.delete(TABLE_EXAM, COLUMN_EXAM_ID + " = ?", new String[] {String.valueOf(examId)});
      db.close();
    }

    /** 清空考试表 */
    public void clearAllExams() {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      db.delete(TABLE_EXAM, null, null);
      db.close();
    }

    public List<MyModel.MyExamListItem> getAllExams() {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      List<MyModel.MyExamListItem> exams = new ArrayList<>();
      Cursor cursor =
          db.query(
              TABLE_EXAM,
              new String[] {COLUMN_EXAM_ID, COLUMN_EXAM_NAME, COLUMN_EXAM_TIME},
              null,
              null,
              null,
              null,
              null);
      while (cursor.moveToNext()) {
        long examId = cursor.getLong(0);
        String name = cursor.getString(1);
        long time = cursor.getLong(2);
        exams.add(new MyModel.MyExamListItem(examId, name, time));
      }
      cursor.close();
      db.close();
      return exams;
    }
  }
}
