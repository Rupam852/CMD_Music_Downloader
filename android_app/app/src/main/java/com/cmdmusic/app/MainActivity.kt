package com.cmdmusic.app

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
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
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.cmdmusic.app.data.model.Playlist
import com.cmdmusic.app.data.model.Song
import com.cmdmusic.app.downloader.MusicFetcher
import com.cmdmusic.app.service.MusicService
import com.cmdmusic.app.ui.components.ImportProgressDialog
import com.cmdmusic.app.ui.components.NowPlayingBar
import com.cmdmusic.app.ui.screens.HomeScreen
import com.cmdmusic.app.ui.screens.ImportPlaylistDialog
import com.cmdmusic.app.ui.screens.NowPlayingScreen
import com.cmdmusic.app.ui.theme.AmoledBackground
import com.cmdmusic.app.ui.theme.CMDMusicTheme
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    // Android 13+ Runtime Permissions Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Handled
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

        // 3. Android 13+ Runtime Permissions Request
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

                var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
                var currentSong by remember { mutableStateOf<Song?>(null) }
                var isPlaying by remember { mutableStateOf(false) }
                var isNowPlayingExpanded by remember { mutableStateOf(false) }
                var showImportDialog by remember { mutableStateOf(false) }
                var currentPositionMs by remember { mutableStateOf(0L) }
                var durationMs by remember { mutableStateOf(0L) }

                // Live Import Progress Popup State
                var isImporting by remember { mutableStateOf(false) }
                var importCurrentTrack by remember { mutableStateOf(0) }
                var importTotalTracks by remember { mutableStateOf(0) }
                var importStatusMessage by remember { mutableStateOf("Connecting to music service...") }

                // Playback state listener
                DisposableEffect(mediaController) {
                    val listener = object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            durationMs = mediaController?.duration ?: 0L
                        }
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            val mediaId = mediaItem?.mediaId
                            if (mediaId != null) {
                                currentSong = songs.find { it.id == mediaId }
                            }
                        }
                    }
                    mediaController?.addListener(listener)
                    onDispose {
                        mediaController?.removeListener(listener)
                    }
                }

                // Live Seekbar / Position Poller ticker
                LaunchedEffect(isPlaying) {
                    while (isPlaying) {
                        currentPositionMs = mediaController?.currentPosition ?: 0L
                        durationMs = mediaController?.duration ?: 0L
                        delay(500)
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
                            selectedPlaylistId = selectedPlaylistId,
                            onPlaylistSelect = { selectedPlaylistId = it },
                            onSongClick = { song ->
                                currentSong = song
                                playQueue(songs, song)
                            },
                            onPlayAllClick = { queue ->
                                if (queue.isNotEmpty()) {
                                    currentSong = queue.first()
                                    playQueue(queue, queue.first())
                                }
                            },
                            onDeleteSong = { song ->
                                coroutineScope.launch {
                                    repository.deleteSong(song)
                                    Toast.makeText(this@MainActivity, "Deleted '${song.title}'", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDeletePlaylist = { playlist ->
                                coroutineScope.launch {
                                    repository.deletePlaylist(playlist)
                                    if (selectedPlaylistId == playlist.id) {
                                        selectedPlaylistId = null
                                    }
                                    Toast.makeText(this@MainActivity, "Deleted playlist '${playlist.name}'", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onAddPlaylistClick = { showImportDialog = true },
                            onCastClick = { 
                                Toast.makeText(this@MainActivity, "Searching for Google Cast devices...", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // Floating Mini-Player Bar (Bottom of screen)
                        if (currentSong != null && !isNowPlayingExpanded) {
                            NowPlayingBar(
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onPlayPauseClick = { togglePlayPause() },
                                onNextClick = { mediaController?.seekToNext() },
                                onCastClick = { 
                                    Toast.makeText(this@MainActivity, "Searching for Google Cast devices...", Toast.LENGTH_SHORT).show()
                                },
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
                                onSeek = { pos -> 
                                    currentPositionMs = pos
                                    mediaController?.seekTo(pos) 
                                },
                                onCastClick = { 
                                    Toast.makeText(this@MainActivity, "Searching for Google Cast devices...", Toast.LENGTH_SHORT).show()
                                },
                                onCloseClick = { isNowPlayingExpanded = false }
                            )
                        }

                        // Input Dialog to Enter URL
                        if (showImportDialog) {
                            ImportPlaylistDialog(
                                onDismiss = { showImportDialog = false },
                                onImport = { url, quality, format ->
                                    showImportDialog = false
                                    isImporting = true
                                    importCurrentTrack = 0
                                    importTotalTracks = 0
                                    importStatusMessage = "Connecting to playlist..."

                                    coroutineScope.launch {
                                        try {
                                            val (playlist, fetchedSongs) = MusicFetcher.resolveAndFetch(url, quality, format) { cur, tot, msg ->
                                                importCurrentTrack = cur
                                                importTotalTracks = tot
                                                importStatusMessage = msg
                                            }
                                            
                                            // Save complete playlist and all songs to Room DB
                                            repository.savePlaylist(playlist, fetchedSongs)
                                            isImporting = false
                                            selectedPlaylistId = playlist.id
                                            Toast.makeText(this@MainActivity, "Added '${playlist.name}' (${fetchedSongs.size} tracks) to Library!", Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            isImporting = false
                                            Toast.makeText(this@MainActivity, "Failed to import: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }

                        // Live Import Progress Popup Dialog (Shows 0% to 100% real-time progress)
                        if (isImporting) {
                            ImportProgressDialog(
                                current = importCurrentTrack,
                                total = importTotalTracks,
                                statusMessage = importStatusMessage
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestAndroid13Permissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    private fun playQueue(songList: List<Song>, startSong: Song) {
        val mediaItems = songList.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id)
                .setUri(song.localFilePath ?: song.streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .build()
                )
                .build()
        }

        val startIndex = songList.indexOfFirst { it.id == startSong.id }.coerceAtLeast(0)

        mediaController?.setMediaItems(mediaItems, startIndex, 0L)
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
