package com.cyq.awa.hfspro.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.widget.ProgressBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textview.MaterialTextView;
import java.text.SimpleDateFormat;
import java.util.List;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import com.cyq.awa.hfspro.R;
import android.view.LayoutInflater;
import com.cyq.awa.hfspro.tools.MyModel.MyExam;

public class ExamListAdapter extends RecyclerView.Adapter<ExamListAdapter.ViewHolder> {
  private List<MyExam> list;
private Context context;
  public ExamListAdapter(List<MyExam> list) {
    this.list = list;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      context = parent.getContext();
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.exam_list_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      // 毫秒级时间戳转标准时间
String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(list.get(position).getTime()));
    holder.nametextView.setText(list.get(position).getName());
    holder.timetextView.setText(time);
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    MaterialTextView nametextView;
    MaterialTextView timetextView;

    ViewHolder(View itemView) {
      super(itemView);
      nametextView = itemView.findViewById(R.id.exam_name);
      timetextView = itemView.findViewById(R.id.exam_time);
    }
  }
}
