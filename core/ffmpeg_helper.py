import os
import shutil
import zipfile
import urllib.request
from pathlib import Path
from rich.progress import Progress, SpinnerColumn, BarColumn, TextColumn, DownloadColumn, TransferSpeedColumn
from core.config import BIN_DIR, FFMPEG_WIN_URL

def get_ffmpeg_path() -> str | None:
    """
    Checks if ffmpeg is available in system PATH or in the local bin/ directory.
    Returns path or directory containing ffmpeg executable.
    """
    # 1. Check system PATH
    system_ffmpeg = shutil.which("ffmpeg")
    if system_ffmpeg:
        return system_ffmpeg

    # 2. Check local bin directory
    direct_exe = BIN_DIR / "ffmpeg.exe"
    if direct_exe.exists():
        return str(direct_exe)

    # Check nested subdirectories inside bin
    nested_exes = list(BIN_DIR.rglob("ffmpeg.exe"))
    if nested_exes:
        return str(nested_exes[0])

    return None

def download_ffmpeg(console=None) -> str:
    """
    Downloads portable static FFmpeg build for Windows into bin/ directory.
    """
    zip_target = BIN_DIR / "ffmpeg.zip"
    
    if console:
        console.print("[cyan]⬇️ Downloading portable FFmpeg for high-quality audio conversion...[/cyan]")

    # Download with rich progress bar
    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        DownloadColumn(),
        TransferSpeedColumn(),
    ) as progress:
        task = progress.add_task("Downloading FFmpeg...", total=None)
        
        def reporthook(block_num, block_size, total_size):
            if total_size > 0:
                progress.update(task, total=total_size, completed=block_num * block_size)

        urllib.request.urlretrieve(FFMPEG_WIN_URL, zip_target, reporthook=reporthook)

    if console:
        console.print("[yellow]📦 Extracting FFmpeg binaries...[/yellow]")

    with zipfile.ZipFile(zip_target, 'r') as zip_ref:
        # Extract only the bin files or entire archive
        zip_ref.extractall(BIN_DIR)

    # Clean up zip
    if zip_target.exists():
        zip_target.unlink()

    ffmpeg_path = get_ffmpeg_path()
    if console:
        console.print(f"[bold green]✅ FFmpeg successfully configured at: {ffmpeg_path}[/bold green]")
    
    return ffmpeg_path

def ensure_ffmpeg(console=None) -> str:
    """
    Ensures FFmpeg is available, auto-downloading if missing on Windows.
    """
    path = get_ffmpeg_path()
    if path:
        return path
    return download_ffmpeg(console)
