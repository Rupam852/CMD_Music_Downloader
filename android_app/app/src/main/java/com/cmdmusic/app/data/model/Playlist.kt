package com.cmdmusic.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val sourcePlatform: String, // "YOUTUBE" or "SPOTIFY"
    val originalUrl: String,
    val coverUrl: String? = null,
    val trackCount: Int = 0,
    val isFullyDownloaded: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
