# 🎵 CMD & Web Music Downloader (YouTube & Spotify to 320kbps MP3 / FLAC ZIP)

<p align="center">
  <img src="https://img.shields.io/badge/Python-3.10+-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python" />
  <img src="https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20Cloud%20Shell-4EAA25?style=for-the-badge" alt="Platform" />
  <img src="https://img.shields.io/badge/Web%20UI-FastAPI%20%2B%20Glassmorphism-8b5cf6?style=for-the-badge" alt="Web UI" />
  <img src="https://img.shields.io/badge/Quality-320kbps%20%7C%20Lossless%20FLAC-FF6F00?style=for-the-badge" alt="Quality" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge" alt="License" />
</p>

A powerful dual-mode tool (**Interactive CMD CLI + Mobile-Responsive Web Application**) to download **YouTube & Spotify** playlists, albums, and songs in **320kbps MP3** or **Lossless FLAC/WAV**, with embedded ID3 metadata tags (Title, Artist, Album, Year) and high-resolution Album Artwork, bundled with **automatic ZIP packaging & smart disk cleanup**.

---

## 🌟 Key Features

- 🎧 **Multi-Platform**: YouTube & Spotify playlists, albums, and tracks.
- 🎚️ **Studio Audio Quality**: 320kbps MP3 or lossless FLAC / WAV / M4A with cover art.
- 🌐 **Dual Interface**:
  - **Terminal Mode (`main.py`)**: Colorful step-by-step interactive CMD prompts with classic box progress bars `[████████░░]`.
  - **Web Application Mode (`web_app.py`)**: Modern glassmorphism UI for Mobile & Desktop browsers with instant download triggers.
- ☁️ **Google Cloud Shell Ready**: Run on Google Cloud Shell's Gigabit connection with Free HTTPS Web Preview!
- 📦 **3-in-1 Storage Options**: Only ZIP (auto-cleans raw files), Only Folder, or Both.

---

## ☁️ How to Run in Google Cloud Shell (Free HTTPS Web App)

Google Cloud Shell me run karne ke liye yeh simple steps follow karein:

```bash
# 1. Clone repository
git clone https://github.com/Rupam852/CMD_Music_Downloader.git
cd CMD_Music_Downloader

# 2. Install Linux FFmpeg
sudo apt update && sudo apt install -y ffmpeg

# 3. Install dependencies
pip install -r requirements.txt

# 4. Start the Web Server
python web_app.py --port 8080
```

### 🌐 Open Google HTTPS Web Preview:
1. Google Cloud Shell ke top-right corner me **"Web Preview"** (Eye / Window icon) par click karein.
2. **"Preview on port 8080"** select karein.
3. Google turant ek **Free Official HTTPS Webpage** open kar dega jise aap Mobile phone ya PC browser me use kar sakte hain!

---

## 💻 How to Run Locally on Windows

### Option A: Interactive CMD Mode
Double-click [`run.bat`](run.bat) or run:
```bash
python main.py
```

### Option B: Local Web Server Mode
Double-click [`start_web.bat`](start_web.bat) or run:
```bash
python web_app.py --port 8080
```
Then open `http://localhost:8080` in your browser.

---

## 📄 License
This project is licensed under the MIT License.
