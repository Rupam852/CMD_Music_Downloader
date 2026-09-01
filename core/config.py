import os
from pathlib import Path

# Base Paths
BASE_DIR = Path(__file__).resolve().parent.parent
DOWNLOADS_DIR = BASE_DIR / "downloads"
BIN_DIR = BASE_DIR / "bin"
TEMP_DIR = BASE_DIR / "temp"

# Ensure necessary directories exist
DOWNLOADS_DIR.mkdir(parents=True, exist_ok=True)
BIN_DIR.mkdir(parents=True, exist_ok=True)
TEMP_DIR.mkdir(parents=True, exist_ok=True)

# Audio Formats & Quality
DEFAULT_AUDIO_FORMAT = "mp3"
DEFAULT_BITRATE = "320" # 320 kbps high quality

# Supported URL Domains
YOUTUBE_DOMAINS = ["youtube.com", "youtu.be", "music.youtube.com"]
SPOTIFY_DOMAINS = ["spotify.com", "open.spotify.com"]

# FFmpeg Windows Static Binary URL (portable essentials build)
FFMPEG_WIN_URL = "https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip"
