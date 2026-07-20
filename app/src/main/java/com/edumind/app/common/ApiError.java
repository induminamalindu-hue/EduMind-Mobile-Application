package com.edumind.app.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Response;

/** Every EduMind backend error response looks like { "message": "..." }. */
public class ApiError {

    public static String from(Response<?> response) {
        if (response == null) return "Something went wrong.";
        try (ResponseBody body = response.errorBody()) {
            if (body == null) return "Something went wrong (HTTP " + response.code() + ").";
            String raw = body.string();
            JsonObject obj = new Gson().fromJson(raw, JsonObject.class);
            if (obj != null && obj.has("message")) {
                return obj.get("message").getAsString();
            }
            return "Something went wrong (HTTP " + response.code() + ").";
        } catch (Exception e) {
            return "Something went wrong (HTTP " + response.code() + ").";
        }
    }
}
