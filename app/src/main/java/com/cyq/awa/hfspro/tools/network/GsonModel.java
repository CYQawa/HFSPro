package com.cyq.awa.hfspro.tools.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GsonModel {

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
}