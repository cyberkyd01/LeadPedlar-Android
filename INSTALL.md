# Install LeadPedlar for Android

This guide gets the LeadPedlar Android app onto a phone. You can install a built APK, or build the
APK yourself from this repository. It also covers the smart calling system: which calling apps it
uses, the permissions involved, and what the app needs from the LeadPedlar backend.

## At a glance

| | |
|---|---|
| App | **LeadPedlar**, version `1.0` (versionCode 1) |
| Package name | `com.example.leadpedlar`. Debug and release builds share this name, so only one can be installed at a time |
| Phone requirement | Android **7.0** (API 24) or newer. Targets Android 16 (API 36). Needs an up-to-date *Android System WebView* (or Chrome) |
| Prebuilt APK | None. This repository has no GitHub Releases, so you build the APK yourself ([step 2](#2-build-the-apk-from-source)) |
| Build tools | JDK **17** (21 also works), Android SDK **Platform 36** + **Build-Tools 36.0.0**. Gradle **9.1.0** downloads itself through the wrapper. Android Gradle Plugin 9.0.1, Kotlin 2.3.20, Jetpack Compose |
| Build output | `app/build/outputs/apk/debug/app-debug.apk` (installable) and `app/build/outputs/apk/release/app-release-unsigned.apk` (**must be signed before it can be installed**, see [2.5](#25-sign-the-release-apk)) |
| Backend | The LeadPedlar web app at **`https://www.leadpedlar.xyz`**. This address is built into the app. The app is a native shell around the web app (see [What the app needs from the backend](#what-the-app-needs-from-the-backend)) |
| Runtime permission prompts | **None.** The app never asks for any permission at runtime (see [Permissions](#permissions-and-why-the-app-needs-them)) |
| Background activity | None. It runs no services or boot receivers, so it needs **no battery-optimisation exemption** |

## Requirements

- An Android phone with Android 7.0 or newer and internet access.
- A **LeadPedlar account** on `www.leadpedlar.xyz`. You sign in on the site's own login page inside the app.
- Optional: the calling apps you want to use, such as WhatsApp, WhatsApp Business, Telegram, Skype, Truecaller, Viber, Zoiper or any SIP softphone. The phone's own dialer always works.
- To build the APK: a Windows, macOS or Linux computer with a JDK and the Android SDK ([step 2](#2-build-the-apk-from-source)).
- To install over USB: **adb** ([step 1, option B](#option-b-install-with-adb-from-a-computer)).

## 1. Install a built APK

Use `app-debug.apk`, or your **signed** release APK (`leadpedlar-release.apk` from [2.5](#25-sign-the-release-apk)).
`app-release-unsigned.apk` **can't** be installed. Android rejects it with *App not installed* or
`INSTALL_PARSE_FAILED_NO_CERTIFICATES`.

### Option A: Sideload on the phone

1. Copy the APK to the phone (USB, cloud drive, email to yourself, etc.).
2. Open it from the **Files** app (or Chrome's downloads). The first time, Android says that, for your security, it isn't allowed to install unknown apps from this source. Tap **Settings** and switch on **Allow from this source** for that app, then go back.
   On Android 7.x this is a single switch instead: *Settings › Security › Unknown sources*. On Android 8+ you can also find it at *Settings › Apps › Special app access › Install unknown apps*. The exact path varies by manufacturer.
3. Tap **Install**. If Google Play Protect warns about an app from an unknown developer, tap **More details › Install anyway**.
4. Optional: switch **Allow from this source** (or *Unknown sources*) off again afterwards.

### Option B: Install with adb from a computer

1. On the phone, enable USB debugging:
   1. Go to *Settings › About phone* and tap **Build number** seven times.
   2. Go to *Settings › System › Developer options* and switch on **USB debugging**.
2. On the computer, install Google's platform-tools (adb). Unpack the zip and run `adb` from that folder, or add it to PATH:
   - Windows: <https://dl.google.com/android/repository/platform-tools-latest-windows.zip>
   - macOS: <https://dl.google.com/android/repository/platform-tools-latest-darwin.zip> (or `brew install --cask android-platform-tools`)
   - Linux: <https://dl.google.com/android/repository/platform-tools-latest-linux.zip>
   If you installed Android Studio for step 2, adb is already in `<Android SDK>/platform-tools`.
3. Connect the phone by USB, accept **Allow USB debugging?** on the phone, then check the connection:

   ```bash
   adb devices
   ```

   You should see your device's serial number followed by `device`.

4. Install the APK. `-r` replaces an existing install and keeps its data.

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

   You should see `Success`. To confirm:

   ```bash
   adb shell "dumpsys package com.example.leadpedlar | grep versionName"
   ```

   This prints `versionName=1.0`.

These adb commands work the same in PowerShell on Windows. Use `.\adb` from the platform-tools folder if it isn't on your PATH.

## 2. Build the APK from source

### 2.1 Install a JDK and the Android SDK

**Option 1: Android Studio (easiest).**

1. Install a current [Android Studio](https://developer.android.com/studio). This project uses Android Gradle Plugin 9.0.1, so older Android Studio versions can't sync it. Command-line builds work regardless.
2. Open *Settings › Languages & Frameworks › Android SDK* (on macOS: *Android Studio › Settings…*):
   - On the **SDK Platforms** tab, tick **Android 16.0 ("Baklava") – API 36**.
   - On the **SDK Tools** tab, tick **Android SDK Build-Tools** (36.0.0).
   - Click **Apply**.
3. Android Studio includes a JDK (version 21), which can run this build. To use it from a terminal, set `JAVA_HOME` to:
   - macOS: `/Applications/Android Studio.app/Contents/jbr/Contents/Home`
   - Windows: `C:\Program Files\Android\Android Studio\jbr`
   - Linux: `<where you unpacked Android Studio>/jbr`

**Option 2: Command line only.**

1. Install JDK 17 (Eclipse Temurin):
   - Windows: `winget install --id EclipseAdoptium.Temurin.17.JDK -e`
   - macOS: `brew install --cask temurin@17`
   - Ubuntu/Debian: `sudo apt-get install -y --no-install-recommends openjdk-17-jdk-headless`
2. Download the **Command line tools only** package from <https://developer.android.com/studio#command-line-tools-only>. Unpack it so that the `bin` folder ends up at `<sdk>/cmdline-tools/latest/bin`. Use one of these SDK folders:
   - Windows: `%LOCALAPPDATA%\Android\Sdk`
   - macOS: `~/Library/Android/sdk`
   - Linux: `~/Android/Sdk`
3. Install the SDK packages and accept the licences.

   macOS/Linux:

   ```bash
   SDK="$HOME/Library/Android/sdk"   # Linux: SDK="$HOME/Android/Sdk"
   "$SDK/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-36" "build-tools;36.0.0"
   "$SDK/cmdline-tools/latest/bin/sdkmanager" --licenses
   ```

   Windows (PowerShell):

   ```powershell
   $SDK = "$env:LOCALAPPDATA\Android\Sdk"
   & "$SDK\cmdline-tools\latest\bin\sdkmanager.bat" "platform-tools" "platforms;android-36" "build-tools;36.0.0"
   & "$SDK\cmdline-tools\latest\bin\sdkmanager.bat" --licenses
   ```

**Check the JDK** (in the terminal you'll build from):

```bash
java -version
```

It must report version **17** or **21**. The Kotlin code is compiled with a Java 17 toolchain
(`jvmToolchain(17)`). If no JDK 17 is installed, Gradle downloads one automatically into
`~/.gradle/jdks` through the Foojay toolchain resolver configured in `settings.gradle.kts`.

### 2.2 Get the code

```bash
git clone https://github.com/cyberkyd01/LeadPedlar-Android.git
```

```bash
cd LeadPedlar-Android
```

### 2.3 Tell Gradle where the SDK is

Android Studio does this automatically when you open the project. On the command line, write the
git-ignored `local.properties` file. Use the line for your system.

macOS:

```bash
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

Linux:

```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

Windows (PowerShell):

```powershell
"sdk.dir=$($env:LOCALAPPDATA -replace '\\','/')/Android/Sdk" | Set-Content -Encoding ascii local.properties
```

As an alternative, set the `ANDROID_HOME` environment variable to the SDK folder.

### 2.4 Build

Use `./gradlew` on macOS/Linux and `.\gradlew.bat` on Windows. The first build downloads Gradle
9.1.0 and the dependencies, which takes a few minutes.

**Debug APK.** It's signed with your computer's debug key and can be installed right away:

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

**Release APK.** It's **unsigned**, because the project has no signing configuration:

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release-unsigned.apk`. Sign it as in 2.5 before installing.

Both builds should end with `BUILD SUCCESSFUL`.

### 2.5 Sign the release APK

Android only installs APKs that are signed, and only installs an **update** if it's signed with the
**same key** as the installed app. Create one release key and keep using it.

1. **Create a keystore once.** Keep it **outside** the repository folder. This repo's `.gitignore`
   doesn't exclude keystore files, so a key inside the folder could be committed by accident.
   `keytool` comes with the JDK; if it isn't on your PATH, use `"$JAVA_HOME/bin/keytool"`. It asks for a password and a name.

   ```bash
   keytool -genkeypair -v -keystore "$HOME/leadpedlar-release.jks" -alias leadpedlar -keyalg RSA -keysize 4096 -validity 10000
   ```

   On Windows PowerShell, use the same command with `-keystore "$env:USERPROFILE\leadpedlar-release.jks"`.

2. **Align and sign** the unsigned APK with the SDK's build-tools. `apksigner` asks for the keystore password.

   macOS/Linux, from the repository folder:

   ```bash
   BT="$HOME/Library/Android/sdk/build-tools/36.0.0"   # Linux: BT="$HOME/Android/Sdk/build-tools/36.0.0"
   OUT=app/build/outputs/apk/release
   "$BT/zipalign" -f -p 4 "$OUT/app-release-unsigned.apk" "$OUT/app-release-aligned.apk"
   "$BT/apksigner" sign --ks "$HOME/leadpedlar-release.jks" --ks-key-alias leadpedlar --out "$OUT/leadpedlar-release.apk" "$OUT/app-release-aligned.apk"
   "$BT/apksigner" verify --print-certs "$OUT/leadpedlar-release.apk"
   ```

   Windows (PowerShell), from the repository folder:

   ```powershell
   $BT  = "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0"
   $OUT = "app\build\outputs\apk\release"
   & "$BT\zipalign.exe" -f -p 4 "$OUT\app-release-unsigned.apk" "$OUT\app-release-aligned.apk"
   & "$BT\apksigner.bat" sign --ks "$env:USERPROFILE\leadpedlar-release.jks" --ks-key-alias leadpedlar --out "$OUT\leadpedlar-release.apk" "$OUT\app-release-aligned.apk"
   & "$BT\apksigner.bat" verify --print-certs "$OUT\leadpedlar-release.apk"
   ```

   The last command prints `Signer #1 certificate DN: CN=…` with the name you entered. Install
   `app/build/outputs/apk/release/leadpedlar-release.apk` as in [step 1](#1-install-a-built-apk).
   You can ignore the `.idsig` file `apksigner` creates next to it.

**Back up `leadpedlar-release.jks` and its password.** If you lose them, every phone has to uninstall
and reinstall to get the next version.

## 3. First run

1. Open **LeadPedlar**. It loads `https://www.leadpedlar.xyz` with a bottom bar: **Home**, **Marketplace**, **My Leads**, **Escrow** and **SIP Dialer**.
2. **Sign in** with your LeadPedlar account on the site's login page, shown inside the app. The session is kept in the app's web view between launches.
3. **Place a call.** Tap a phone number or a call button on a lead. The app opens its **calling app picker**, which marks the apps installed on the phone with *Installed*:

   | Choice | What happens |
   |---|---|
   | Phone Dialer | Opens the phone's dialer with the number filled in. Tap the call button to dial |
   | WhatsApp / WhatsApp Business | Opens the chat for that number in WhatsApp (start the call from there) |
   | Telegram | Opens `t.me/<number>` in Telegram |
   | Skype | `skype:<number>?call` |
   | Truecaller | Opens Truecaller's dialer with the number |
   | Viber | `viber://chat?number=<number>` |
   | Zoiper Softphone | `zoiper:<number>`. Falls back to the phone dialer if Zoiper isn't installed |
   | SIP Client (Eyebeam / Bria) | `sip:<number>` to whichever SIP app handles it. Falls back to the phone dialer |
   | Always Ask (System Chooser) | Android's own *Make call with…* picker |

   If you tick the option to always use the chosen app, the app dials through it directly next time and skips the picker. The web app's dialer settings can reset this to *Always Ask*. Clearing the app's storage also resets it. If you pick an app that isn't installed, LeadPedlar opens its Google Play page.

4. **Nothing else to set up.** There are no permission prompts, no accessibility or overlay settings, no default-dialer role, and no battery settings. The app always hands the call to another app.

### Permissions and why the app needs them

From `app/src/main/AndroidManifest.xml` and the merged manifest of the built APK:

| Permission | Type | Why |
|---|---|---|
| `INTERNET` | Normal, granted automatically | Load the LeadPedlar web app |
| `ACCESS_NETWORK_STATE` | Normal, granted automatically | Check connectivity |
| `CALL_PHONE` | Dangerous (runtime) | Declared, but **never requested and never used**. The app doesn't place calls itself: every call goes through `ACTION_DIAL` or the chosen app, where you press call, and that needs no permission. App info shows *Phone: Not allowed*. Leave it that way |
| `com.example.leadpedlar.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | Internal, added by AndroidX | Not user-facing |

The app does **not** request contacts, call log, phone state, microphone, notifications, *Display
over other apps*, an accessibility service, the default phone app (dialer) role, or background
location. It has no background service, so it doesn't need a battery-optimisation exemption.

The manifest's `<queries>` block isn't a permission. On Android 11+ it lets the app see whether
WhatsApp, WhatsApp Business, Telegram, Telegram X, Skype, Truecaller, Viber, Google Meet/Duo and
apps that handle `tel:` dial links are installed, which is what the *Installed* badges use.

### What the app needs from the backend

The backend is **not** part of this repository. It's the LeadPedlar web app (a Next.js app; in
the owner's account its source is the private `BuymyLeads` repository), served at
`https://www.leadpedlar.xyz`. The app expects:

- **These pages:** `/` (home), `/marketplace`, `/agent/leads`, `/agent/escrow`, `/agent/sip` and `/login`.
- **A web login with cookies.** Sign-in happens on the site itself, and the web view stores its cookies.
- **The native bridge.** The site calls `window.AndroidBridge`, which the app provides with these functions:
  - `openCallSelector(phone, name)`
  - `makeCall(phone)`
  - `launchSpecificApp(appId, phone)`, where `appId` is one of `system_dialer`, `whatsapp`, `whatsapp_business`, `telegram`, `skype`, `truecaller`, `viber`, `zoiper`, `sip_client` or `system_chooser`
  - `resetDialerDefaults()`
  - `isAppInstalled(packageName)`
  - `getPlatform()`, which returns `"AndroidNative"`
  - `getAppVersion()`, which returns `"1.0.0"`

  The site can recognise the app by the user-agent suffix `LeadPedlarApp/1.0 (Android; Mobile)`.
- **Phone and messaging links.** `tel:` links are routed to the calling app picker. `wa.me`, `api.whatsapp.com`, `whatsapp:`, `t.me` and `tg:` links open in the matching app.

**Pointing the app at a different server**, such as your own deployment of the web app, needs a
change to your local copy before you build. The address is hard-coded, and the app's settings sheet
for changing it isn't reachable from the UI. Replace `https://www.leadpedlar.xyz` with your
server's base URL (no trailing slash) in these two places, then build:

- `app/src/main/java/com/example/leadpedlar/data/preferences/AppPreferences.kt`, in the line `const val DEFAULT_SERVER_URL = "https://www.leadpedlar.xyz"`.
- `app/src/main/java/com/example/leadpedlar/ui/main/MainScreen.kt`, in the line `appPrefs.serverUrlFlow.collectAsState(initial = "https://www.leadpedlar.xyz")`.

The app allows plain-HTTP addresses too (`android:usesCleartextTraffic="true"`). Use HTTPS for anything
reachable from the internet.

## Updating

There's no in-app updater. To update:

```bash
cd LeadPedlar-Android
git pull --ff-only
./gradlew assembleRelease
```

Then sign the new APK with the **same keystore** (2.5) and install it over the old one with
`adb install -r app/build/outputs/apk/release/leadpedlar-release.apk`, or sideload it (step 1).
Your sign-in and calling-app choice are kept. If you use debug builds, run `./gradlew assembleDebug`
and `adb install -r app/build/outputs/apk/debug/app-debug.apk`. That only works from the same
computer, because the debug key is per computer.

Updates to the web app itself appear in the app automatically, with no reinstall, because the app loads the live site.

## Uninstall

1. **Remove the app** with any of these:
   - On the phone: long-press the LeadPedlar icon › **App info** › **Uninstall**, or *Settings › Apps › LeadPedlar › Uninstall*.
   - With adb: `adb uninstall com.example.leadpedlar`. This prints `Success`.

   This removes the app, its web session and its calling-app preference. Your LeadPedlar account and
   data on the server aren't touched. The app allows Android backup, so if you reinstall on a phone
   with Google backup enabled, Android may restore its saved preferences.
2. **Optional, on the phone:** switch *Install unknown apps* / *Unknown sources* and *USB debugging* off again if you enabled them only for this.
3. **Optional, on the build computer:** delete the `LeadPedlar-Android` folder. **Keep your backed-up release keystore** if you might build updates. Gradle caches (`~/.gradle`, including any JDK it downloaded into `~/.gradle/jdks`) and the Android SDK are shared with other Android projects; remove them only if nothing else uses them.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| *App not installed* / `INSTALL_PARSE_FAILED_NO_CERTIFICATES` | You tried to install `app-release-unsigned.apk` | Sign it (2.5) or install `app-debug.apk` |
| *App not installed* / *package conflicts with an existing package* / `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | The installed app was signed with a different key (debug vs release, or another computer) | Uninstall the old app (`adb uninstall com.example.leadpedlar`), then install. Use one keystore from now on |
| `INSTALL_FAILED_OLDER_SDK` / *There was a problem parsing the package* | The phone runs Android older than 7.0 | Not supported |
| *For your security, your phone is not allowed to install unknown apps from this source* | Unknown sources aren't allowed for the app that opened the APK | Step 1, option A |
| **Unable to Connect to Server** screen with *Endpoint: https://www.leadpedlar.xyz* | No internet, the site is down, or DNS/firewall blocks it | Check the phone can open `https://www.leadpedlar.xyz` in Chrome, then tap **Retry Connection** |
| Blank or broken pages, buttons do nothing | Outdated *Android System WebView* | Update **Android System WebView** and **Chrome** in Google Play, then restart the app |
| Tapping a number does nothing, or opens the wrong app | A default calling app was saved, or the chosen app can't handle the link | Reset to *Always Ask* in the web app's dialer settings, or *Settings › Apps › LeadPedlar › Storage › Clear storage* (this also signs you out) |
| An installed app isn't marked *Installed* in the picker | Zoiper (`com.zoiper.android.app`) and generic SIP apps aren't listed in the manifest's `<queries>`, so Android 11+ hides them from the check | Cosmetic. Tapping **Zoiper Softphone** or **SIP Client** still opens the app if it's installed |
| WhatsApp opens a chat instead of ringing | By design, WhatsApp has no "call this number" link | Start the voice or video call from the opened chat |
| Build: `SDK location not found` | No `local.properties` and no `ANDROID_HOME` | Step 2.3 |
| Build: `Failed to find target with hash string 'android-36'` or licences not accepted | SDK Platform 36 or Build-Tools 36 missing | `sdkmanager "platforms;android-36" "build-tools;36.0.0"` and `sdkmanager --licenses` |
| Build: *Gradle requires JVM 17 or later* / `Android Gradle plugin requires Java 17` | An older JDK is active | Point `JAVA_HOME` to JDK 17 or 21 |
| Build: `No matching toolchains found for requested specification: {languageVersion=17…}` while offline | No local JDK 17, and Gradle can't download one | Install JDK 17 (step 2.1), or build once with internet access |
| Android Studio: *The project is using an incompatible version of the Android Gradle plugin* | Android Studio is older than AGP 9.0.1 supports | Update Android Studio, or build from the command line |
| Build: `./gradlew: Permission denied` | The executable bit was lost (e.g. the code was copied as a ZIP) | Run `sh ./gradlew …` or `chmod +x gradlew` |
