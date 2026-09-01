package com.cmdmusic.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cmdmusic.app.ui.components.GlassmorphicCard
import com.cmdmusic.app.ui.theme.SapphirePrimary
import com.cmdmusic.app.ui.theme.SurfaceCardElevated
import com.cmdmusic.app.ui.theme.SurfaceDark

@Composable
fun ImportPlaylistDialog(
    onDismiss: () -> Unit,
    onImport: (url: String, quality: String, format: String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var selectedQuality by remember { mutableStateOf("320") }
    var selectedFormat by remember { mutableStateOf("mp3") }

    Dialog(onDismissRequest = onDismiss) {
        GlassmorphicCard(
            shape = RoundedCornerShape(26.dp),
            backgroundColor = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Import Music Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Paste YouTube or Spotify playlist link to download & add to library.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // URL Input
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://spotify.com/... or youtube.com/...") },
                    leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null, tint = SapphirePrimary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphirePrimary,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bitrate Selection
                Text("AUDIO BITRATE", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("320", "256", "192").forEach { q ->
                        FilterChip(
                            selected = selectedQuality == q,
                            onClick = { selectedQuality = q },
                            label = { Text("${q}k") },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Format Selection
                Text("AUDIO FORMAT", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("mp3", "flac", "wav").forEach { f ->
                        FilterChip(
                            selected = selectedFormat == f,
                            onClick = { selectedFormat = f },
                            label = { Text(f.uppercase()) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (url.isNotBlank()) {
                                onImport(url, selectedQuality, selectedFormat)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphirePrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Import & Download")
                    }
                }
            }
        }
    }
}
