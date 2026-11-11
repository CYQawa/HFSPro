package com.cyq.awa.hfspro.tools;

import com.cyq.awa.hfspro.tools.network.GsonModel.PaperOverview;

import java.util.List;

/** 最近考试排名的一次快照，成功获取后落库，供 7 天有效期外查看。 */
public class RankSnapshot {

  private long examId;
  private String examName;
  private long examTime;
  private double score;
  private double manfen;
  private int classRank;
  private int classStuNum;
  private int gradeRank;
  private int gradeStuNum;
  private double classDefeatRatio;
  private double gradeDefeatRatio;
  private double scoreRaise;
  private int rankRaise;
  private double simpleLost;
  private double middleLost;
  private double hardLost;
  private String worstSubject;
  private List<PaperOverview> papers;
  private long savedAt;

  public long getExamId() {
    return examId;
  }

  public void setExamId(long examId) {
    this.examId = examId;
  }

  public String getExamName() {
    return examName;
  }

  public void setExamName(String examName) {
    this.examName = examName;
  }

  public long getExamTime() {
    return examTime;
  }

  public void setExamTime(long examTime) {
    this.examTime = examTime;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public double getManfen() {
    return manfen;
  }

  public void setManfen(double manfen) {
    this.manfen = manfen;
  }

  public int getClassRank() {
    return classRank;
  }

  public void setClassRank(int classRank) {
    this.classRank = classRank;
  }

  public int getClassStuNum() {
    return classStuNum;
  }

  public void setClassStuNum(int classStuNum) {
    this.classStuNum = classStuNum;
  }

  public int getGradeRank() {
    return gradeRank;
  }

  public void setGradeRank(int gradeRank) {
    this.gradeRank = gradeRank;
  }

  public int getGradeStuNum() {
    return gradeStuNum;
  }

  public void setGradeStuNum(int gradeStuNum) {
    this.gradeStuNum = gradeStuNum;
  }

  public double getClassDefeatRatio() {
    return classDefeatRatio;
  }

  public void setClassDefeatRatio(double classDefeatRatio) {
    this.classDefeatRatio = classDefeatRatio;
  }

  public double getGradeDefeatRatio() {
    return gradeDefeatRatio;
  }

  public void setGradeDefeatRatio(double gradeDefeatRatio) {
    this.gradeDefeatRatio = gradeDefeatRatio;
  }

  public double getScoreRaise() {
    return scoreRaise;
  }

  public void setScoreRaise(double scoreRaise) {
    this.scoreRaise = scoreRaise;
  }

  public int getRankRaise() {
    return rankRaise;
  }

  public void setRankRaise(int rankRaise) {
    this.rankRaise = rankRaise;
  }

  public double getSimpleLost() {
    return simpleLost;
  }

  public void setSimpleLost(double simpleLost) {
    this.simpleLost = simpleLost;
  }

  public double getMiddleLost() {
    return middleLost;
  }

  public void setMiddleLost(double middleLost) {
    this.middleLost = middleLost;
  }

  public double getHardLost() {
    return hardLost;
  }

  public void setHardLost(double hardLost) {
    this.hardLost = hardLost;
  }

  public String getWorstSubject() {
    return worstSubject;
  }

  public void setWorstSubject(String worstSubject) {
    this.worstSubject = worstSubject;
  }

  public List<PaperOverview> getPapers() {
    return papers;
  }

  public void setPapers(List<PaperOverview> papers) {
    this.papers = papers;
  }

  public long getSavedAt() {
    return savedAt;
  }

  public void setSavedAt(long savedAt) {
    this.savedAt = savedAt;
  }
}
