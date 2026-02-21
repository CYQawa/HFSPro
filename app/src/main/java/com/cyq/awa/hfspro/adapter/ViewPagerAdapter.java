package com.cyq.awa.hfspro.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.cyq.awa.hfspro.fragments.answer.AnswerSheetFragment;
import com.cyq.awa.hfspro.fragments.answer.PaperPicFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 根据位置返回对应的 Fragment
        switch (position) {
            case 0:
                return new PaperPicFragment();
            case 1:
                return new AnswerSheetFragment();
            default:
                throw new IllegalStateException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 页面总数
    }
}