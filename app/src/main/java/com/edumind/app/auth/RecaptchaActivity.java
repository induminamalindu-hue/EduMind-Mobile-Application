package com.edumind.app.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.edumind.app.R;

public class RecaptchaActivity extends AppCompatActivity {

    public static final String EXTRA_TOKEN = "captcha_token";
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recaptcha);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // 🔥 CRITICAL FIX (WebView blocking issue)
        settings.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 Chrome/88.0.4324.93 Mobile Safari/537.36"
        );

        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.addJavascriptInterface(new RecaptchaBridge(), "Android");

        // load captcha page
        webView.loadUrl("https://sasindusachintha.github.io/recaptcha-app/");
    }

    private class RecaptchaBridge {

        @JavascriptInterface
        public void onToken(String token) {
            runOnUiThread(() -> {
                Intent result = new Intent();
                result.putExtra(EXTRA_TOKEN, token);
                setResult(RESULT_OK, result);
                finish();
            });
        }

        @JavascriptInterface
        public void onExpired() {
            runOnUiThread(() ->
                    Toast.makeText(RecaptchaActivity.this, "CAPTCHA expired", Toast.LENGTH_SHORT).show()
            );
        }

        @JavascriptInterface
        public void onError() {
            runOnUiThread(() ->
                    Toast.makeText(RecaptchaActivity.this, "CAPTCHA failed", Toast.LENGTH_SHORT).show()
            );
        }
    }
}