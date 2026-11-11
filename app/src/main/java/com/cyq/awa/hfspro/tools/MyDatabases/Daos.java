package com.cyq.awa.hfspro.tools.MyDatabases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.cyq.awa.hfspro.tools.MyModel;
import com.cyq.awa.hfspro.tools.RankSnapshot;
import com.cyq.awa.hfspro.tools.network.GsonModel.PaperOverview;
import com.cyq.awa.hfspro.tools.network.LenientGson;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Daos {
  public static final String TABLE_USER = "user";
  public static final String COLUMN_TOKEN = "token";

  public static final String TABLE_EXAM = "exam";
  public static final String COLUMN_EXAM_ID = "examId";
  public static final String COLUMN_EXAM_NAME = "name";
  public static final String COLUMN_EXAM_TIME = "time";

  /** 最近考试排名快照表 */
  public static final String TABLE_RANK = "last_exam_rank";
  public static final String COLUMN_RANK_EXAM_ID = "examId";
  public static final String COLUMN_RANK_NAME = "examName";
  public static final String COLUMN_RANK_TIME = "examTime";
  public static final String COLUMN_RANK_SCORE = "score";
  public static final String COLUMN_RANK_MANFEN = "manfen";
  public static final String COLUMN_RANK_CLASS_RANK = "classRank";
  public static final String COLUMN_RANK_CLASS_NUM = "classStuNum";
  public static final String COLUMN_RANK_GRADE_RANK = "gradeRank";
  public static final String COLUMN_RANK_GRADE_NUM = "gradeStuNum";
  public static final String COLUMN_RANK_CLASS_RATIO = "classDefeatRatio";
  public static final String COLUMN_RANK_GRADE_RATIO = "gradeDefeatRatio";
  public static final String COLUMN_RANK_SCORE_RAISE = "scoreRaise";
  public static final String COLUMN_RANK_RANK_RAISE = "rankRaise";
  public static final String COLUMN_RANK_SIMPLE_LOST = "simpleLost";
  public static final String COLUMN_RANK_MIDDLE_LOST = "middleLost";
  public static final String COLUMN_RANK_HARD_LOST = "hardLost";
  public static final String COLUMN_RANK_WORST = "worstSubject";
  public static final String COLUMN_RANK_PAPERS = "papersJson";
  public static final String COLUMN_RANK_SAVED_AT = "savedAt";

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
  public static class RankDao {

    private static final Type PAPER_LIST_TYPE = new TypeToken<List<PaperOverview>>() {}.getType();

    private final DatabaseHelper dbHelper;

    public RankDao(DatabaseHelper helper) {
      this.dbHelper = helper;
    }

    /** 同一场考试重复保存时覆盖旧记录 */
    public void save(RankSnapshot snapshot) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put(COLUMN_RANK_EXAM_ID, snapshot.getExamId());
      values.put(COLUMN_RANK_NAME, snapshot.getExamName());
      values.put(COLUMN_RANK_TIME, snapshot.getExamTime());
      values.put(COLUMN_RANK_SCORE, snapshot.getScore());
      values.put(COLUMN_RANK_MANFEN, snapshot.getManfen());
      values.put(COLUMN_RANK_CLASS_RANK, snapshot.getClassRank());
      values.put(COLUMN_RANK_CLASS_NUM, snapshot.getClassStuNum());
      values.put(COLUMN_RANK_GRADE_RANK, snapshot.getGradeRank());
      values.put(COLUMN_RANK_GRADE_NUM, snapshot.getGradeStuNum());
      values.put(COLUMN_RANK_CLASS_RATIO, snapshot.getClassDefeatRatio());
      values.put(COLUMN_RANK_GRADE_RATIO, snapshot.getGradeDefeatRatio());
      values.put(COLUMN_RANK_SCORE_RAISE, snapshot.getScoreRaise());
      values.put(COLUMN_RANK_RANK_RAISE, snapshot.getRankRaise());
      values.put(COLUMN_RANK_SIMPLE_LOST, snapshot.getSimpleLost());
      values.put(COLUMN_RANK_MIDDLE_LOST, snapshot.getMiddleLost());
      values.put(COLUMN_RANK_HARD_LOST, snapshot.getHardLost());
      values.put(COLUMN_RANK_WORST, snapshot.getWorstSubject());
      values.put(COLUMN_RANK_PAPERS, toPaperJson(snapshot.getPapers()));
      values.put(COLUMN_RANK_SAVED_AT, snapshot.getSavedAt());
      db.replace(TABLE_RANK, null, values);
      db.close();
    }

    /** 按考试时间倒序 */
    public List<RankSnapshot> getAll() {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      List<RankSnapshot> result = new ArrayList<>();
      Cursor cursor =
          db.query(
              TABLE_RANK,
              null,
              null,
              null,
              null,
              null,
              COLUMN_RANK_TIME + " DESC, " + COLUMN_RANK_SAVED_AT + " DESC");
      while (cursor.moveToNext()) {
        result.add(readSnapshot(cursor));
      }
      cursor.close();
      db.close();
      return result;
    }

    public RankSnapshot getById(long examId) {
      SQLiteDatabase db = dbHelper.getReadableDatabase();
      Cursor cursor =
          db.query(
              TABLE_RANK,
              null,
              COLUMN_RANK_EXAM_ID + " = ?",
              new String[] {String.valueOf(examId)},
              null,
              null,
              null);
      RankSnapshot snapshot = null;
      if (cursor.moveToFirst()) {
        snapshot = readSnapshot(cursor);
      }
      cursor.close();
      db.close();
      return snapshot;
    }

    public void deleteById(long examId) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      db.delete(TABLE_RANK, COLUMN_RANK_EXAM_ID + " = ?", new String[] {String.valueOf(examId)});
      db.close();
    }

    public void clearAll() {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      db.delete(TABLE_RANK, null, null);
      db.close();
    }

    private RankSnapshot readSnapshot(Cursor cursor) {
      RankSnapshot snapshot = new RankSnapshot();
      snapshot.setExamId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RANK_EXAM_ID)));
      snapshot.setExamName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RANK_NAME)));
      snapshot.setExamTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RANK_TIME)));
      snapshot.setScore(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RANK_SCORE)));
      snapshot.setManfen(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RANK_MANFEN)));
      snapshot.setClassRank(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RANK_CLASS_RANK)));
      snapshot.setClassStuNum(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RANK_CLASS_NUM)));
      snapshot.setGradeRank(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RANK_GRADE_RANK)));
      snapshot.setGradeStuNum(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RANK_GRADE_NUM)));
      snapshot.setClassDefeatRatio(
          cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RANK_CLASS_RATIO)));
      snapshot.setGradeDefeatRatio(
          cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RANK_GRADE_RATIO)));
      snapshot.setScoreRaise(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RANK_SCORE_RAISE)));
      snapshot.setRankRaise(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RANK_RANK_RAISE)));
      snapshot.setSimpleLost(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RANK_SIMPLE_LOST)));
      snapshot.setMiddleLost(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RANK_MIDDLE_LOST)));
      snapshot.setHardLost(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RANK_HARD_LOST)));
      snapshot.setWorstSubject(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RANK_WORST)));
      snapshot.setSavedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_RANK_SAVED_AT)));
      snapshot.setPapers(
          parsePapers(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RANK_PAPERS))));
      return snapshot;
    }

    private static String toPaperJson(List<PaperOverview> papers) {
      if (papers == null || papers.isEmpty()) return null;
      return new Gson().toJson(papers);
    }

    private static List<PaperOverview> parsePapers(String json) {
      if (json == null || json.isEmpty()) return new ArrayList<>();
      try {
        List<PaperOverview> list = LenientGson.get().fromJson(json, PAPER_LIST_TYPE);
        return list == null ? new ArrayList<>() : list;
      } catch (Exception e) {
        return new ArrayList<>();
      }
    }
  }
}
