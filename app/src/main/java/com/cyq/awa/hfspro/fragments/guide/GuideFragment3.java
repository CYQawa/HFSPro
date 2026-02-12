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
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.cyq.awa.hfspro.tools.network.RetrofitTools.ApiService;
import com.cyq.awa.hfspro.tools.network.RetrofitTools.RetrofitClient;
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

public class GuideFragment3 extends Fragment {
  private TextInputEditText accountText, passwordText;
  private MaterialAutoCompleteTextView loginTypeText;

  public GuideFragment3() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    // 手动输入Token
    yytext.setOnClickListener(v -> showTokenInputDialog());

    // 忘记密码
    zhtext.setOnClickListener(
        v -> {
          int roleType = isStudent() ? 1 : 2;
          String url = "https://app.haofenshu.com/findPwd/?roleType=" + roleType;
          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
          startActivity(intent);
        });

    // 登录按钮
    loginButton.setOnClickListener(
        v -> {
          String account = accountText.getText().toString().trim();
          String password = passwordText.getText().toString().trim();

          // 简单验证
          if (account.isEmpty()) {
            showDialog("请输入账号", "请输入账号啊宝子");
            return;
          }

          if (password.isEmpty()) {
            showDialog("请输入密码", "请输入密码啊宝子");
            return;
          }

          // 执行登录
          performLogin(account, password);
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

  private void performLogin(String account, String password) {
    ApiService apiService = RetrofitClient.getAuthService();

    int roleType = isStudent() ? 1 : 2;
    LoginRequest loginRequest = new LoginRequest(account, encodeToBase64(password), roleType);
    Call<ApiResponse<LoginData>> call = apiService.login(loginRequest);

    call.enqueue(
        new Callback<ApiResponse<LoginData>>() {
          @Override
          public void onResponse(
              Call<ApiResponse<LoginData>> call, Response<ApiResponse<LoginData>> response) {
            if (response.isSuccessful() && response.body() != null) {
              ApiResponse<LoginData> loginResponse = response.body();

              if (loginResponse.isSuccess()) {
                // 登陆成功
                String token = loginResponse.getData().getToken();
                
                DatabaseManager dbm = DatabaseManager.getInstance();
                dbm.saveToken(token);

                // 显示成功消息并跳转
                showToast("登录成功");
                navigateToMainActivity();
              } else {
                // 业务逻辑错误
                String errorMsg = loginResponse.getMsg();
                showDialog(
                    "登陆失败", String.format("登录失败: %s\ncode: %d", errorMsg, loginResponse.getCode()));
              }
            } else {
              // HTTP错误（如404, 500等）
              showDialog("登陆失败", "服务器错误: " + response.code());
            }
          }

          @Override
          public void onFailure(Call<ApiResponse<LoginData>> call, Throwable t) {
            showDialog("登陆失败", "网络请求失败！");
          }
        });
  }

  private void showTokenInputDialog() {
    View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input, null);
    TextInputEditText inputEditText = dialogView.findViewById(R.id.token_text);

    new MaterialAlertDialogBuilder(requireContext())
        .setTitle("请输入有效的token")
        .setView(dialogView)
        .setPositiveButton(
            "确定",
            (dialog, which) -> {
              String inputText = inputEditText.getText().toString().trim();
              if (!inputText.isEmpty()) {
                DatabaseManager dbm = DatabaseManager.getInstance();
                dbm.saveToken(inputText);
                navigateToMainActivity();
              }
            })
        .setNegativeButton("取消", null)
        .show();
  }

  private void navigateToMainActivity() {
    requireActivity().startActivity(new Intent(requireContext(), MainActivity.class));
    requireActivity().finish();
  }

  private void showToast(String message) {
    requireActivity()
        .runOnUiThread(() -> Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
  }

  private void showDialog(String title, String message) {
    requireActivity()
        .runOnUiThread(
            () -> {
              MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
              builder.setTitle(title).setMessage(message).setPositiveButton("确定", null).show();
            });
  }

  public static String encodeToBase64(String text) {
    byte[] data = text.getBytes(StandardCharsets.UTF_8);
    return Base64.encodeToString(data, Base64.DEFAULT);
  }
}
