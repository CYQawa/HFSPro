package com.cyq.awa.hfspro.tools;

import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MyModel {
  // 考试概览数据（对应 v3/exam/{examId}/overview 的 data 字段）
  public static class MyExamOverviewData {
    private double score;
    private int manfen;
    private List<MyPaperOverview> papers;

    public double getScore() {
      return score;
    }

    public int getManfen() {
      return manfen;
    }

    public List<MyPaperOverview> getPapers() {
      return papers;
    }
  }

  // 试卷概览信息（对应 papers 数组中的每一项）
  public static class MyPaperOverview implements Serializable {
    private String paperId;
    private String subject;
    private double score; // 该卷得分
    private int manfen; // 该卷满分
    private int weakAdvantageStatus;

    public MyPaperOverview(PaperOverview p) {
      paperId = p.getPaperId();
      subject = p.getSubject();
      score = p.getScore();
      manfen = p.getManfen();
      weakAdvantageStatus = p.getWeakAdvantageStatus();
    }

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

  public static class MyExamListItem implements Serializable {
    private long examId;
    private String name;
    private Long time;
    private boolean is_network;

    public MyExamListItem(ExamListItem examlistitem) {
      examId = examlistitem.getExamId();
      name = examlistitem.getName();
      time = examlistitem.getTime();
      is_network = true;
    }

    public MyExamListItem(long examId, String name, Long time) {
      this.examId = examId;
      this.name = name;
      this.time = time;
      is_network = false;
    }

    public long getExamId() {
      return examId;
    }

    public String getName() {
      return name;
    }

    public long getTime() {
      return time;
    }

    public boolean getIs_network() {
      return this.is_network;
    }

    public void setIs_network(boolean is_network) {
      this.is_network = is_network;
    }
  }
}
