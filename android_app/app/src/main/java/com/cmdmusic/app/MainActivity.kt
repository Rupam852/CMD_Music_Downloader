package com.cmdmusic.app

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.cmdmusic.app.data.model.Playlist
import com.cmdmusic.app.data.model.Song
import com.cmdmusic.app.service.MusicService
import com.cmdmusic.app.ui.components.NowPlayingBar
import com.cmdmusic.app.ui.screens.HomeScreen
import com.cmdmusic.app.ui.screens.ImportPlaylistDialog
import com.cmdmusic.app.ui.screens.NowPlayingScreen
import com.cmdmusic.app.ui.theme.AmoledBackground
import com.cmdmusic.app.ui.theme.CMDMusicTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    // Android 13+ Runtime Permissions Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Enable Hardware Acceleration & Max Display Refresh Rate (90Hz / 120Hz / 144Hz)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        enableMaxRefreshRate()

        // 2. Edge-to-Edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 3. Android 13+ (API 33+) Runtime Permissions Request
        checkAndRequestAndroid13Permissions()

        val app = application as MusicApplication
        val repository = app.repository

        // 4. Connect to Media3 Service
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
        }, MoreExecutors.directExecutor())

        setContent {
            CMDMusicTheme {
                val coroutineScope = rememberCoroutineScope()
                val playlists by repository.allPlaylists.collectAsState(initial = emptyList())
                val songs by repository.allSongs.collectAsState(initial = emptyList())

                var currentSong by remember { mutableStateOf<Song?>(null) }
                var isPlaying by remember { mutableStateOf(false) }
                var isNowPlayingExpanded by remember { mutableStateOf(false) }
                var showImportDialog by remember { mutableStateOf(false) }
                var currentPositionMs by remember { mutableStateOf(0L) }
                var durationMs by remember { mutableStateOf(0L) }

                // Playback listener
                DisposableEffect(mediaController) {
                    val listener = object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            durationMs = mediaController?.duration ?: 0L
                        }
                    }
                    mediaController?.addListener(listener)
                    onDispose {
                        mediaController?.removeListener(listener)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AmoledBackground
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        HomeScreen(
                            playlists = playlists,
                            songs = songs,
                            currentSong = currentSong,
                            onSongClick = { song ->
                                currentSong = song
                                playSong(song)
                            },
                            onAddPlaylistClick = { showImportDialog = true },
                            onCastClick = { /* Trigger Google Cast dialog */ }
                        )

                        // Floating Mini-Player Bar
                        if (currentSong != null && !isNowPlayingExpanded) {
                            NowPlayingBar(
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                onPlayPauseClick = { togglePlayPause() },
                                onNextClick = { mediaController?.seekToNext() },
                                onCastClick = { /* Cast */ },
                                onClick = { isNowPlayingExpanded = true },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }

                        // Full Screen Now Playing Sheet
                        if (isNowPlayingExpanded) {
                            NowPlayingScreen(
                                song = currentSong,
                                isPlaying = isPlaying,
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onPlayPauseClick = { togglePlayPause() },
                                onPreviousClick = { mediaController?.seekToPrevious() },
                                onNextClick = { mediaController?.seekToNext() },
                                onSeek = { pos -> mediaController?.seekTo(pos) },
                                onCastClick = { /* Cast */ },
                                onCloseClick = { isNowPlayingExpanded = false }
                            )
                        }

                        // Import Playlist Dialog
                        if (showImportDialog) {
                            ImportPlaylistDialog(
                                onDismiss = { showImportDialog = false },
                                onImport = { url, quality, format ->
                                    coroutineScope.launch {
                                        val isSpotify = url.contains("spotify", ignoreCase = true)
                                        val playlist = Playlist(
                                            id = System.currentTimeMillis().toString(),
                                            name = if (isSpotify) "Spotify Collection" else "YouTube Playlist",
                                            sourcePlatform = if (isSpotify) "SPOTIFY" else "YOUTUBE",
                                            originalUrl = url
                                        )
                                        repository.savePlaylist(playlist, emptyList())
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestAndroid13Permissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            val permissionsToRequest = mutableListOf<String>()
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    private fun enableMaxRefreshRate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val modes = window.windowManager.defaultDisplay.supportedModes
            val highestRefreshRateMode = modes.maxByOrNull { it.refreshRate }
            if (highestRefreshRateMode != null) {
                val params = window.attributes
                params.preferredDisplayModeId = highestRefreshRateMode.modeId
                window.attributes = params
            }
        }
    }

    private fun playSong(song: Song) {
        val mediaItem = MediaItem.fromUri(song.localFilePath ?: song.streamUrl)
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
    }

    private fun togglePlayPause() {
        if (mediaController?.isPlaying == true) {
            mediaController?.pause()
        } else {
            mediaController?.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
