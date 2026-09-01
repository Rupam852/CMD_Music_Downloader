import re
import json
import os
import sys
import urllib.parse
from pathlib import Path
import requests
import yt_dlp
from rich.console import Console
from rich.progress import Progress, SpinnerColumn, TextColumn, DownloadColumn, TransferSpeedColumn, TimeRemainingColumn
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
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36',
    'Accept-Language': 'en-US,en;q=0.9',
}

def sanitize_filename(name: str) -> str:
    """Sanitizes strings for Windows filesystem safety."""
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
    Extracts full track list and metadata from Spotify using embed page and official Web API token.
    Returns (collection_title, list_of_track_dicts).
    """
    entity_type, entity_id = parse_spotify_url(url)
    if not entity_type or not entity_id:
        raise ValueError("Invalid Spotify URL format. Expected: https://open.spotify.com/playlist/... or track/... or album/...")

    embed_url = f"https://open.spotify.com/embed/{entity_type}/{entity_id}"
    res = requests.get(embed_url, headers=HEADERS, timeout=15)
    if res.status_code != 200:
        raise ValueError(f"Could not reach Spotify embed page (HTTP {res.status_code}).")

    tracks = []
    collection_title = "Spotify_Music"
    token = None

    match = re.search(r'<script[^>]+id=["\']__NEXT_DATA__["\'][^>]*>(.*?)</script>', res.text)
    if match:
        try:
            next_data = json.loads(match.group(1))
            state_data = next_data.get('props', {}).get('pageProps', {}).get('state', {}).get('data', {})
            entity = state_data.get('entity', {})
            settings = state_data.get('settings', {})
            token = settings.get('session', {}).get('accessToken')
            
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
                artist = entity.get('subtitle') or ", ".join([a.get('name', '') for a in entity.get('artists', [])])
                tracks.append({
                    'title': title,
                    'artist': artist,
                    'album': collection_title,
                    'year': '',
                    'cover_url': cover_url,
                    'track_number': 1,
                })
            else:
                track_list = entity.get('trackList', [])
                for idx, tr in enumerate(track_list, 1):
                    tracks.append({
                        'title': tr.get('title', 'Unknown Title'),
                        'artist': tr.get('subtitle', 'Unknown Artist'),
                        'album': collection_title,
                        'year': '',
                        'cover_url': cover_url,
                        'track_number': idx,
                    })
        except Exception:
            pass

    if token:
        try:
            api_headers = {'Authorization': f'Bearer {token}'}
            if entity_type == 'playlist':
                api_url = f"https://api.spotify.com/v1/playlists/{entity_id}"
                api_res = requests.get(api_url, headers=api_headers, timeout=10)
                if api_res.status_code == 200:
                    pdata = api_res.json()
                    collection_title = pdata.get('name', collection_title)
                    images = pdata.get('images', [])
                    playlist_cover = images[0]['url'] if images else ""

                    api_tracks = []
                    track_items = pdata.get('tracks', {}).get('items', [])
                    for i, item in enumerate(track_items, 1):
                        tr = item.get('track')
                        if tr and tr.get('name'):
                            artist_name = ", ".join([a['name'] for a in tr.get('artists', [])])
                            album_name = tr.get('album', {}).get('name', collection_title)
                            year = tr.get('album', {}).get('release_date', '')[:4]
                            tr_imgs = tr.get('album', {}).get('images', [])
                            tr_cover = tr_imgs[0]['url'] if tr_imgs else playlist_cover
                            api_tracks.append({
                                'title': tr.get('name'),
                                'artist': artist_name,
                                'album': album_name,
                                'year': year,
                                'cover_url': tr_cover,
                                'track_number': i,
                            })
                    if api_tracks:
                        tracks = api_tracks
            elif entity_type == 'album':
                api_url = f"https://api.spotify.com/v1/albums/{entity_id}"
                api_res = requests.get(api_url, headers=api_headers, timeout=10)
                if api_res.status_code == 200:
                    adata = api_res.json()
                    collection_title = adata.get('name', collection_title)
                    album_artists = ", ".join([a['name'] for a in adata.get('artists', [])])
                    year = adata.get('release_date', '')[:4]
                    images = adata.get('images', [])
                    album_cover = images[0]['url'] if images else ""

                    api_tracks = []
                    for i, tr in enumerate(adata.get('tracks', {}).get('items', []), 1):
                        artist_name = ", ".join([a['name'] for a in tr.get('artists', [])]) or album_artists
                        api_tracks.append({
                            'title': tr.get('name'),
                            'artist': artist_name,
                            'album': collection_title,
                            'year': year,
                            'cover_url': album_cover,
                            'track_number': i,
                        })
                    if api_tracks:
                        tracks = api_tracks
            elif entity_type == 'track':
                api_url = f"https://api.spotify.com/v1/tracks/{entity_id}"
                api_res = requests.get(api_url, headers=api_headers, timeout=10)
                if api_res.status_code == 200:
                    tdata = api_res.json()
                    artist_name = ", ".join([a['name'] for a in tdata.get('artists', [])])
                    album_name = tdata.get('album', {}).get('name', '')
                    year = tdata.get('album', {}).get('release_date', '')[:4]
                    images = tdata.get('album', {}).get('images', [])
                    cover_url = images[0]['url'] if images else ""
                    tracks = [{
                        'title': tdata.get('name'),
                        'artist': artist_name,
                        'album': album_name,
                        'year': year,
                        'cover_url': cover_url,
                        'track_number': 1,
                    }]
                    collection_title = f"{artist_name} - {tdata.get('name')}"
        except Exception:
            pass

    if not tracks:
        try:
            oembed_url = f"https://open.spotify.com/oembed?url={urllib.parse.quote(url)}"
            oembed_res = requests.get(oembed_url, headers=HEADERS, timeout=10)
            if oembed_res.status_code == 200:
                odata = oembed_res.json()
                title = odata.get('title', 'Spotify Track')
                tracks.append({
                    'title': title,
                    'artist': '',
                    'album': 'Spotify Music',
                    'year': '',
                    'cover_url': odata.get('thumbnail_url', ''),
                    'track_number': 1,
                })
                collection_title = title
        except Exception:
            pass

    if not tracks:
        raise ValueError("Could not extract tracks from Spotify link. Please check that the link is accessible.")

    return sanitize_filename(collection_title), tracks

def download_spotify(
    url: str,
    output_dir: Path = None,
    bitrate: str = DEFAULT_BITRATE,
    audio_format: str = DEFAULT_AUDIO_FORMAT,
    console: Console = None
) -> tuple[Path, str]:
    """
    Downloads Spotify tracks with classic box progress bar: [██████░░░░] 0% to 100%.
    """
    if console is None:
        console = Console()

    ffmpeg_path = ensure_ffmpeg(console)
    ffmpeg_dir = str(Path(ffmpeg_path).parent) if ffmpeg_path else None

    console.print("\n[bold cyan]🔍 Fetching Spotify playlist metadata...[/bold cyan]")
    collection_title, tracks = fetch_spotify_metadata(url, console)

    base_output = Path(output_dir) if output_dir else DOWNLOADS_DIR
    target_dir = base_output / collection_title
    target_dir.mkdir(parents=True, exist_ok=True)

    console.print(f"[bold green]🎵 Target: {collection_title} ({len(tracks)} songs)[/bold green]")
    console.print(f"[bold cyan]📁 Destination: {target_dir}[/bold cyan]\n")

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
            title="Starting Spotify download..."
        )

        for i, track in enumerate(tracks, 1):
            if track.get('artist'):
                song_name = f"{track['artist']} - {track['title']}"
                query = f"ytsearch1:{track['artist']} {track['title']} audio"
            else:
                song_name = track['title']
                query = f"ytsearch1:{track['title']} audio"

            safe_name = sanitize_filename(song_name)
            output_file = target_dir / f"{safe_name}.{audio_format}"
            short_name = (song_name[:25] + "..") if len(song_name) > 25 else song_name

            progress.update(
                download_task,
                total=100,
                completed=0,
                title=f"[{i}/{len(tracks)}] Searching & Downloading: {short_name}"
            )

            if output_file.exists():
                console.print(f"[dim green]⏩ [{i}/{len(tracks)}] Already exists: {safe_name}[/dim green]")
                continue

            def spotify_hook(d):
                if d.get('status') == 'downloading':
                    total_bytes = d.get('total_bytes') or d.get('total_bytes_estimate') or 1
                    downloaded = d.get('downloaded_bytes') or 0
                    progress.update(
                        download_task,
                        total=total_bytes,
                        completed=downloaded,
                        title=f"[{i}/{len(tracks)}] {short_name}"
                    )
                elif d.get('status') == 'finished':
                    progress.update(download_task, title=f"🔄 Processing: {short_name}")

            track_opts = {
                'format': 'bestaudio/best',
                'outtmpl': str(target_dir / f"{safe_name}.%(ext)s"),
                'ffmpeg_location': ffmpeg_dir or ffmpeg_path,
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
                        total_tracks=len(tracks),
                        year=track['year']
                    )
                    console.print(f"[bold green]✅ [{i}/{len(tracks)}] Saved: {safe_name}[/bold green]")
            except Exception as e:
                console.print(f"[red]⚠️ Error downloading {song_name}: {e}[/red]")

        progress.update(download_task, completed=100, total=100, title="[bold green]✅ Spotify Download Completed![/bold green]")

    console.print(f"\n[bold green]✅ All Spotify songs successfully saved to:[/bold green] [yellow]{target_dir}[/yellow]")
    return target_dir, collection_title
