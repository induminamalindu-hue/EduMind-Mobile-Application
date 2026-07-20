package com.edumind.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.edumind.app.R;
import com.edumind.app.admin.AdminDashboardActivity;
import com.edumind.app.common.ApiError;
import com.edumind.app.faculty.FacultyDashboardActivity;
import com.edumind.app.models.LoginResponse;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;
import com.edumind.app.network.SessionManager;
import com.edumind.app.student.StudentDashboardActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private TextView errorText, captchaStatusText;
    private MaterialButton verifyCaptchaButton, loginButton;
    private ProgressBar loadingBar;
    private String captchaToken = null;

    private final ActivityResultLauncher<Intent> recaptchaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    captchaToken = result.getData().getStringExtra(RecaptchaActivity.EXTRA_TOKEN);
                    captchaStatusText.setText("✓ CAPTCHA verified");
                    loginButton.setEnabled(true);
                } else {
                    captchaToken = null;
                    loginButton.setEnabled(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        errorText = findViewById(R.id.errorText);
        captchaStatusText = findViewById(R.id.captchaStatusText);
        verifyCaptchaButton = findViewById(R.id.verifyCaptchaButton);
        loginButton = findViewById(R.id.loginButton);
        loadingBar = findViewById(R.id.loadingBar);

        verifyCaptchaButton.setOnClickListener(v -> recaptchaLauncher.launch(new Intent(this, RecaptchaActivity.class)));
        loginButton.setOnClickListener(v -> attemptLogin());
        findViewById(R.id.registerLink).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        // Forgot-password flow reuses the same reCAPTCHA + OTP pattern; wire to your own screen if you build one.
        findViewById(R.id.forgotPasswordLink).setOnClickListener(v ->
                Toast.makeText(this, "Forgot-password screen not wired up yet — see MainActivity comments.", Toast.LENGTH_LONG).show());
    }

    private void attemptLogin() {
        String email = String.valueOf(emailInput.getText()).trim();
        String password = String.valueOf(passwordInput.getText());
        errorText.setVisibility(View.GONE);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Please enter both email and password.");
            return;
        }
        if (captchaToken == null) {
            showError("Please verify the CAPTCHA first.");
            return;
        }

        setLoading(true);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("captchaToken", captchaToken);

        ApiService api = ApiClient.getApiService(this);
        api.login(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    SessionManager.getInstance(LoginActivity.this).saveSession(loginResponse.token, loginResponse.user);
                    routeToDashboard(loginResponse.user.role);
                } else {
                    showError(ApiError.from(response));
                    captchaToken = null;
                    loginButton.setEnabled(false);
                    captchaStatusText.setText("CAPTCHA not verified yet");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void routeToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case "faculty":
                intent = new Intent(this, FacultyDashboardActivity.class);
                break;
            default:
                intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        loadingBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading && captchaToken != null);
    }
}
