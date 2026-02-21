package com.cyq.awa.hfspro.fragments.answer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.ImageAdapter;

import java.util.ArrayList;
import java.util.List;

public class PaperPicFragment extends Fragment {

  private static final String ARG_IMAGE_URLS = "image_urls";
  private List<String> imageUrls = new ArrayList<>();

  // 静态工厂方法，用于从 Activity 传递数据
  public static PaperPicFragment newInstance(List<String> imageUrls) {
    PaperPicFragment fragment = new PaperPicFragment();
    Bundle args = new Bundle();
    args.putStringArrayList(ARG_IMAGE_URLS, new ArrayList<>(imageUrls));
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      imageUrls = getArguments().getStringArrayList(ARG_IMAGE_URLS);
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_paperpic, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity())); // 线性布局

    // 创建分割线对象（默认样式）
    DividerItemDecoration divider =
        new DividerItemDecoration(
            recyclerView.getContext(), DividerItemDecoration.VERTICAL // 垂直列表（水平列表用 HORIZONTAL）
            );

    recyclerView.addItemDecoration(divider); // 添加分割线

    ImageAdapter adapter = new ImageAdapter(imageUrls);
    recyclerView.setAdapter(adapter);
  }
}
