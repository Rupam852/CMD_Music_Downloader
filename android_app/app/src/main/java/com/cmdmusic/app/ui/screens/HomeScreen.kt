package com.cmdmusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    playlists: List<Playlist>,
    songs: List<Song>,
    currentSong: Song?,
    onSongClick: (Song) -> Unit,
    onAddPlaylistClick: () -> Unit,
    onCastClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "CMD Music",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Section 1: Playlists Carousel
            item {
                Text(
                    text = "Imported Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                if (playlists.isEmpty()) {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                    text = "No Playlists Imported",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tap + to add YouTube or Spotify playlist",
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
                            PlaylistCardItem(playlist = playlist)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 2: All Songs Header
            item {
                Text(
                    text = "Music Library (${songs.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Section 3: Songs List
            items(songs, key = { it.id }) { song ->
                SongItemView(
                    song = song,
                    isPlaying = currentSong?.id == song.id,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

@Composable
fun PlaylistCardItem(playlist: Playlist) {
    GlassmorphicCard(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
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
