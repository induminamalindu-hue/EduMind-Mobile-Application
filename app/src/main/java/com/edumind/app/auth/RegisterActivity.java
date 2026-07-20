package com.edumind.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private Spinner roleSpinner;
    private TextView errorText, captchaStatusText;
    private MaterialButton verifyCaptchaButton, registerButton;
    private ProgressBar loadingBar;
    private String captchaToken = null;

    private final ActivityResultLauncher<Intent> recaptchaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    captchaToken = result.getData().getStringExtra(RecaptchaActivity.EXTRA_TOKEN);
                    captchaStatusText.setText("✓ CAPTCHA verified");
                    registerButton.setEnabled(true);
                } else {
                    captchaToken = null;
                    registerButton.setEnabled(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        roleSpinner = findViewById(R.id.roleSpinner);
        errorText = findViewById(R.id.errorText);
        captchaStatusText = findViewById(R.id.captchaStatusText);
        verifyCaptchaButton = findViewById(R.id.verifyCaptchaButton);
        registerButton = findViewById(R.id.registerButton);
        loadingBar = findViewById(R.id.loadingBar);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"student", "faculty"});
        roleSpinner.setAdapter(adapter);

        verifyCaptchaButton.setOnClickListener(v -> recaptchaLauncher.launch(new Intent(this, RecaptchaActivity.class)));
        registerButton.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.loginLink).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = String.valueOf(nameInput.getText()).trim();
        String email = String.valueOf(emailInput.getText()).trim();
        String password = String.valueOf(passwordInput.getText());
        String confirmPassword = String.valueOf(confirmPasswordInput.getText());
        String role = String.valueOf(roleSpinner.getSelectedItem());
        errorText.setVisibility(View.GONE);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError("Please fill in all fields.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }
        if (captchaToken == null) {
            showError("Please verify the CAPTCHA first.");
            return;
        }

        setLoading(true);
        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        body.put("role", role);
        body.put("captchaToken", captchaToken);

        ApiService api = ApiClient.getApiService(this);
        api.register(body).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Intent intent = new Intent(RegisterActivity.this, OtpVerifyActivity.class);
                    intent.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email);
                    startActivity(intent);
                    finish();
                } else {
                    showError(ApiError.from(response));
                    captchaToken = null;
                    registerButton.setEnabled(false);
                    captchaStatusText.setText("CAPTCHA not verified yet");
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
        registerButton.setEnabled(!loading && captchaToken != null);
    }
}
