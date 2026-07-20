# EduMind — Android (Java)

A native Android client for your existing EduMind backend, written in Java, ready to open directly in Android Studio.

## 1. Open it
Android Studio → **Open** → select the `EduMind` folder (this one, the one containing `settings.gradle`) → let Gradle sync.

Requires: Android Studio Hedgehog+ (AGP 8.2), JDK 17 (bundled with recent Android Studio), Android SDK 34.

## 2. Point it at your backend
Your Express backend must be running (`npm start` in your `backend/` folder) **before** you run the app.

Edit `app/build.gradle`, in `defaultConfig`:

```groovy
buildConfigField "String", "API_BASE_URL", "\"http://10.0.2.2:5000/api/\""
buildConfigField "String", "UPLOADS_BASE_URL", "\"http://10.0.2.2:5000/\""
buildConfigField "String", "RECAPTCHA_SITE_KEY", "\"YOUR_RECAPTCHA_SITE_KEY\""
```

- **`10.0.2.2`** is a special address that only works from the **Android Emulator** — it means "my computer's localhost." If you're using the emulator, leave this as-is.
- **Testing on a real phone?** Your phone and computer must be on the same Wi-Fi. Find your computer's LAN IP (Windows: `ipconfig`, look for IPv4 Address, e.g. `192.168.1.20`) and use `http://192.168.1.20:5000/api/` instead. `10.0.2.2` will NOT work on a real device.
- **`RECAPTCHA_SITE_KEY`**: same reCAPTCHA v2 site key you're using in the web app's `.env`.
- Also add `localhost` as an allowed domain in your [reCAPTCHA admin console](https://www.google.com/recaptcha/admin) — the in-app CAPTCHA WebView presents itself as `https://localhost`, so that domain must be registered (see `RecaptchaActivity.java` for why).

After editing, click the elephant/sync icon or **File → Sync Project with Gradle Files**.

## 3. Run it
Pick a device (emulator or a phone with USB debugging on) → click ▶ Run.

## What's implemented (all three roles work against your real backend, no mock data)

| Role | Screens |
|---|---|
| **Auth** | Login, Register, OTP verification (no CAPTCHA on this step, matching the backend), real Google reCAPTCHA v2 checkbox via WebView |
| **Admin** | Dashboard hub, Students (list/create/delete), Faculty (list/create/delete), Branches, Subjects, Exams, Notices, User Approvals (approve/disable), Activity Logs, Attendance Report, Performance Report |
| **Faculty** | Dashboard hub, My Subjects, Take Attendance (manual roster **and** QR generation with live countdown), Attendance Report (read-only history %), Marks entry, Materials (upload with file picker, list, delete), My Notes, Profile |
| **Student** | Dashboard hub, Scan Attendance QR (device camera via ZXing), Attendance % by subject, Attendance history, Schedule, Upcoming Exams, Marks, Materials (view/open files), Notices, Notifications (tap to mark read), My Notes, Profile |

## How it's built (read this before extending it)

To cover this much surface area in Java without an enormous number of hand-written screens, most **list-style** modules (Admin's Students/Faculty/Branches/Subjects/Exams/Notices, Faculty's Notes, Student's Materials/Notices/Notes/Marks/Schedule/Attendance-history) are powered by one reusable, configurable screen: **`common/JsonListActivity.java`**. It fetches whatever JSON array an endpoint returns, renders it as a list (via `GenericJsonAdapter`), and — if configured — lets you create or delete rows, all through a small `Builder` API. Look at `AdminDashboardActivity.java` or `StudentDashboardActivity.java` for examples of configuring one.

Modules with genuinely custom interaction (not just list/create/delete) got dedicated, hand-written Activities instead:
- **Attendance** (QR generation with countdown, manual roster with per-student status buttons, edit-window lock) — `faculty/FacultyAttendanceActivity.java`
- **QR scanning** — `student/ScanAttendanceActivity.java` (ZXing embedded scanner)
- **Marks entry** (editable per-student rows) — `faculty/FacultyMarksActivity.java`
- **Materials upload** (file picker + multipart) — `faculty/FacultyMaterialsActivity.java`
- **User approvals** (approve/disable action) — `admin/AdminUsersActivity.java`
- **Notifications** (mark-as-read action) — `student/StudentNotificationsActivity.java`

## Honest limitations (worth knowing before a viva or a demo)

- **This was written without ever compiling it** — there's no Android SDK/emulator in the environment I built it in. I checked every layout/Java file for matching IDs, balanced braces, and correct imports by hand, but Android Studio may still flag small things on first build (a missing import, a Gradle version mismatch, etc.) — these are normally one-line fixes. Please build it and tell me the exact error if something doesn't compile; I'll fix it immediately.
- **Generic list screens show raw field names** (e.g. `internalMarks: 78`) in the "Details" popup rather than a custom-formatted layout — functional, not pretty. Tell me which ones matter most and I'll give them bespoke layouts.
- **No offline mode, no push notifications, no app icon design** (the launcher icon is a placeholder vector, not real artwork).
- **Editing existing rows** (e.g. changing a student's phone number) isn't wired up in the generic screens — only create + delete. Let me know which entities need real "edit" and I'll add it.
- **Shared notes** (faculty/student "notes shared with me") aren't shown — only "my notes" — since the backend returns them as a second array (`shared`) that the generic list doesn't display two lists at once.

## Testing the full attendance loop on one machine
Same idea as testing the web app: log in as faculty on one device/emulator, generate the QR, and either point a second device's camera at the QR image on screen, or grab the `scanUrl` from Logcat (HTTP logging is on in debug builds) and open it as a deep link via adb:
```
adb shell am start -a android.intent.action.VIEW -d "http://10.0.2.2:5000/attendance/scan/TOKEN_HERE"
```
(Note: the deep-link URL flow itself isn't wired to open directly in-app the way the web app's `/attendance/scan/:token` route is — on Android, scanning always goes through `ScanAttendanceActivity`, which calls the same `/attendance/mark` endpoint directly. Functionally equivalent, just always in-app rather than via a browser deep link.)
