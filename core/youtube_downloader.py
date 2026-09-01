import re
import os
import sys
from pathlib import Path
import yt_dlp
from rich.console import Console
from rich.progress import Progress, SpinnerColumn, TextColumn, DownloadColumn, TransferSpeedColumn, TimeRemainingColumn
from core.config import DOWNLOADS_DIR, DEFAULT_AUDIO_FORMAT, DEFAULT_BITRATE
from core.ffmpeg_helper import ensure_ffmpeg
from core.progress import ClassicBoxBarColumn

# Ensure UTF-8 output encoding
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

def sanitize_filename(name: str) -> str:
    """Sanitizes strings for Windows filesystem safety."""
    return re.sub(r'[\\/*?:"<>|]', "", name).strip()

def download_youtube(
    url: str,
    output_dir: Path = None,
    bitrate: str = DEFAULT_BITRATE,
    audio_format: str = DEFAULT_AUDIO_FORMAT,
    console: Console = None
) -> tuple[Path, str]:
    """
    Downloads YouTube playlist or single track with classic box progress bar: [██████░░░░] 0% to 100%.
    """
    if console is None:
        console = Console()

    ffmpeg_path = ensure_ffmpeg(console)
    ffmpeg_dir = str(Path(ffmpeg_path).parent) if ffmpeg_path else None

    console.print("\n[bold cyan]🔍 Fetching playlist & track information...[/bold cyan]")
    
    extract_opts = {
        'extract_flat': 'in_playlist',
        'skip_download': True,
        'quiet': True,
        'no_warnings': True,
    }
    
    with yt_dlp.YoutubeDL(extract_opts) as ydl:
        try:
            info = ydl.extract_info(url, download=False)
        except Exception as e:
            console.print(f"[bold red]❌ Failed to fetch info from YouTube: {e}[/bold red]")
            raise e

    is_playlist = 'entries' in info and info['entries'] is not None
    title = info.get('title') or "YouTube_Music"
    clean_title = sanitize_filename(title)

    base_output = Path(output_dir) if output_dir else DOWNLOADS_DIR
    target_dir = base_output / clean_title
    target_dir.mkdir(parents=True, exist_ok=True)

    entries_list = list(info['entries']) if is_playlist and info.get('entries') else [info]
    total_count = len(entries_list)

    console.print(f"[bold green]🎵 Target: {clean_title} ({'Playlist - ' + str(total_count) + ' songs' if is_playlist else 'Single Song'})[/bold green]")
    console.print(f"[bold cyan]📁 Destination: {target_dir}[/bold cyan]\n")

    outtmpl = str(target_dir / '%(title)s.%(ext)s')

    # Setup Rich Progress Bars with Classic Box Progress Bar
    progress = Progress(
        SpinnerColumn(),
        TextColumn("[bold yellow]{task.fields[title]}[/bold yellow]"),
        ClassicBoxBarColumn(bar_width=25),
        TextColumn("[bold green]{task.percentage:>3.0f}%[/bold green]"),
        TextColumn("•"),
        DownloadColumn(),
        TextColumn("•"),
        TransferSpeedColumn(),
        TextColumn("•"),
        TimeRemainingColumn(),
        console=console
    )

    with progress:
        download_task = progress.add_task(
            "download",
            total=100,
            completed=0,
            title="Initializing download..."
        )

        current_item = {"index": 0, "title": ""}

        def yt_hook(d):
            status = d.get('status')
            if status == 'downloading':
                filename = d.get('filename', '')
                info_d = d.get('info_dict', {})
                song_title = info_d.get('title') or Path(filename).stem
                
                disp_title = (song_title[:25] + "..") if len(song_title) > 25 else song_title
                p_idx = d.get('playlist_index') or (current_item["index"] if current_item["index"] > 0 else 1)
                p_tot = d.get('playlist_count') or total_count

                total_bytes = d.get('total_bytes') or d.get('total_bytes_estimate') or 1
                downloaded = d.get('downloaded_bytes') or 0

                progress.update(
                    download_task,
                    total=total_bytes,
                    completed=downloaded,
                    title=f"[{p_idx}/{p_tot}] {disp_title}"
                )

            elif status == 'finished':
                filename = d.get('filename', '')
                song_title = Path(filename).stem
                progress.update(download_task, title=f"🔄 Processing: {song_title[:25]}")

        ydl_opts = {
            'format': 'bestaudio/best',
            'outtmpl': outtmpl,
            'ffmpeg_location': ffmpeg_dir or ffmpeg_path,
            'writethumbnail': True,
            'postprocessors': [
                {
                    'key': 'FFmpegExtractAudio',
                    'preferredcodec': audio_format,
                    'preferredquality': bitrate,
                },
                {
                    'key': 'EmbedThumbnail',
                },
                {
                    'key': 'FFmpegMetadata',
                    'add_metadata': True,
                }
            ],
            'quiet': True,
            'no_warnings': True,
            'ignoreerrors': True,
            'progress_hooks': [yt_hook],
        }

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])

        progress.update(download_task, completed=100, total=100, title="[bold green]✅ Download Completed![/bold green]")

    console.print(f"\n[bold green]✅ All songs successfully saved to:[/bold green] [yellow]{target_dir}[/yellow]")
    return target_dir, clean_title
