# 🎵 CMD Music Downloader (YouTube & Spotify to 320kbps MP3 / FLAC ZIP)

<p align="center">
  <img src="https://img.shields.io/badge/Python-3.10+-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python" />
  <img src="https://img.shields.io/badge/Interface-Interactive%20CMD%20CLI-000000?style=for-the-badge&logo=gnubash&logoColor=white" alt="Interface" />
  <img src="https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20Google%20Cloud%20Shell-4EAA25?style=for-the-badge" alt="Platform" />
  <img src="https://img.shields.io/badge/Quality-320kbps%20%7C%20Lossless%20FLAC-FF6F00?style=for-the-badge" alt="Quality" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="License" />
</p>

A powerful **Interactive CMD CLI Terminal Tool** to download **YouTube & Spotify** playlists, albums, and songs in **320kbps MP3** or **Lossless FLAC / WAV / M4A**, with embedded ID3 metadata tags (Title, Artist, Album, Release Year, High-Resolution Cover Art) and **automatic ZIP packaging & smart disk cleanup**.

---

## 🌟 Features

- 🎧 **Multi-Platform Support**: Download YouTube & Spotify playlists, albums, and single songs.
- 🎚️ **Studio Quality Audio**: 320kbps, 256kbps, 192kbps, 128kbps in MP3, FLAC, WAV, or M4A.
- 🖼️ **Full ID3 Tagging**: Embeds high-resolution cover artwork, title, artist, album, and year tags automatically.
- 📊 **Dual Progress Bars**: 
  - **Overall Playlist Bar**: Real-time total progress across the whole playlist.
  - **Dedicated Per-Song Bar**: Classic Box progress bar `[████████░░░░]` showing download speed, downloaded MBs, and ETA for each song individually.
- 📦 **3-in-1 Storage Options**:
  - **Only ZIP File**: Compresses all songs into a `.zip` archive and auto-cleans unzipped files to save storage.
  - **Only Uncompressed Folder**: Keeps normal audio files inside a playlist folder.
  - **Both**: Keeps both `.zip` archive and raw files.
- ⚡ **Google Cloud Shell & VPS Ready**: Download huge playlists at 1000+ Mbps Gigabit internet speeds!

---

## ☁️ How to Run in Google Cloud Shell (CMD Terminal)

Google Cloud Shell me is tool ko direct command line me run karne ke liye yeh steps follow karein:

### Step 1: Open Google Cloud Shell
1. Apne browser me **[Google Cloud Shell](https://shell.cloud.google.com/)** open karein.

### Step 2: Clone & Setup
Terminal me yeh commands run karein:

```bash
# 1. Clone repository & enter folder
git clone https://github.com/Rupam852/CMD_Music_Downloader.git
cd CMD_Music_Downloader

# 2. Install FFmpeg
sudo apt update && sudo apt install -y ffmpeg

# 3. Install Python requirements
pip install -r requirements.txt
```

### Step 3: Run Interactive Downloader
```bash
python main.py
```

### ⚡ Direct Command-Line Arguments (Optional):
Aap direct URL pass karke bhi download kar sakte hain:
```bash
# Example: Download Spotify Playlist in 320kbps MP3 as ZIP
python main.py -u "https://open.spotify.com/playlist/YOUR_PLAYLIST_ID" -q 320 -f mp3 --mode zip

# Example: Download YouTube Playlist
python main.py -u "https://www.youtube.com/playlist?list=YOUR_PLAYLIST_ID" -q 320 -f mp3
```

---

## 💻 How to Run Locally on Windows

### Method 1: 1-Click Launcher (Easiest)
Double-click [`run.bat`](run.bat) file.

### Method 2: Command Prompt / PowerShell
```bash
# 1. Install dependencies
pip install -r requirements.txt

# 2. Run
python main.py
```

---

## 📂 Project Structure

```
CMD MUSIC DOWNLOADER/
├── core/
│   ├── config.py             # Global paths and defaults
│   ├── ffmpeg_helper.py      # Portable FFmpeg auto-downloader & checker
│   ├── progress.py           # Classic Box & Per-Song Progress Bars
│   ├── spotify_downloader.py # Spotify metadata extractor & 320kbps engine
│   ├── tagger.py             # ID3 tagger & album art embedder
│   ├── youtube_downloader.py # YouTube playlist & song downloader
│   └── zipper.py             # ZIP packaging & auto disk cleaner
├── main.py                   # Interactive CMD CLI Application
├── requirements.txt          # Python dependencies
├── run.bat                   # 1-Click Windows CMD Launcher
└── README.md                 # Documentation & Guides
```

---

## 📄 License
This project is open-source and licensed under the [MIT License](LICENSE).
