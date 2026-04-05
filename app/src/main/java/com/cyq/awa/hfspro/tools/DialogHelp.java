package com.cyq.awa.hfspro.tools;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cyq.awa.hfspro.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;

public class DialogHelp {

    private static WeakReference<Dialog> dialogRef;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    
    private static WeakReference<Activity> currentActivityRef = null;

  
    public static void init(Application application) {
        //，初始化
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                currentActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                currentActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                // 不置空，避免切换时丢失
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                if (currentActivityRef != null && currentActivityRef.get() == activity) {
                    
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (currentActivityRef != null && currentActivityRef.get() == activity) {
                    currentActivityRef.clear();
                }
            }
        });
    }

  
    @Nullable
    private static Activity getCurrentActivity() {
        return currentActivityRef != null ? currentActivityRef.get() : null;
    }

   
    public static void show(@NonNull String title) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            
            return;
        }
        showInternal(activity, title);
    }

    public static void show() {
        show("请稍候");
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
                .setCancelable(false)
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