package com.cyq.awa.hfspro.tools.MyDatabases;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;

public class Daos {
  public static final String TABLE_USER = "user";
  public static final String COLUMN_TOKEN = "token";

  public static class TokenDao {

    private final DatabaseHelper dbHelper;

    public TokenDao(DatabaseHelper helper) {
      this.dbHelper = helper;
    }

    /** 保存 token（先清空旧数据，再插入新数据） */
    public void saveToken(String token) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();

      // 清空表（因为只需要一个token）
      db.delete(TABLE_USER, null, null);

      // 插入新token
      ContentValues values = new ContentValues();
      values.put(COLUMN_TOKEN, token);
      db.insert(TABLE_USER, null, values);

      db.close();
    }

    /** 获取 token */
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

    /** 检查是否有 token */
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
}
