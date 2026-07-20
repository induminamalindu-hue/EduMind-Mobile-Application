package com.edumind.app.network;

import com.edumind.app.models.AttendanceListResponse;
import com.edumind.app.models.AttendanceReportResponse;
import com.edumind.app.models.AttendanceSessionResponse;
import com.edumind.app.models.LoginResponse;
import com.edumind.app.models.MarkAttendanceResponse;
import com.edumind.app.models.MessageResponse;
import com.edumind.app.models.QrVerifyResponse;
import com.edumind.app.models.Subject;
import com.edumind.app.models.User;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {

    /* ---------------- AUTH ---------------- */

    @POST("auth/login")
    Call<LoginResponse> login(@Body Map<String, String> body);

    @POST("auth/register")
    Call<MessageResponse> register(@Body Map<String, String> body);

    @POST("auth/verify-otp")
    Call<MessageResponse> verifyOtp(@Body Map<String, String> body);

    @POST("auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body Map<String, String> body);

    @POST("auth/verify-reset-otp")
    Call<MessageResponse> verifyResetOtp(@Body Map<String, String> body);

    @POST("auth/reset-password")
    Call<MessageResponse> resetPassword(@Body Map<String, String> body);

    @GET("auth/me")
    Call<User> getMe();

    @PUT("auth/change-password")
    Call<MessageResponse> changePassword(@Body Map<String, String> body);

    /* ---------------- FACULTY: subjects (used for dropdowns) ---------------- */

    @GET("faculty/subjects")
    Call<List<Subject>> getMySubjects();

    /* ---------------- ATTENDANCE (typed — QR flow) ---------------- */

    @GET("faculty/attendance")
    Call<AttendanceListResponse> getAttendanceForSession(@Query("subjectId") int subjectId, @Query("date") String date);

    @POST("faculty/attendance")
    Call<MessageResponse> markManualAttendance(@Body Map<String, Object> body);

    @GET("faculty/attendance/report")
    Call<AttendanceReportResponse> getAttendanceReport(
            @Query("subjectId") int subjectId, @Query("from") String from, @Query("to") String to);

    @GET("faculty/marks")
    Call<List<com.edumind.app.models.MarksRow>> getMarksForSubject(@Query("subjectId") int subjectId);

    @POST("faculty/marks")
    Call<MessageResponse> saveMarks(@Body Map<String, Object> body);

    @POST("attendance/create-session")
    Call<AttendanceSessionResponse> createAttendanceSession(@Body Map<String, Object> body);

    @GET("attendance/qr/{token}")
    Call<QrVerifyResponse> verifyQrToken(@Path("token") String token);

    @POST("attendance/mark")
    Call<MarkAttendanceResponse> markAttendance(@Body Map<String, String> body);

    /* ---------------- GENERIC JSON (admin CRUD, marks, materials, notices, etc.) ----------------
     * `path` is relative to the base URL, e.g. "admin/students" or "student/overview".
     * Used by JsonListActivity so every remaining module (Admin CRUD screens, Marks, Materials,
     * Notices, Exams, Logs, Notes, Notifications, Reports...) works against the real backend
     * without needing a hand-written POJO + Activity for every single entity. */

    @GET
    Call<JsonElement> getJson(@Url String path, @QueryMap Map<String, String> query);

    @POST
    Call<JsonElement> postJson(@Url String path, @Body Map<String, Object> body);

    @PUT
    Call<JsonElement> putJson(@Url String path, @Body Map<String, Object> body);

    @DELETE
    Call<JsonElement> deleteJson(@Url String path);

    @Multipart
    @POST
    Call<JsonElement> uploadFile(@Url String path, @Part MultipartBody.Part file, @PartMap Map<String, RequestBody> fields);
}
