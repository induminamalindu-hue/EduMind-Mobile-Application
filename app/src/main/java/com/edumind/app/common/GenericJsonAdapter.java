package com.edumind.app.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edumind.app.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a JsonArray of JsonObjects as a two-line list (title / subtitle),
 * built from whichever field keys the caller configures. This is what backs
 * every "generic" module screen (Admin CRUD, Marks, Materials, Notices,
 * Logs, Notes, etc.) so we don't need a bespoke adapter per entity.
 */
public class GenericJsonAdapter extends RecyclerView.Adapter<GenericJsonAdapter.VH> {

    public interface OnRowClick {
        void onClick(JsonObject item);
    }

    private final List<JsonObject> items = new ArrayList<>();
    private final String[] titleKeys;
    private final String[] subtitleKeys;
    private final OnRowClick onClick;

    /**
     * @param titleKeys    field keys tried in order for the bold top line (first non-null wins)
     * @param subtitleKeys field keys joined with " · " for the grey second line
     */
    public GenericJsonAdapter(String[] titleKeys, String[] subtitleKeys, OnRowClick onClick) {
        this.titleKeys = titleKeys;
        this.subtitleKeys = subtitleKeys;
        this.onClick = onClick;
    }

    public void submit(JsonArray array) {
        items.clear();
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) items.add(array.get(i).getAsJsonObject());
            }
        }
        notifyDataSetChanged();
    }

    public JsonObject getItem(int position) {
        return items.get(position);
    }

    private static String firstNonNull(JsonObject obj, String[] keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) return obj.get(key).getAsString();
        }
        return "";
    }

    private static String joinNonNull(JsonObject obj, String[] keys) {
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(obj.get(key).getAsString());
            }
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generic_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        JsonObject item = items.get(position);
        holder.title.setText(firstNonNull(item, titleKeys));
        String subtitle = joinNonNull(item, subtitleKeys);
        holder.subtitle.setText(subtitle);
        holder.subtitle.setVisibility(subtitle.isEmpty() ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rowTitle);
            subtitle = itemView.findViewById(R.id.rowSubtitle);
        }
    }
}
