package com.edumind.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.edumind.app.R;
import com.edumind.app.common.ApiError;
import com.edumind.app.models.MessageResponse;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * OTP verification only requires email + otp — no CAPTCHA on this step,
 * matching the backend route (`/auth/verify-otp` has no verifyRecaptcha
 * middleware).
 */
public class OtpVerifyActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";

    private String email;
    private TextInputEditText otpInput;
    private TextView subtitleText, errorText, successText;
    private MaterialButton verifyButton;
    private ProgressBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);

        email = getIntent().getStringExtra(EXTRA_EMAIL);

        subtitleText = findViewById(R.id.subtitleText);
        errorText = findViewById(R.id.errorText);
        successText = findViewById(R.id.successText);
        otpInput = findViewById(R.id.otpInput);
        verifyButton = findViewById(R.id.verifyButton);
        loadingBar = findViewById(R.id.loadingBar);

        subtitleText.setText("We sent a 6-digit code to " + email);
        verifyButton.setOnClickListener(v -> attemptVerify());
    }

    private void attemptVerify() {
        String otp = String.valueOf(otpInput.getText()).trim();
        errorText.setVisibility(View.GONE);

        if (TextUtils.isEmpty(otp)) {
            showError("Please enter the OTP sent to your email.");
            return;
        }

        setLoading(true);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("otp", otp);

        ApiService api = ApiClient.getApiService(this);
        api.verifyOtp(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    successText.setText(response.body() != null && response.body().message != null
                            ? response.body().message : "Account verified.");
                    successText.setVisibility(View.VISIBLE);
                    verifyButton.setEnabled(false);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(new Intent(OtpVerifyActivity.this, LoginActivity.class));
                        finish();
                    }, 900);
                } else {
                    showError(ApiError.from(response));
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                setLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        loadingBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        verifyButton.setEnabled(!loading);
    }
}
