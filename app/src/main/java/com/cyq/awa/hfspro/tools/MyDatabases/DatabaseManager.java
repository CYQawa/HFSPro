package com.cyq.awa.hfspro.tools.MyDatabases;

import com.cyq.awa.hfspro.App;
import com.cyq.awa.hfspro.tools.MyDatabases.Daos.TokenDao;

public class DatabaseManager {
  private static DatabaseManager instance;
  private final TokenDao tokenDao;

  private DatabaseManager() {
    DatabaseHelper dbHelper = App.getDatabaseHelper();
    this.tokenDao = new TokenDao(dbHelper);
  }

  /** 获取 TokenManager 单例 */
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

  /** 检查是否有 token */
  public boolean hasToken() {
    return tokenDao.hasToken();
  }

  /** 清除 token */
  public void clearToken() {
    tokenDao.clearToken();
  }
}
