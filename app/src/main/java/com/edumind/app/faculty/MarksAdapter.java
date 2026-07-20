package com.edumind.app.faculty;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edumind.app.R;
import com.edumind.app.models.MarksRow;

import java.util.List;

public class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.VH> {

    private final List<MarksRow> rows;

    public MarksAdapter(List<MarksRow> rows) {
        this.rows = rows;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marks_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        MarksRow row = rows.get(position);
        holder.name.setText(row.name);
        holder.studentNo.setText(row.student_no);
        holder.internalInput.setText(row.internalMarks != null ? String.valueOf(row.internalMarks) : "");
        holder.examInput.setText(row.examMarks != null ? String.valueOf(row.examMarks) : "");

        holder.internalInput.removeTextChangedListener(holder.internalWatcher);
        holder.internalWatcher = simpleWatcher(text -> row.internalMarks = parseOrNull(text));
        holder.internalInput.addTextChangedListener(holder.internalWatcher);

        holder.examInput.removeTextChangedListener(holder.examWatcher);
        holder.examWatcher = simpleWatcher(text -> row.examMarks = parseOrNull(text));
        holder.examInput.addTextChangedListener(holder.examWatcher);
    }

    private Double parseOrNull(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private interface TextConsumer {
        void accept(String text);
    }

    private TextWatcher simpleWatcher(TextConsumer consumer) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                consumer.accept(s.toString());
            }
        };
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, studentNo;
        EditText internalInput, examInput;
        TextWatcher internalWatcher, examWatcher;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            studentNo = itemView.findViewById(R.id.studentNo);
            internalInput = itemView.findViewById(R.id.internalInput);
            examInput = itemView.findViewById(R.id.examInput);
        }
    }
}
