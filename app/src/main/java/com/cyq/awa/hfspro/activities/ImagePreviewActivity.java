package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;          // 如果使用Glide加载网络图片
import com.bumptech.glide.request.target.Target;
import com.cyq.awa.hfspro.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;
public class ImagePreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        PhotoView photoView = findViewById(R.id.photo_view);
        

        // 获取传递的图片资源ID
        int imageRes = getIntent().getIntExtra("image_res", 0);
        if (imageRes != 0) {
            // 本地资源图片
            photoView.setImageResource(imageRes);
        } else {
            // 如果是网络图片，可以用Glide加载
            String imageUrl = getIntent().getStringExtra("image_url");
            Glide.with(this).load(imageUrl).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).into(photoView);
        }
        Snackbar.make(findViewById(R.id.background),"按返回键返回",Snackbar.LENGTH_SHORT).show();

        // PhotoView 自带缩放、平移功能，无需额外代码
        // 可选：设置最大缩放比例
        // photoView.setMaximumScale(10);
    }
}