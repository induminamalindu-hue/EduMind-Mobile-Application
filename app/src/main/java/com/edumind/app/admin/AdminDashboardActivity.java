package com.edumind.app.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.edumind.app.R;
import com.edumind.app.auth.LoginActivity;
import com.edumind.app.common.DashboardMenuHelper;
import com.edumind.app.common.JsonListActivity;
import com.edumind.app.network.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.admin));
        toolbar.setTitle("Admin — " + SessionManager.getInstance(this).getName());
        toolbar.setSubtitle("EduMind");
        setSupportActionBar(toolbar);

        LinearLayout container = findViewById(R.id.menuContainer);
        int accent = getResources().getColor(R.color.admin);

        DashboardMenuHelper.addSectionHeader(this, container, "Overview & Reports");
        DashboardMenuHelper.addMenuItem(this, container, "Attendance Report", "Attendance % by student across the college", accent, () ->
                JsonListActivity.Builder.from(this, "admin/reports/attendance")
                        .title("Attendance Report")
                        .titleKeys("name").subtitleKeys("student_no", "branchName")
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Performance Report", "Average marks by subject", accent, () ->
                JsonListActivity.Builder.from(this, "admin/reports/performance")
                        .title("Performance Report")
                        .titleKeys("subjectName").subtitleKeys("code")
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Activity Logs", "Every login and admin action", accent, () ->
                JsonListActivity.Builder.from(this, "admin/logs")
                        .title("Activity Logs")
                        .titleKeys("action").subtitleKeys("userName", "created_at")
                        .start(this));

        DashboardMenuHelper.addSectionHeader(this, container, "People");
        DashboardMenuHelper.addMenuItem(this, container, "Students", "Manage student accounts", accent, () ->
                JsonListActivity.Builder.from(this, "admin/students")
                        .title("Students")
                        .titleKeys("name").subtitleKeys("student_no", "email")
                        .createFields(
                                new String[]{"name", "email", "password", "phone", "studentNo", "branchId", "semester", "enrollmentDate"},
                                new String[]{"Full name", "Email", "Temp. password", "Phone", "Student No.", "Branch ID", "Semester", "Enrollment date (YYYY-MM-DD)"})
                        .allowDelete(true)
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Faculty", "Manage faculty accounts", accent, () ->
                JsonListActivity.Builder.from(this, "admin/faculty")
                        .title("Faculty")
                        .titleKeys("name").subtitleKeys("faculty_no", "designation")
                        .createFields(
                                new String[]{"name", "email", "password", "phone", "facultyNo", "designation", "branchId"},
                                new String[]{"Full name", "Email", "Temp. password", "Phone", "Faculty No.", "Designation", "Branch ID"})
                        .allowDelete(true)
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "User Approvals", "Approve or disable accounts", accent, () ->
                startActivity(new Intent(this, AdminUsersActivity.class)));

        DashboardMenuHelper.addSectionHeader(this, container, "Academics");
        DashboardMenuHelper.addMenuItem(this, container, "Branches", "Departments / branches", accent, () ->
                JsonListActivity.Builder.from(this, "admin/branches")
                        .title("Branches")
                        .titleKeys("name").subtitleKeys("code")
                        .createFields(new String[]{"name", "code", "description"}, new String[]{"Name", "Code", "Description"})
                        .allowDelete(true)
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Subjects", "Courses per branch", accent, () ->
                JsonListActivity.Builder.from(this, "admin/subjects")
                        .title("Subjects")
                        .titleKeys("name").subtitleKeys("code", "branchName")
                        .createFields(
                                new String[]{"name", "code", "credits", "semester", "branchId", "facultyId"},
                                new String[]{"Name", "Code", "Credits", "Semester", "Branch ID", "Faculty ID"})
                        .allowDelete(true)
                        .start(this));
        DashboardMenuHelper.addMenuItem(this, container, "Exams", "Exam schedule", accent, () ->
                JsonListActivity.Builder.from(this, "admin/exams")
                        .title("Exams")
                        .titleKeys("subjectName").subtitleKeys("exam_date", "venue")
                        .createFields(
                                new String[]{"subjectId", "examDate", "startTime", "durationMinutes", "venue"},
                                new String[]{"Subject ID", "Exam date (YYYY-MM-DD)", "Start time (HH:MM)", "Duration (minutes)", "Venue"})
                        .allowDelete(true)
                        .start(this));

        DashboardMenuHelper.addSectionHeader(this, container, "Communication");
        DashboardMenuHelper.addMenuItem(this, container, "Notices", "College-wide announcements", accent, () ->
                JsonListActivity.Builder.from(this, "admin/notices")
                        .title("Notices")
                        .titleKeys("title").subtitleKeys("audience", "postedBy")
                        .createFields(new String[]{"title", "content", "audience"}, new String[]{"Title", "Content", "Audience (all/student/faculty)"})
                        .allowDelete(true)
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
