package com.cyq.awa.hfspro.fragments.answer;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cyq.awa.hfspro.R;

public class PaperPicFragment extends Fragment{
  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_paperpic, container, false);
    }
}
