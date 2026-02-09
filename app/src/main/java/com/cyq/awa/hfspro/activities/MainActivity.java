package com.cyq.awa.hfspro.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.fragments.main.HomeFragment;
import com.cyq.awa.hfspro.fragments.main.MineFragment;
import com.cyq.awa.hfspro.tools.Databases;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
  private FragmentContainerView fragmentContainerView;
  private HomeFragment homefragment;
  private MineFragment mineFragment;
  private FragmentManager fragmentManager;
  private Fragment nowFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Databases db = Databases.getInstance(this);
    if (!db.hasToken()) {
      startActivity(new Intent(MainActivity.this, GuideActivity.class));
      finish();
    }
    fragmentContainerView = findViewById(R.id.fragment_container);
    BottomNavigationView bottomNavigationView = findViewById(R.id.main_nav_view);

    homefragment = new HomeFragment();
    mineFragment = new MineFragment();

    fragmentManager = getSupportFragmentManager();
    fragmentManager
        .beginTransaction()
        .add(R.id.fragment_container, homefragment)
        .add(R.id.fragment_container, mineFragment)
        .hide(mineFragment)
        .commit();

    showFragment(homefragment);

    bottomNavigationView.setOnItemSelectedListener(
        item -> {
          int id = item.getItemId();
          if (id == R.id.nav_home) {
            showFragment(homefragment);
            return true;
          } else if (id == R.id.nav_profilye) {
            showFragment(mineFragment);
            return true;
          }
          return true;
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
