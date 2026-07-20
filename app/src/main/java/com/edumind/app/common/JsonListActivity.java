package com.edumind.app.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edumind.app.R;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A single reusable screen that lists whatever JSON array a backend endpoint
 * returns, and (optionally) lets the user create or delete rows. This backs
 * every "plain list" module in the app (Admin's Students/Faculty/Branches/
 * Subjects/Notices/Exams/Logs/Users, Faculty's Materials/Notes, Student's
 * Materials/Notices/Notifications/Notes) so each one doesn't need its own
 * hand-written Activity + adapter + model class.
 *
 * Launch it via the static Builder, e.g.:
 * <pre>
 * JsonListActivity.Builder.from(this, "admin/students")
 *     .title("Students")
 *     .titleKeys("name")
 *     .subtitleKeys("student_no", "email")
 *     .createFields(new String[]{"name","email","student_no","branchId","semester","password"},
 *                   new String[]{"Full name","Email","Student No.","Branch ID","Semester","Password"})
 *     .allowDelete(true)
 *     .start(this);
 * </pre>
 */
public class JsonListActivity extends AppCompatActivity {

    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_ENDPOINT = "endpoint";
    private static final String EXTRA_ARRAY_KEY = "array_key";
    private static final String EXTRA_TITLE_KEYS = "title_keys";
    private static final String EXTRA_SUBTITLE_KEYS = "subtitle_keys";
    private static final String EXTRA_CREATE_FIELDS = "create_fields";
    private static final String EXTRA_CREATE_LABELS = "create_labels";
    private static final String EXTRA_ALLOW_DELETE = "allow_delete";
    private static final String EXTRA_ID_KEY = "id_key";
    private static final String EXTRA_QUERY_KEYS = "query_keys";
    private static final String EXTRA_QUERY_VALUES = "query_values";
    private static final String EXTRA_FILE_URL_KEY = "file_url_key";
    private static final String EXTRA_BASE_FILE_URL = "base_file_url";

    private String endpoint, arrayKey, idKey, fileUrlKey, baseFileUrl;
    private String[] titleKeys, subtitleKeys, createFields, createLabels, queryKeys, queryValues;
    private boolean allowDelete;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private GenericJsonAdapter adapter;
    private ApiService api;

    public static class Builder {
        private final Intent intent;

        private Builder(Context context, String endpoint) {
            intent = new Intent(context, JsonListActivity.class);
            intent.putExtra(EXTRA_ENDPOINT, endpoint);
        }

        public static Builder from(Context context, String endpoint) {
            return new Builder(context, endpoint);
        }

        public Builder title(String title) {
            intent.putExtra(EXTRA_TITLE, title);
            return this;
        }

        public Builder arrayKey(String key) {
            intent.putExtra(EXTRA_ARRAY_KEY, key);
            return this;
        }

        public Builder titleKeys(String... keys) {
            intent.putExtra(EXTRA_TITLE_KEYS, keys);
            return this;
        }

        public Builder subtitleKeys(String... keys) {
            intent.putExtra(EXTRA_SUBTITLE_KEYS, keys);
            return this;
        }

        public Builder createFields(String[] fields, String[] labels) {
            intent.putExtra(EXTRA_CREATE_FIELDS, fields);
            intent.putExtra(EXTRA_CREATE_LABELS, labels);
            return this;
        }

        public Builder allowDelete(boolean allow) {
            intent.putExtra(EXTRA_ALLOW_DELETE, allow);
            return this;
        }

        public Builder idKey(String key) {
            intent.putExtra(EXTRA_ID_KEY, key);
            return this;
        }

        public Builder query(String[] keys, String[] values) {
            intent.putExtra(EXTRA_QUERY_KEYS, keys);
            intent.putExtra(EXTRA_QUERY_VALUES, values);
            return this;
        }

        /** If rows have a file path field (e.g. "file_path"), show an "Open file" button in the detail dialog. */
        public Builder fileLink(String jsonKey, String baseUrl) {
            intent.putExtra(EXTRA_FILE_URL_KEY, jsonKey);
            intent.putExtra(EXTRA_BASE_FILE_URL, baseUrl);
            return this;
        }

        public void start(Context context) {
            context.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_list);

