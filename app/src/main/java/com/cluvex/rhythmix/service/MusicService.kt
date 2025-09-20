package com.cluvex.rhythmix.service

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.cluvex.rhythmix.MainActivity
import com.cluvex.rhythmix.model.RepeatMode
import com.cluvex.rhythmix.model.ShuffleMode
import com.cluvex.rhythmix.model.Song
import com.cluvex.rhythmix.repository.MusicRepository
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MusicService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var musicRepository: MusicRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE = "TOGGLE_SHUFFLE"
        private const val CUSTOM_COMMAND_TOGGLE_REPEAT = "TOGGLE_REPEAT"
        private const val CUSTOM_COMMAND_TOGGLE_FAVORITE = "TOGGLE_FAVORITE"
    }

    override fun onCreate() {
        super.onCreate()
        
        musicRepository = MusicRepository(this)
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        val sessionActivityPendingIntent = packageManager.getLaunchIntentForPackage(packageName)!!.let { sessionIntent ->
            PendingIntent.getActivity(
                this, 0, sessionIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(MediaSessionCallback())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_ENDED -> {
                        handleTrackEnded()
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                mediaItem?.let {
                    serviceScope.launch {
                        val songId = it.mediaId.toLongOrNull() ?: return@launch
                        musicRepository.saveLastPlayedSong(songId, player.currentPosition)
                    }
                }
            }
        })
    }

    private fun handleTrackEnded() {
        when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> {
                player.seekTo(0)
                player.play()
            }
            Player.REPEAT_MODE_ALL -> {
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                } else {
                    player.seekTo(0, 0)
                }
                player.play()
            }
            else -> {
                if (player.hasNextMediaItem()) {
                    player.seekToNextMediaItem()
                    player.play()
                }
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val availableSessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_TOGGLE_REPEAT, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_TOGGLE_FAVORITE, Bundle.EMPTY))
                .build()
            
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands,
                MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                CUSTOM_COMMAND_TOGGLE_SHUFFLE -> {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                    serviceScope.launch {
                        val shuffleMode = if (player.shuffleModeEnabled) ShuffleMode.ON else ShuffleMode.OFF
                        val (repeatMode, _) = musicRepository.getPlayerState()
                        musicRepository.savePlayerState(repeatMode, shuffleMode)
                    }
                }
                CUSTOM_COMMAND_TOGGLE_REPEAT -> {
                    player.repeatMode = when (player.repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                        else -> Player.REPEAT_MODE_OFF
                    }
                    serviceScope.launch {
                        val repeatMode = when (player.repeatMode) {
                            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                            else -> RepeatMode.OFF
                        }
                        val (_, shuffleMode) = musicRepository.getPlayerState()
                        musicRepository.savePlayerState(repeatMode, shuffleMode)
                    }
                }
                CUSTOM_COMMAND_TOGGLE_FAVORITE -> {
                    val currentMediaItem = player.currentMediaItem
                    currentMediaItem?.let { mediaItem ->
                        serviceScope.launch {
                            val songId = mediaItem.mediaId.toLongOrNull() ?: return@launch
                            val favorites = musicRepository.getFavoriteSongs()
                            favorites.collect { favSet ->
                                if (favSet.contains(songId.toString())) {
                                    musicRepository.removeFromFavorites(songId)
                                } else {
                                    musicRepository.addToFavorites(songId)
                                }
                            }
                        }
                    }
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArt?.let { android.net.Uri.parse(it) })
                        .build()
                )
                .build()
        }
        
        player.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
        player.prepare()
    }

    fun addToQueue(song: Song) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArt?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()
        
        player.addMediaItem(mediaItem)
    }

    fun removeFromQueue(index: Int) {
        if (index >= 0 && index < player.mediaItemCount) {
            player.removeMediaItem(index)
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        player.moveMediaItem(from, to)
    }
}
