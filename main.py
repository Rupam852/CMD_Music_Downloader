import os
import sys

# Ensure UTF-8 output encoding for Windows CMD / PowerShell
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

import argparse
import subprocess
from pathlib import Path

from rich.console import Console
from rich.panel import Panel
from rich.prompt import Prompt, Confirm
from rich.table import Table
from rich import print as rprint

from core.config import (
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

console = Console()

BANNER = """[bold magenta]
 █▀▄▀█ █░█ █▀ █ █▀▀   █▀▄ █▀█ █░█░█ █▄░█ █░░ █▀█ ▄▀█ █▀▄ █▀▀ █▀█
 █░▀░█ █▄█ ▄█ █ █▄▄   █▄▀ █▄█ ▀▄▀▄▀ █░▀█ █▄▄ █▄█ █▀█ █▄▀ ██▄ █▀▄
[/bold magenta]
[bold cyan]  🎵 YouTube & Spotify Playlist Downloader (High Quality 320kbps + ZIP) 🚀[/bold cyan]
"""

def detect_platform(url: str) -> str:
    """Detects whether the URL is Spotify, YouTube, or unknown."""
    url_lower = url.lower()
    for domain in SPOTIFY_DOMAINS:
        if domain in url_lower:
            return "spotify"
    for domain in YOUTUBE_DOMAINS:
        if domain in url_lower:
            return "youtube"
    return "unknown"

def print_banner():
    console.print(Panel(BANNER, border_style="cyan", padding=(1, 2)))

def open_folder_in_explorer(path: Path):
    """Opens folder or reveals file in Windows Explorer."""
    try:
        if os.name == 'nt':
            if path.is_file():
                subprocess.run(f'explorer /select,"{path}"', shell=True)
            else:
                os.startfile(str(path))
    except Exception:
        pass

def process_download(
    url: str,
    output_dir: Path = None,
    quality: str = "320",
    audio_format: str = "mp3",
    do_zip: bool = True,
    keep_folder: bool = False,
    open_folder: bool = False
):
    platform = detect_platform(url)

    if platform == "unknown":
        console.print("[bold red]❌ Error: Invalid URL. Please provide a valid YouTube or Spotify link.[/bold red]")
        return

    if output_dir is None:
        output_dir = DOWNLOADS_DIR
    else:
        output_dir = Path(output_dir)
        output_dir.mkdir(parents=True, exist_ok=True)

    console.print(f"\n[bold green]🎯 Platform Detected:[/bold green] [bold yellow]{platform.upper()}[/bold yellow]")
    console.print(f"[bold cyan]⚙️ Quality Setting:[/bold cyan] [bold white]{quality}kbps {audio_format.upper()}[/bold white]")
    console.print(f"[bold magenta]📁 Destination Folder:[/bold magenta] [bold white]{output_dir}[/bold white]\n")

    try:
        if platform == "youtube":
            folder_path, collection_title = download_youtube(
                url=url,
                output_dir=output_dir,
                bitrate=quality,
                audio_format=audio_format,
                console=console
            )
        else: # spotify
            folder_path, collection_title = download_spotify(
                url=url,
                output_dir=output_dir,
                bitrate=quality,
                audio_format=audio_format,
                console=console
            )

        zip_path = None
        if do_zip:
            zip_path = zip_folder(folder_path, delete_source=not keep_folder, console=console)

        # Summary Table
        table = Table(title="🎉 Download Summary", style="bold green", show_header=True, header_style="bold magenta")
        table.add_column("Property", style="cyan", width=20)
        table.add_column("Details", style="white")

        table.add_row("Title", collection_title)
        table.add_row("Platform", platform.upper())
        table.add_row("Format & Bitrate", f"{audio_format.upper()} ({quality} kbps)")
        if zip_path and zip_path.exists():
            table.add_row("ZIP Archive", str(zip_path))
            table.add_row("ZIP Size", f"{zip_path.stat().st_size / (1024 * 1024):.2f} MB")
            if not keep_folder:
                table.add_row("Unzipped Folder", "Auto-cleaned (Disk space saved!) 🧹")
            else:
                table.add_row("Folder", str(folder_path))
        else:
            table.add_row("Folder", str(folder_path))

        console.print("\n")
        console.print(table)
        console.print("\n[bold green]✨ All done! Enjoy your high-quality music! 🎧[/bold green]\n")

        if open_folder and os.name == 'nt':
            target = zip_path if zip_path and zip_path.exists() else folder_path
            open_folder_in_explorer(target)

    except Exception as e:
        console.print(f"\n[bold red]❌ Download encountered an error: {e}[/bold red]")

def pick_folder_gui() -> str | None:
    """Optional GUI folder picker using tkinter on Windows."""
    try:
        if os.name == 'nt':
            import tkinter as tk
            from tkinter import filedialog
            root = tk.Tk()
            root.withdraw()
            root.attributes('-topmost', True)
            selected = filedialog.askdirectory(title="Select Destination Folder to Save Music & ZIP")
            root.destroy()
            return selected if selected else None
    except Exception:
        pass
    return None

def interactive_mode():
    print_banner()
    
    # Ensure FFmpeg is ready
    ensure_ffmpeg(console)

    while True:
        # Step 1: URL Prompt
        console.print("\n[bold cyan]───────────────── 🎵 Step 1: Music Link ─────────────────[/bold cyan]")
        url = Prompt.ask("[bold yellow]🔗 Paste YouTube or Spotify (Playlist/Song) URL[/bold yellow] [dim](or 'q' to quit)[/dim]").strip()
        if not url or url.lower() in ('q', 'quit', 'exit'):
            console.print("\n[bold magenta]👋 Thank you for using CMD Music Downloader! Enjoy your music! 🎧[/bold magenta]\n")
            break

        # Step 2: Quality Selection
        console.print("\n[bold cyan]───────────────── 🎚️ Step 2: Audio Bitrate ─────────────────[/bold cyan]")
        console.print("  [bold yellow]1.[/bold yellow] [bold white]320 kbps[/bold white] [dim](Best Quality - Crystal Clear - Recommended)[/dim]")
        console.print("  [bold yellow]2.[/bold yellow] [bold white]256 kbps[/bold white] [dim](High Quality)[/dim]")
        console.print("  [bold yellow]3.[/bold yellow] [bold white]192 kbps[/bold white] [dim](Standard Quality)[/dim]")
        console.print("  [bold yellow]4.[/bold yellow] [bold white]128 kbps[/bold white] [dim](Compact Size)[/dim]")
        
        q_map = {"1": "320", "2": "256", "3": "192", "4": "128"}
        q_choice = Prompt.ask(
            "[bold cyan]Select Quality (1-4)[/bold cyan]",
            choices=["1", "2", "3", "4"],
            default="1",
            show_choices=False
        )
        quality_choice = q_map[q_choice]

        # Step 3: Format Selection
        console.print("\n[bold cyan]───────────────── 🎼 Step 3: Audio Format ─────────────────[/bold cyan]")
        console.print("  [bold yellow]1.[/bold yellow] [bold white]MP3[/bold white]  [dim](Universal Compatibility - Recommended)[/dim]")
        console.print("  [bold yellow]2.[/bold yellow] [bold white]FLAC[/bold white] [dim](Lossless Studio High-Resolution)[/dim]")
        console.print("  [bold yellow]3.[/bold yellow] [bold white]WAV[/bold white]  [dim](Uncompressed Lossless Audio)[/dim]")
        console.print("  [bold yellow]4.[/bold yellow] [bold white]M4A[/bold white]  [dim](High Efficiency AAC Audio)[/dim]")
        
        f_map = {"1": "mp3", "2": "flac", "3": "wav", "4": "m4a"}
        f_choice = Prompt.ask(
            "[bold cyan]Select Format (1-4)[/bold cyan]",
            choices=["1", "2", "3", "4"],
            default="1",
            show_choices=False
        )
        format_choice = f_map[f_choice]

        # Step 4: Storage Option Selection
        console.print("\n[bold cyan]───────────────── 📦 Step 4: Storage Mode ─────────────────[/bold cyan]")
        console.print("  [bold yellow]1.[/bold yellow] [bold white]Only ZIP File[/bold white] [dim](Packs into .zip & auto-cleans raw files - Recommended)[/dim]")
        console.print("  [bold yellow]2.[/bold yellow] [bold white]Only Uncompressed Folder[/bold white] [dim](Direct audio files inside folder)[/dim]")
        console.print("  [bold yellow]3.[/bold yellow] [bold white]Both (ZIP + Folder)[/bold white] [dim](Keep .zip file AND songs folder)[/dim]")

        storage_choice = Prompt.ask(
            "[bold cyan]Select Storage Mode (1-3)[/bold cyan]",
            choices=["1", "2", "3"],
            default="1",
            show_choices=False
        )

        if storage_choice == "1":
            do_zip = True
            keep_folder = False
        elif storage_choice == "2":
            do_zip = False
            keep_folder = True
        else: # "3"
            do_zip = True
            keep_folder = True

        # Step 5: Save Location Selection
        console.print("\n[bold cyan]───────────────── 📁 Step 5: Destination Folder ─────────────────[/bold cyan]")
        console.print(f"  [bold yellow]1.[/bold yellow] [bold white]Default Downloads Folder[/bold white] [dim]({DOWNLOADS_DIR})[/dim]")
        console.print("  [bold yellow]2.[/bold yellow] [bold white]Custom Path[/bold white] [dim](Type your own folder path)[/dim]")
        
        is_win = (os.name == 'nt')
        if is_win:
            console.print("  [bold yellow]3.[/bold yellow] [bold white]Browse Window[/bold white] [dim](Open Windows folder picker popup)[/dim]")
            dest_choices = ["1", "2", "3"]
            dest_prompt = "[bold cyan]Select Destination (1-3)[/bold cyan]"
        else:
            dest_choices = ["1", "2"]
            dest_prompt = "[bold cyan]Select Destination (1-2)[/bold cyan]"

        dest_choice = Prompt.ask(
            dest_prompt,
            choices=dest_choices,
            default="1",
            show_choices=False
        )

        if dest_choice == "1":
            chosen_dir = DOWNLOADS_DIR
        elif dest_choice == "2":
            custom_path = Prompt.ask("[bold yellow]Enter Custom Folder Path[/bold yellow]").strip()
            chosen_dir = Path(custom_path) if custom_path else DOWNLOADS_DIR
        else: # "3" (Windows only)
            console.print("[cyan]Opening folder picker window...[/cyan]")
            gui_folder = pick_folder_gui()
            chosen_dir = Path(gui_folder) if gui_folder else DOWNLOADS_DIR

        # Step 6: Post-Download Action (Only on Windows GUI environments)
        if is_win:
            console.print("\n[bold cyan]───────────────── 🚀 Step 6: Completion Action ─────────────────[/bold cyan]")
            open_folder = Confirm.ask("[bold blue]📂 Open destination folder when download finishes?[/bold blue]", default=True)
        else:
            open_folder = False

        process_download(
            url=url,
            output_dir=chosen_dir,
            quality=quality_choice,
            audio_format=format_choice,
            do_zip=do_zip,
            keep_folder=keep_folder,
            open_folder=open_folder
        )

        # Step 7: Continuous Download Prompt Loop
        console.print("\n[bold cyan]─────────────────────────────────────────────────────────────────[/bold cyan]")
        again = Confirm.ask("[bold yellow]🔄 Would you like to download another song or playlist?[/bold yellow]", default=True)
        if not again:
            console.print("\n[bold magenta]👋 Thank you for using CMD Music Downloader! Enjoy your high quality music! 🎧[/bold magenta]\n")
            break

def main():
    parser = argparse.ArgumentParser(
        description="CMD Music Downloader: Download YouTube & Spotify playlists in high quality 320kbps MP3 and create ZIP archives."
    )
    parser.add_argument("--url", "-u", type=str, help="YouTube or Spotify Playlist/Song URL")
    parser.add_argument("--output", "-o", type=str, default=None, help="Destination directory to save songs & ZIP")
    parser.add_argument("--quality", "-q", type=str, default="320", choices=["320", "256", "192", "128"], help="Audio bitrate in kbps (default: 320)")
    parser.add_argument("--format", "-f", type=str, default="mp3", choices=["mp3", "flac", "wav", "m4a"], help="Audio output format (default: mp3)")
    parser.add_argument("--mode", "-m", type=str, default="zip", choices=["zip", "folder", "both"], help="Output mode: 'zip' (only zip), 'folder' (only raw songs), 'both' (keep both)")
    parser.add_argument("--zip", "-z", action="store_true", default=None, help="Create a ZIP file of downloaded songs")
    parser.add_argument("--no-zip", action="store_true", default=False, help="Do not create a ZIP file (same as --mode folder)")
    parser.add_argument("--keep-folder", action="store_true", default=False, help="Keep uncompressed songs folder alongside the ZIP archive")
    parser.add_argument("--open", action="store_true", help="Open folder in Explorer after completion")

    args = parser.parse_args()

    if args.url:
        print_banner()
        ensure_ffmpeg(console)
        dest = Path(args.output) if args.output else DOWNLOADS_DIR

        # Determine zip and keep_folder settings
        if args.no_zip or args.mode == "folder":
            do_zip = False
            keep_folder = True
        elif args.mode == "both" or args.keep_folder:
            do_zip = True
            keep_folder = True
        else: # "zip"
            do_zip = True
            keep_folder = False

        process_download(
            url=args.url,
            output_dir=dest,
            quality=args.quality,
            audio_format=args.format,
            do_zip=do_zip,
            keep_folder=keep_folder,
            open_folder=args.open
        )
    else:
        interactive_mode()

if __name__ == "__main__":
    main()
