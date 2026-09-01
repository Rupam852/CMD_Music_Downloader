# 🎵 CMD Music & Playlist Downloader

<p align="center">
  <img src="https://img.shields.io/badge/Python-3.10+-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python" />
  <img src="https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-4EAA25?style=for-the-badge" alt="Platform" />
  <img src="https://img.shields.io/badge/Audio%20Quality-320kbps%20%7C%20Lossless%20FLAC-FF6F00?style=for-the-badge" alt="Quality" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="License" />
</p>

A powerful, modern Command Line (CMD) tool to download **YouTube & Spotify** playlists, albums, and single tracks in **Crystal Clear 320kbps MP3** or **Lossless FLAC/WAV**, complete with embedded ID3 metadata tags (Title, Artist, Album, Year) and high-resolution Album Artwork, bundled with flexible **ZIP packaging & smart disk cleanup**.

---

## ✨ Features

- 🎧 **Multi-Platform Support**:
  - **YouTube & YouTube Music**: Full playlists, mixes, albums, and single tracks.
  - **Spotify**: Playlists, albums, and tracks (extracts Spotify metadata & matches highest-fidelity audio).
- 🎚️ **High Audio Fidelity**:
  - Transcode to **320kbps MP3** (Studio quality) or lossless formats (**FLAC / WAV / M4A**).
  - Embeds accurate ID3 tags + High-Res Cover Art via `mutagen`.
- 🕹️ **Classic Box Progress Bar**:
  - Live ASCII/Box progress indicator `[██████████░░░░] 0% to 100%`.
  - Shows real-time download speed (`MB/s`), downloaded/total size, and ETA.
- 📦 **3-in-1 Storage Modes**:
  1. **Only ZIP File**: Compresses all songs into `.zip` and cleans up raw duplicate files to save hard drive space.
  2. **Only Uncompressed Folder**: Downloads normal audio files directly into a folder without zipping.
  3. **Both (ZIP + Folder)**: Keeps both the `.zip` archive AND the raw audio folder.
- 📁 **Custom Save Location**: Choose default `downloads/`, type your own path, or use a graphical folder picker dialog (`browse`).
- ⚡ **Auto-FFmpeg Setup**: Automatically detects or downloads a portable `ffmpeg` binary on Windows so you never encounter missing FFmpeg errors!

---

## 🚀 Quick Start & Installation

### 1. Clone the Repository
```bash
git clone https://github.com/Rupam852/CMD_Music_Downloader.git
cd CMD_Music_Downloader
```

### 2. Install Dependencies
```bash
pip install -r requirements.txt
```

---

## 💻 How to Use

### 🟢 Method 1: Interactive Menu (Recommended)
Simply double-click [`run.bat`](run.bat) on Windows, or run:
```bash
python main.py
```

You will be guided through 6 interactive steps:
```text
───────────────── 🎵 Step 1: Music Link ─────────────────
🔗 Paste YouTube or Spotify (Playlist/Song) URL: <your-link>

───────────────── 🎚️ Step 2: Audio Bitrate ─────────────────
  1. 320 kbps (Best Quality - Crystal Clear) [Default]
  2. 256 kbps (High Quality - Balanced)
  3. 192 kbps (Standard Quality)
  4. 128 kbps (Compact Size)
Select Quality [1/2/3/4] (1): 1

───────────────── 🎼 Step 3: Audio Format ─────────────────
  1. MP3  (Universal Compatibility - Recommended) [Default]
  2. FLAC (Lossless Studio High-Resolution)
  3. WAV  (Uncompressed Lossless Audio)
  4. M4A  (High Efficiency AAC Audio)
Select Format [1/2/3/4] (1): 1

───────────────── 📦 Step 4: Storage Mode ─────────────────
  1. Only ZIP File (Packs into .zip & auto-cleans raw files to save disk space) [Default]
  2. Only Uncompressed Folder (Direct audio files inside normal folder)
  3. Both (ZIP + Folder) (Keep .zip file AND uncompressed songs folder)
Select Option [1/2/3] (1): 1

───────────────── 📁 Step 5: Destination Folder ─────────────────
  1. Default Downloads Folder (./downloads) [Default]
  2. Browse Window (Open graphical folder selector popup)
  3. Custom Path (Type your own folder path)
Select Destination [1/2/3] (1): 1

───────────────── 🚀 Step 6: Completion Action ─────────────────
📂 Open destination folder when download finishes? [Y/n] (Y): Y
```

---

### 🟣 Method 2: Command Line Flags (Fast & Direct)

```bash
# 1. Download YouTube Playlist as 320kbps MP3 ZIP
python main.py --url "https://www.youtube.com/playlist?list=PLxxxxxx"

# 2. Download Spotify Playlist to custom folder
python main.py --url "https://open.spotify.com/playlist/37i9dQZF1DXcBWIGoYBM5M" --output "D:\MyPlaylists"

# 3. Download directly to uncompressed songs folder (No ZIP)
python main.py --url "<url>" --mode folder

# 4. Download and keep BOTH .zip and raw folder
python main.py --url "<url>" --mode both

# 5. Download in FLAC lossless format
python main.py --url "<url>" --format flac

# 6. Auto-open output folder when done
python main.py --url "<url>" --open
```

---

## 📁 Project Structure

```
CMD_Music_Downloader/
├── core/
│   ├── config.py              # Configuration & quality settings
│   ├── ffmpeg_helper.py       # Auto-detects & downloads portable FFmpeg
│   ├── progress.py            # Classic Box progress bar column
│   ├── spotify_downloader.py  # Spotify metadata parser & audio searcher
│   ├── tagger.py              # ID3 tagger & album cover art embedder
│   ├── youtube_downloader.py  # YouTube playlist/track audio extractor
│   └── zipper.py              # ZIP archiver & smart disk cleaner
├── downloads/                 # Default output folder for music & ZIPs
├── main.py                    # Main interactive CLI application
├── requirements.txt           # Python dependencies
├── run.bat                    # 1-click Windows launcher
└── README.md                  # Project documentation
```

---

## 🛠️ Requirements

- **Python**: 3.10 or higher
- **Packages**:
  - `yt-dlp` (Audio extraction engine)
  - `mutagen` (ID3 audio tagging)
  - `rich` (Terminal styling & progress bars)
  - `requests` & `spotipy` (Spotify metadata resolution)

---

## 📜 Disclaimer
This tool is intended for personal and educational use. Please respect the copyright of content creators and artists.

## 📄 License
This project is licensed under the MIT License.
