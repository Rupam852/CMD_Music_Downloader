import os
import urllib.request
from pathlib import Path
from mutagen.id3 import ID3, TIT2, TPE1, TALB, APIC, TRCK, TDRC, ID3NoHeaderError
from mutagen.easyid3 import EasyID3

def embed_metadata(
    file_path: str | Path,
    title: str,
    artist: str,
    album: str = "",
    cover_url: str = "",
    track_number: int = None,
    total_tracks: int = None,
    year: str = ""
) -> bool:
    """
    Embeds ID3 metadata and album art cover into an MP3 file.
    """
    file_path = Path(file_path)
    if not file_path.exists() or not file_path.suffix.lower() == ".mp3":
        return False

    try:
        # Load or create ID3 header
        try:
            audio = ID3(file_path)
        except ID3NoHeaderError:
            audio = ID3()

        # Set text metadata
        if title:
            audio["TIT2"] = TIT2(encoding=3, text=title)
        if artist:
            audio["TPE1"] = TPE1(encoding=3, text=artist)
        if album:
            audio["TALB"] = TALB(encoding=3, text=album)
        if year:
            audio["TDRC"] = TDRC(encoding=3, text=str(year))
        if track_number is not None:
            trck_str = f"{track_number}/{total_tracks}" if total_tracks else str(track_number)
            audio["TRCK"] = TRCK(encoding=3, text=trck_str)

        # Fetch and embed cover image
        if cover_url:
            try:
                req = urllib.request.Request(
                    cover_url,
                    headers={'User-Agent': 'Mozilla/5.0'}
                )
                with urllib.request.urlopen(req, timeout=10) as response:
                    image_data = response.read()
                    mime_type = 'image/jpeg'
                    if cover_url.lower().endswith('.png'):
                        mime_type = 'image/png'

                    audio["APIC"] = APIC(
                        encoding=3,
                        mime=mime_type,
                        type=3, # Cover (front)
                        desc='Cover',
                        data=image_data
                    )
            except Exception as img_err:
                pass # Silently proceed if cover download fails

        audio.save(file_path, v2_version=3)
        return True
    except Exception as e:
        return False
