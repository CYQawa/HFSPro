package com.cyq.awa.hfspro.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.cyq.awa.hfspro.tools.network.RetrofitTools.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
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
          BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

          View sheetView =
              LayoutInflater.from(requireContext())
                  .inflate(R.layout.exam_list_bottom_sheet_dialog, null);
          dialog.setContentView(sheetView);
          
//          Call<ApiResponse<ExamHomeData>> call = apiService.getExamHomePage();
//          call.enqueue(
//              new Callback<ApiResponse<ExamHomeData>>() {
//                @Override
//                public void onResponse(
//                    Call<ApiResponse<ExamHomeData>> call,
//                    Response<ApiResponse<ExamHomeData>> response) {
//                  if (response.isSuccessful() && response.body() != null) {
//                    ApiResponse<ExamHomeData> loginResponse = response.body();
//
//                    if (loginResponse.isSuccess()) {
//                      // 请求成功
//
//                    } else {
//                      // 业务逻辑错误
//                    }
//                  } else {
//                    // HTTP错误（如404, 500等）
//
//                  }
//                }
//
//                @Override
//                public void onFailure(Call<ApiResponse<ExamHomeData>> call, Throwable t) {}
//              });
//              
              
          dialog.show();
        });
  }
}
