package com.cyq.awa.hfspro.activities;

import android.os.Bundle;
import android.view.View;
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_guide);

    gf1 = new GuideFragment1();
    gf2 = new GuideFragment2();
    gf3 = new GuideFragment3();

    fragmentContainerView = findViewById(R.id.fragment_guide);

    fragmentManager = getSupportFragmentManager();
    fragmentManager
        .beginTransaction()
        .add(R.id.fragment_guide, gf1)
        .add(R.id.fragment_guide, gf2)
        .add(R.id.fragment_guide, gf3)
        .hide(gf2)
        .hide(gf3)
        .commit();

    showFragment(gf1);
    page = 1;
    ExtendedFloatingActionButton fb = findViewById(R.id.floating_button);
    ExtendedFloatingActionButton fbb = findViewById(R.id.floating_back);

    fb.setOnClickListener(
        v -> {
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

    fbb.shrink();
    fbb.setVisibility(View.GONE);

    fbb.setOnClickListener(
        v -> {
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

  private void showFragment(Fragment aimfragment) {
    if (aimfragment == nowFragment) return;
    FragmentTransaction fragmentt = fragmentManager.beginTransaction();
    fragmentt.setCustomAnimations(
        R.anim.mtrl_bottom_sheet_slide_in, R.anim.mtrl_bottom_sheet_slide_out);
    if (nowFragment != null) {
      fragmentt.hide(nowFragment);
    }
    fragmentt.show(aimfragment);
    fragmentt.commit();

    nowFragment = aimfragment;
  }
}
