# 📱 CMD Music — Native Android Application (Kotlin + Jetpack Compose)

A modern, high-performance native Android Music Downloader & Player built with **Kotlin**, **Jetpack Compose (Material 3)**, and **120Hz High Refresh Rate** hardware acceleration.

---

## 🌟 Key Features

- **🚀 120Hz / 144Hz Max Refresh Rate Mode**:
  - Automatically queries the device's display hardware modes and locks into the maximum available refresh rate (90Hz / 120Hz / 144Hz) with hardware acceleration.
- **🖤 Material 3 Glassmorphism UI**:
  - Deep AMOLED Dark Theme (`#090B10`), frosted glass cards, and sapphire blue accents.
- **🎵 AndroidX Media3 / ExoPlayer Engine**:
  - Background audio playback with system `MediaSessionService`.
  - Notification and lockscreen media playback controls.
- **📡 Google Cast / Chromecast Integration**:
  - Cast audio streams to Android TVs, Google Nest, and Smart Speakers.
- **📁 Multi-Playlist Management & Offline Library**:
  - Room Database for storing and organizing multiple imported playlists and downloaded tracks.
- **🔗 YouTube & Spotify Playlist Importer**:
  - Paste any playlist URL to import, download, tag, and listen offline.

---

## 🏗️ Architecture & Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Kotlin 2.0+ |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Audio Engine** | AndroidX Media3 (ExoPlayer + MediaSessionService) |
| **Local Database** | Room Database (KSP) |
| **Image Loading** | Coil Compose |
| **Casting** | Google Play Services Cast Framework |
| **Target SDK** | Android 15 (API 35) / Min SDK 26 (Android 8.0+) |

---

## 🚀 How to Open & Build

1. Open **Android Studio** (Ladybug or newer).
2. Click **Open** and select the `android_app` folder.
3. Let Gradle sync dependencies.
4. Connect your Android device (or launch an emulator) and click **Run (Shift + F10)**!
