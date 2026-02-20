package com.cyq.awa.hfspro.tools.MyDatabases;

import com.cyq.awa.hfspro.App;
import com.cyq.awa.hfspro.tools.MyDatabases.Daos.*;
import com.cyq.awa.hfspro.tools.MyModel;
import java.util.List;

public class DatabaseManager {
  private static DatabaseManager instance;
  private final TokenDao tokenDao;
  private final ExamDao examDao;

  private DatabaseManager() {
    DatabaseHelper dbHelper = App.getDatabaseHelper();
    this.tokenDao = new TokenDao(dbHelper);
    this.examDao = new ExamDao(dbHelper);
  }

  /** 获取单例 */
  public static synchronized DatabaseManager getInstance() {
    if (instance == null) {
      instance = new DatabaseManager();
    }
    return instance;
  }

  public void saveToken(String token) {
    tokenDao.saveToken(token);
  }

  /** 获取 token */
  public String getToken() {
    return tokenDao.getToken();
  }

  public boolean hasToken() {
    return tokenDao.hasToken();
  }

  public void clearToken() {
    tokenDao.clearToken();
  }

  /** exam方法 */
  public List<Long> getAllExamIds() {
    return examDao.getAllExamIds();
  }

  public void insertOrUpdateExam(MyModel.MyExamListItem exam) {
    examDao.insertOrUpdateExam(exam);
  }

  public List<MyModel.MyExamListItem> getAllExams() {
    return examDao.getAllExams();
  }
}
