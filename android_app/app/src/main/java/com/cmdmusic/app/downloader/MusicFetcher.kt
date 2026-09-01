package com.cmdmusic.app.downloader

import com.cmdmusic.app.data.model.Playlist
import com.cmdmusic.app.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

object MusicFetcher {

    suspend fun resolveAndFetch(
        inputUrl: String,
        quality: String = "320",
        format: String = "mp3",
        onProgress: (current: Int, total: Int, message: String) -> Unit
    ): Pair<Playlist, List<Song>> = withContext(Dispatchers.IO) {
        val trimmed = inputUrl.trim()
        val isSpotify = trimmed.contains("spotify", ignoreCase = true)
        val playlistId = "pl_${System.currentTimeMillis()}"

        if (isSpotify) {
            fetchSpotifyPlaylist(trimmed, playlistId, onProgress)
        } else {
            fetchYouTubePlaylist(trimmed, playlistId, onProgress)
        }
    }

    private fun fetchSpotifyPlaylist(
        urlStr: String,
        playlistId: String,
        onProgress: (current: Int, total: Int, message: String) -> Unit
    ): Pair<Playlist, List<Song>> {
        onProgress(0, 0, "Connecting to Spotify...")

        // Match playlist or album ID
        val playlistMatcher = Pattern.compile("playlist/([a-zA-Z0-9]+)").matcher(urlStr)
        val albumMatcher = Pattern.compile("album/([a-zA-Z0-9]+)").matcher(urlStr)
        val trackMatcher = Pattern.compile("track/([a-zA-Z0-9]+)").matcher(urlStr)

        val songs = mutableListOf<Song>()
        var playlistTitle = "Spotify Collection"
        var playlistCover: String? = null

        try {
            if (playlistMatcher.find() || albumMatcher.find()) {
                val isAlbum = albumMatcher.reset().find()
                val id = if (isAlbum) albumMatcher.group(1) else {
                    playlistMatcher.reset()
                    playlistMatcher.find()
                    playlistMatcher.group(1)
                }
                val embedType = if (isAlbum) "album" else "playlist"
                val embedUrl = "https://open.spotify.com/embed/$embedType/$id"

                onProgress(1, 0, "Extracting playlist tracks...")
                val html = httpGet(embedUrl)

                // Extract __NEXT_DATA__
                val nextDataPattern = Pattern.compile("<script id=\"__NEXT_DATA__\" type=\"application/json\">(.*?)</script>", Pattern.DOTALL)
                val matcher = nextDataPattern.matcher(html)

                if (matcher.find()) {
                    val jsonText = matcher.group(1) ?: "{}"
                    val root = JSONObject(jsonText)
                    val state = root.optJSONObject("props")?.optJSONObject("pageProps")?.optJSONObject("state")
                    val entity = state?.optJSONObject("data")?.optJSONObject("entity")

                    playlistTitle = entity?.optString("title", playlistTitle) ?: playlistTitle
                    
                    val visualIdentity = entity?.optJSONObject("visualIdentity")
                    playlistCover = visualIdentity?.optJSONObject("image")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")
                        ?: entity?.optJSONObject("coverArt")?.optJSONArray("sources")?.optJSONObject(0)?.optString("url")

                    val trackList = entity?.optJSONArray("trackList") ?: JSONArray()
                    val totalTracks = trackList.length()

                    for (i in 0 until totalTracks) {
                        val item = trackList.optJSONObject(i) ?: continue
                        val trackTitle = item.optString("title", "Unknown Track")
                        val subtitle = item.optString("subtitle", "Various Artists")
                        val duration = item.optLong("duration", 0L)
                        val uri = item.optString("uri", "")
                        val trackId = "sp_${playlistId}_$i"

                        onProgress(i + 1, totalTracks, "Loading: $trackTitle - $subtitle")

                        songs.add(
                            Song(
                                id = trackId,
                                title = trackTitle,
                                artist = subtitle,
                                album = playlistTitle,
                                durationMs = duration,
                                artworkUrl = playlistCover,
                                streamUrl = if (uri.isNotEmpty()) "https://open.spotify.com/track/${uri.substringAfterLast(":")}" else urlStr,
                                playlistId = playlistId,
                                isDownloaded = false
                            )
                        )
                    }
                }
            } else if (trackMatcher.find()) {
                onProgress(1, 1, "Resolving single track...")
                val oembedUrl = "https://open.spotify.com/oembed?url=${URLEncoder.encode(urlStr, "UTF-8")}"
                val json = JSONObject(httpGet(oembedUrl))
                playlistTitle = json.optString("title", "Spotify Track")
                playlistCover = json.optString("thumbnail_url", "").ifEmpty { null }

                songs.add(
                    Song(
                        id = "sp_${System.currentTimeMillis()}",
                        title = playlistTitle,
                        artist = "Spotify Artist",
                        album = "Spotify Single",
                        artworkUrl = playlistCover,
                        streamUrl = urlStr,
                        playlistId = playlistId,
                        isDownloaded = false
                    )
                )
            }
        } catch (e: Exception) {
            // Fallback parsing via oEmbed
            try {
                val oembedUrl = "https://open.spotify.com/oembed?url=${URLEncoder.encode(urlStr, "UTF-8")}"
                val json = JSONObject(httpGet(oembedUrl))
                playlistTitle = json.optString("title", playlistTitle)
                playlistCover = json.optString("thumbnail_url", "").ifEmpty { null }

                songs.add(
                    Song(
                        id = "sp_${System.currentTimeMillis()}",
                        title = playlistTitle,
                        artist = "Spotify",
                        album = playlistTitle,
                        artworkUrl = playlistCover,
                        streamUrl = urlStr,
                        playlistId = playlistId
                    )
                )
            } catch (_: Exception) {}
        }

        if (songs.isEmpty()) {
            songs.add(
                Song(
                    id = "sp_${System.currentTimeMillis()}",
                    title = playlistTitle,
                    artist = "Spotify Music",
                    album = "Single",
                    streamUrl = urlStr,
                    playlistId = playlistId
                )
            )
        }

        val playlist = Playlist(
            id = playlistId,
            name = playlistTitle,
            sourcePlatform = "SPOTIFY",
            originalUrl = urlStr,
            coverUrl = playlistCover,
            trackCount = songs.size
        )

        onProgress(songs.size, songs.size, "Completed! Added ${songs.size} tracks.")
        return Pair(playlist, songs)
    }

