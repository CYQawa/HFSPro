package com.cyq.awa.hfspro.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.MyModel.MyPaperOverview;
import com.google.android.material.textview.MaterialTextView;
import java.util.List;

public class PaperGridAdapter extends RecyclerView.Adapter<PaperGridAdapter.ViewHolder> {
  private List<MyPaperOverview> list;

  public PaperGridAdapter(List<MyPaperOverview> MyPaperItem) {
    list = MyPaperItem;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.paper_item_grid, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    int status = list.get(position).getWeakAdvantageStatus();
    if (status == 3) {
      holder.name.setText(list.get(position).getSubject() + "(劣)");
    } else if (status == 1) {
      holder.name.setText(list.get(position).getSubject() + "(优)");
    } else {
      holder.name.setText(list.get(position).getSubject());
    }
    holder.score.setText("" + list.get(position).getScore());
    holder.manfen.setText("/" + list.get(position).getManfen());
  }

  @Override
  public int getItemCount() {
    return list.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    MaterialTextView name;
    MaterialTextView score;
    MaterialTextView manfen;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      name = itemView.findViewById(R.id.name);
      score = itemView.findViewById(R.id.score);
      manfen = itemView.findViewById(R.id.manfen);
    }
  }
}
