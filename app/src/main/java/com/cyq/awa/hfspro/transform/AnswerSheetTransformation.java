package com.cyq.awa.hfspro.transform;

import android.content.Context;
import android.graphics.*;
import androidx.annotation.NonNull;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.cyq.awa.hfspro.tools.MyModel.MarkInfo;
import java.security.MessageDigest;
import java.util.List;

public class AnswerSheetTransformation extends BitmapTransformation {
    private final List<MarkInfo> marks;
    private final Paint correctPaint;
    private final Paint wrongPaint;
    private final Paint subjectiveBoxPaint;
    private final Paint textPaint;          // 普通文本画笔（主观题得分）
    private final Paint largeTextPaint;      // 大号文本画笔（总体情况）

    public AnswerSheetTransformation(List<MarkInfo> marks) {
        this.marks = marks;
        correctPaint = new Paint();
        correctPaint.setStyle(Paint.Style.STROKE);
        correctPaint.setColor(Color.BLUE);
        correctPaint.setStrokeWidth(5f);

        wrongPaint = new Paint();
        wrongPaint.setStyle(Paint.Style.STROKE);
        wrongPaint.setColor(Color.RED);
        wrongPaint.setStrokeWidth(5f);

        subjectiveBoxPaint = new Paint();
        subjectiveBoxPaint.setStyle(Paint.Style.STROKE);
        subjectiveBoxPaint.setColor(Color.RED);
        subjectiveBoxPaint.setStrokeWidth(1f);
      
        // 普通文本画笔 
        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(50f);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.LEFT);

        // 大号文本画笔
        largeTextPaint = new Paint();
        largeTextPaint.setColor(Color.RED);
        largeTextPaint.setTextSize(80f);
        largeTextPaint.setAntiAlias(true);
        largeTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap result = toTransform.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);

        for (MarkInfo mark : marks) {
            int type = mark.getType();
            if (type == 0) { // 客观题标记
                Paint paint = mark.getRight() == 1 ? correctPaint : wrongPaint;
                canvas.drawRect(mark.getX(), mark.getY(),
                        mark.getX() + mark.getW(), mark.getY() + mark.getH(), paint);
            } else if (type == 1) { // 主观题区域框
                canvas.drawRect(mark.getX(), mark.getY(),
                        mark.getX() + mark.getW(), mark.getY() + mark.getH(), subjectiveBoxPaint);
            } else if (type == 2) { // 主观题得分文本 或 总体情况文本
                String content = mark.getContent();
                Paint paint;
                
                if (content != null && (content.startsWith("总分:") || content.startsWith("主观:") || content.startsWith("客观:"))) {
                    paint = largeTextPaint;
                } else {
                    paint = textPaint;
                }
                canvas.drawText(content, mark.getX(), mark.getY(), paint);
            }
        }
        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((getClass().getName() + marks.hashCode()).getBytes(CHARSET));
    }
}