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
import com.cyq.awa.hfspro.activities.LastExamActivity;
import com.cyq.awa.hfspro.adapter.ExamListAdapter;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.cyq.awa.hfspro.tools.MyModel.*;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.cyq.awa.hfspro.tools.network.RetrofitTools.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
  private RetrofitTools.ApiService apiService;

  public HomeFragment() {}

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    MaterialCardView exam = view.findViewById(R.id.exam);
    MaterialCardView lastexam = view.findViewById(R.id.lastExam);

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
                  if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ExamListData> examlistResponse = response.body();
                    if (examlistResponse.isSuccess()) {
                      List<ExamListItem> listexamtiem = examlistResponse.getData().getList();
                      RecyclerView recyclerView = sheetView.findViewById(R.id.recyclerView);
                      recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

                      // 1. 将网络数据转换为 MyExamListItem
                      List<MyExamListItem> dataList = new ArrayList<>();
                      for (ExamListItem e : listexamtiem) {
                        dataList.add(new MyExamListItem(e));
                      }

                      // 2. 获取本地所有考试
                      List<MyExamListItem> localExams = DatabaseManager.getInstance().getAllExams();

                      // 3. 构建网络考试ID集合，用于去重
                      Set<Long> networkExamIds = new HashSet<>();
                      for (MyExamListItem item : dataList) {
                        networkExamIds.add(item.getExamId());
                      }

                      // 4. 将本地独有的考试添加到列表末尾
                      for (MyExamListItem localExam : localExams) {
                        if (!networkExamIds.contains(localExam.getExamId())) {
                          dataList.add(localExam);
                        }
                      }

                      // 5. 设置适配器
                      ExamListAdapter adapter = new ExamListAdapter(requireContext(), dataList);
                      dataList.sort((o1, o2) -> Long.compare(o2.getTime(), o1.getTime()));
                      adapter.notifyDataSetChanged();
                      recyclerView.setAdapter(adapter);

                      dialog.show();
                      BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                      // 设置为展开状态
                      behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                      // 可选：设置滑出高度为屏幕高度（全屏效果）
                      int screenHeight = getResources().getDisplayMetrics().heightPixels;
                      behavior.setPeekHeight(screenHeight); // 或根据

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
                    .setMessage("输入examId查看考试，超过120天的也能看。\n请求成功后，会自动记录到本地")
                    .setView(dialogView)
                    .setPositiveButton(
                        "确定",
                        (dialog1, which) -> {
                          String inputText = inputEditText.getText().toString().trim();
                          if (!inputText.isEmpty()) {
                            try {
                              Long id = Long.parseLong(inputText);
                              MyExamListItem item = new MyExamListItem(id, null, null);

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
          MaterialButton button2 = sheetView.findViewById(R.id.getallexam);
          button2.setOnClickListener(
              vvv -> {
                showLoading();
                Call<ApiResponse<List<moreExamlist>>> call2 =
                    RetrofitClient.getAuthService().getMoreExamList();
                ;
                call2.enqueue(
                    new Callback<ApiResponse<List<moreExamlist>>>() {
                      @Override
                      public void onResponse(
                          Call<ApiResponse<List<moreExamlist>>> call,
                          Response<ApiResponse<List<moreExamlist>>> response) {
                        if (response.isSuccessful()
                            && response.body() != null
                            && response.body().isSuccess()) {
                          List<moreExamlist> data = response.body().getData();
                          Set<Long> seenExamIds = new HashSet<>();
                          List<MyExamListItem> distinctExams = new ArrayList<>();

                          for (moreExamlist subject : data) {
                            for (moreExamItem exam : subject.getExamList()) {
                              if (!seenExamIds.contains(exam.getExamId())) {
                                seenExamIds.add(exam.getExamId());
                                distinctExams.add(
                                    new MyExamListItem(
                                        exam.getExamId(), exam.getExamName(), exam.getExamTime()));
                              }
                            }
                          }
                          // 现在 distinctExams 中包含去重后的考试信息
                          DatabaseManager.getInstance().insertOrUpdateExams(distinctExams);
                          hideLoading();
                          showDialog("加载全部考试成功","加载成功，共"+distinctExams.size()+"场考试");
                          dialog.dismiss();
                        }else{
                            String errorMsg = response.body().getMsg();
                      hideLoading();
                      showDialog(
                          "请求失败",
                          String.format(
                              "请求失败: %s\ncode: %d", errorMsg, response.body().getCode()));
                        }
                      }

                      @Override
                      public void onFailure(
                          Call<ApiResponse<List<moreExamlist>>> call, Throwable t) {
                        hideLoading();
                        showDialog("请求失败", "网络请求失败！");
                      }
                    });
              });
        });
    lastexam.setOnClickListener(
        v -> {
          Intent intent = new Intent(requireContext(), LastExamActivity.class);
          requireContext().startActivity(intent);
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
