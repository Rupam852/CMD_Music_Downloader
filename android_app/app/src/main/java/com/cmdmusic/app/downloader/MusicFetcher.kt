package com.cmdmusic.app.downloader

import com.cmdmusic.app.data.model.Playlist
import com.cmdmusic.app.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object MusicFetcher {

    suspend fun resolveAndFetch(
        inputUrl: String,
        quality: String = "320",
        format: String = "mp3"
    ): Pair<Playlist, List<Song>> = withContext(Dispatchers.IO) {
        val trimmed = inputUrl.trim()
        val isSpotify = trimmed.contains("spotify", ignoreCase = true)
        val playlistId = "pl_${System.currentTimeMillis()}"

        if (isSpotify) {
            fetchSpotify(trimmed, playlistId)
        } else {
            fetchYouTube(trimmed, playlistId)
        }
    }

    private fun fetchSpotify(urlStr: String, playlistId: String): Pair<Playlist, List<Song>> {
        return try {
            val oembedUrl = "https://open.spotify.com/oembed?url=${URLEncoder.encode(urlStr, "UTF-8")}"
            val jsonStr = httpGet(oembedUrl)
            val json = JSONObject(jsonStr)

            val title = json.optString("title", "Spotify Music")
            val thumbnail = json.optString("thumbnail_url", "")
            val entityType = json.optString("type", "rich")

            val songs = mutableListOf<Song>()
            val songId = "sp_${System.currentTimeMillis()}"

            // Create initial song entry
            val song = Song(
                id = songId,
                title = title,
                artist = "Spotify Artist",
                album = "Spotify Album",
                artworkUrl = thumbnail.ifEmpty { null },
                streamUrl = urlStr,
                playlistId = playlistId,
                isDownloaded = false
            )
            songs.add(song)

            val playlist = Playlist(
                id = playlistId,
                name = title,
                sourcePlatform = "SPOTIFY",
                originalUrl = urlStr,
                coverUrl = thumbnail.ifEmpty { null },
                trackCount = songs.size
            )

            Pair(playlist, songs)
        } catch (e: Exception) {
            // Fallback playlist
            val playlist = Playlist(
                id = playlistId,
                name = "Spotify Track",
                sourcePlatform = "SPOTIFY",
                originalUrl = urlStr,
                trackCount = 1
            )
            val song = Song(
                id = "sp_${System.currentTimeMillis()}",
                title = "Spotify Track",
                artist = "Unknown Artist",
                album = "Single",
                streamUrl = urlStr,
                playlistId = playlistId
            )
            Pair(playlist, listOf(song))
        }
    }

    private fun fetchYouTube(urlStr: String, playlistId: String): Pair<Playlist, List<Song>> {
        return try {
            val oembedUrl = "https://www.youtube.com/oembed?url=${URLEncoder.encode(urlStr, "UTF-8")}&format=json"
            val jsonStr = httpGet(oembedUrl)
            val json = JSONObject(jsonStr)

            val title = json.optString("title", "YouTube Audio")
            val author = json.optString("author_name", "YouTube Creator")
            val thumbnail = json.optString("thumbnail_url", "")

            val songId = "yt_${System.currentTimeMillis()}"
            val song = Song(
                id = songId,
                title = title,
                artist = author,
                album = "YouTube Single",
                artworkUrl = thumbnail.ifEmpty { null },
                streamUrl = urlStr,
                playlistId = playlistId,
                isDownloaded = false
            )

            val playlist = Playlist(
                id = playlistId,
                name = title,
                sourcePlatform = "YOUTUBE",
                originalUrl = urlStr,
                coverUrl = thumbnail.ifEmpty { null },
                trackCount = 1
            )

            Pair(playlist, listOf(song))
        } catch (e: Exception) {
            val playlist = Playlist(
                id = playlistId,
                name = "YouTube Music",
                sourcePlatform = "YOUTUBE",
                originalUrl = urlStr,
                trackCount = 1
            )
            val song = Song(
                id = "yt_${System.currentTimeMillis()}",
                title = "YouTube Audio Track",
                artist = "YouTube",
                album = "Single",
                streamUrl = urlStr,
                playlistId = playlistId
            )
            Pair(playlist, listOf(song))
        }
    }

    private fun httpGet(urlStr: String): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")

        return conn.inputStream.bufferedReader().use { it.readText() }
    }
}
