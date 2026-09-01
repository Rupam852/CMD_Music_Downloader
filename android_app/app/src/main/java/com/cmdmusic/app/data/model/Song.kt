package com.cmdmusic.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long = 0,
    val artworkUrl: String? = null,
    val streamUrl: String,
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false,
    val playlistId: String? = null,
    val addedTimestamp: Long = System.currentTimeMillis()
)
