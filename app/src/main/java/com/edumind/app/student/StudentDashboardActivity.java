package com.edumind.app.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.edumind.app.BuildConfig;
import com.edumind.app.R;
import com.edumind.app.auth.LoginActivity;
import com.edumind.app.common.DashboardMenuHelper;
import com.edumind.app.common.JsonListActivity;
import com.edumind.app.network.SessionManager;

public class StudentDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.student));
        toolbar.setTitle("Student — " + SessionManager.getInstance(this).getName());
        toolbar.setSubtitle("EduMind");
        setSupportActionBar(toolbar);

        LinearLayout container = findViewById(R.id.menuContainer);
        int accent = getResources().getColor(R.color.student);

        DashboardMenuHelper.addSectionHeader(this, container, "Attendance");
        DashboardMenuHelper.addMenuItem(this, container, "Scan Attendance QR", "Use your camera to mark attendance", accent, () ->
                startActivity(new Intent(this, ScanAttendanceActivity.class)));
        DashboardMenuHelper.addMenuItem(this, container, "Attendance % by Subject", "Your attendance percentage per subject", accent, () ->
                JsonListActivity.Builder.from(this, "student/attendance")
                        .title("Attendance % by Subject")
                        .arrayKey("bySubject")
                        .titleKeys("subjectName").subtitleKeys("code", "percentage", "attended")
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Attendance History", "Your last 50 attendance records", accent, () ->
                JsonListActivity.Builder.from(this, "student/attendance")
                        .title("Attendance History")
                        .arrayKey("records")
                        .titleKeys("subjectName").subtitleKeys("date", "status")
                        .start(this));

        DashboardMenuHelper.addSectionHeader(this, container, "Academics");
        DashboardMenuHelper.addMenuItem(this, container, "Schedule", "Your subjects", accent, () ->
                JsonListActivity.Builder.from(this, "student/schedule")
                        .title("Schedule")
                        .arrayKey("subjects")
                        .titleKeys("name").subtitleKeys("code")
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Upcoming Exams", "Exam dates & venues", accent, () ->
                JsonListActivity.Builder.from(this, "student/schedule")
                        .title("Upcoming Exams")
                        .arrayKey("exams")
                        .titleKeys("subjectName").subtitleKeys("exam_date", "venue")
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Marks", "Internal & exam marks", accent, () ->
                JsonListActivity.Builder.from(this, "student/marks")
                        .title("My Marks")
                        .titleKeys("subjectName", "name").subtitleKeys("internalMarks", "examMarks")
                        .start(this));

        DashboardMenuHelper.addSectionHeader(this, container, "Content");
        DashboardMenuHelper.addMenuItem(this, container, "Materials", "Study materials from your faculty", accent, () ->
                JsonListActivity.Builder.from(this, "student/materials")
                        .title("Materials")
                        .titleKeys("title").subtitleKeys("subjectName", "file_name")
                        .fileLink("file_path", BuildConfig.UPLOADS_BASE_URL + "uploads/")
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Notices", "College announcements", accent, () ->
                JsonListActivity.Builder.from(this, "student/notices")
                        .title("Notices")
                        .titleKeys("title").subtitleKeys("audience", "created_at")
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Notifications", "Personal alerts", accent, () ->
                startActivity(new Intent(this, StudentNotificationsActivity.class)));
        DashboardMenuHelper.addMenuItem(this, container, "My Notes", "Personal notes", accent, () ->
                JsonListActivity.Builder.from(this, "student/notes")
                        .title("My Notes")
                        .arrayKey("own")
                        .titleKeys("title").subtitleKeys("created_at")
                        .createFields(new String[]{"title", "content"}, new String[]{"Title", "Content"})
                        .allowDelete(true)
                        .start(this));

        DashboardMenuHelper.addSectionHeader(this, container, "Account");
        DashboardMenuHelper.addMenuItem(this, container, "Profile", "View / update your profile", accent, () ->
                JsonListActivity.Builder.from(this, "student/profile")
                        .title("My Profile")
                        .titleKeys("name").subtitleKeys("email", "student_no")
                        .start(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Logout");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ("Logout".contentEquals(item.getTitle())) {
            SessionManager.getInstance(this).clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
