import re
import os
import sys
from pathlib import Path
import yt_dlp
from rich.console import Console
from rich.progress import (
    Progress,
    SpinnerColumn,
    TextColumn,
    DownloadColumn,
    TransferSpeedColumn,
    TimeRemainingColumn,
    TaskProgressColumn
)
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

# Universal player clients to bypass YouTube bot detection & datacenter IP blocks (Cloud Shell / VPS)
YOUTUBE_EXTRACTOR_ARGS = {
    'youtube': {
        'player_client': ['android', 'ios', 'web_embedded', 'tv'],
        'player_skip': ['webpage', 'configs'],
    }
}

def sanitize_filename(name: str) -> str:
    """Sanitizes strings for Windows/Linux filesystem safety."""
    return re.sub(r'[\\/*?:"<>|]', "", name).strip()

def download_youtube(
    url: str,
    output_dir: Path = None,
    bitrate: str = DEFAULT_BITRATE,
    audio_format: str = DEFAULT_AUDIO_FORMAT,
    console: Console = None
) -> tuple[Path, str]:
    """
    Downloads YouTube playlist or single track with individual per-song progress bars and overall playlist tracking.
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
        'extractor_args': YOUTUBE_EXTRACTOR_ARGS,
    }
    
    with yt_dlp.YoutubeDL(extract_opts) as ydl:
        try:
            info = ydl.extract_info(url, download=False)
        except Exception as e:
            console.print(f"[bold red]❌ Failed to fetch info from YouTube: {e}[/bold red]")
            raise e

    is_playlist = 'entries' in info and info['entries'] is not None
    title = info.get('title') or "YouTube_Music"
    clean_title = sanitize_filename(title) or "YouTube_Collection"

    base_output = Path(output_dir) if output_dir else DOWNLOADS_DIR
    target_dir = base_output / clean_title
    target_dir.mkdir(parents=True, exist_ok=True)

    if is_playlist and info.get('entries'):
        entries_list = [e for e in info['entries'] if e]
    else:
        entries_list = [info]

    total_count = len(entries_list)

    console.print(f"[bold green]🎵 Target: {clean_title} ({total_count} Songs Total)[/bold green]")
    console.print(f"[bold cyan]📁 Destination: {target_dir}[/bold cyan]\n")

    progress = Progress(
        SpinnerColumn(),
        TextColumn("[bold cyan]{task.description}[/bold cyan]"),
        ClassicBoxBarColumn(bar_width=25),
        TaskProgressColumn(),
        TextColumn("•"),
        DownloadColumn(),
        TextColumn("•"),
        TransferSpeedColumn(),
        TextColumn("•"),
        TimeRemainingColumn(),
        console=console
    )

    with progress:
        # 1. Overall Playlist Progress Bar
        overall_task = progress.add_task(
            f"📦 Overall Playlist [{total_count} Songs]",
            total=total_count,
            completed=0
        )

        for i, entry in enumerate(entries_list, 1):
            entry_title = entry.get('title') or f"Track_{i}"
            safe_name = sanitize_filename(entry_title)
            output_file = target_dir / f"{safe_name}.{audio_format}"
            short_name = (entry_title[:28] + "..") if len(entry_title) > 28 else entry_title
            
            entry_url = entry.get('url') or entry.get('webpage_url') or entry.get('id')
            if not entry_url.startswith('http'):
                entry_url = f"https://www.youtube.com/watch?v={entry_url}"

            if output_file.exists():
                progress.advance(overall_task, 1)
                console.print(f"[dim green]⏩ [{i}/{total_count}] Already exists: {safe_name}[/dim green]")
                continue

            # 2. Individual Per-Song Separate Progress Bar
            song_task = progress.add_task(
                f"🎵 [{i}/{total_count}] {short_name}",
                total=100,
                completed=0
            )

            def yt_hook(d):
                status = d.get('status')
                if status == 'downloading':
                    total_bytes = d.get('total_bytes') or d.get('total_bytes_estimate') or 1
                    downloaded = d.get('downloaded_bytes') or 0
                    progress.update(
                        song_task,
                        total=total_bytes,
                        completed=downloaded,
                        description=f"🎵 [{i}/{total_count}] {short_name}"
                    )
                elif status == 'finished':
                    progress.update(
                        song_task,
                        description=f"🔄 [{i}/{total_count}] Converting & Embedding Artwork: {short_name}"
                    )

            ydl_opts = {
                'format': 'bestaudio/best',
                'outtmpl': str(target_dir / f"{safe_name}.%(ext)s"),
                'ffmpeg_location': ffmpeg_dir or ffmpeg_path,
                'writethumbnail': True,
                'extractor_args': YOUTUBE_EXTRACTOR_ARGS,
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

            try:
                with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                    ydl.download([entry_url])

                console.print(f"[bold green]✅ [{i}/{total_count}] Saved: {safe_name}[/bold green]")
            except Exception as e:
                console.print(f"[red]❌ [{i}/{total_count}] Error downloading {entry_title}: {e}[/red]")
            finally:
                progress.remove_task(song_task)
                progress.advance(overall_task, 1)

        progress.update(overall_task, completed=total_count, description="[bold green]📦 All YouTube Songs Completed![/bold green]")

    console.print(f"\n[bold green]✅ All {total_count} songs successfully saved to:[/bold green] [yellow]{target_dir}[/yellow]")
    return target_dir, clean_title
