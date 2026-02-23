package com.cyq.awa.hfspro.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.cyq.awa.hfspro.fragments.answer.AnswerSheetFragment;
import com.cyq.awa.hfspro.fragments.answer.PaperPicFragment;
import com.cyq.awa.hfspro.tools.network.GsonModel.AnswerPictureData;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private List<String> paperPicUrls;
    private AnswerPictureData answerData;
    private boolean hasPaperPic;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                            List<String> paperPicUrls,
                            AnswerPictureData answerData) {
        super(fragmentActivity);
        this.paperPicUrls = paperPicUrls;
        this.answerData = answerData;
        this.hasPaperPic = paperPicUrls != null && !paperPicUrls.isEmpty();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (hasPaperPic) {
            switch (position) {
                case 0:
                    return PaperPicFragment.newInstance(paperPicUrls);
                case 1:
                    return AnswerSheetFragment.newInstance(answerData);
                default:
                    throw new IllegalStateException("Invalid position: " + position);
            }
        } else {
            if (position == 0) {
                return AnswerSheetFragment.newInstance(answerData);
            } else {
                throw new IllegalStateException("Invalid position: " + position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return hasPaperPic ? 2 : 1;
    }
}