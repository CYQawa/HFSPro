package com.cyq.awa.hfspro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.RankSnapshot;
import com.cyq.awa.hfspro.tools.ScoreFormat;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RankListAdapter extends RecyclerView.Adapter<RankListAdapter.ViewHolder> {

  public interface OnItemClickListener {
    void onItemClick(RankSnapshot item);
  }

  private final List<RankSnapshot> list;
  private final OnItemClickListener listener;
  private final SimpleDateFormat dateFormat =
      new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

  public RankListAdapter(List<RankSnapshot> list, OnItemClickListener listener) {
    this.list = list;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rank_record, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    RankSnapshot item = list.get(position);

    String name = item.getExamName();
    if (name == null || name.trim().isEmpty()) {
      name = "考试 " + item.getExamId();
    }
    holder.name.setText(name);
    holder.meta.setText(buildMeta(item));
    holder.score.setText(ScoreFormat.format(item.getScore()));
    holder.manfen.setText("/" + ScoreFormat.format(item.getManfen()));

    holder.itemView.setOnClickListener(
        v -> {
          if (listener != null) listener.onItemClick(item);
        });
  }

  private String buildMeta(RankSnapshot item) {
    StringBuilder meta = new StringBuilder();
    meta.append(item.getExamTime() > 0 ? dateFormat.format(new Date(item.getExamTime())) : "时间未知");
    if (item.getClassRank() > 0) {
      meta.append(" · 班级 ").append(item.getClassRank());
    }
    if (item.getGradeRank() > 0) {
      meta.append(" · 年段 ").append(item.getGradeRank());
    }
    return meta.toString();
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final MaterialTextView name;
    final MaterialTextView meta;
    final MaterialTextView score;
    final MaterialTextView manfen;

    ViewHolder(View itemView) {
      super(itemView);
      name = itemView.findViewById(R.id.exam_name);
      meta = itemView.findViewById(R.id.exam_meta);
      score = itemView.findViewById(R.id.exam_score);
      manfen = itemView.findViewById(R.id.exam_manfen);
    }
  }
}
