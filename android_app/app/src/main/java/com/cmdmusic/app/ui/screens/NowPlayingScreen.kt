package com.cmdmusic.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cmdmusic.app.data.model.Song
import com.cmdmusic.app.ui.theme.AmoledBackground
import com.cmdmusic.app.ui.theme.SapphirePrimary
import com.cmdmusic.app.ui.theme.SurfaceCardElevated
import com.cmdmusic.app.ui.theme.SurfaceDark

@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onCastClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    if (song == null) return

    var isShuffle by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(false) }

    // Vinyl Rotation Animation when song is actively playing
    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SurfaceDark,
                        AmoledBackground
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM PLAYLIST",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                IconButton(onClick = onCastClick) {
                    Icon(
                        imageVector = Icons.Rounded.Cast,
                        contentDescription = "Google Cast",
                        tint = SapphirePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Vinyl Record / Album Cover with Shadow & Rotation
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .shadow(36.dp, CircleShape, spotColor = SapphirePrimary)
                    .clip(CircleShape)
                    .background(SurfaceCardElevated)
                    .rotate(if (isPlaying) rotationAngle else 0f),
                contentAlignment = Alignment.Center
            ) {
                if (!song.artworkUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.artworkUrl,
                        contentDescription = song.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = SapphirePrimary,
                        modifier = Modifier.size(80.dp)
                    )
                }

                // Vinyl Center Hole
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AmoledBackground)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title & Artist Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            // Seekbar
            Column(modifier = Modifier.fillMaxWidth()) {
                val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                Slider(
                    value = progress,
                    onValueChange = { onSeek((it * durationMs).toLong()) },
                    colors = SliderDefaults.colors(
                        thumbColor = SapphirePrimary,
                        activeTrackColor = SapphirePrimary,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatDuration(currentPositionMs), style = MaterialTheme.typography.labelSmall)
                    Text(text = formatDuration(durationMs), style = MaterialTheme.typography.labelSmall)
                }
            }

            // Playback Controls (Shuffle, Prev, Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { isShuffle = !isShuffle }) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) SapphirePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Previous
                IconButton(onClick = onPreviousClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Big Play / Pause Button
                FloatingActionButton(
                    onClick = onPlayPauseClick,
                    containerColor = SapphirePrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(38.dp)
                    )
                }

                // Next
                IconButton(onClick = onNextClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat
                IconButton(onClick = { isRepeat = !isRepeat }) {
                    Icon(
                        imageVector = Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (isRepeat) SapphirePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
