package com.cmdmusic.app.data.repository

import com.cmdmusic.app.data.db.PlaylistDao
import com.cmdmusic.app.data.db.SongDao
import com.cmdmusic.app.data.model.Playlist
import com.cmdmusic.app.data.model.Song
import kotlinx.coroutines.flow.Flow

class MusicRepository(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val downloadedSongs: Flow<List<Song>> = songDao.getDownloadedSongs()

    fun getSongsByPlaylist(playlistId: String): Flow<List<Song>> {
        return songDao.getSongsByPlaylist(playlistId)
    }

    suspend fun savePlaylist(playlist: Playlist, songs: List<Song>) {
        playlistDao.insertPlaylist(playlist)
        songDao.insertSongs(songs)
    }

    suspend fun updateSong(song: Song) {
        songDao.insertSong(song)
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }
}
