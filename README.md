# 🎵 CMD Music Downloader (YouTube & Spotify to 320kbps MP3 / FLAC ZIP)

<p align="center">
  <img src="https://img.shields.io/badge/Python-3.10+-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python" />
  <img src="https://img.shields.io/badge/Interface-Interactive%20CMD%20CLI-000000?style=for-the-badge&logo=gnubash&logoColor=white" alt="Interface" />
  <img src="https://img.shields.io/badge/Platform-Windows%20PC-0078D6?style=for-the-badge&logo=windows&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/Quality-320kbps%20%7C%20Lossless%20FLAC-FF6F00?style=for-the-badge" alt="Quality" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="License" />
</p>

A powerful, sleek **Interactive CMD CLI Terminal Downloader** to download **YouTube & Spotify** playlists, albums, and songs in **320kbps MP3** or **Lossless FLAC / WAV / M4A**, with embedded ID3 metadata tags (Title, Artist, Album, Release Year, High-Resolution Cover Art) and **automatic ZIP packaging & smart disk cleanup**.

---

## 🌟 Key Features

- 🎧 **Multi-Platform Support**: Download YouTube & Spotify playlists, albums, and single tracks.
- 🎚️ **Studio Quality Audio**: 320kbps (Default / Highest MP3), 256kbps, 192kbps, 128kbps, or Lossless FLAC / WAV.
- 🖼️ **Full ID3 Tagging**: Embeds high-resolution cover artwork, title, artist, album, and year tags automatically into every song.
- 📊 **Dual Progress Bars**: 
  - **Overall Playlist Bar**: Real-time total progress across the whole playlist.
  - **Dedicated Per-Song Bar**: Classic Box progress bar `[████████░░░░]` showing download speed, downloaded MBs, and ETA for each song individually.
- 📦 **3-in-1 Storage Options**:
  - **Only ZIP File**: Compresses all songs into a `.zip` archive and auto-cleans unzipped files to save storage.
  - **Only Uncompressed Folder**: Keeps normal audio files inside a playlist folder.
  - **Both**: Keeps both `.zip` archive and raw files.
- ⚡ **Automatic Portable FFmpeg**: No manual FFmpeg setup required — the script auto-detects or downloads portable FFmpeg on Windows!

---

## 💻 How to Run on Your Computer (Windows PC)

### 📌 Method 1: 1-Click Launcher (Sabse Asaan)
1. GitHub se project download karein ya `git clone https://github.com/Rupam852/CMD_Music_Downloader.git` karein.
2. Folder ke andar **[`run.bat`](run.bat)** par direct double-click karein!
   * Yeh automatically Python dependencies check karega, FFmpeg setup karega aur Interactive Downloader screen open kar dega!

---

### 📌 Method 2: Command Prompt / PowerShell
```bash
# 1. Clone repository & enter folder
git clone https://github.com/Rupam852/CMD_Music_Downloader.git
cd CMD_Music_Downloader

# 2. Install requirements
pip install -r requirements.txt

# 3. Run Downloader
python main.py
```

---

## ⚡ Direct Command-Line Arguments (Advanced Usage)

Aap bina interactive prompts ke direct single command se bhi download kar sakte hain:

```bash
# Spotify Playlist in 320kbps MP3 as ZIP
python main.py -u "https://open.spotify.com/playlist/YOUR_PLAYLIST_ID" -q 320 -f mp3 --mode zip

# YouTube Playlist in Lossless FLAC
python main.py -u "https://www.youtube.com/playlist?list=YOUR_PLAYLIST_ID" -q 320 -f flac --mode zip

# Single YouTube Song to Custom Folder
python main.py -u "https://www.youtube.com/watch?v=VIDEO_ID" -o "D:/MyMusic"
```

---

## 📂 Project Structure

```
CMD MUSIC DOWNLOADER/
├── core/
│   ├── config.py             # Global paths and settings
│   ├── ffmpeg_helper.py      # Portable FFmpeg auto-downloader & detector
│   ├── progress.py           # Classic Box & Per-Song Progress Bars
│   ├── spotify_downloader.py # Spotify metadata extractor & 320kbps engine
│   ├── tagger.py             # ID3 tagger & album art embedder
│   ├── youtube_downloader.py # YouTube playlist & song downloader
│   └── zipper.py             # ZIP packaging & auto disk cleaner
├── main.py                   # Interactive CMD CLI Application
├── requirements.txt          # Python dependencies
├── run.bat                   # 1-Click Windows CMD Launcher
└── README.md                 # Full Documentation & Guides
```

---

## 📄 License
This project is open-source and licensed under the [MIT License](LICENSE).
