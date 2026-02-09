package com.cyq.awa.hfspro.fragments.guide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.activities.MainActivity;
import com.cyq.awa.hfspro.tools.Databases;
import com.cyq.awa.hfspro.tools.GsonModel.LoginRequest;
import com.cyq.awa.hfspro.tools.GsonModel.LoginResponse;
import com.cyq.awa.hfspro.tools.RetrofitTools.ApiService;
import com.cyq.awa.hfspro.tools.RetrofitTools.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    MaterialTextView yytext = view.findViewById(R.id.yytext);
    MaterialButton loginButton = view.findViewById(R.id.loginButton);

    setAutoComplete();
    yytext.setOnClickListener(
        v -> {
          View dialogView =
              LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input, null);

          TextInputEditText inputEditText = dialogView.findViewById(R.id.token_text);

          new MaterialAlertDialogBuilder(requireContext())
              .setTitle("请输入有效的token")
              .setView(dialogView)
              .setPositiveButton(
                  "确定",
                  (dialog, which) -> {
                    String inputText = inputEditText.getText().toString().trim();
                    if (!inputText.isEmpty()) {
                      // 处理输入的内容
                      Databases db = Databases.getInstance(requireContext());
                      db.saveToken(inputText);
                      requireActivity()
                          .startActivity(new Intent(requireContext(), MainActivity.class));
                      requireActivity().finish();
                    }
                  })
              .setNegativeButton("取消", null)
              .show();
        });
    zhtext.setOnClickListener(
        v -> {
          int roleType = isStudent() ? 1 : 2;
          String url = "https://app.haofenshu.com/findPwd/?roleType=" + roleType;
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          startActivity(intent);
        });
    loginButton.setOnClickListener(
        v -> {
          String account = accountText.getText().toString().trim();
          String password = passwordText.getText().toString().trim();

          // 简单验证
          if (account.isEmpty()) {
            dialog("请输入账号", "请输入账号啊宝子");
            return;
          }

          if (password.isEmpty()) {
            dialog("请输入密码", "请输入密码啊宝子");
            return;
          }

          // 执行登录
          Login(account, password);
        });
  }

  private boolean isStudent() {
    return "学生端".equals(loginTypeText.getText().toString());
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

  private void Login(String account, String password) {

    ApiService apiService = RetrofitClient.getAuthService();

    int roleType = isStudent() ? 1 : 2;
    LoginRequest loginRequest = new LoginRequest(account, encodeToBase64(password), roleType);
    Call<LoginResponse> call = apiService.login(loginRequest);

    call.enqueue(
        new Callback<LoginResponse>() {
          @Override
          public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
              LoginResponse loginResponse = response.body();

              if (loginResponse.getCode() == 0) {
                // 登陆成功
                LoginResponse.LoginData data = loginResponse.getData();
                String token = data.getToken();
                String userId = data.getUserId();

                Databases db = Databases.getInstance(requireContext());
                db.saveToken(token);

                requireActivity()
                    .runOnUiThread(
                        () -> Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show());
                requireActivity().startActivity(new Intent(requireContext(), MainActivity.class));
                requireActivity().finish();
              } else {
                // 业务逻辑错误
                String errorMsg = loginResponse.getMessage();

                requireActivity()
                    .runOnUiThread(
                        () ->
                            dialog(
                                "登陆失败", "登录失败: " + errorMsg + "\ncode:" + loginResponse.getCode()));
              }
            } else {
              // HTTP错误（如404, 500等）

              requireActivity().runOnUiThread(() -> dialog("登陆失败", "服务器错误: " + response.code()));
            }
          }

          @Override
          public void onFailure(Call<LoginResponse> call, Throwable t) {
            requireActivity().runOnUiThread(() -> dialog("登陆失败", "网络请求失败！"));
          }
        });
  }

  public static String encodeToBase64(String text) {
    byte[] data = text.getBytes(StandardCharsets.UTF_8);
    return Base64.encodeToString(data, Base64.DEFAULT);
  }

  public void dialog(String title, String msg) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
    builder
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton("确定", null)
        .show();
  }
}
