# Rehab Insight

This is a guide from beginning to end of installing android studio and pulling the latest version
of the Rehab Insight app for personal testing a feedback.

---

## What you need before you start

1. A Windows, Mac, or Linux computer.
2. An internet connection (for downloading Android Studio and the project).
3. About 15-20 GB of free disk space (Android Studio + the Android emulator take up space).

---

## Step 1 — Install Android Studio

1. Go to [developer.android.com/studio](https://developer.android.com/studio) in your web browser.
2. Click the big **Download Android Studio** button and accept the terms.
3. Once it's downloaded, run the installer and click **Next** through the setup wizard,
   keeping all the default options selected.
4. When Android Studio opens for the first time, it may run a **Setup Wizard** that downloads
   the Android SDK. Let this finish — it can take a few minutes.

---

## Step 2 — Get the project from GitHub

You do **not** need to know Git commands. Android Studio can pull the project for you.

1. Open Android Studio.
2. On the **Welcome to Android Studio** screen, click **Get from VCS** (VCS = "Version Control
   System"). If you don't see a Welcome screen (e.g. a project is already open), go to the top
   menu: **File → New → Project from Version Control...**
3. In the dialog that appears:
   - **Version control**: make sure it's set to `Git`.
   - **URL**: paste in
     ```
     https://github.com/Gr1m0305/RehabInsightApp.git
     ```
   - **Directory**: choose (or leave the default) folder on your computer where the project
     will be downloaded to.
4. Click **Clone**.
5. If Android Studio asks whether to trust the project or open it, click **Trust Project** /
   **Yes**.

Android Studio will now open the project and start "syncing" it — you'll see a progress bar
at the bottom of the window (this downloads all the pieces the app needs to build). This can
take several minutes the first time. **Wait until it finishes** before moving on.

> If you ever see a banner asking to install/update the Kotlin plugin, Android Gradle Plugin,
> or SDK components, click the **Install** / **Update** / **Accept** button it suggests — that's
> normal and expected on first open.

---

## Step 3 — Use the built-in emulator

1. In Android Studio, click the **Device Manager** icon in the top-right toolbar (it looks like
   a phone with a small arrow), or go to **Tools → Device Manager**.
2. Click **Create device** (a `+` icon).
3. Pick any phone from the list (e.g. "Pixel 4") and click **Next**.
4. Pick a system image (a version of Android) — if it's not downloaded yet, click the **Download**
   link next to it first, then **Next**.
5. Click **Finish**. Your new virtual device will now appear in the Device Manager list and in
   the device dropdown at the top of the main Android Studio window.

---

## Step 4 — Run the app

1. At the top of the Android Studio window, make sure the dropdown next to the green ▶️ Run
   button shows **app** as the run configuration, and your emulator/phone is selected as the
   target device.
2. Click the green ▶️ **Run** button (or press `Shift + F10`).
3. Android Studio will build the app (this can take a minute or two the first time) and then
   automatically install and launch it on your chosen device/emulator.

That's it — the app should now be running! 🎉

---

## Using the app once it's running

- **First launch**: you'll land on the **Login / Sign up** screen. Tap **Create account** and
  fill in the sign-up form to create a new client account.
- After signing up, you'll go through the **Setup Questionnaire** (a short series of questions
  about sleep, energy, mood, pain, and goals), which generates your personalised daily checklist.
- From there you land on the **Home** screen with bottom navigation for **Home**, **Tasks**,
  **Library**, and **Profile**.
- **Admin access**: on the Login screen, tap **Admin login**. Use the passcode:
  ```
  admin123
  ```
  This opens the read-only Admin Dashboard, which shows mock client progress data.

---

## Troubleshooting

- **"Gradle sync failed" or red error banners**: click the **Sync Project with Gradle Files**
  button (an elephant icon with a small refresh arrow) in the toolbar, then wait for it to finish.
  Make sure you have an internet connection, since the first sync downloads dependencies.
- **No devices in the dropdown**: go back to Step 3 and make sure you've either created a
  virtual device or plugged in and authorised a real phone.
- **Build errors after cloning**: try **File → Invalidate Caches / Restart...** and choose
  **Invalidate and Restart**, then let Gradle sync again.
- **App crashes immediately**: make sure you selected a system image with **Android 8.0 (API 26)**
  or higher when creating your virtual device.
