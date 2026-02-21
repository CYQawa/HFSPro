package com.cyq.awa.hfspro.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.cyq.awa.hfspro.fragments.answer.AnswerSheetFragment;
import com.cyq.awa.hfspro.fragments.answer.PaperPicFragment;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private List<String> imageUrls; 

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<String> imageUrls) {
        super(fragmentActivity);
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // 传递图片 URL 给 PaperPicFragment
                return PaperPicFragment.newInstance(imageUrls);
            case 1:
                return new AnswerSheetFragment();
            default:
                throw new IllegalStateException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}