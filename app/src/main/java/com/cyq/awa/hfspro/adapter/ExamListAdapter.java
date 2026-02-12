package com.cyq.awa.hfspro.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.widget.ProgressBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textview.MaterialTextView;
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
    holder.nametextView.setText(list.get(position).getName());
    holder.idtextView.setText(String.valueOf(list.get(position).getExamId()));
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    MaterialTextView nametextView;
    MaterialTextView idtextView;

    ViewHolder(View itemView) {
      super(itemView);
      nametextView = itemView.findViewById(R.id.exam_name);
      idtextView = itemView.findViewById(R.id.exam_id);
    }
  }
}
