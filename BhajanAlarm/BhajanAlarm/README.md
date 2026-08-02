# Bhajan Alarm — Build & Install Guide

This folder is a complete Android app project (the source code) for the
"Bhajan Alarm" app you asked for. It automatically plays a chosen MP3 file
(Bhajan/Aarti) on the days and time you pick, and switches off by itself
when the song finishes. Interface has a Hindi/Gujarati toggle. Manual
Play / Pause / Stop controls are also included.

**I cannot generate the final .apk file directly in this chat** — turning
this source code into an installable .apk requires Android's official build
tools (Android Studio), which only run on a regular computer. Below are the
two ways to do that. Option A is easiest if you (or anyone you know) has a
Windows/Mac/Linux computer.

---

## Option A — Build with Android Studio (recommended, ~10–15 minutes)

1. On a computer, download and install **Android Studio** (free):
   https://developer.android.com/studio
2. Open Android Studio → **Open** → select this `BhajanAlarm` folder.
3. Let it "Sync" (first time it downloads some files — needs internet,
   takes a few minutes). If it asks to create a Gradle wrapper, click Yes/OK.
4. Once sync finishes, go to the menu: **Build → Build Bundle(s)/APK(s) → Build APK(s)**.
5. When it finishes, click the **"locate"** link in the notification, or find
   the file at:
   `BhajanAlarm/app/build/outputs/apk/debug/app-debug.apk`
6. Copy that `app-debug.apk` file to your grandparents' Motorola G05
   (via USB cable, WhatsApp to yourself, Google Drive, or email).

## Option B — No computer available (free online build service)

If you don't have access to a computer, you can use a free CI build service
like **GitHub Actions**:
1. Create a free GitHub account (github.com) and a new repository.
2. Upload this entire `BhajanAlarm` folder to that repository.
3. Add a simple GitHub Actions workflow file (search "build android apk
   github actions" for a ready-made template) — it will compile the APK in
   the cloud automatically on every upload.
4. Download the resulting `.apk` from the Actions run's "Artifacts" section.

(If you'd like, I can also generate this GitHub Actions workflow file for
you — just ask.)

---

## Installing the APK on the Motorola G05

1. Copy the `app-debug.apk` file onto the phone (USB transfer, Drive, email,
   or WhatsApp — send it to yourself and download it on the phone).
2. On the phone, open the **Files** app and tap the `app-debug.apk` file.
3. Android will ask permission to "install unknown apps" for that app
   (e.g. Files/Chrome/Gmail) — tap **Settings**, then turn on
   **"Allow from this source"**, then go back and tap **Install**.
4. Open the app once installed. It will:
   - Ask for **notification permission** — allow it (needed to show
     "now playing" controls).
   - Possibly show a popup asking to allow **"Alarms & reminders"** —
     tap **"Open settings"** and turn it on. This is required so the
     bhajan plays exactly on time, even if the phone is idle/locked.

## Using the app

1. Tap **"+ नया शेड्यूल जोड़ें / + નવું શેડ્યુલ ઉમેરો"** (Add New Schedule).
2. Tap **"Choose song from file"** and pick the MP3 (Bhajan/Aarti) from the
   phone's storage.
3. Tap the days it should play on (e.g. Tuesday, Saturday).
4. Tap the time button and set **06:00 AM**.
5. Tap **Save**.

The song will now play automatically every selected day at that time, and
switch off on its own once finished. You can also add more schedules for
other songs/times, turn any schedule on/off with the switch, or edit/delete
it with the pencil/trash icons.

To toggle the interface language, use the **हिंदी / ગુજરાતી** buttons in
the top-right corner of the home screen.

---

## Notes

- The app works completely offline — no internet or account needed after
  installation.
- Keep the MP3 file on the phone in a stable location (Downloads/Music
  folder); if it's later deleted or moved, the schedule using it won't
  have anything to play.
- Battery optimization: on some phones, Android may try to restrict
  background apps to save battery. If a scheduled alarm ever doesn't fire,
  go to Phone Settings → Apps → Bhajan Alarm → Battery, and set it to
  **"Unrestricted"** / **"No restrictions"**.
