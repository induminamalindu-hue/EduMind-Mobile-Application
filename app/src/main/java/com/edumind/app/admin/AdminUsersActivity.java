package com.edumind.app.admin;

import android.app.AlertDialog;
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
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyText;
    private GenericJsonAdapter adapter;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("User Approvals");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyText = findViewById(R.id.emptyText);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new GenericJsonAdapter(new String[]{"name"}, new String[]{"email", "role", "status"}, this::showActions);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.fabAdd).setVisibility(View.GONE);

        api = ApiClient.getApiService(this);
        swipeRefresh.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        swipeRefresh.setRefreshing(true);
        api.getJson("admin/users", new HashMap<>()).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isJsonArray()) {
                    adapter.submit(response.body().getAsJsonArray());
                    emptyText.setVisibility(response.body().getAsJsonArray().size() == 0 ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(AdminUsersActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminUsersActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showActions(JsonObject user) {
        int id = user.get("id").getAsInt();
        String name = user.get("name").getAsString();

        new AlertDialog.Builder(this)
                .setTitle(name)
                .setItems(new String[]{"Approve (status = 1)", "Disable (status = 0)"}, (dialog, which) -> {
                    int newStatus = which == 0 ? 1 : 0;
                    updateStatus(id, newStatus);
                })
                .show();
    }

    private void updateStatus(int userId, int status) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        api.putJson("admin/users/" + userId + "/status", body).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminUsersActivity.this, "Status updated.", Toast.LENGTH_SHORT).show();
                    load();
                } else {
                    Toast.makeText(AdminUsersActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(AdminUsersActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
