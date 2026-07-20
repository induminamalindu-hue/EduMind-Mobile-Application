package com.edumind.app.student;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.edumind.app.R;
import com.edumind.app.common.ApiError;
import com.edumind.app.models.MarkAttendanceResponse;
import com.edumind.app.network.ApiClient;
import com.edumind.app.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Uses the ZXing embedded scanner (com.journeyapps:zxing-android-embedded) to
 * read the QR code shown on the faculty's screen with the device camera —
 * exactly like scanning it with a phone's native camera app, but staying
 * inside the app.
 */
public class ScanAttendanceActivity extends AppCompatActivity {

    private ApiService api;
    private TextView resultText;

    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> scanLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    handleScanned(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_attendance);
        api = ApiClient.getApiService(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        resultText = findViewById(R.id.resultText);
        MaterialButton scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> launchScanner());
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Point your camera at the attendance QR code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        scanLauncher.launch(options);
    }

    /** Accepts either a bare token or a full https://host/attendance/scan/{token} URL. */
    private String extractToken(String scanned) {
        Pattern pattern = Pattern.compile("/attendance/scan/([^/?#]+)");
        Matcher matcher = pattern.matcher(scanned);
        if (matcher.find()) return matcher.group(1);
        return scanned.trim();
    }

    private void handleScanned(String scannedText) {
        String token = extractToken(scannedText);
        resultText.setText("Marking attendance…");

        api.markAttendance(java.util.Collections.singletonMap("token", token)).enqueue(new Callback<MarkAttendanceResponse>() {
            @Override
            public void onResponse(Call<MarkAttendanceResponse> call, Response<MarkAttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MarkAttendanceResponse body = response.body();
                    resultText.setText("✓ " + body.message + "\n" + body.subjectCode + " — " + body.subjectName);
                } else {
                    resultText.setText("✗ " + ApiError.from(response));
                    Toast.makeText(ScanAttendanceActivity.this, ApiError.from(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MarkAttendanceResponse> call, Throwable t) {
                resultText.setText("Network error: " + t.getMessage());
            }
        });
    }
}
