import re
import json
import os
import sys
import urllib.parse
from pathlib import Path
import requests
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
from core.tagger import embed_metadata
from core.progress import ClassicBoxBarColumn

# Ensure UTF-8 output encoding
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36',
    'Accept-Language': 'en-US,en;q=0.9',
}

# Universal player clients to bypass YouTube bot detection & datacenter IP blocks (Cloud Shell / VPS)
YOUTUBE_EXTRACTOR_ARGS = {
    'youtube': {
        'player_client': ['ios', 'android', 'web_embedded', 'tv_embedded'],
        'player_skip': ['webpage', 'configs'],
    },
    'youtubetab': {
        'skip': ['webpage'],
    }
}

def sanitize_filename(name: str) -> str:
    """Sanitizes strings for Windows/Linux filesystem safety."""
    return re.sub(r'[\\/*?:"<>|]', "", name).strip()

def parse_spotify_url(url: str) -> tuple[str, str] | tuple[None, None]:
    """
    Extracts entity type ('track', 'playlist', 'album') and entity ID from a Spotify URL.
    """
    parsed = urllib.parse.urlparse(url)
    path_parts = parsed.path.strip("/").split("/")
    if len(path_parts) >= 2:
        entity_type = path_parts[0]
        entity_id = path_parts[1].split("?")[0]
        return entity_type, entity_id
    return None, None

def fetch_spotify_metadata(url: str, console: Console = None) -> tuple[str, list[dict]]:
    """
    Extracts accurate track list and metadata from Spotify using embed page and oEmbed.
    Returns (collection_title, list_of_track_dicts).
    """
    entity_type, entity_id = parse_spotify_url(url)
    if not entity_type or not entity_id:
        raise ValueError("Invalid Spotify URL format. Expected: https://open.spotify.com/playlist/... or track/... or album/...")

    embed_url = f"https://open.spotify.com/embed/{entity_type}/{entity_id}"
    res = requests.get(embed_url, headers=HEADERS, timeout=15)
    if res.status_code != 200:
        raise ValueError(f"Could not reach Spotify embed page (HTTP {res.status_code}).")

    raw_tracks = []
    collection_title = "Spotify_Music"

    match = re.search(r'<script[^>]+id=["\']__NEXT_DATA__["\'][^>]*>(.*?)</script>', res.text)
    if match:
        try:
            next_data = json.loads(match.group(1))
            state_data = next_data.get('props', {}).get('pageProps', {}).get('state', {}).get('data', {})
            entity = state_data.get('entity', {})
            
            collection_title = entity.get('name') or entity.get('title') or collection_title

            cover_sources = (
                entity.get('coverArt', {}).get('sources', [])
                or entity.get('visualIdentity', {}).get('image', [])
                or entity.get('images', [])
            )
            cover_url = cover_sources[-1].get('url', '') if cover_sources else ''
            if not cover_url and entity.get('coverArt', {}).get('sources'):
                cover_url = entity['coverArt']['sources'][0].get('url', '')

            if entity_type == 'track':
                title = entity.get('title') or entity.get('name', 'Track')
                artist = entity.get('subtitle') or ", ".join([a.get('name', '') for a in entity.get('artists', []) if a.get('name')])
                raw_tracks.append({
                    'title': title.strip(),
                    'artist': artist.strip(),
                    'album': collection_title.strip(),
                    'year': '',
                    'cover_url': cover_url,
                })
            else:
                track_list = entity.get('trackList', [])
                for tr in track_list:
                    t_title = tr.get('title', '').strip()
                    t_artist = tr.get('subtitle', '').strip()
                    if t_title:
                        raw_tracks.append({
                            'title': t_title,
                            'artist': t_artist,
                            'album': collection_title.strip(),
                            'year': '',
                            'cover_url': cover_url,
                        })
        except Exception:
            pass

    # Fallback via oEmbed if empty
    if not raw_tracks:
        try:
            oembed_url = f"https://open.spotify.com/oembed?url={urllib.parse.quote(url)}"
            oembed_res = requests.get(oembed_url, headers=HEADERS, timeout=10)
            if oembed_res.status_code == 200:
                odata = oembed_res.json()
                title = odata.get('title', 'Spotify Track').strip()
                raw_tracks.append({
                    'title': title,
                    'artist': '',
                    'album': 'Spotify Music',
                    'year': '',
                    'cover_url': odata.get('thumbnail_url', ''),
                })
                collection_title = title
        except Exception:
            pass

    if not raw_tracks:
        raise ValueError("Could not extract tracks from Spotify link. Please check that the link is public and accessible.")

    # Deduplicate tracks while preserving exact playlist order
    tracks = []
    seen = set()
    for tr in raw_tracks:
        key = (tr['title'].lower(), tr['artist'].lower())
        if key not in seen:
            seen.add(key)
            tr['track_number'] = len(tracks) + 1
            tracks.append(tr)

    clean_collection_title = sanitize_filename(collection_title) or "Spotify_Playlist"
    return clean_collection_title, tracks

