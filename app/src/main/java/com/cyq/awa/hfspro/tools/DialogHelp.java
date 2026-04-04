package com.cyq.awa.hfspro.tools;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;

public class DialogHelp {

    private static WeakReference<Dialog> dialogRef;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void show(@NonNull Activity activity) {
        show(activity, "请稍候", "正在加载中...");
    }

    public static void show(@NonNull Activity activity, @NonNull String message) {
        show(activity, "请稍候", message);
    }

    public static void show(@NonNull Activity activity, @NonNull String title, @NonNull String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(() -> showInternal(activity, title, message));
        } else {
            showInternal(activity, title, message);
        }
    }

    private static void showInternal(@NonNull Activity activity, @NonNull String title, @NonNull String message) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        dismiss();

        Dialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)          // 禁止按返回键取消
                .create();

        
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        dialogRef = new WeakReference<>(dialog);
    }

    public static void dismiss() {
        if (dialogRef != null) {
            Dialog dialog = dialogRef.get();
            if (dialog != null && dialog.isShowing()) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    dialog.dismiss();
                } else {
                    mainHandler.post(dialog::dismiss);
                }
            }
            dialogRef.clear();
            dialogRef = null;
        }
    }

    public static boolean isShowing() {
        return dialogRef != null && dialogRef.get() != null && dialogRef.get().isShowing();
    }
}