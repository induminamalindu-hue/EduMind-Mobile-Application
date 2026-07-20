package com.edumind.app.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.edumind.app.admin.AdminDashboardActivity;
import com.edumind.app.faculty.FacultyDashboardActivity;
import com.edumind.app.network.SessionManager;
import com.edumind.app.student.StudentDashboardActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = SessionManager.getInstance(this);
        Intent intent;
        if (!session.isLoggedIn()) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            String role = session.getRole();
            if ("admin".equals(role)) intent = new Intent(this, AdminDashboardActivity.class);
            else if ("faculty".equals(role)) intent = new Intent(this, FacultyDashboardActivity.class);
            else intent = new Intent(this, StudentDashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
