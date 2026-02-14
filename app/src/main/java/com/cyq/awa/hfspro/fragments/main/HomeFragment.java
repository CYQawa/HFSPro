package com.cyq.awa.hfspro.fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.activities.ExamActivity;
import com.cyq.awa.hfspro.adapter.ExamListAdapter;
import com.cyq.awa.hfspro.tools.MyModel.*;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.cyq.awa.hfspro.tools.network.RetrofitTools.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
  private RetrofitTools.ApiService apiService;

  public HomeFragment() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // 加载布局文件
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    MaterialCardView exam = view.findViewById(R.id.exam);

    apiService = RetrofitTools.RetrofitClient.getAuthService();
    exam.setOnClickListener(
        v -> {
          showLoading();
          BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
          View sheetView =
              LayoutInflater.from(requireContext())
                  .inflate(R.layout.exam_list_bottom_sheet_dialog, null);
          dialog.setContentView(sheetView);

          View bottomSheet =
              dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
          if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(R.drawable.bg_bottom_sheet_rounded_top);
          }
          Call<ApiResponse<ExamListData>> call = apiService.getExamList();

          call.enqueue(
              new Callback<ApiResponse<ExamListData>>() {
                @Override
                public void onResponse(
                    Call<ApiResponse<ExamListData>> call,
                    Response<ApiResponse<ExamListData>> response) {
                  ApiResponse<ExamListData> examlistResponse = response.body();
                  if (response.isSuccessful() && response.body() != null) {
                    ExamListData data = response.body().getData();
                    if (examlistResponse.isSuccess()) {
                      List<ExamListItem> listexamtiem = examlistResponse.getData().getList();
                      RecyclerView recyclerView = sheetView.findViewById(R.id.recyclerView);
                      recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

                      List<MyExamList> dataList = new ArrayList<>();

                      for (int i = 0; i < listexamtiem.size(); i++) {
                        ExamListItem e = listexamtiem.get(i);
                        dataList.add(new MyExamList(e));
                      }
                      ExamListAdapter adapter = new ExamListAdapter(requireContext(), dataList);
                      recyclerView.setAdapter(adapter);

                      dialog.show();
                      hideLoading();
                    } else {
                      String errorMsg = examlistResponse.getMsg();
                      hideLoading();
                      showDialog(
                          "请求失败",
                          String.format(
                              "请求失败: %s\ncode: %d", errorMsg, examlistResponse.getCode()));
                    }
                  } else {
                    showDialog("请求失败", "服务器错误: " + response.code());
                    hideLoading();
                  }
                }

                @Override
                public void onFailure(Call<ApiResponse<ExamListData>> call, Throwable t) {
                  hideLoading();
                  showDialog("请求失败", "网络请求失败！");
                }
              });

          MaterialButton button = sheetView.findViewById(R.id.enter_id);
          button.setOnClickListener(
              vv -> {
                View dialogView =
                    LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input, null);
                TextInputEditText inputEditText = dialogView.findViewById(R.id.token_text);

                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("请输入examid")
                    .setMessage("超过120天的考试也能看哦，只要你记得examid的话~~")
                    .setView(dialogView)
                    .setPositiveButton(
                        "确定",
                        (dialog1, which) -> {
                          String inputText = inputEditText.getText().toString().trim();
                          if (!inputText.isEmpty()) {
                            try {
                              Long id = Long.parseLong(inputText);
                              MyExamList item = new MyExamList(id, null, null);

                              Intent intent = new Intent(requireContext(), ExamActivity.class);
                              intent.putExtra("myexam", item);
                              requireContext().startActivity(intent);
                            } catch (NumberFormatException e) {
                              new MaterialAlertDialogBuilder(requireContext())
                                  .setTitle("提示")
                                  .setMessage("请输入有效的数字ID哦~")
                                  .setPositiveButton("确定", null)
                                  .show();
                            }
                          }
                        })
                    .setNegativeButton("取消", null)
                    .show();
              });
        });
  }

  private AlertDialog createLoadingDialog() {
    // 创建ProgressBar
    // 创建对话框
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
    builder.setTitle("请稍候");
    builder.setMessage("正在加载中...");
    builder.setCancelable(false); // 禁止点击外部取消

    return builder.create();
  }

  // 使用示例
  private AlertDialog loadingDialog;

  public void showLoading() {
    if (loadingDialog == null) {
      loadingDialog = createLoadingDialog();
    }
    loadingDialog.show();
  }

  public void hideLoading() {
    if (loadingDialog != null && loadingDialog.isShowing()) {
      loadingDialog.dismiss();
    }
  }

  private void showDialog(String title, String message) {
    requireActivity()
        .runOnUiThread(
            () -> {
              MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
              builder.setTitle(title).setMessage(message).setPositiveButton("确定", null).show();
            });
  }
}
