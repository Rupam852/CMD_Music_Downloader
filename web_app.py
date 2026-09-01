import os
import sys
import uuid
import asyncio
import shutil
from pathlib import Path
from typing import Dict, Any

from fastapi import FastAPI, BackgroundTasks, HTTPException
from fastapi.responses import HTMLResponse, FileResponse, StreamingResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
import uvicorn

from core.config import (
    BASE_DIR,
    DOWNLOADS_DIR,
    DEFAULT_AUDIO_FORMAT,
    DEFAULT_BITRATE,
    YOUTUBE_DOMAINS,
    SPOTIFY_DOMAINS,
)
from core.ffmpeg_helper import ensure_ffmpeg
from core.youtube_downloader import download_youtube
from core.spotify_downloader import download_spotify
from core.zipper import zip_folder

app = FastAPI(title="Music Downloader Web API")

# Mount static folder
STATIC_DIR = BASE_DIR / "static"
STATIC_DIR.mkdir(parents=True, exist_ok=True)

# In-memory progress tracker: task_id -> task_state
tasks: Dict[str, Dict[str, Any]] = {}

class DownloadRequest(BaseModel):
    url: str
    quality: str = "320"
    format: str = "mp3"
    mode: str = "zip" # zip, folder, both

def detect_platform(url: str) -> str:
    url_lower = url.lower()
    for domain in SPOTIFY_DOMAINS:
        if domain in url_lower:
            return "spotify"
    for domain in YOUTUBE_DOMAINS:
        if domain in url_lower:
            return "youtube"
    return "unknown"

def run_download_task(task_id: str, req: DownloadRequest):
    platform = detect_platform(req.url)
    if platform == "unknown":
        tasks[task_id]["status"] = "error"
        tasks[task_id]["error"] = "Invalid URL. Please provide a YouTube or Spotify link."
        return

    tasks[task_id]["platform"] = platform
    tasks[task_id]["status"] = "downloading"
    tasks[task_id]["percentage"] = 5
    tasks[task_id]["message"] = f"Connecting to {platform.upper()}..."

    task_dir = DOWNLOADS_DIR / f"web_{task_id}"
    task_dir.mkdir(parents=True, exist_ok=True)

    try:
        if platform == "youtube":
            folder_path, collection_title = download_youtube(
                url=req.url,
                output_dir=task_dir,
                bitrate=req.quality,
                audio_format=req.format
            )
        else: # spotify
            folder_path, collection_title = download_spotify(
                url=req.url,
                output_dir=task_dir,
                bitrate=req.quality,
                audio_format=req.format
            )

        tasks[task_id]["title"] = collection_title
        tasks[task_id]["percentage"] = 80
        tasks[task_id]["message"] = "Finalizing audio files..."

        if req.mode in ["zip", "both"]:
            tasks[task_id]["message"] = "Compressing files into ZIP archive..."
            zip_path = zip_folder(
                source_dir=folder_path,
                delete_source=(req.mode == "zip")
            )
            tasks[task_id]["file_path"] = str(zip_path)
            tasks[task_id]["file_name"] = zip_path.name
        else:
            # If folder mode, find single file or zip folder
            files = list(folder_path.rglob("*"))
            if len(files) == 1 and files[0].is_file():
                tasks[task_id]["file_path"] = str(files[0])
                tasks[task_id]["file_name"] = files[0].name
            else:
                zip_path = zip_folder(source_dir=folder_path, delete_source=False)
                tasks[task_id]["file_path"] = str(zip_path)
                tasks[task_id]["file_name"] = zip_path.name

        tasks[task_id]["status"] = "completed"
        tasks[task_id]["percentage"] = 100
        tasks[task_id]["message"] = "Download Complete!"

    except Exception as e:
        tasks[task_id]["status"] = "error"
        tasks[task_id]["error"] = str(e)
        tasks[task_id]["message"] = f"Error: {e}"

@app.get("/", response_class=HTMLResponse)
async def serve_ui():
    index_file = STATIC_DIR / "index.html"
    if index_file.exists():
        return HTMLResponse(content=index_file.read_text(encoding="utf-8"))
    return HTMLResponse("<h1>Music Downloader Web UI is loading...</h1>")

@app.post("/api/download")
async def start_download(req: DownloadRequest, background_tasks: BackgroundTasks):
    task_id = str(uuid.uuid4())[:8]
    tasks[task_id] = {
        "status": "queued",
        "percentage": 0,
        "message": "Initializing...",
        "platform": detect_platform(req.url),
        "file_path": None,
        "file_name": None,
        "error": None
    }
    background_tasks.add_task(run_download_task, task_id, req)
    return {"task_id": task_id, "status": "queued"}

@app.get("/api/progress/{task_id}")
async def get_progress(task_id: str):
    if task_id not in tasks:
        raise HTTPException(status_code=404, detail="Task not found")
    return tasks[task_id]

@app.get("/api/download-file/{task_id}")
async def download_file(task_id: str):
    if task_id not in tasks:
        raise HTTPException(status_code=404, detail="Task not found")
    
    task_info = tasks[task_id]
    if task_info.get("status") != "completed" or not task_info.get("file_path"):
        raise HTTPException(status_code=400, detail="File is not ready yet.")

    file_path = Path(task_info["file_path"])
    if not file_path.exists():
        raise HTTPException(status_code=404, detail="File was removed from server.")

    return FileResponse(
        path=str(file_path),
        filename=task_info.get("file_name", file_path.name),
        media_type="application/octet-stream"
    )

def main():
    import argparse
    parser = argparse.ArgumentParser(description="CMD Music Downloader Web Server")
    parser.add_argument("--host", type=str, default="0.0.0.0", help="Host address (default: 0.0.0.0)")
    parser.add_argument("--port", type=int, default=8080, help="Port number (default: 8080 for Cloud Shell)")
    args = parser.parse_args()

    print("\n=======================================================")
    print(f"🎵 Music Downloader Web Server Starting on http://{args.host}:{args.port}")
    print("=======================================================\n")
    
    # Ensure FFmpeg is ready
    ensure_ffmpeg()
    uvicorn.run(app, host=args.host, port=args.port)

if __name__ == "__main__":
    main()
