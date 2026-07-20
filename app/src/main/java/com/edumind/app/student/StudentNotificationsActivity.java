package com.edumind.app.student;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edumind.app.R;
import com.edumind.app.common.ApiError;
import com.edumind.app.common.GenericJsonAdapter;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentNotificationsActivity extends AppCompatActivity {

    private ApiService api;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyText;
    private GenericJsonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_list);
        api = ApiClient.getApiService(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Notifications");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyText = findViewById(R.id.emptyText);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GenericJsonAdapter(new String[]{"title", "message"}, new String[]{"created_at"}, this::markRead);
        recyclerView.setAdapter(adapter);
        findViewById(R.id.fabAdd).setVisibility(View.GONE);

        swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        api.getJson("student/notifications", new HashMap<>()).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isJsonArray()) {
                    adapter.submit(response.body().getAsJsonArray());
                    emptyText.setVisibility(response.body().getAsJsonArray().size() == 0 ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(StudentNotificationsActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(StudentNotificationsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void markRead(JsonObject item) {
        if (!item.has("id")) return;
        int id = item.get("id").getAsInt();
        api.putJson("student/notifications/" + id + "/read", new HashMap<>()).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(StudentNotificationsActivity.this, "Marked as read.", Toast.LENGTH_SHORT).show();
                    load();
                } else {
                    Toast.makeText(StudentNotificationsActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(StudentNotificationsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
