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
import java.util.ArrayList;
import java.util.List;

public class AnswerSheetFragment extends Fragment {
    private static final String ARG_DATA = "answer_data";
    private AnswerPictureData data;
    private List<List<MarkInfo>> marksPerSheet = new ArrayList<>();

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

    private void buildMarksPerSheet() {
        if (data == null) return;
        List<String> urls = data.getUrl();
        if (urls == null) return;
        int sheetCount = urls.size();
        for (int i = 0; i < sheetCount; i++) {
            marksPerSheet.add(new ArrayList<>());
        }

        List<QuestionItem> questions = data.getQuestions();
        if (questions != null) {
            for (QuestionItem q : questions) {
                if (q.getType() == 2) { // 客观题
                    List<AnswerOptionItem> options = q.getAnswerOption();
                    if (options != null) {
                        for (AnswerOptionItem opt : options) {
                            int sheetIndex = opt.getIndex();
                            if (sheetIndex >= 0 && sheetIndex < sheetCount) {
                                MarkInfo mark = new MarkInfo(
                                        Float.parseFloat(opt.getX()),
                                        Float.parseFloat(opt.getY()),
                                        Float.parseFloat(opt.getW()),
                                        Float.parseFloat(opt.getH()),
                                        opt.getRight(),
                                        opt.getOption()
                                );
                                marksPerSheet.get(sheetIndex).add(mark);
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_answersheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        ImageAdapter adapter = new ImageAdapter(data.getUrl(), marksPerSheet, requireActivity());
        recyclerView.setAdapter(adapter);
    }
}