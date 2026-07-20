package com.edumind.app.models;

public class AttendanceSessionResponse {
    public int sessionId;
    public String subjectName;
    public String subjectCode;
    public String qrToken;
    public String scanUrl;
    public String qrImage; // base64 data URL (image/png)
    public String expiresAt; // ISO datetime string
    public int durationMinutes;
}
