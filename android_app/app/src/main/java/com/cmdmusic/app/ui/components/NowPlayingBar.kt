package com.cmdmusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cmdmusic.app.data.model.Song
import com.cmdmusic.app.ui.theme.SapphirePrimary
import com.cmdmusic.app.ui.theme.SurfaceCardElevated

@Composable
fun NowPlayingBar(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onCastClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return

    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = SurfaceCardElevated,
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art Thumbnail
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (!currentSong.artworkUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = currentSong.artworkUrl,
                            contentDescription = currentSong.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Song Title & Artist
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Google Cast Icon
                IconButton(onClick = onCastClick) {
                    Icon(
                        imageVector = Icons.Rounded.Cast,
                        contentDescription = "Cast",
                        tint = SapphirePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play/Pause Button
                IconButton(onClick = onPlayPauseClick) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next Button
                IconButton(onClick = onNextClick) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Bottom Progress Bar Line in Mini Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Color(0x1AFFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress)
                        .background(SapphirePrimary)
                )
            }
        }
    }
}
