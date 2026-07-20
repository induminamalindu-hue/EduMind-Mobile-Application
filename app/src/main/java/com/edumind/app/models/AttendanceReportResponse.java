package com.edumind.app.models;

import java.util.List;

public class AttendanceReportResponse {
    public String subjectName;
    public String subjectCode;
    public String from;
    public String to;
    public int totalSessions;
    public List<String> sessionDates;
    public List<Row> students;

    public static class Row {
        public int studentId;
        public String student_no;
        public String name;
        public int present;
        public int late;
        public int absent;
        public int totalSessions;
        public Double percentage; // null if no sessions yet
    }
}
