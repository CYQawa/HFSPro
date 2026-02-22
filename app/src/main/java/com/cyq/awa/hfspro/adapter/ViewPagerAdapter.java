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
  private List<String> ans;
  private boolean hasPaperPic;

  public ViewPagerAdapter(
      @NonNull FragmentActivity fragmentActivity, List<String> imageUrls, List<String> ans) {
    super(fragmentActivity);
    this.imageUrls = imageUrls;
    this.ans = ans;
    this.hasPaperPic = imageUrls != null && !imageUrls.isEmpty();
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    if (hasPaperPic) {
      // 有原卷图片时：第0页为原卷，第1页为答题卡
      switch (position) {
        case 0:
          return PaperPicFragment.newInstance(imageUrls);
        case 1:
          return AnswerSheetFragment.newInstance(ans);
        default:
          throw new IllegalStateException("Invalid position: " + position);
      }
    } else {
      // 无原卷图片时：只有第0页，且为答题卡
      if (position == 0) {
        return  AnswerSheetFragment.newInstance(ans);
      } else {
        throw new IllegalStateException("Invalid position: " + position);
      }
    }
  }

  @Override
  public int getItemCount() {
    return hasPaperPic ? 2 : 1; // 动态返回页面数
  }
}
