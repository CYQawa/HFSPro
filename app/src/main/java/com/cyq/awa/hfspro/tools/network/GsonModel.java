package com.cyq.awa.hfspro.tools.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GsonModel {
  public static class ApiResponse<T> {
    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String msg;

    @SerializedName("data")
    private T data;

    public int getCode() {
      return code;
    }

    public String getMsg() {
      return msg;
    }

    public T getData() {
      return data;
    }

    public boolean isSuccess() {
      return code == 0;
    }
  }

  public static class LastExamData {

    @SerializedName("examId")
    private int examId;

    @SerializedName("subjectNumber")
    private int subjectNumber;

    @SerializedName("isManfen")
    private boolean isManfen;

    @SerializedName("classDefeatLevel")
    private int classDefeatLevel;

    @SerializedName("gradeDefeatLevel")
    private int gradeDefeatLevel;

    @SerializedName("extend")
    private Extend extend;

    @SerializedName("worstSubjectText")
    private String worstSubjectText;

    @SerializedName("simpleQuestionLostScores")
    private int simpleQuestionLostScores;

    @SerializedName("middleQuestionLostScores")
    private int middleQuestionLostScores;

    @SerializedName("hardQuestionLostScores")
    private int hardQuestionLostScores;

    @SerializedName("scoreRaise")
    private int scoreRaise;

    @SerializedName("rankRaise")
    private int rankRaise;

    public int getExamId() {
      return examId;
    }

    public int getSubjectNumber() {
      return subjectNumber;
    }

    public boolean getIsManfen() {
      return isManfen;
    }

    public int getClassDefeatLevel() {
      return classDefeatLevel;
    }

    public int getGradeDefeatLevel() {
      return gradeDefeatLevel;
    }

    public Extend getExtend() {
      return extend;
    }

    public String getWorstSubjectText() {
      return worstSubjectText;
    }

    public int getSimpleQuestionLostScores() {
      return simpleQuestionLostScores;
    }

    public int getMiddleQuestionLostScores() {
      return middleQuestionLostScores;
    }

    public int getHardQuestionLostScores() {
      return hardQuestionLostScores;
    }

    public int getScoreRaise() {
      return scoreRaise;
    }

    public int getRankRaise() {
      return rankRaise;
    }

    public static class Extend {

      @SerializedName("classRank")
      private int classRank;

      @SerializedName("classStuNum")
      private int classStuNum;

      @SerializedName("classDefeatRatio")
      private double classDefeatRatio;

      @SerializedName("gradeRank")
      private int gradeRank;

      @SerializedName("gradeStuNum")
      private int gradeStuNum;

      @SerializedName("gradeDefeatRatio")
      private double gradeDefeatRatio;

      public int getClassRank() {
        return classRank;
      }

      public int getClassStuNum() {
        return classStuNum;
      }

      public double getClassDefeatRatio() {
        return classDefeatRatio;
      }

      public int getGradeRank() {
        return gradeRank;
      }

      public int getGradeStuNum() {
        return gradeStuNum;
      }

      public double getGradeDefeatRatio() {
        return gradeDefeatRatio;
      }
    }
  }

  public static class CompareRankData {
    @SerializedName("compare")
    private Compare compare;

    public Compare getCompare() {
      return compare;
    }
  }

  public static class Compare {
    @SerializedName("curGradeRank")
    private Integer curGradeRank;

    public Integer getCurGradeRank() {
      return curGradeRank;
    }
  }

  public static class ExamOverviewData {
    @SerializedName("name")
    private String name;

    @SerializedName("score")
    private double score;

    @SerializedName("time")
    private Long time;

    @SerializedName("manfen")
    private int manfen;

    @SerializedName("papers")
    private List<PaperOverview> papers;

    public String getName() {
      return name;
    }

    public double getScore() {
      return score;
    }

    public int getManfen() {
      return manfen;
    }

    public List<PaperOverview> getPapers() {
      return papers;
    }

    public Long getTime() {
      return this.time;
    }

    public void setTime(Long time) {
      this.time = time;
    }
  }

  public static class PaperOverview {
    @SerializedName("paperId")
    private String paperId;

    @SerializedName("pid")
    private String pid;

    @SerializedName("subject")
    private String subject;

    @SerializedName("score")
    private double score;

    @SerializedName("manfen")
    private double manfen;

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

    public double getManfen() {
      return manfen;
    }

    public int getWeakAdvantageStatus() {
      return weakAdvantageStatus;
    }

    public String getPid() {
      return this.pid;
    }

    public void setPid(String pid) {
      this.pid = pid;
    }
  }

  public static class ExamListData {
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

    // 保留构造函数用于创建请求对象
    public LoginRequest(String loginName, String password, int roleType) {
      this.loginName = loginName;
      this.password = password;
      this.roleType = roleType;
    }

    // 如果需要读取请求字段，可添加 getter（但通常请求对象只用于序列化）
    public String getLoginName() {
      return loginName;
    }

    public String getPassword() {
      return password;
    }

    public int getRoleType() {
      return roleType;
    }

    public int getLoginType() {
      return loginType;
    }

    public int getRememberMe() {
      return rememberMe;
    }
  }

  public static class LoginData {
    @SerializedName("token")
    private String token;

    public String getToken() {
      return token;
    }
  }

  public static class moreExamlist {
    @SerializedName("examList")
    private List<moreExamItem> examList;

    public List<moreExamItem> getExamList() {
      return examList;
    }
  }

  // 表示考试维度的错题项
  public static class moreExamItem {
    @SerializedName("examName")
    private String examName;

    @SerializedName("examTime")
    private long examTime;

    @SerializedName("examId")
    private String examId; // 注意：响应中 examId 为字符串类型

    public String getExamName() {
      return examName;
    }

    public Long getExamTime() {
      return examTime;
    }

    public long getExamId() {
      return Long.parseLong(examId);
    }
  }

  // 答题卡信息响应 data 部分
  public static class AnswerPictureData {
    @SerializedName("url")
    private List<String> url; // 原图URL列表

    @SerializedName("urlResize")
    private List<String> urlResize; // 缩略图URL列表

    @SerializedName("isExam2")
    private boolean isExam2; // 是否为双卷

    @SerializedName("questions")
    private List<QuestionItem> questions; // 题目列表

    @SerializedName("paperPic")
    private List<String> paperPic; // 试卷原图列表

    @SerializedName("paperPicResize")
    private List<String> paperPicResize; // 试卷缩略图列表

    @SerializedName("score")
    private double score; // 总分

    // 其他字段可按需添加，如 nonvipScore, nonvipQuestion, show, config 等

    public List<String> getUrl() {
      return url;
    }

    public List<String> getUrlResize() {
      return urlResize;
    }

    public boolean isExam2() {
      return isExam2;
    }

    public List<QuestionItem> getQuestions() {
      return questions;
    }

    public List<String> getPaperPic() {
      return paperPic;
    }

    public List<String> getPaperPicResize() {
      return paperPicResize;
    }

    public double getScore() {
      return score;
    }
  }

  // 题目项
  public static class QuestionItem {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("manfen")
    private double manfen; // 满分

    @SerializedName("score")
    private double score; // 得分

    @SerializedName("type")
    private int type; // 题目类型

    @SerializedName("myAnswer")
    private String myAnswer; // 我的答案

    @SerializedName("answerOption")
    private List<AnswerOptionItem> answerOption; // 选项坐标信息（仅选择题有）

    @SerializedName("answer")
    private String answer; // 正确答案

    @SerializedName("trasRatio")
    private int trasRatio; // 折算比例

    @SerializedName("scoreS")
    private String scoreS; // 得分字符串（可能含小数）

    @SerializedName("remark2")
    private List<Object> remark2; // 批注信息（按需）

    @SerializedName("url")
    private List<String> url; // 该题对应的图片URL（主观题可能有）

    // Getters
    public String getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public double getManfen() {
      return manfen;
    }

    public double getScore() {
      return score;
    }

    public int getType() {
      return type;
    }

    public String getMyAnswer() {
      return myAnswer;
    }

    public List<AnswerOptionItem> getAnswerOption() {
      return answerOption;
    }

    public String getAnswer() {
      return answer;
    }

    public int getTrasRatio() {
      return trasRatio;
    }

    public String getScoreS() {
      return scoreS;
    }

    public List<Object> getRemark2() {
      return remark2;
    }

    public List<String> getUrl() {
      return url;
    }
  }

  // 选项坐标信息
  public static class AnswerOptionItem {
    @SerializedName("option")
    private String option; // 选项字母

    @SerializedName("index")
    private int index; // 索引

    @SerializedName("x")
    private String x; // x坐标（字符串，可转为int）

    @SerializedName("y")
    private String y; // y坐标

    @SerializedName("w")
    private String w; // 宽度

    @SerializedName("h")
    private String h; // 高度

    @SerializedName("right")
    private int right; // 是否正确（1正确，0错误）

    public String getOption() {
      return option;
    }

    public int getIndex() {
      return index;
    }

    public String getX() {
      return x;
    }

    public String getY() {
      return y;
    }

    public String getW() {
      return w;
    }

    public String getH() {
      return h;
    }

    public int getRight() {
      return right;
    }
  }
}
