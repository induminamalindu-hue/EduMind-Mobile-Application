package com.edumind.app.faculty;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edumind.app.R;
import com.edumind.app.models.AttendanceReportResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AttendanceReportAdapter extends RecyclerView.Adapter<AttendanceReportAdapter.VH> {

    private List<AttendanceReportResponse.Row> rows = new ArrayList<>();

    public void submit(List<AttendanceReportResponse.Row> newRows) {
        rows = newRows != null ? newRows : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_report_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        AttendanceReportResponse.Row row = rows.get(position);
        holder.name.setText(row.name);
        holder.counts.setText(row.student_no + " · Present " + row.present + " · Late " + row.late + " · Absent " + row.absent);
        if (row.percentage == null) {
            holder.percentage.setText("—");
            holder.percentage.setTextColor(Color.parseColor("#88000000"));
        } else {
            holder.percentage.setText(String.format(Locale.US, "%.1f%%", row.percentage));
            if (row.percentage >= 75) holder.percentage.setTextColor(Color.parseColor("#2E7D32"));
            else if (row.percentage >= 50) holder.percentage.setTextColor(Color.parseColor("#B26A00"));
            else holder.percentage.setTextColor(Color.parseColor("#C62828"));
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, counts, percentage;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            counts = itemView.findViewById(R.id.counts);
            percentage = itemView.findViewById(R.id.percentage);
        }
    }
}
