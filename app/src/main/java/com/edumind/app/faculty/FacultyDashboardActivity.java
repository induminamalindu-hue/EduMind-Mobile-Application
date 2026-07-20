package com.edumind.app.faculty;

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

public class FacultyDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.faculty));
        toolbar.setTitle("Faculty — " + SessionManager.getInstance(this).getName());
        toolbar.setSubtitle("EduMind");
        setSupportActionBar(toolbar);

        LinearLayout container = findViewById(R.id.menuContainer);
        int accent = getResources().getColor(R.color.faculty);

        DashboardMenuHelper.addSectionHeader(this, container, "Attendance");
        DashboardMenuHelper.addMenuItem(this, container, "Take Attendance", "Manual roster or generate a QR code", accent, () ->
                startActivity(new Intent(this, FacultyAttendanceActivity.class)));
        DashboardMenuHelper.addMenuItem(this, container, "Attendance Report", "Read-only history & percentages", accent, () ->
                startActivity(new Intent(this, FacultyAttendanceReportActivity.class)));

        DashboardMenuHelper.addSectionHeader(this, container, "Academics");
        DashboardMenuHelper.addMenuItem(this, container, "My Subjects", "Subjects you teach", accent, () ->
                JsonListActivity.Builder.from(this, "faculty/subjects")
                        .title("My Subjects")
                        .titleKeys("name").subtitleKeys("code", "branchName")
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Marks", "Enter internal & exam marks", accent, () ->
                startActivity(new Intent(this, FacultyMarksActivity.class)));
        DashboardMenuHelper.addMenuItem(this, container, "Search Students", "Find any student by name/no.", accent, () ->
                JsonListActivity.Builder.from(this, "faculty/students")
                        .title("Search Students")
                        .titleKeys("name").subtitleKeys("student_no", "email")
                        .start(this));

        DashboardMenuHelper.addSectionHeader(this, container, "Content");
        DashboardMenuHelper.addMenuItem(this, container, "Materials", "Upload & manage study materials", accent, () ->
                startActivity(new Intent(this, FacultyMaterialsActivity.class)));
        DashboardMenuHelper.addMenuItem(this, container, "My Notes", "Personal or shared notes", accent, () ->
                JsonListActivity.Builder.from(this, "faculty/notes")
                        .title("My Notes")
                        .titleKeys("title").subtitleKeys("created_at")
                        .createFields(new String[]{"title", "content"}, new String[]{"Title", "Content"})
                        .allowDelete(true)
                        .start(this));

        DashboardMenuHelper.addSectionHeader(this, container, "Account");
        DashboardMenuHelper.addMenuItem(this, container, "Profile", "View / update your profile", accent, () ->
                JsonListActivity.Builder.from(this, "faculty/profile")
                        .title("My Profile")
                        .arrayKey("__self__")
                        .titleKeys("name").subtitleKeys("email", "designation")
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
