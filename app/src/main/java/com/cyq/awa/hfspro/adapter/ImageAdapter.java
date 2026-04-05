package com.cyq.awa.hfspro.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.graphics.Bitmap;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.activities.ImagePreviewActivity;
import com.cyq.awa.hfspro.tools.MyModel.MarkInfo;
import com.cyq.awa.hfspro.transform.AnswerSheetTransformation;

import com.google.android.material.loadingindicator.LoadingIndicator;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private List<String> imageUrls;
    @Nullable private List<List<MarkInfo>> marksPerSheet;

    // 原卷使用此构造函数（无标记）
    public ImageAdapter(List<String> imageUrls, Context context) {
        this(imageUrls, null, context);
    }

    // 答题卡使用此构造函数（带标记）
    public ImageAdapter(
            List<String> imageUrls, @Nullable List<List<MarkInfo>> marksPerSheet, Context context) {
        this.imageUrls = imageUrls;
        this.marksPerSheet = marksPerSheet;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = imageUrls.get(position);

        // 重置状态
        holder.imageView.setImageDrawable(null);
        holder.loadingIndicator.setVisibility(View.VISIBLE);

        Glide.with(holder.itemView.getContext()).clear(holder.imageView); // 清除之前的加载

        Glide.with(holder.itemView.getContext())
                .load(url)
                .override(Target.SIZE_ORIGINAL)
                .transform(getTransformationForPosition(position))
                .listener(
                        new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(
                                    @Nullable GlideException e,
                                    Object model,
                                    Target<Drawable> target,
                                    boolean isFirstResource) {
                                holder.loadingIndicator.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(
                                    Drawable resource,
                                    Object model,
                                    Target<Drawable> target,
                                    DataSource dataSource,
                                    boolean isFirstResource) {
                                holder.loadingIndicator.setVisibility(View.GONE);
                                return false;
                            }
                        })
                .into(holder.imageView);

        holder.imageView.setOnClickListener(
                v -> {
                    Intent intent = new Intent(context, ImagePreviewActivity.class);
                    intent.putExtra("image_url", url);

                    List<MarkInfo> marksForThisImage = null;
                    if (marksPerSheet != null && marksPerSheet.size() > position) {
                        marksForThisImage = marksPerSheet.get(position);
                    }
                    intent.putExtra("marks", (java.io.Serializable) marksForThisImage);
                    context.startActivity(intent);
                });
    }

    // 根据位置返回对应的 Transformation，如果无标记则返回空 Transformation
    private com.bumptech.glide.load.Transformation<android.graphics.Bitmap>
            getTransformationForPosition(int position) {
        if (marksPerSheet == null || marksPerSheet.size() <= position) {
            return new com.bumptech.glide.load.resource.bitmap.CenterCrop(); // 或任何默认转换
        }
        List<MarkInfo> marks = marksPerSheet.get(position);
        if (marks == null || marks.isEmpty()) {
            return new com.bumptech.glide.load.resource.bitmap.CenterCrop();
        }
        return new AnswerSheetTransformation(marks);
    }

    @Override
    public int getItemCount() {
        return imageUrls == null ? 0 : imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        LoadingIndicator loadingIndicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            loadingIndicator = itemView.findViewById(R.id.loading_indicator);
        }
    }
}