def download_spotify(
    url: str,
    output_dir: Path = None,
    bitrate: str = DEFAULT_BITRATE,
    audio_format: str = DEFAULT_AUDIO_FORMAT,
    console: Console = None
) -> tuple[Path, str]:
    """
    Downloads Spotify tracks with clean, dedicated per-song progress bar.
    """
    if console is None:
        console = Console()

    ffmpeg_path = ensure_ffmpeg(console)
    ffmpeg_dir = str(Path(ffmpeg_path).parent) if ffmpeg_path else None

    console.print("\n[bold cyan]🔍 Fetching Spotify playlist metadata...[/bold cyan]")
    collection_title, tracks = fetch_spotify_metadata(url, console)
    total_tracks = len(tracks)

    base_output = Path(output_dir) if output_dir else DOWNLOADS_DIR
    target_dir = base_output / collection_title
    target_dir.mkdir(parents=True, exist_ok=True)

    console.print(f"[bold green]🎵 Target: {collection_title} ({total_tracks} Songs Total)[/bold green]")
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
        for i, track in enumerate(tracks, 1):
            if track.get('artist'):
                song_name = f"{track['artist']} - {track['title']}"
                query = f"ytsearch1:{track['artist']} {track['title']} audio"
            else:
                song_name = track['title']
                query = f"ytsearch1:{track['title']} audio"

            safe_name = sanitize_filename(song_name)
            output_file = target_dir / f"{safe_name}.{audio_format}"
            short_name = (song_name[:28] + "..") if len(song_name) > 28 else song_name

            if output_file.exists():
                console.print(f"[dim green]⏩ [{i}/{total_tracks}] Already exists: {safe_name}[/dim green]")
                continue

            # Dedicated Single Per-Song Progress Bar
            song_task = progress.add_task(
                f"🎵 [{i}/{total_tracks}] {short_name}",
                total=100,
                completed=0
            )

            def spotify_hook(d):
                if d.get('status') == 'downloading':
                    total_bytes = d.get('total_bytes') or d.get('total_bytes_estimate') or 1
                    downloaded = d.get('downloaded_bytes') or 0
                    progress.update(
                        song_task,
                        total=total_bytes,
                        completed=downloaded,
                        description=f"🎵 [{i}/{total_tracks}] {short_name}"
                    )
                elif d.get('status') == 'finished':
                    progress.update(
                        song_task,
                        description=f"🔄 [{i}/{total_tracks}] Converting & Tagging: {short_name}"
                    )

            track_opts = {
                'format': 'bestaudio/best',
                'outtmpl': str(target_dir / f"{safe_name}.%(ext)s"),
                'ffmpeg_location': ffmpeg_dir or ffmpeg_path,
                'extractor_args': YOUTUBE_EXTRACTOR_ARGS,
                'postprocessors': [
                    {
                        'key': 'FFmpegExtractAudio',
                        'preferredcodec': audio_format,
                        'preferredquality': bitrate,
                    }
                ],
                'quiet': True,
                'no_warnings': True,
                'ignoreerrors': True,
                'progress_hooks': [spotify_hook],
            }

            try:
                with yt_dlp.YoutubeDL(track_opts) as ydl:
                    ydl.download([query])

                if output_file.exists():
                    embed_metadata(
                        file_path=output_file,
                        title=track['title'],
                        artist=track['artist'],
                        album=track['album'],
                        cover_url=track['cover_url'],
                        track_number=i,
                        total_tracks=total_tracks,
                        year=track['year']
                    )
                    console.print(f"[bold green]✅ [{i}/{total_tracks}] Saved: {safe_name}[/bold green]")
                else:
                    console.print(f"[yellow]⚠️ [{i}/{total_tracks}] Conversion completed: {safe_name}[/yellow]")
            except Exception as e:
                console.print(f"[red]❌ [{i}/{total_tracks}] Error downloading {song_name}: {e}[/red]")
            finally:
                progress.remove_task(song_task)

    console.print(f"\n[bold green]✅ All {total_tracks} songs successfully saved to:[/bold green] [yellow]{target_dir}[/yellow]")
    return target_dir, collection_title
