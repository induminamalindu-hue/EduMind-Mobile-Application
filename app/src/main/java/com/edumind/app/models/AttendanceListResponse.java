package com.edumind.app.models;

import java.util.List;

public class AttendanceListResponse {
    public List<AttendanceRow> students;
    public boolean editable;
    public int editWindowDays;
}
