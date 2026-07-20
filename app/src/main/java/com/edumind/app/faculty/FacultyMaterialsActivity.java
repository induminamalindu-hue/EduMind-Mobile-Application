package com.edumind.app.faculty;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edumind.app.R;
import com.edumind.app.common.ApiError;
import com.edumind.app.common.GenericJsonAdapter;
import com.edumind.app.models.Subject;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FacultyMaterialsActivity extends AppCompatActivity {

    private ApiService api;
    private Spinner subjectSpinner;
    private List<Subject> subjects;
    private TextInputEditText titleInput, descriptionInput;
    private MaterialButton pickFileButton;
    private GenericJsonAdapter adapter;
    private Uri pickedFileUri;

    private final ActivityResultLauncher<String> filePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                pickedFileUri = uri;
                if (uri != null) pickFileButton.setText("File selected: " + queryFileName(uri));
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_materials);
        api = ApiClient.getApiService(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        subjectSpinner = findViewById(R.id.subjectSpinner);
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        pickFileButton = findViewById(R.id.pickFileButton);

        pickFileButton.setOnClickListener(v -> filePicker.launch("*/*"));
        findViewById(R.id.uploadButton).setOnClickListener(v -> upload());

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GenericJsonAdapter(new String[]{"title"}, new String[]{"subjectName", "file_name"}, this::confirmDelete);
        recyclerView.setAdapter(adapter);

        loadSubjects();
        loadMaterials();
    }

    private String queryFileName(Uri uri) {
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return cursor.getString(idx);
            }
        } catch (Exception ignored) {}
        return "file";
    }

    private void loadSubjects() {
        api.getMySubjects().enqueue(new Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subjects = response.body();
                    subjectSpinner.setAdapter(new ArrayAdapter<>(FacultyMaterialsActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, subjects));
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                Toast.makeText(FacultyMaterialsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMaterials() {
        api.getJson("faculty/materials", new HashMap<>()).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isJsonArray()) {
                    adapter.submit(response.body().getAsJsonArray());
                } else {
                    Toast.makeText(FacultyMaterialsActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(FacultyMaterialsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void upload() {
        if (subjects == null || subjects.isEmpty()) {
            Toast.makeText(this, "No subjects available.", Toast.LENGTH_SHORT).show();
            return;
        }
        int pos = subjectSpinner.getSelectedItemPosition();
        int subjectId = subjects.get(pos).id;
        String title = String.valueOf(titleInput.getText());
        String description = String.valueOf(descriptionInput.getText());

        if (pickedFileUri == null || title.trim().isEmpty()) {
            Toast.makeText(this, "Pick a file and enter a title first.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream is = getContentResolver().openInputStream(pickedFileUri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int read;
            while (is != null && (read = is.read(data)) != -1) buffer.write(data, 0, read);
            byte[] fileBytes = buffer.toByteArray();

            RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("application/octet-stream"));
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", queryFileName(pickedFileUri), fileBody);

            java.util.Map<String, RequestBody> fields = new HashMap<>();
            fields.put("subjectId", RequestBody.create(String.valueOf(subjectId), MediaType.parse("text/plain")));
            fields.put("title", RequestBody.create(title, MediaType.parse("text/plain")));
            fields.put("description", RequestBody.create(description, MediaType.parse("text/plain")));

            api.uploadFile("faculty/materials", filePart, fields).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(FacultyMaterialsActivity.this, "Uploaded.", Toast.LENGTH_SHORT).show();
                        titleInput.setText("");
                        descriptionInput.setText("");
                        pickedFileUri = null;
                        pickFileButton.setText("Choose file");
                        loadMaterials();
                    } else {
                        Toast.makeText(FacultyMaterialsActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Toast.makeText(FacultyMaterialsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Could not read the selected file.", Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDelete(JsonObject item) {
        int id = item.get("id").getAsInt();
        new AlertDialog.Builder(this)
                .setTitle("Delete this material?")
                .setPositiveButton("Delete", (d, w) -> api.deleteJson("faculty/materials/" + id).enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(FacultyMaterialsActivity.this, "Deleted.", Toast.LENGTH_SHORT).show();
                            loadMaterials();
                        } else {
                            Toast.makeText(FacultyMaterialsActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Toast.makeText(FacultyMaterialsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
