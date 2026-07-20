package com.edumind.app.faculty;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edumind.app.R;
import com.edumind.app.models.AttendanceRow;

import java.util.List;

public class AttendanceRosterAdapter extends RecyclerView.Adapter<AttendanceRosterAdapter.VH> {

    private final List<AttendanceRow> rows;
    private final boolean editable;

    public AttendanceRosterAdapter(List<AttendanceRow> rows, boolean editable) {
        this.rows = rows;
        this.editable = editable;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AttendanceRow row = rows.get(position);
        holder.name.setText(row.name);
        holder.studentNo.setText(row.student_no);

        holder.present.setEnabled(editable);
        holder.late.setEnabled(editable);
        holder.absent.setEnabled(editable);

        highlight(holder, row.status);

        holder.present.setOnClickListener(v -> {
            row.status = "present";
            highlight(holder, row.status);
        });
        holder.late.setOnClickListener(v -> {
            row.status = "late";
            highlight(holder, row.status);
        });
        holder.absent.setOnClickListener(v -> {
            row.status = "absent";
            highlight(holder, row.status);
        });
    }

    private void highlight(VH holder, String status) {
        reset(holder.present);
        reset(holder.late);
        reset(holder.absent);
        if ("present".equals(status)) tint(holder.present, Color.parseColor("#2E7D32"));
        else if ("late".equals(status)) tint(holder.late, Color.parseColor("#B26A00"));
        else if ("absent".equals(status)) tint(holder.absent, Color.parseColor("#C62828"));
    }

    private void reset(Button b) {
        b.setBackgroundColor(Color.parseColor("#EEEEEE"));
        b.setTextColor(Color.parseColor("#88000000"));
    }

    private void tint(Button b, int color) {
        b.setBackgroundColor(color);
        b.setTextColor(Color.WHITE);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, studentNo;
        Button present, late, absent;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.studentName);
            studentNo = itemView.findViewById(R.id.studentNo);
            present = itemView.findViewById(R.id.presentButton);
            late = itemView.findViewById(R.id.lateButton);
            absent = itemView.findViewById(R.id.absentButton);
        }
    }
}
