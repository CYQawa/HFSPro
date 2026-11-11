package com.cyq.awa.hfspro.tools.MyDatabases;

import com.cyq.awa.hfspro.App;
import com.cyq.awa.hfspro.tools.MyDatabases.Daos.*;
import com.cyq.awa.hfspro.tools.MyModel;
import com.cyq.awa.hfspro.tools.RankSnapshot;
import java.util.List;

public class DatabaseManager {
  private static DatabaseManager instance;
  private final TokenDao tokenDao;
  private final ExamDao examDao;
  private final RankDao rankDao;

  private DatabaseManager() {
    DatabaseHelper dbHelper = App.getDatabaseHelper();
    this.tokenDao = new TokenDao(dbHelper);
    this.examDao = new ExamDao(dbHelper);
    this.rankDao = new RankDao(dbHelper);
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
      //，获取全部考试记录id
    return examDao.getAllExamIds();
  }

  public void insertOrUpdateExam(MyModel.MyExamListItem exam) {
      //，插入考试记录
    examDao.insertOrUpdateExam(exam);
  }

  public List<MyModel.MyExamListItem> getAllExams() {
      //，获取全部考试记录
    return examDao.getAllExams();
  }

  public void insertOrUpdateExams(List<MyModel.MyExamListItem> exams) {
      //，批量插入考试记录
    examDao.insertOrUpdateExams(exams);
  }

  public void deleteExamById(long examId) {
      //，通过id删除考试记录
    examDao.deleteExamById(examId);
  }
  
  public void clearAllExams() {
      //，清除全部考试记录
    examDao.clearAllExams();
  }

  /** 最近考试排名快照 */
  public void saveRankSnapshot(RankSnapshot snapshot) {
    rankDao.save(snapshot);
  }

  public List<RankSnapshot> getAllRankSnapshots() {
    return rankDao.getAll();
  }

  public RankSnapshot getRankSnapshot(long examId) {
    return rankDao.getById(examId);
  }

  public void deleteRankSnapshot(long examId) {
    rankDao.deleteById(examId);
  }
}