    private fun fetchYouTubePlaylist(
        urlStr: String,
        playlistId: String,
        onProgress: (current: Int, total: Int, message: String) -> Unit
    ): Pair<Playlist, List<Song>> {
        onProgress(1, 1, "Resolving YouTube audio stream...")

        var playlistTitle = "YouTube Music"
        var playlistCover: String? = null
        var author = "YouTube Creator"

        try {
            val oembedUrl = "https://www.youtube.com/oembed?url=${URLEncoder.encode(urlStr, "UTF-8")}&format=json"
            val json = JSONObject(httpGet(oembedUrl))
            playlistTitle = json.optString("title", playlistTitle)
            author = json.optString("author_name", author)
            playlistCover = json.optString("thumbnail_url", "").ifEmpty { null }
        } catch (_: Exception) {}

        val song = Song(
            id = "yt_${System.currentTimeMillis()}",
            title = playlistTitle,
            artist = author,
            album = "YouTube Track",
            artworkUrl = playlistCover,
            streamUrl = urlStr,
            playlistId = playlistId,
            isDownloaded = false
        )

        val playlist = Playlist(
            id = playlistId,
            name = playlistTitle,
            sourcePlatform = "YOUTUBE",
            originalUrl = urlStr,
            coverUrl = playlistCover,
            trackCount = 1
        )

        onProgress(1, 1, "Completed! Added $playlistTitle")
        return Pair(playlist, listOf(song))
    }

    private fun httpGet(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 10000
        conn.readTimeout = 10000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")

        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}
