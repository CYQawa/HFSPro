package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.fragments.guide.GuideFragment1;
import com.cyq.awa.hfspro.fragments.guide.GuideFragment2;
import com.cyq.awa.hfspro.fragments.guide.GuideFragment3;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class GuideActivity extends AppCompatActivity {
    private FragmentContainerView fragmentContainerView;
    private FragmentManager fragmentManager;
    private Fragment nowFragment;
    private GuideFragment1 gf1;
    private GuideFragment2 gf2;
    private GuideFragment3 gf3;
    private int page;

    // 用于保存和恢复的 Key
    private static final String KEY_PAGE = "current_page";
    private static final String TAG_FRAGMENT_1 = "guide_fragment_1";
    private static final String TAG_FRAGMENT_2 = "guide_fragment_2";
    private static final String TAG_FRAGMENT_3 = "guide_fragment_3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        fragmentContainerView = findViewById(R.id.fragment_guide);
        fragmentManager = getSupportFragmentManager();
        ExtendedFloatingActionButton fb = findViewById(R.id.floating_button);
        ExtendedFloatingActionButton fbb = findViewById(R.id.floating_back);
        

        if (savedInstanceState == null) {
           
            gf1 = new GuideFragment1();
            gf2 = new GuideFragment2();
            gf3 = new GuideFragment3();

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_guide, gf1, TAG_FRAGMENT_1)
                    .add(R.id.fragment_guide, gf2, TAG_FRAGMENT_2)
                    .add(R.id.fragment_guide, gf3, TAG_FRAGMENT_3)
                    .hide(gf2)
                    .hide(gf3)
                    .commit();

            // 初始显示第一个
            showFragment(gf1);
            page = 1;
        } else {
            
            gf1 = (GuideFragment1) fragmentManager.findFragmentByTag(TAG_FRAGMENT_1);
            gf2 = (GuideFragment2) fragmentManager.findFragmentByTag(TAG_FRAGMENT_2);
            gf3 = (GuideFragment3) fragmentManager.findFragmentByTag(TAG_FRAGMENT_3);

            // 恢复页面编号
            page = savedInstanceState.getInt(KEY_PAGE, 1);

           
            if (page == 1) {
                nowFragment = gf1;
            } else if (page == 2) {
                nowFragment = gf2;
            } else if (page == 3) {
                nowFragment = gf3;
            }
        }

        
        if (page == 1) {
            
            fb.extend();
            fb.setVisibility(View.VISIBLE);
            fbb.shrink();
            fbb.setVisibility(View.GONE);
        } else if (page == 2) {
            fb.shrink();
            fbb.shrink();  
            fb.setVisibility(View.VISIBLE);
            fbb.setVisibility(View.VISIBLE);
        } else if (page == 3) {
            fbb.shrink();  
            fb.setVisibility(View.GONE);
            fbb.setVisibility(View.VISIBLE);
        }

        // 绑定按钮点击事件
        fb.setOnClickListener(v -> {
            if (page == 1) {
                fb.shrink();
                fbb.setVisibility(View.VISIBLE);
                showFragment(gf2);
                page = 2;
            } else if (page == 2) {
                fb.setVisibility(View.GONE);
                showFragment(gf3);
                page = 3;
            }
        });

        fbb.setOnClickListener(v -> {
            if (page == 2) {
                fb.extend();
                fbb.setVisibility(View.GONE);
                showFragment(gf1);
                page = 1;
            } else if (page == 3) {
                fb.setVisibility(View.VISIBLE);
                showFragment(gf2);
                page = 2;
            }
        });
    }

    private void showFragment(Fragment aimFragment) {
        if (aimFragment == nowFragment) return;
        FragmentTransaction fragmentt = fragmentManager.beginTransaction();
        fragmentt.setCustomAnimations(
                R.anim.mtrl_bottom_sheet_slide_in, R.anim.mtrl_bottom_sheet_slide_out);
        if (nowFragment != null) {
            fragmentt.hide(nowFragment);
        }
        fragmentt.show(aimFragment);
        fragmentt.commit();
        nowFragment = aimFragment;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putInt(KEY_PAGE, page);
    }
}