package com.cmdmusic.app

import android.app.Application
import com.cmdmusic.app.data.db.AppDatabase
import com.cmdmusic.app.data.repository.MusicRepository

class MusicApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: MusicRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = MusicRepository(database.songDao(), database.playlistDao())
    }
}