        endpoint = getIntent().getStringExtra(EXTRA_ENDPOINT);
        arrayKey = getIntent().getStringExtra(EXTRA_ARRAY_KEY);
        idKey = getIntent().getStringExtra(EXTRA_ID_KEY);
        if (idKey == null) idKey = "id";
        titleKeys = getIntent().getStringArrayExtra(EXTRA_TITLE_KEYS);
        subtitleKeys = getIntent().getStringArrayExtra(EXTRA_SUBTITLE_KEYS);
        createFields = getIntent().getStringArrayExtra(EXTRA_CREATE_FIELDS);
        createLabels = getIntent().getStringArrayExtra(EXTRA_CREATE_LABELS);
        allowDelete = getIntent().getBooleanExtra(EXTRA_ALLOW_DELETE, false);
        queryKeys = getIntent().getStringArrayExtra(EXTRA_QUERY_KEYS);
        queryValues = getIntent().getStringArrayExtra(EXTRA_QUERY_VALUES);
        fileUrlKey = getIntent().getStringExtra(EXTRA_FILE_URL_KEY);
        baseFileUrl = getIntent().getStringExtra(EXTRA_BASE_FILE_URL);
        if (titleKeys == null) titleKeys = new String[]{"name", "title", "message"};
        if (subtitleKeys == null) subtitleKeys = new String[]{};

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra(EXTRA_TITLE));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        emptyText = findViewById(R.id.emptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GenericJsonAdapter(titleKeys, subtitleKeys, this::showDetail);
        recyclerView.setAdapter(adapter);

        api = ApiClient.getApiService(this);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        if (createFields != null && createFields.length > 0) {
            fab.setOnClickListener(v -> showCreateDialog());
        } else {
            fab.setVisibility(View.GONE);
        }

        swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    private Map<String, String> buildQuery() {
        Map<String, String> q = new HashMap<>();
        if (queryKeys != null) {
            for (int i = 0; i < queryKeys.length; i++) {
                q.put(queryKeys[i], i < queryValues.length ? queryValues[i] : "");
            }
        }
        return q;
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        api.getJson(endpoint, buildQuery()).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                swipeRefresh.setRefreshing(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(JsonListActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                    return;
                }
                JsonArray array = extractArray(response.body());
                adapter.submit(array);
                emptyText.setVisibility(array == null || array.size() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(JsonListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private JsonArray extractArray(JsonElement root) {
        if (root.isJsonArray()) return root.getAsJsonArray();
        if (root.isJsonObject()) {
            JsonObject obj = root.getAsJsonObject();
            if (arrayKey != null && obj.has(arrayKey) && obj.get(arrayKey).isJsonArray()) {
                return obj.get(arrayKey).getAsJsonArray();
            }
            for (String key : obj.keySet()) {
                if (obj.get(key).isJsonArray()) return obj.get(key).getAsJsonArray();
            }
            // No array field found at all — this is a single-object response (e.g. a profile
            // endpoint). Wrap it as a one-row "array" so it still renders via the same adapter.
            JsonArray single = new JsonArray();
            single.add(obj);
            return single;
        }
        return new JsonArray();
    }

    private void showDetail(JsonObject item) {
        StringBuilder sb = new StringBuilder();
        for (String key : item.keySet()) {
            JsonElement value = item.get(key);
            sb.append(key).append(": ").append(value.isJsonNull() ? "—" : value.toString()).append('\n');
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Details")
                .setMessage(sb.toString())
                .setPositiveButton("Close", null);

        if (fileUrlKey != null && item.has(fileUrlKey) && !item.get(fileUrlKey).isJsonNull()) {
            String path = item.get(fileUrlKey).getAsString();
            String fullUrl = (baseFileUrl != null ? baseFileUrl : "") + path;
            builder.setNeutralButton("Open file", (dialog, which) -> {
                Intent openIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(fullUrl));
                try {
                    startActivity(openIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "No app found to open this file.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (allowDelete && item.has(idKey)) {
            builder.setNegativeButton("Delete", (dialog, which) -> confirmDelete(item.get(idKey).getAsString()));
        }
        builder.show();
    }

    private void confirmDelete(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this record?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> api.deleteJson(endpoint + "/" + id).enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(JsonListActivity.this, "Deleted.", Toast.LENGTH_SHORT).show();
                            load();
                        } else {
                            Toast.makeText(JsonListActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {
                        Toast.makeText(JsonListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCreateDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        EditText[] inputs = new EditText[createFields.length];
        for (int i = 0; i < createFields.length; i++) {
            EditText input = new EditText(this);
            input.setHint(i < createLabels.length ? createLabels[i] : createFields[i]);
            layout.addView(input);
            inputs[i] = input;
        }

        new AlertDialog.Builder(this)
                .setTitle("Add new")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    for (int i = 0; i < createFields.length; i++) {
                        body.put(createFields[i], inputs[i].getText().toString());
                    }
                    api.postJson(endpoint, body).enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(JsonListActivity.this, "Saved.", Toast.LENGTH_SHORT).show();
                                load();
                            } else {
                                Toast.makeText(JsonListActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonElement> call, Throwable t) {
                            Toast.makeText(JsonListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
