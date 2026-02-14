package com.cyq.awa.hfspro.tools.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GsonModel {
  public static class CompareRankData {
    @SerializedName("compare")
    private Compare compare;

    public Compare getCompare() {
      return this.compare;
    }

    public void setCompare(Compare compare) {
      this.compare = compare;
    }
  }

  public static class Compare {
    @SerializedName("curGradeRank")
    private Integer curGradeRank;

    public Integer getCurGradeRank() {
      return this.curGradeRank;
    }

    public void setCurGradeRank(Integer curGradeRank) {
      this.curGradeRank = curGradeRank;
    }
  }

  public static class ExamOverviewData {
    @SerializedName("name")
    private String name;

    @SerializedName("score")
    private double score;

    @SerializedName("manfen")
    private int manfen;

    @SerializedName("papers")
    private List<PaperOverview> papers;

    public double getScore() {
      return score;
    }

    public int getManfen() {
      return manfen;
    }

    public List<PaperOverview> getPapers() {
      return papers;
    }

    public String getName() {
      return this.name;
    }
  }

  public static class PaperOverview {
    @SerializedName("paperId")
    private String paperId;

    @SerializedName("subject")
    private String subject;

    @SerializedName("score")
    private double score;

    @SerializedName("manfen")
    private int manfen;

    @SerializedName("weakAdvantageStatus")
    private int weakAdvantageStatus;

    public String getPaperId() {
      return paperId;
    }

    public String getSubject() {
      return subject;
    }

    public double getScore() {
      return score;
    }

    public int getManfen() {
      return manfen;
    }

    public int getWeakAdvantageStatus() {
      return weakAdvantageStatus;
    }
  }

  public class ExamListData {

    @SerializedName("list")
    private List<ExamListItem> list;

    public List<ExamListItem> getList() {
      return list;
    }
  }

  public static class ExamListItem {
    @SerializedName("examId")
    private long examId;

    @SerializedName("name")
    private String name;

    @SerializedName("time")
    private long time;

    public long getExamId() {
      return examId;
    }

    public String getName() {
      return name;
    }

    public long getTime() {
      return time;
    }
  }

  public static class LoginRequest {
    @SerializedName("loginName")
    private String loginName;

    @SerializedName("password")
    private String password;

    @SerializedName("roleType")
    private int roleType;

    @SerializedName("loginType")
    private int loginType = 1;

    @SerializedName("rememberMe")
    private int rememberMe = 1;

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
