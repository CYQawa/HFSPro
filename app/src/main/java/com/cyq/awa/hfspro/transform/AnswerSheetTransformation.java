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

    public AnswerSheetTransformation(List<MarkInfo> marks) {
        this.marks = marks;
        correctPaint = new Paint();
        correctPaint.setStyle(Paint.Style.STROKE);
        correctPaint.setColor(Color.BLUE);
        correctPaint.setStrokeWidth(5f); // 边框宽度

        wrongPaint = new Paint();
        wrongPaint.setStyle(Paint.Style.STROKE);
        wrongPaint.setColor(Color.RED);
        wrongPaint.setStrokeWidth(5f);
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        // 创建可变副本
        Bitmap result = toTransform.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(result);
        for (MarkInfo mark : marks) {
            Paint paint = mark.getRight() == 1 ? correctPaint : wrongPaint;
            canvas.drawRect(mark.getX(), mark.getY(),
                    mark.getX() + mark.getW(), mark.getY() + mark.getH(), paint);
             
        }
        return result;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update((getClass().getName() + marks.hashCode()).getBytes(CHARSET));
    }
}