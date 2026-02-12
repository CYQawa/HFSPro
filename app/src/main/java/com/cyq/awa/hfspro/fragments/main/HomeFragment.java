package com.cyq.awa.hfspro.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.widget.ProgressBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.ExamListAdapter;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.cyq.awa.hfspro.tools.network.RetrofitTools.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import java.util.ArrayList;
import com.cyq.awa.hfspro.tools.MyModel.MyExam;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
  private RetrofitTools.ApiService apiService;

  public HomeFragment() {
    // 必需的空构造函数
  }

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

          Call<ApiResponse<ExamHomeData>> call = apiService.getExamHomePage();
          call.enqueue(
              new Callback<ApiResponse<ExamHomeData>>() {
                @Override
                public void onResponse(
                    Call<ApiResponse<ExamHomeData>> call,
                    Response<ApiResponse<ExamHomeData>> response) {
                  if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ExamHomeData> examResponse = response.body();

                    if (examResponse.isSuccess()) {
                      List<ExamItem> listexamtiem = examResponse.getData().getList();
                      RecyclerView recyclerView = sheetView.findViewById(R.id.recyclerView);
                      recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

                      // 模拟数据
                      List<MyExam> dataList = new ArrayList<>();

                      // 添加元素...

                      for (int i = 0; i < listexamtiem.size(); i++) {
                        ExamItem e = listexamtiem.get(i);
                        dataList.add(new MyExam(e));
                      }
                      ExamListAdapter adapter = new ExamListAdapter(dataList);
                      recyclerView.setAdapter(adapter);

                      dialog.show();
                      hideLoading();

                    } else {
                      String errorMsg = examResponse.getMsg();
                      hideLoading();
                      showDialog(
                          "请求失败",
                          String.format("请求失败: %s\ncode: %d", errorMsg, examResponse.getCode()));
                    }
                  } else {
                    // HTTP错误（如404, 500等）
                    showDialog("请求失败", "服务器错误: " + response.code());
                    hideLoading();
                  }
                }

                @Override
                public void onFailure(Call<ApiResponse<ExamHomeData>> call, Throwable t) {
                    hideLoading();
                  showDialog("请求失败", "网络请求失败！");
                }
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
