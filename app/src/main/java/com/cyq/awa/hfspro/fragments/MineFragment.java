package com.cyq.awa.hfspro.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.cyq.awa.hfspro.R;

public class MineFragment extends Fragment {  // ✅ 正确：继承 Fragment
    
    public MineFragment() {
        // 必需的空构造函数
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 加载布局文件
        return inflater.inflate(R.layout.fragment_mine, container, false);
    }
}
