package com.cmdmusic.app.data.db

import androidx.room.*
import com.cmdmusic.app.data.model.Playlist
import com.cmdmusic.app.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY addedTimestamp DESC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE playlistId = :playlistId")
    fun getSongsByPlaylist(playlistId: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1")
    fun getDownloadedSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM songs WHERE playlistId = :playlistId")
    suspend fun deleteSongsByPlaylist(playlistId: String)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdTimestamp DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)
}
