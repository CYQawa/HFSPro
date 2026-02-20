package com.cyq.awa.hfspro.adapter;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.widget.ProgressBar;
import com.cyq.awa.hfspro.activities.ExamActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textview.MaterialTextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import com.cyq.awa.hfspro.R;
import android.view.LayoutInflater;
import com.cyq.awa.hfspro.tools.MyModel.MyExamListItem;

public class ExamListAdapter extends RecyclerView.Adapter<ExamListAdapter.ViewHolder> {
  private List<MyExamListItem> list;
  private Context context;

  public ExamListAdapter(Context context, List<MyExamListItem> list) {
    this.context = context;
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
    String time =
        new SimpleDateFormat("yyyy-MM-dd").format(new Date(list.get(position).getTime()));
    holder.nametextView.setText(list.get(position).getName());
    holder.timetextView.setText(time);
        holder.idtextView.setText("examId："+list.get(position).getExamId());

    holder.itemView.setOnClickListener(
        v -> {
          Intent intent = new Intent(context, ExamActivity.class);
          intent.putExtra("myexam", list.get(position)); 
          context.startActivity(intent);
        });
        
        if(list.get(position).getIs_network()){
            holder.status.setVisibility(View.GONE);
        }
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    MaterialTextView nametextView;
    MaterialTextView timetextView;
    MaterialTextView idtextView;
        MaterialTextView status;

    ViewHolder(View itemView) {
      super(itemView);
      nametextView = itemView.findViewById(R.id.exam_name);
      timetextView = itemView.findViewById(R.id.exam_time);
      idtextView = itemView.findViewById(R.id.exam_id);
      status =itemView.findViewById(R.id.status);
    }
  }
}
