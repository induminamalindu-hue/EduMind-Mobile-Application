package com.edumind.app.faculty;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edumind.app.R;
import com.edumind.app.common.ApiError;
import com.edumind.app.models.MarksRow;
import com.edumind.app.models.MessageResponse;
import com.edumind.app.models.Subject;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacultyMarksActivity extends AppCompatActivity {

    private ApiService api;
    private Spinner subjectSpinner;
    private List<Subject> subjects;
    private List<MarksRow> rows = new ArrayList<>();
    private MarksAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_marks);
        api = ApiClient.getApiService(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        subjectSpinner = findViewById(R.id.subjectSpinner);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MarksAdapter(rows);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.saveButton).setOnClickListener(v -> save());

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

    private void loadSubjects() {
        api.getMySubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects = response.body();
                    subjectSpinner.setAdapter(new ArrayAdapter<>(FacultyMarksActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, subjects));
                    if (!subjects.isEmpty()) load();
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(FacultyMarksActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Integer selectedSubjectId() {
        int pos = subjectSpinner.getSelectedItemPosition();
        if (subjects == null || pos < 0 || pos >= subjects.size()) return null;
        return subjects.get(pos).id;
    }

    private void load() {
        Integer subjectId = selectedSubjectId();
        if (subjectId == null) return;

        api.getMarksForSubject(subjectId).enqueue(new Callback<List<MarksRow>>() {
            @Override
            public void onResponse(Call<List<MarksRow>> call, Response<List<MarksRow>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rows.clear();
                    rows.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(FacultyMarksActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<MarksRow>> call, Throwable t) {
                Toast.makeText(FacultyMarksActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void save() {
        Integer subjectId = selectedSubjectId();
        if (subjectId == null) return;

        List<Map<String, Object>> records = new ArrayList<>();
        for (MarksRow row : rows) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("studentId", row.studentId);
            r.put("internalMarks", row.internalMarks);
            r.put("examMarks", row.examMarks);
            records.add(r);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("subjectId", subjectId);
        body.put("records", records);

        api.saveMarks(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(FacultyMarksActivity.this, "Marks saved.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FacultyMarksActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Toast.makeText(FacultyMarksActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
