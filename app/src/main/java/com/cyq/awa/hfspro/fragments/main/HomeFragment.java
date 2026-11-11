package com.cyq.awa.hfspro.fragments.main;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
import com.cyq.awa.hfspro.tools.DialogHelp;
import com.cyq.awa.hfspro.tools.MyDatabases.DatabaseManager;
import com.cyq.awa.hfspro.tools.MyModel.*;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import com.cyq.awa.hfspro.tools.network.NetworkErrors;
import com.cyq.awa.hfspro.tools.network.RetrofitTools;
import com.cyq.awa.hfspro.tools.network.RetrofitTools.*;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
  private RetrofitTools.ApiService apiService;
  private boolean updateAvailable = false;
  private String currentVersion = "";
  private String latestVersion = "";
  private Runnable onUpdateClickListener;

  public HomeFragment() {}

  /** 由 MainActivity 调用，通知是否有可用更新、版本信息及点击事件 */
  public void setUpdateAvailable(boolean available, String curVer, String latVer, Runnable onClick) {
    updateAvailable = available;
    currentVersion = curVer;
    latestVersion = latVer;
    onUpdateClickListener = onClick;
    updateCardUi();
  }

  private void updateCardUi() {
    if (getView() == null) return;
    MaterialCardView card = getView().findViewById(R.id.update_card);
    if (card == null) return;
    card.setVisibility(updateAvailable ? View.VISIBLE : View.GONE);
    if (updateAvailable) {
      TextView verText = getView().findViewById(R.id.update_version_text);
      if (verText != null) {
        verText.setText(currentVersion + " → " + latestVersion);
      }
      card.setOnClickListener(v -> {
        if (onUpdateClickListener != null) onUpdateClickListener.run();
      });
    } else {
      card.setOnClickListener(null);
    }
  }

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

    // 卡片容器添加 LayoutTransition，update_card 显隐时有动画
    LinearLayout cardContainer = view.findViewById(R.id.card_container);
    LayoutTransition transition = new LayoutTransition();
    transition.setDuration(400);
    cardContainer.setLayoutTransition(transition);

    // 根据更新状态控制卡片显隐和点击
    updateCardUi();

    apiService = RetrofitTools.RetrofitClient.getAuthService();
    exam.setOnClickListener(
        v -> {
          DialogHelp.show();

          // 把数据库查询丢到了后台
          new Thread(
                  () -> {
                    List<MyExamListItem> localExams = DatabaseManager.getInstance().getAllExams();

                    new Handler(Looper.getMainLooper())
                        .post(
                            () -> {
                              if (!isAdded()) {
                                DialogHelp.dismiss();
                                return;
                              }
                              if (localExams.isEmpty()) {
                                // 本地没有记录时，先用「全部考试」接口补齐，再展示弹窗；
                                // 补齐失败也会照常展示弹窗（空态 + 重试），不再直接报错退出。
                                loadAllExamsSilent(this::showExamListDialog);
                              } else {
                                showExamListDialog();
                              }
                            });
                  })
              .start();
        });
    lastexam.setOnClickListener(
        v -> {
          Intent intent = new Intent(requireContext(), LastExamActivity.class);
          requireContext().startActivity(intent);
        });
  }

  /**
   * 通过「全部考试」接口获取考试并写入本地库。 无论成功与否都会执行 onComplete，调用方据此决定如何展示。
   */
  private void loadAllExamsSilent(Runnable onComplete) {
    Call<ApiResponse<List<moreExamlist>>> call = apiService.getMoreExamList();
    call.enqueue(
        new Callback<ApiResponse<List<moreExamlist>>>() {
          @Override
          public void onResponse(
              Call<ApiResponse<List<moreExamlist>>> call,
              Response<ApiResponse<List<moreExamlist>>> response) {
            boolean ok = false;
            int count = 0;
            try {
              ApiResponse<List<moreExamlist>> body = response.body();
              if (response.isSuccessful()
                  && body != null
                  && body.isSuccess()
                  && body.getData() != null) {
                Set<Long> seenExamIds = new HashSet<>();
                List<MyExamListItem> distinctExams = new ArrayList<>();
                for (moreExamlist subject : body.getData()) {
                  if (subject == null || subject.getExamList() == null) continue;
                  for (moreExamItem exam : subject.getExamList()) {
                    if (exam == null) continue;
                    long examId;
                    try {
                      examId = exam.getExamId();
                    } catch (NumberFormatException e) {
                      // examId 是非数字字符串，跳过这一条，避免整批失败
                      continue;
                    }
                    if (seenExamIds.contains(examId)) continue;
                    seenExamIds.add(examId);
                    distinctExams.add(
                        new MyExamListItem(examId, exam.getExamName(), exam.getExamTime()));
                  }
                }
                DatabaseManager.getInstance().insertOrUpdateExams(distinctExams);
                count = distinctExams.size();
                ok = true;
              }
            } catch (Exception e) {
              Log.e("HomeFragment", "loadAllExamsSilent failed", e);
            }

            if (isAdded()) {
              Toast.makeText(
                      requireContext(),
                      ok ? "已获取全部考试，共" + count + "场" : "获取全部考试失败",
                      Toast.LENGTH_SHORT)
                  .show();
            }
            if (onComplete != null) onComplete.run();
          }

          @Override
          public void onFailure(Call<ApiResponse<List<moreExamlist>>> call, Throwable t) {
            Log.e("HomeFragment", "获取全部考试失败", t);
            if (isAdded()) {
              Toast.makeText(
                      requireContext(),
                      "获取全部考试失败：" + NetworkErrors.describe(t),
                      Toast.LENGTH_SHORT)
                  .show();
            }
            if (onComplete != null) onComplete.run();
          }
        });
  }

  /**
   * 展示考试列表底部弹窗。
   *
   * <p>关键点：弹窗的显示不再依赖 /v3/exam/list 是否成功——先用本地缓存立即渲染并 show，
   * 在线列表只作为「增量刷新」，失败时静默降级，避免接口返回 code=1（暂无数据）时整个弹窗都不出现。
   */
  private void showExamListDialog() {
    if (!isAdded() || getContext() == null) {
      DialogHelp.dismiss();
      return;
    }
    BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
    View sheetView =
        LayoutInflater.from(requireContext()).inflate(R.layout.exam_list_bottom_sheet_dialog, null);
    dialog.setContentView(sheetView);

    View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
    if (bottomSheet != null) {
      bottomSheet.setBackgroundResource(R.drawable.bg_bottom_sheet_rounded_top);
    }

    RecyclerView recyclerView = sheetView.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

    View emptyContainer = sheetView.findViewById(R.id.empty_container);
    TextView emptyText = sheetView.findViewById(R.id.empty_text);
    View emptyRetry = sheetView.findViewById(R.id.empty_retry);

    final List<MyExamListItem> dataList = new ArrayList<>();
    final ExamListAdapter adapter = new ExamListAdapter(requireContext(), dataList);
    recyclerView.setAdapter(adapter);

    // 1. 先用本地数据立即渲染，保证弹窗一定能出现
    List<MyExamListItem> localExams = DatabaseManager.getInstance().getAllExams();
    renderExams(dataList, adapter, emptyContainer, mergeExams(localExams, null));

    ExtendedFloatingActionButton edit = sheetView.findViewById(R.id.edit_fab);
    ExtendedFloatingActionButton load = sheetView.findViewById(R.id.load_fab);
    hide(load, 1);
    hide(edit, 1);

    edit.setOnClickListener(vv -> showExamIdInputDialog());

    load.setOnClickListener(
        vvv ->
            loadAllExamsSilent(
                () -> {
                  if (!isAdded()) return;
                  renderExams(
                      dataList,
                      adapter,
                      emptyContainer,
                      mergeExams(DatabaseManager.getInstance().getAllExams(), null));
                }));

    if (emptyRetry != null) {
      emptyRetry.setOnClickListener(
          v -> {
            if (emptyText != null) emptyText.setText("正在重新获取考试列表…");
            if (emptyContainer != null) emptyContainer.setVisibility(View.VISIBLE);
            loadAllExamsSilent(
                () -> {
                  if (!isAdded()) return;
                  renderExams(
                      dataList,
                      adapter,
                      emptyContainer,
                      mergeExams(DatabaseManager.getInstance().getAllExams(), null));
                  fetchOnlineExams(dataList, adapter, emptyContainer, emptyText);
                });
          });
    }

    final int[] type = {1}; // 使用数组包装，数组引用是final，但内容可修改
    FloatingActionButton fab = sheetView.findViewById(R.id.floating_button);

    fab.setOnClickListener(
        v -> {
          if (type[0] == 1) {
            // 展开操作
            type[0] = 2;
            fab.setImageResource(R.drawable.ic_down);
            show(load, 400);
            show(edit, 300);
          } else {
            // 收缩操作
            type[0] = 1;
            hide(load, 300);
            hide(edit, 400);
            fab.setImageResource(R.drawable.ic_up);
          }
        });

    // 2. 先显示弹窗，再拉在线列表
    dialog.show();
    if (bottomSheet != null) {
      BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    DialogHelp.dismiss();

    // 3. 在线列表只做增量刷新，失败静默降级
    fetchOnlineExams(dataList, adapter, emptyContainer, emptyText);
  }

  /** 请求 /v3/exam/list：成功则与本地数据合并刷新；失败/无数据只降级提示，绝不阻断弹窗。 */
  private void fetchOnlineExams(
      List<MyExamListItem> dataList,
      ExamListAdapter adapter,
      View emptyContainer,
      TextView emptyText) {
    apiService
        .getExamList()
        .enqueue(
            new Callback<ApiResponse<ExamListData>>() {
              @Override
              public void onResponse(
                  Call<ApiResponse<ExamListData>> call,
                  Response<ApiResponse<ExamListData>> response) {
                if (!isAdded()) return;
                ApiResponse<ExamListData> body = response.body();
                List<ExamListItem> onlineList = null;
                if (response.isSuccessful()
                    && body != null
                    && body.isSuccess()
                    && body.getData() != null) {
                  onlineList = body.getData().getList();
                }
                if (onlineList != null && !onlineList.isEmpty()) {
                  renderExams(
                      dataList,
                      adapter,
                      emptyContainer,
                      mergeExams(DatabaseManager.getInstance().getAllExams(), onlineList));
                } else {
                  degradeToLocal(dataList, emptyContainer, emptyText);
                }
              }

              @Override
              public void onFailure(Call<ApiResponse<ExamListData>> call, Throwable t) {
                Log.e("HomeFragment", "获取在线考试列表失败", t);
                if (!isAdded()) return;
                degradeToLocal(dataList, emptyContainer, emptyText);
              }
            });
  }

  /** 在线接口不可用时的降级：有本地数据就提示一句，没有就显示空态让用户重试。 */
  private void degradeToLocal(
      List<MyExamListItem> dataList, View emptyContainer, TextView emptyText) {
    if (!isAdded()) return;
    if (dataList.isEmpty()) {
      if (emptyContainer != null) emptyContainer.setVisibility(View.VISIBLE);
      if (emptyText != null) {
        emptyText.setText("暂无考试数据\n在线列表接口不可用，可点「重试」重新获取");
      }
    } else {
      Toast.makeText(requireContext(), "在线列表接口无数据，已展示本地考试", Toast.LENGTH_SHORT)
          .show();
    }
  }

  /**
   * 合并本地记录与在线列表：按 examId 去重，在线项覆盖本地项，最后按考试时间倒序。
   *
   * <p>这样即使 /v3/exam/list 返回 code=1，已经由「全部考试」接口写入本地的记录也能正常展示。
   */
  private List<MyExamListItem> mergeExams(
      List<MyExamListItem> local, List<ExamListItem> online) {
    Map<Long, MyExamListItem> merged = new LinkedHashMap<>();
    if (local != null) {
      for (MyExamListItem item : local) {
        if (item == null) continue;
        merged.put(item.getExamId(), normalizeExam(item));
      }
    }
    if (online != null) {
      for (ExamListItem item : online) {
        if (item == null) continue;
        MyExamListItem examItem = new MyExamListItem(item);
        if (examItem.getName() == null || examItem.getName().trim().isEmpty()) {
          examItem =
              new MyExamListItem(
                  examItem.getExamId(), "考试 " + examItem.getExamId(), examItem.getTime());
          examItem.setIs_network(true);
        }
        merged.put(examItem.getExamId(), examItem);
      }
    }
    List<MyExamListItem> result = new ArrayList<>(merged.values());
    result.sort((a, b) -> Long.compare(b.getTime(), a.getTime()));
    return result;
  }

  private MyExamListItem normalizeExam(MyExamListItem item) {
    String name = item.getName();
    if (name != null && !name.trim().isEmpty()) return item;
    return new MyExamListItem(item.getExamId(), "考试 " + item.getExamId(), item.getTime());
  }

  private void renderExams(
      List<MyExamListItem> dataList,
      ExamListAdapter adapter,
      View emptyContainer,
      List<MyExamListItem> exams) {
    dataList.clear();
    dataList.addAll(exams);
    adapter.notifyDataSetChanged();
    if (emptyContainer != null) {
      emptyContainer.setVisibility(dataList.isEmpty() ? View.VISIBLE : View.GONE);
    }
  }

  private void showExamIdInputDialog() {
    View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_input, null);
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
  }

  private void showDialog(String title, String message) {
    requireActivity()
        .runOnUiThread(
            () -> {
              MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
              builder.setTitle(title).setMessage(message).setPositiveButton("确定", null).show();
            });
  }

  public void hide(final View view, int duration) {
    // 创建从 1.0 缩放到 0.0 的动画，轴点为中心 (0.5, 0.5)
    ScaleAnimation animation =
        new ScaleAnimation(
            1.0f,
            0.0f, // fromX, toX
            1.0f,
            0.0f, // fromY, toY
            Animation.RELATIVE_TO_SELF,
            0.5f, // pivotX 类型及值
            Animation.RELATIVE_TO_SELF,
            0.5f); // pivotY 类型及值
    animation.setDuration(duration);

    // 开始动画
    view.startAnimation(animation);
    view.setVisibility(View.INVISIBLE);
  }

  public void show(View view, int duration) {

    view.setVisibility(View.VISIBLE);

    ScaleAnimation animation =
        new ScaleAnimation(
            0.0f,
            1.0f,
            0.0f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f);
    animation.setDuration(duration);
    view.startAnimation(animation);
  }
}
