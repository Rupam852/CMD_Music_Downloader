package com.cmdmusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cmdmusic.app.data.model.Playlist
import com.cmdmusic.app.data.model.Song
import com.cmdmusic.app.ui.components.GlassmorphicCard
import com.cmdmusic.app.ui.components.SongItemView
import com.cmdmusic.app.ui.theme.SapphirePrimary
import com.cmdmusic.app.ui.theme.SurfaceCard
import com.cmdmusic.app.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    playlists: List<Playlist>,
    songs: List<Song>,
    currentSong: Song?,
    selectedPlaylistId: String?,
    onPlaylistSelect: (String?) -> Unit,
    onSongClick: (Song) -> Unit,
    onPlayAllClick: (List<Song>) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddPlaylistClick: () -> Unit,
    onCastClick: () -> Unit
) {
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    // Delete Playlist Confirmation Dialog
    if (playlistToDelete != null) {
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete Playlist") },
            text = { Text("Are you sure you want to delete '${playlistToDelete?.name}' and its songs from your library?") },
            confirmButton = {
                Button(
                    onClick = {
                        playlistToDelete?.let { onDeletePlaylist(it) }
                        playlistToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Delete Song Confirmation Dialog
    if (songToDelete != null) {
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Delete Song") },
            text = { Text("Are you sure you want to remove '${songToDelete?.title}' from your library?") },
            confirmButton = {
                Button(
                    onClick = {
                        songToDelete?.let { onDeleteSong(it) }
                        songToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aura Music",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onCastClick) {
                        Icon(
                            imageVector = Icons.Rounded.Cast,
                            contentDescription = "Cast",
                            tint = SapphirePrimary
                        )
                    }
                    IconButton(onClick = onAddPlaylistClick) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Import Playlist",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        val displayedSongs = if (selectedPlaylistId != null) {
            songs.filter { it.playlistId == selectedPlaylistId }
        } else {
            songs
        }

        val selectedPlaylist = playlists.find { it.id == selectedPlaylistId }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 130.dp)
        ) {
            // Section 1: Playlists Carousel
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playlists (${playlists.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedPlaylistId != null) {
                        TextButton(onClick = { onPlaylistSelect(null) }) {
                            Text("Show All (${songs.size})", color = SapphirePrimary)
                        }
                    }
                }

                if (playlists.isEmpty()) {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = onAddPlaylistClick
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LibraryMusic,
                                contentDescription = null,
                                tint = SapphirePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "No Playlists Added",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tap + to paste YouTube or Spotify link",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(playlists, key = { it.id }) { playlist ->
                            PlaylistCardItem(
                                playlist = playlist,
                                isSelected = selectedPlaylistId == playlist.id,
                                onClick = {
                                    if (selectedPlaylistId == playlist.id) {
                                        onPlaylistSelect(null)
                                    } else {
                                        onPlaylistSelect(playlist.id)
                                    }
                                },
                                onDeleteClick = { playlistToDelete = playlist }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 2: All Songs Header with Play All Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedPlaylist != null) selectedPlaylist.name else "All Tracks (${displayedSongs.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (selectedPlaylist != null) {
                            Text(
                                text = "${displayedSongs.size} Tracks in this playlist",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    if (displayedSongs.isNotEmpty()) {
                        Button(
                            onClick = { onPlayAllClick(displayedSongs) },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play All", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 3: Song Items List
            if (displayedSongs.isEmpty()) {
                item {
                    Text(
                        text = "No songs found. Tap + to import your favourite playlist or songs!",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            } else {
                items(displayedSongs, key = { it.id }) { song ->
                    SongItemView(
                        song = song,
                        isPlaying = currentSong?.id == song.id,
                        onClick = { onSongClick(song) },
                        onMenuClick = { songToDelete = song }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaylistCardItem(
    playlist: Playlist,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    GlassmorphicCard(
        modifier = Modifier
            .width(160.dp)
            .height(210.dp),
        borderColor = if (isSelected) SapphirePrimary else Color(0x1AFFFFFF),
        borderWidth = if (isSelected) 2.dp else 1.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                if (!playlist.coverUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = playlist.coverUrl,
                        contentDescription = playlist.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.LibraryMusic,
                        contentDescription = null,
                        tint = SapphirePrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Top Right Delete Menu Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = SurfaceDark
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Playlist", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = "${playlist.trackCount} Tracks • ${playlist.sourcePlatform}",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}
