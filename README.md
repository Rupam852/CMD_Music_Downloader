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

## 🌟 Key Features

- 🎧 **Multi-Platform Support**: Download YouTube & Spotify playlists, albums, and single tracks.
- 🎚️ **Studio Quality Audio**: 320kbps (Default / Highest MP3), 256kbps, 192kbps, 128kbps, or Lossless FLAC / WAV.
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

## ☁️ Option 1: How to Run in Google Cloud Shell (Cloud Terminal)

Google Cloud Shell me run karne se aapko **1000+ Mbps Gigabit internet speed** milti hai jisse 100 gaano ki playlist sirf 1 minute me download ho jati hai!

### 📌 Step 1: Open Google Cloud Shell
1. Apne browser me **[Google Cloud Shell](https://shell.cloud.google.com/)** open karein.
2. Apne Google account se sign in karein.

### 📌 Step 2: Run Setup Commands
Cloud Shell terminal me yeh commands paste karein:

```bash
# 1. Clone repository & enter folder
git clone https://github.com/Rupam852/CMD_Music_Downloader.git
cd CMD_Music_Downloader

# 2. Install FFmpeg
sudo apt update && sudo apt install -y ffmpeg

# 3. Install Python requirements
pip install -r requirements.txt
```

### 📌 Step 3: Run Downloader
```bash
python main.py
```
* **Step 1**: Apna YouTube ya Spotify link paste karein.
* **Step 2 to 6**: Direct **Enter** press karte jayein (Default 320kbps MP3 + ZIP select ho jayega).

### 📥 Step 4: Download ZIP from Cloud Shell to Your PC / Mobile:
Jab download complete ho jaye, toh Cloud Shell se `.zip` file apne computer/phone me download karne ke liye:
```bash
# Replace 'Your_Playlist_Name.zip' with your actual zip file name inside downloads/ folder:
cloudshell download downloads/*.zip
```
Ya phir Cloud Shell ke top-right corner me **Three Dots (⋮) -> "Download File"** par click karke path daalein:
`CMD_Music_Downloader/downloads/Your_Playlist_Name.zip`

---

## 💻 Option 2: How to Run on Your Computer (Windows PC)

### 📌 Step 1: Download Project to Computer
1. Is page ke top par green **"Code"** button par click karke **"Download ZIP"** karein, aur use extract karein.
   * *Ya phir Git terminal me:* `git clone https://github.com/Rupam852/CMD_Music_Downloader.git`

### 📌 Step 2: Python Setup (If not already installed)
1. **[Python 3.10+](https://www.python.org/downloads/)** install karein.
2. Install karte waqt **"Add python.exe to PATH"** checkbox ko zaroor tick karein!

### 📌 Step 3: 1-Click Run
1. Folder ke andar **[`run.bat`](run.bat)** file par double-click karein!
   * Yeh automatically requirements install karega, portable FFmpeg setup karega aur downloader open kar dega!
2. *Ya phir CMD / PowerShell me run karein:*
   ```bash
   pip install -r requirements.txt
   python main.py
   ```

---

## ⚡ Direct CLI Arguments (Advanced / Scripts):

Aap bina interactive prompts ke direct single command se bhi download kar sakte hain:

```bash
# Spotify Playlist in 320kbps MP3 as ZIP
python main.py -u "https://open.spotify.com/playlist/YOUR_PLAYLIST_ID" -q 320 -f mp3 --mode zip

# YouTube Playlist in Lossless FLAC
python main.py -u "https://www.youtube.com/playlist?list=YOUR_PLAYLIST_ID" -q 320 -f flac --mode zip
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
