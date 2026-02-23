package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.MyModel.MarkInfo;
import com.cyq.awa.hfspro.transform.AnswerSheetTransformation;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

public class ImagePreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        PhotoView photoView = findViewById(R.id.photo_view);

        // 获取传递的图片 URL
        String imageUrl = getIntent().getStringExtra("image_url");

        // 获取传递的标记列表（可能为 null）
        List<MarkInfo> marks = (List<MarkInfo>) getIntent().getSerializableExtra("marks");

        if (imageUrl != null) {
            // 加载网络图片，如果有标记则应用转换
            if (marks != null && !marks.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .transform(new AnswerSheetTransformation(marks))
                        .into(photoView);
            } else {
                Glide.with(this)
                        .load(imageUrl)
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .into(photoView);
            }
        } else {
            // 本地资源图片（基本上用不到）
            int imageRes = getIntent().getIntExtra("image_res", 0);
            if (imageRes != 0) {
                photoView.setImageResource(imageRes);
            }
        }

    }
}