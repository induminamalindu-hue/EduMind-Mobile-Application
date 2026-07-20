package com.edumind.app.faculty;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edumind.app.R;
import com.edumind.app.common.ApiError;
import com.edumind.app.models.AttendanceListResponse;
import com.edumind.app.models.AttendanceSessionResponse;
import com.edumind.app.models.MessageResponse;
import com.edumind.app.models.Subject;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacultyAttendanceActivity extends AppCompatActivity {

    private ApiService api;
    private Spinner subjectSpinner;
    private List<Subject> subjects = new ArrayList<>();
    private String selectedDate;
    private MaterialButton pickDateButton, manualTabButton, qrTabButton, saveAttendanceButton, generateQrButton;
    private View manualPanel, qrPanel;
    private TextView lockBanner, qrCountdownText;
    private RecyclerView rosterRecyclerView;
    private AttendanceRosterAdapter rosterAdapter;
    private List<com.edumind.app.models.AttendanceRow> rosterRows = new ArrayList<>();
    private ImageView qrImageView;
    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private long expiresAtMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_attendance);
        api = ApiClient.getApiService(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        subjectSpinner = findViewById(R.id.subjectSpinner);
        pickDateButton = findViewById(R.id.pickDateButton);
        manualTabButton = findViewById(R.id.manualTabButton);
        qrTabButton = findViewById(R.id.qrTabButton);
        manualPanel = findViewById(R.id.manualPanel);
        qrPanel = findViewById(R.id.qrPanel);
        lockBanner = findViewById(R.id.lockBanner);
        rosterRecyclerView = findViewById(R.id.rosterRecyclerView);
        saveAttendanceButton = findViewById(R.id.saveAttendanceButton);
        generateQrButton = findViewById(R.id.generateQrButton);
        qrImageView = findViewById(R.id.qrImageView);
        qrCountdownText = findViewById(R.id.qrCountdownText);

        rosterRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Calendar today = Calendar.getInstance();
        selectedDate = isoDate(today);
        pickDateButton.setText("Date: " + selectedDate);

        pickDateButton.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                Calendar picked = Calendar.getInstance();
                picked.set(year, month, day);
                selectedDate = isoDate(picked);
                pickDateButton.setText("Date: " + selectedDate);
                loadRoster();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        manualTabButton.setOnClickListener(v -> showManual());
        qrTabButton.setOnClickListener(v -> showQr());
        saveAttendanceButton.setOnClickListener(v -> saveAttendance());
        generateQrButton.setOnClickListener(v -> generateQr());

        subjectSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadRoster();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        loadSubjects();
    }

    private String isoDate(Calendar c) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(c.getTime());
    }

    private void loadSubjects() {
        api.getMySubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects = response.body();
                    ArrayAdapter<Subject> adapter = new ArrayAdapter<>(FacultyAttendanceActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, subjects);
                    subjectSpinner.setAdapter(adapter);
                    if (!subjects.isEmpty()) loadRoster();
                } else {
                    Toast.makeText(FacultyAttendanceActivity.this, "Could not load your subjects.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(FacultyAttendanceActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Integer selectedSubjectId() {
        int pos = subjectSpinner.getSelectedItemPosition();
        if (pos < 0 || pos >= subjects.size()) return null;
        return subjects.get(pos).id;
    }

    private void showManual() {
        manualPanel.setVisibility(View.VISIBLE);
        qrPanel.setVisibility(View.GONE);
    }

    private void showQr() {
        manualPanel.setVisibility(View.GONE);
        qrPanel.setVisibility(View.VISIBLE);
    }

    private void loadRoster() {
        Integer subjectId = selectedSubjectId();
        if (subjectId == null) return;

        api.getAttendanceForSession(subjectId, selectedDate).enqueue(new Callback<AttendanceListResponse>() {
            @Override
            public void onResponse(Call<AttendanceListResponse> call, Response<AttendanceListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AttendanceListResponse data = response.body();
                    rosterRows = data.students != null ? data.students : new ArrayList<>();
                    for (com.edumind.app.models.AttendanceRow row : rosterRows) {
                        if (row.status == null) row.status = "present";
                    }
                    rosterAdapter = new AttendanceRosterAdapter(rosterRows, data.editable);
                    rosterRecyclerView.setAdapter(rosterAdapter);
                    saveAttendanceButton.setVisibility(data.editable ? View.VISIBLE : View.GONE);
                    if (!data.editable) {
                        lockBanner.setVisibility(View.VISIBLE);
                        lockBanner.setText("This date is more than " + data.editWindowDays
                                + " days in the past — view only. Use Attendance Report for full history.");
                    } else {
                        lockBanner.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(FacultyAttendanceActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceListResponse> call, Throwable t) {
                Toast.makeText(FacultyAttendanceActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveAttendance() {
        Integer subjectId = selectedSubjectId();
        if (subjectId == null) return;

        List<Map<String, Object>> records = new ArrayList<>();
        for (com.edumind.app.models.AttendanceRow row : rosterRows) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("studentId", row.studentId);
            r.put("status", row.status);
            records.add(r);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("subjectId", subjectId);
        body.put("date", selectedDate);
        body.put("records", records);

        api.markManualAttendance(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FacultyAttendanceActivity.this, "Attendance saved.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FacultyAttendanceActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(FacultyAttendanceActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void generateQr() {
        Integer subjectId = selectedSubjectId();
        if (subjectId == null) return;

        String classLabel = ((android.widget.EditText) findViewById(R.id.classLabelInput)).getText().toString();
        String durationStr = ((android.widget.EditText) findViewById(R.id.durationInput)).getText().toString();
        int duration = durationStr.isEmpty() ? 5 : Integer.parseInt(durationStr);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("subjectId", subjectId);
        body.put("classLabel", classLabel);
        body.put("sessionDate", selectedDate);
        body.put("durationMinutes", duration);

        api.createAttendanceSession(body).enqueue(new Callback<AttendanceSessionResponse>() {
            @Override
            public void onResponse(Call<AttendanceSessionResponse> call, Response<AttendanceSessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showQrResult(response.body());
                } else {
                    Toast.makeText(FacultyAttendanceActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AttendanceSessionResponse> call, Throwable t) {
                Toast.makeText(FacultyAttendanceActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showQrResult(AttendanceSessionResponse data) {
        String base64 = data.qrImage.substring(data.qrImage.indexOf(',') + 1);
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        qrImageView.setImageBitmap(bitmap);
        qrImageView.setVisibility(View.VISIBLE);
        qrCountdownText.setVisibility(View.VISIBLE);

        expiresAtMillis = parseIsoToMillis(data.expiresAt);
        countdownHandler.removeCallbacksAndMessages(null);
        tickCountdown();
    }

    private long parseIsoToMillis(String iso) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(iso).getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }

    private void tickCountdown() {
        long remainingMs = expiresAtMillis - System.currentTimeMillis();
        if (remainingMs <= 0) {
            qrCountdownText.setText("QR expired");
            qrCountdownText.setTextColor(getResources().getColor(R.color.danger));
            return;
        }
        long totalSeconds = remainingMs / 1000;
        qrCountdownText.setText(String.format(Locale.US, "Expires in %02d:%02d", totalSeconds / 60, totalSeconds % 60));
        qrCountdownText.setTextColor(getResources().getColor(R.color.faculty));
        countdownHandler.postDelayed(this::tickCountdown, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countdownHandler.removeCallbacksAndMessages(null);
    }
}
