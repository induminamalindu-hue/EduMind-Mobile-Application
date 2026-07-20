package com.edumind.app.faculty;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edumind.app.R;
import com.edumind.app.common.ApiError;
import com.edumind.app.models.AttendanceReportResponse;
import com.edumind.app.models.Subject;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacultyAttendanceReportActivity extends AppCompatActivity {

    private ApiService api;
    private Spinner subjectSpinner;
    private List<Subject> subjects;
    private MaterialButton fromButton, toButton;
    private TextView summaryText;
    private AttendanceReportAdapter adapter;
    private String from, to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_attendance_report);
        api = ApiClient.getApiService(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        subjectSpinner = findViewById(R.id.subjectSpinner);
        fromButton = findViewById(R.id.fromButton);
        toButton = findViewById(R.id.toButton);
        summaryText = findViewById(R.id.summaryText);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceReportAdapter();
        recyclerView.setAdapter(adapter);

        Calendar now = Calendar.getInstance();
        to = isoDate(now);
        now.add(Calendar.DAY_OF_YEAR, -30);
        from = isoDate(now);
        fromButton.setText("From: " + from);
        toButton.setText("To: " + to);

        fromButton.setOnClickListener(v -> pickDate(true));
        toButton.setOnClickListener(v -> pickDate(false));

        subjectSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                load();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        loadSubjects();
    }

    private String isoDate(Calendar c) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.getTime());
    }

    private void pickDate(boolean isFrom) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            String iso = isoDate(picked);
            if (isFrom) {
                from = iso;
                fromButton.setText("From: " + from);
            } else {
                to = iso;
                toButton.setText("To: " + to);
            }
            load();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadSubjects() {
        api.getMySubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects = response.body();
                    subjectSpinner.setAdapter(new ArrayAdapter<>(FacultyAttendanceReportActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, subjects));
                    if (!subjects.isEmpty()) load();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(FacultyAttendanceReportActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void load() {
        if (subjects == null || subjects.isEmpty()) return;
        int pos = subjectSpinner.getSelectedItemPosition();
        if (pos < 0) return;
        int subjectId = subjects.get(pos).id;

        api.getAttendanceReport(subjectId, from, to).enqueue(new Callback<AttendanceReportResponse>() {
            @Override
            public void onResponse(Call<AttendanceReportResponse> call, Response<AttendanceReportResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AttendanceReportResponse data = response.body();
                    adapter.submit(data.students);
                    summaryText.setText(data.subjectCode + " — " + data.subjectName + " · " + data.totalSessions + " session(s)");
                } else {
                    Toast.makeText(FacultyAttendanceReportActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceReportResponse> call, Throwable t) {
                Toast.makeText(FacultyAttendanceReportActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
