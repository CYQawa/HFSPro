package com.cyq.awa.hfspro.tools.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GsonModel {
  // Login请求体模型
  public static class LoginRequest {
    @SerializedName("loginName")
    private String loginName;

    @SerializedName("password")
    private String password;

    @SerializedName("roleType")
    private int roleType;

    @SerializedName("loginType")
    private int loginType = 1; // 固定值

    @SerializedName("rememberMe")
    private int rememberMe = 1; // 固定值

    public LoginRequest(String loginName, String password, int s) {
      this.loginName = loginName;
      this.password = password;
      this.roleType = s;
    }
  }

  // Login登录响应
  public static class LoginData {
    @SerializedName("token")
    private String token;

    public String getToken() {
      return this.token;
    }

    public void setToken(String token) {
      this.token = token;
    }
  }


  public static class ExamHomeData {
    @SerializedName("list")
    private List<ExamItem> list;

    public List<ExamItem> getList() {
      return list;
    }
  }

  // 单个考试项
  public static class ExamItem {
    @SerializedName("examId")
    private long examId;

    @SerializedName("name")
    private String name;

    @SerializedName("time")
    private long time;

    @SerializedName("manfen")
    private int manfen;

    @SerializedName("score")
    private String score;

    @SerializedName("papers")
    private List<PaperItem> papers;

    // 只取需要的字段，加 getter
    public long getExamId() {
      return examId;
    }

    public String getName() {
      return name;
    }

    public long getTime() {
      return time;
    }

    public int getManfen() {
      return manfen;
    }

    public String getScore() {
      return score;
    }

    public List<PaperItem> getPapers() {
      return papers;
    }
  }

  // 试卷科目项
  public static class PaperItem {
    @SerializedName("id")
    private String id;

    @SerializedName("subject")
    private String subject;

    @SerializedName("manfen")
    private int manfen;

    @SerializedName("score")
    private String score;

    public String getId() {
      return id;
    }

    public String getSubject() {
      return subject;
    }

    public int getManfen() {
      return manfen;
    }

    public String getScore() {
      return score;
    }
  }

  // 通用的API响应类
  public static class ApiResponse<T> {
    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String msg;

    @SerializedName("data")
    private T data;

    public int getCode() {
      return this.code;
    }

    public String getMsg() {
      return this.msg;
    }

    public T getData() {
      return this.data;
    }

    // 通用方法
    public boolean isSuccess() {
      return code == 0;
    }
  }
}
