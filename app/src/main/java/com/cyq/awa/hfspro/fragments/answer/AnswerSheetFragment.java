package com.cyq.awa.hfspro.fragments.answer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cyq.awa.hfspro.R;
import com.cyq.awa.hfspro.adapter.ImageAdapter;
import com.cyq.awa.hfspro.tools.MyModel.MarkInfo;
import com.cyq.awa.hfspro.tools.network.GsonModel.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnswerSheetFragment extends Fragment {
  private static final String ARG_DATA = "answer_data";
  private AnswerPictureData data;
  private List<List<MarkInfo>> marksPerSheet = new ArrayList<>();
  private static final String TAG = "AnswerSheetFragment";

  public static AnswerSheetFragment newInstance(AnswerPictureData data) {
    AnswerSheetFragment fragment = new AnswerSheetFragment();
    Bundle args = new Bundle();
    args.putSerializable(ARG_DATA, data);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      data = (AnswerPictureData) getArguments().getSerializable(ARG_DATA);
      buildMarksPerSheet();
    }
  }

  private String extractBaseUrl(String fullUrl) {
    int queryIdx = fullUrl.indexOf('?');
    String noQuery = queryIdx > 0 ? fullUrl.substring(0, queryIdx) : fullUrl;
    int atIdx = noQuery.indexOf("%40");
    if (atIdx > 0) {
      return noQuery.substring(0, atIdx);
    }
    return noQuery;
  }

  private float[] parseCoordinates(String fullUrl) {
    try {
      int atIdx = fullUrl.indexOf("%40");
      if (atIdx < 0) return null;
      int queryIdx = fullUrl.indexOf('?');
      String cropPart =
          (queryIdx > atIdx) ? fullUrl.substring(atIdx, queryIdx) : fullUrl.substring(atIdx);
      cropPart = URLDecoder.decode(cropPart, "UTF-8");
      // "@c_1,x_140,y_1158,w_966,h_179|f_auto"
      Pattern pattern = Pattern.compile("x_(\\d+),y_(\\d+),w_(\\d+),h_(\\d+)");
      Matcher matcher = pattern.matcher(cropPart);
      if (matcher.find()) {
        return new float[] {
          Float.parseFloat(matcher.group(1)),
          Float.parseFloat(matcher.group(2)),
          Float.parseFloat(matcher.group(3)),
          Float.parseFloat(matcher.group(4))
        };
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** 根据基础 URL 查找对应的答题卡图片索引 */
  private int findSheetIndex(String baseUrl, List<String> sheetUrls) {
    for (int i = 0; i < sheetUrls.size(); i++) {
      String sheetUrl = sheetUrls.get(i);
      String sheetBase =
          sheetUrl.contains("?") ? sheetUrl.substring(0, sheetUrl.indexOf('?')) : sheetUrl;
      if (sheetBase.equals(baseUrl)) {
        return i;
      }
    }
    return -1;
  }

  private void buildMarksPerSheet() {
    if (data == null) return;
    List<String> urls = data.getUrl(); // 答题卡原图列表
    if (urls == null || urls.isEmpty()) {
      return;
    }
    int sheetCount = urls.size();
    for (int i = 0; i < sheetCount; i++) {
      marksPerSheet.add(new ArrayList<>());
    }

    // 用于收集每个答题卡上每个坐标区域对应的主观题列表
    List<Map<String, List<QuestionItem>>> regionQuestionsPerSheet = new ArrayList<>(sheetCount);
    for (int i = 0; i < sheetCount; i++) {
      regionQuestionsPerSheet.add(new HashMap<>());
    }

    List<QuestionItem> questions = data.getQuestions();
    if (questions == null) return;

    // 这里计算总体得分
    double totalScore = data.getScore();
    double subjectiveScore = 0;
    double objectiveScore = 0;
    for (QuestionItem q : questions) {
      if (q.getType() == 1) {
        subjectiveScore += q.getScore();
      } else if (q.getType() == 2) {
        objectiveScore += q.getScore();
      }
    }
    // 这里为第一张答题卡添加总体情况文本
    if (!marksPerSheet.isEmpty()) {
      List<MarkInfo> firstSheetMarks = marksPerSheet.get(0);
      float x = 20;
      float y = 100;
      float lineHeight = 80;
      String totalText = "总分:" + String.format("%.1f", totalScore);
      String subjText = "主观:" + String.format("%.1f", subjectiveScore);
      String objText = "客观:" + String.format("%.1f", objectiveScore);
      firstSheetMarks.add(new MarkInfo(x, y, 0, 0, 0, null, 2, totalText));
      firstSheetMarks.add(new MarkInfo(x, y + lineHeight, 0, 0, 0, null, 2, subjText));
      firstSheetMarks.add(new MarkInfo(x, y + 2 * lineHeight, 0, 0, 0, null, 2, objText));
    }

    // 收集客观题信息
    for (QuestionItem q : questions) {
      int type = q.getType();
      if (type == 2) { // 客观题—— 直接添加标记
        List<AnswerOptionItem> options = q.getAnswerOption();
        if (options != null) {
          for (AnswerOptionItem opt : options) {
            int sheetIndex = opt.getIndex();
            if (sheetIndex >= 0 && sheetIndex < sheetCount) {
              MarkInfo mark =
                  new MarkInfo(
                      Float.parseFloat(opt.getX()),
                      Float.parseFloat(opt.getY()),
                      Float.parseFloat(opt.getW()),
                      Float.parseFloat(opt.getH()),
                      opt.getRight(),
                      opt.getOption());
              marksPerSheet.get(sheetIndex).add(mark);
            }
          }
        }
      } else if (type == 1) { // 主观题 
        List<String> qUrls = q.getUrl();
        if (qUrls == null || qUrls.isEmpty()) continue;

        for (String qUrl : qUrls) {
          String baseUrl = extractBaseUrl(qUrl);
          int sheetIndex = findSheetIndex(baseUrl, urls);
          if (sheetIndex < 0) continue;

          float[] coords = parseCoordinates(qUrl);
          if (coords == null) continue;
          float x = coords[0];
          float y = coords[1];
          float w = coords[2];
          float h = coords[3];

          // 生成该区域的唯一标识（基于坐标，保留两位小数防止精度问题）
          String key = String.format(Locale.US, "%.2f_%.2f_%.2f_%.2f", x, y, w, h);
          Map<String, List<QuestionItem>> sheetMap = regionQuestionsPerSheet.get(sheetIndex);
          List<QuestionItem> list = sheetMap.get(key);
          if (list == null) {
            list = new ArrayList<>();
            sheetMap.put(key, list);
          }
          list.add(q); // 将当前题目加入列表（同一个区域可能有多题）
        }
      }
    }

    // 为每个区域添加框和合并后的得分文本
    for (int sheetIdx = 0; sheetIdx < sheetCount; sheetIdx++) {
      Map<String, List<QuestionItem>> sheetMap = regionQuestionsPerSheet.get(sheetIdx);
      for (Map.Entry<String, List<QuestionItem>> entry : sheetMap.entrySet()) {
        List<QuestionItem> qList = entry.getValue();
        if (qList.isEmpty()) continue;

        // 取第一个题的坐标作为该区域的位置（所有题坐标相同）
        QuestionItem firstQ = qList.get(0);
        float x = 0, y = 0, w = 0, h = 0;
        // 需要重新解析坐标，因为 entry 的 key 只是字符串，最好从第一个题的 url 重新解析
        List<String> firstUrls = firstQ.getUrl();
        if (firstUrls != null && !firstUrls.isEmpty()) {
          float[] coords = parseCoordinates(firstUrls.get(0));
          if (coords != null) {
            x = coords[0];
            y = coords[1];
            w = coords[2];
            h = coords[3];
          }
        }
        // 如果解析失败，跳过该区域
        if (w == 0 || h == 0) continue;

        // 添加主观题区域框 (type=1)
        MarkInfo boxMark = new MarkInfo(x, y, w, h, 0, null, 1, null);
        marksPerSheet.get(sheetIdx).add(boxMark);

        // 构建合并得分文本
        double totalScoreRegion = 0;
        double totalManfenRegion = 0;
        StringBuilder scoresBuilder = new StringBuilder();
        for (int i = 0; i < qList.size(); i++) {
          QuestionItem qi = qList.get(i);
          totalScoreRegion += qi.getScore();
          totalManfenRegion += qi.getManfen();
          if (i > 0) scoresBuilder.append(",");
          scoresBuilder.append(formatScore(qi.getScore()));
        }
        String scoreText;
        if (qList.size() == 1) {
          // 单个小题：直接显示得分/满分
          scoreText = formatScore(totalScoreRegion) + "分/" + formatScore(totalManfenRegion)+"分";
        } else {
          // 多个小题：显示总分/总满分 (小题分:分1,分2,...)
          scoreText =
              String.format(
                  "%s分/%s分 (小题分:%s)",
                  formatScore(totalScoreRegion),
                  formatScore(totalManfenRegion),
                  scoresBuilder.toString());
        }

        // 添加主观题得分文本 (type=2)
        float textX = x + 10;
        float textY = y + 35;
        MarkInfo textMark = new MarkInfo(textX, textY, 0, 0, 0, null, 2, scoreText);
        marksPerSheet.get(sheetIdx).add(textMark);
      }
    }
  }

  //去除末尾多余的 .0
  private String formatScore(double score) {
    if (score == (long) score) {
      return String.valueOf((long) score);
    } else {
      return String.valueOf(score);
    }
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_answersheet, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
    recyclerView.addItemDecoration(
        new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

    ImageAdapter adapter = new ImageAdapter(data.getUrl(), marksPerSheet, requireActivity());
    recyclerView.setAdapter(adapter);
  }
}
