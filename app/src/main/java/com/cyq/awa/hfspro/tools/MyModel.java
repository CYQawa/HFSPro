package com.cyq.awa.hfspro.tools;

import com.cyq.awa.hfspro.tools.network.GsonModel.ExamItem;
import com.cyq.awa.hfspro.tools.network.GsonModel.PaperItem; // 新增导入
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MyModel {

  public static class MyExam implements Serializable{
        private long examId;
        private String name;
        private long time;
        private int manfen;
        private String score;
        private List<MyPaperItem> papers;
        
        public MyExam(){}

        //ExamItem -> MyExam
        public MyExam(ExamItem examitem) {
            this.examId = examitem.getExamId();
            this.name = examitem.getName();
            this.time = examitem.getTime();
            this.manfen = examitem.getManfen();
            this.score = examitem.getScore();

            // 转换 papers 列表
            if (examitem.getPapers() != null) {
                this.papers = new ArrayList<>();
                for (PaperItem paper : examitem.getPapers()) {
                    this.papers.add(new MyPaperItem(paper));
                }
            }
        }

        // getters
        public long getExamId() { return examId; }
        public String getName() { return name; }
        public long getTime() { return time; }
        public int getManfen() { return manfen; }
        public String getScore() { return score; }
        public List<MyPaperItem> getPapers() { return papers; }
    }

    public static class MyPaperItem implements Serializable{
        private String id;
        private String subject;
        private int manfen;
        private String score;
        
        public MyPaperItem(){}
        public MyPaperItem(PaperItem item) {
            this.id = item.getId();
            this.subject = item.getSubject();
            this.manfen = item.getManfen();
            this.score = item.getScore();
        }

        // getters
        public String getId() { return id; }
        public String getSubject() { return subject; }
        public int getManfen() { return manfen; }
        public String getScore() { return score; }
    }
}