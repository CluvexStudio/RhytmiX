package com.cluvex.rhythmix.model

import android.net.Uri
import androidx.palette.graphics.Palette

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val data: String,
    val albumId: Long,
    val artistId: Long,
    val uri: Uri,
    val albumArt: String? = null,
    val size: Long = 0,
    val dateAdded: Long = 0,
    val year: Int = 0,
    val track: Int = 0,
    val genre: String = "",
    val bitrate: Int = 0,
    val palette: Palette? = null,
    val isFavorite: Boolean = false,
    val playCount: Long = 0,
    val lastPlayed: Long = 0
)

data class Album(
    val id: Long,
    val name: String,
    val artist: String,
    val numberOfSongs: Int,
    val albumArt: String? = null,
    val year: Int = 0,
    val songs: List<Song> = emptyList()
)

data class Artist(
    val id: Long,
    val name: String,
    val numberOfTracks: Int,
    val numberOfAlbums: Int,
    val albums: List<Album> = emptyList(),
    val songs: List<Song> = emptyList()
)

data class Playlist(
    val id: Long = 0,
    val name: String,
    val songs: List<Song> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isSystemPlaylist: Boolean = false
)

enum class RepeatMode {
    OFF, ALL, ONE
}

enum class ShuffleMode {
    OFF, ON
}

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val playbackPosition: Long = 0,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val volume: Float = 1.0f,
    val playbackSpeed: Float = 1.0f
)
