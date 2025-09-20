package com.cluvex.rhythmix.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cluvex.rhythmix.model.RepeatMode
import com.cluvex.rhythmix.model.ShuffleMode
import com.cluvex.rhythmix.model.Song
import com.cluvex.rhythmix.viewmodel.MusicViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalAnimationApi::class)
@Composable
fun FullPlayerScreen(
    viewModel: MusicViewModel = viewModel(),
    onBackPressed: () -> Unit
) {
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        )
    )

    val backgroundScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        currentSong?.albumArt?.let { albumArt ->
            GlideImage(
                model = albumArt,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = backgroundScale, scaleY = backgroundScale)
                    .blur(50.dp),
                contentScale = ContentScale.Crop
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.8f),
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopControls(
                onBackPressed = onBackPressed,
                currentSong = currentSong
            )

            Spacer(modifier = Modifier.height(32.dp))

            currentSong?.let { song ->
                AlbumArtSection(
                    song = song,
                    isPlaying = isPlaying,
                    rotation = if (isPlaying) rotation else 0f
                )

                Spacer(modifier = Modifier.height(40.dp))

                SongInfoSection(song = song)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AudioVisualizer(
                    isPlaying = isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                ProgressSection(
                    currentPosition = playbackPosition,
                    duration = duration,
                    onSeek = { position ->
                        viewModel.seekTo(position)
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))

                PlayerControls(
                    isPlaying = isPlaying,
                    repeatMode = repeatMode,
                    shuffleMode = shuffleMode,
                    onPlayPause = { viewModel.playPause() },
                    onPrevious = { viewModel.skipToPrevious() },
                    onNext = { viewModel.skipToNext() },
                    onRepeat = { viewModel.toggleRepeat() },
                    onShuffle = { viewModel.toggleShuffle() },
                    onFavorite = { 
                        currentSong?.let { viewModel.toggleFavorite(it) }
                    },
                    onAddToPlaylist = {
                        showAddToPlaylistSheet = true
                    },
                    onShare = {
                    },
                    isFavorite = currentSong?.let { uiState.favorites.contains(it.id.toString()) } ?: false
                )
            }
        }
    }
    
    currentSong?.let { song ->
        if (showAddToPlaylistSheet) {
            AddToPlaylistBottomSheet(
                song = song,
                playlists = uiState.playlists,
                onDismiss = { showAddToPlaylistSheet = false },
                onCreateNewPlaylist = { playlistName ->
                    viewModel.createPlaylist(playlistName)
                },
                onAddToExistingPlaylist = { playlist ->
                    viewModel.addSongToPlaylist(song, playlist.id)
                }
            )
        }
    }
}

@Composable
fun TopControls(
    onBackPressed: () -> Unit,
    currentSong: Song?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Now Playing",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        )

        IconButton(
            onClick = { },
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "More",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumArtSection(
    song: Song,
    isPlaying: Boolean,
    rotation: Float
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Box(
        modifier = Modifier
            .size(300.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Card(
            modifier = Modifier.size(280.dp),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                            )
                        )
                    )
            ) {
                if (song.albumArt != null) {
                    GlideImage(
                        model = song.albumArt,
                        contentDescription = song.album,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(80.dp),
                        tint = Color.White
                    )
                }

                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun SongInfoSection(song: Song) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color.White
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.artist,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = song.album,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.6f)
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProgressSection(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }

    val progress = if (duration > 0) {
        if (isDragging) dragPosition else currentPosition.toFloat() / duration.toFloat()
    } else 0f

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Slider(
            value = progress,
            onValueChange = { value ->
                isDragging = true
                dragPosition = value
            },
            onValueChangeFinished = {
                isDragging = false
                val seekPosition = (dragPosition * duration).roundToInt().toLong()
                onSeek(seekPosition)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    repeatMode: RepeatMode,
    shuffleMode: ShuffleMode,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRepeat: () -> Unit,
    onShuffle: () -> Unit,
    onFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onShare: () -> Unit = {},
    isFavorite: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onShuffle,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (shuffleMode == ShuffleMode.ON) 
                            Color.White.copy(alpha = 0.2f) 
                        else 
                            Color.Transparent,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleMode == ShuffleMode.ON) 
                        Color.White 
                    else 
                        Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onPrevious,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            FloatingActionButton(
                onClick = onPlayPause,
                modifier = Modifier.size(64.dp),
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(
                onClick = onRepeat,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (repeatMode != RepeatMode.OFF) 
                            Color.White.copy(alpha = 0.2f) 
                        else 
                            Color.Transparent,
                        CircleShape
                    )
            ) {
                val icon = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Rounded.RepeatOne
                    else -> Icons.Rounded.Repeat
                }
                Icon(
                    icon,
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.OFF) 
                        Color.White 
                    else 
                        Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onFavorite,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (isFavorite) Color.Red else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onAddToPlaylist,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.PlaylistAdd,
                    contentDescription = "Add to playlist",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Rounded.Share,
                    contentDescription = "Share",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
