package com.cyq.awa.hfspro.tools;

import android.app.Activity;
import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.cyq.awa.hfspro.R;
import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.loadingindicator.LoadingIndicator;

import java.lang.ref.WeakReference;

public class DialogHelp {

    private static WeakReference<Dialog> dialogRef;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void show(@NonNull Activity activity) {
        showInternal(activity, "请稍候");
    }

    public static void show(@NonNull Activity activity, @NonNull String title) {
        showInternal(activity, title);
    }

    

    private static void showInternal(@NonNull Activity activity, @NonNull String title) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        dismiss();

       
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_loading_content, null);
        
        Dialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setView(dialogView)          
                .setCancelable(false)         // 禁止按返回键取消
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