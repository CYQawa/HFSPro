package com.cyq.awa.hfspro.fragments.guide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cyq.awa.hfspro.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;

public class GuideFrament3 extends Fragment {
  private TextInputEditText accountText, passwordText;
  private MaterialAutoCompleteTextView loginTypeText;

  public GuideFrament3() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // 加载布局文件
    return inflater.inflate(R.layout.guide_3, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    accountText = view.findViewById(R.id.account);
    passwordText = view.findViewById(R.id.password);
    loginTypeText = view.findViewById(R.id.loginType);
    MaterialTextView zhtext = view.findViewById(R.id.zhtext);
    
    setAutoComplete();
    
    zhtext.setOnClickListener(v ->{
        if ("学生端".equals(loginTypeText.getText().toString())){
            String url = "https://app.haofenshu.com/findPwd/?roleType=1";
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          startActivity(intent);
        } else{
            String url = "https://app.haofenshu.com/findPwd/?roleType=2";
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          startActivity(intent);
        }
    });
  }

  private void setAutoComplete() {
    List<String> logintypes = new ArrayList<>();
    logintypes.add("家长端");
    logintypes.add("学生端");

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_dropdown_item_1line, logintypes);
    loginTypeText.setAdapter(adapter);
    loginTypeText.setText("家长端", false);
  }
}
