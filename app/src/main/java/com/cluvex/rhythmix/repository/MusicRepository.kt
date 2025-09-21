package com.cluvex.rhythmix.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.cluvex.rhythmix.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "music_preferences")

class MusicRepository(private val context: Context) {
    
    private val contentResolver: ContentResolver = context.contentResolver
    private val dataStore = context.dataStore
    
    companion object {
        private val FAVORITE_SONGS = stringSetPreferencesKey("favorite_songs")
        private val RECENT_SONGS = stringSetPreferencesKey("recent_songs")
        private val PLAY_COUNT = stringPreferencesKey("play_count")
        private val LAST_PLAYED_SONG = longPreferencesKey("last_played_song")
        private val LAST_POSITION = longPreferencesKey("last_position")
        private val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        private val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    suspend fun getAllSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val yearColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: "Unknown"
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val album = it.getString(albumColumn) ?: "Unknown Album"
                val duration = it.getLong(durationColumn)
                val data = it.getString(dataColumn) ?: ""
                val albumId = it.getLong(albumIdColumn)
                val artistId = it.getLong(artistIdColumn)
                val size = it.getLong(sizeColumn)
                val dateAdded = it.getLong(dateAddedColumn) * 1000
                val year = it.getInt(yearColumn)
                val track = it.getInt(trackColumn)

                if (File(data).exists() && duration > 30000) {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    val albumArt = getAlbumArt(albumId)
                    
                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            data = data,
                            albumId = albumId,
                            artistId = artistId,
                            uri = uri,
                            albumArt = albumArt,
                            size = size,
                            dateAdded = dateAdded,
                            year = year,
                            track = track
                        )
                    )
                }
            }
        }
        return songs
    }

    private fun getAlbumArt(albumId: Long): String? {
        val uri = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart"),
            albumId
        )
        return uri.toString()
    }

    suspend fun getAlbums(): List<Album> {
        val albums = mutableListOf<Album>()
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,
            MediaStore.Audio.Albums.FIRST_YEAR
        )

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Audio.Albums.ALBUM} ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val numberOfSongsColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)
            val yearColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.FIRST_YEAR)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(albumColumn) ?: "Unknown Album"
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val numberOfSongs = it.getInt(numberOfSongsColumn)
                val year = it.getInt(yearColumn)
                val albumArt = getAlbumArt(id)

                albums.add(
                    Album(
                        id = id,
                        name = name,
                        artist = artist,
                        numberOfSongs = numberOfSongs,
                        albumArt = albumArt,
                        year = year
                    )
                )
            }
        }
        return albums
    }

    suspend fun getArtists(): List<Artist> {
        val artists = mutableListOf<Artist>()
        val projection = arrayOf(
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME,
            MediaStore.Audio.Playlists.DATE_ADDED,
            MediaStore.Audio.Playlists.DATE_MODIFIED
        )
        
        val sortOrder = "${MediaStore.Audio.Playlists.DATE_ADDED} DESC"
        @Suppress("DEPRECATION")
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val numberOfTracksColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
            val numberOfAlbumsColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val name = it.getString(artistColumn) ?: "Unknown Artist"
                val numberOfTracks = it.getInt(numberOfTracksColumn)
                val numberOfAlbums = it.getInt(numberOfAlbumsColumn)

                artists.add(
                    Artist(
                        id = id,
                        name = name,
                        numberOfTracks = numberOfTracks,
                        numberOfAlbums = numberOfAlbums
                    )
                )
            }
        }
        return artists
    }

    suspend fun addToFavorites(songId: Long) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_SONGS] ?: emptySet()
            preferences[FAVORITE_SONGS] = currentFavorites + songId.toString()
        }
    }

    suspend fun removeFromFavorites(songId: Long) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[FAVORITE_SONGS] ?: emptySet()
            preferences[FAVORITE_SONGS] = currentFavorites - songId.toString()
        }
    }

    fun getFavoriteSongs(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            preferences[FAVORITE_SONGS] ?: emptySet()
        }
    }

    suspend fun saveLastPlayedSong(songId: Long, position: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_PLAYED_SONG] = songId
            preferences[LAST_POSITION] = position
        }
    }

    suspend fun getLastPlayedSong(): Pair<Long, Long> {
        val preferences = dataStore.data.first()
        return Pair(
            preferences[LAST_PLAYED_SONG] ?: -1L,
            preferences[LAST_POSITION] ?: 0L
        )
    }

    suspend fun savePlayerState(repeatMode: RepeatMode, shuffleMode: ShuffleMode) {
        dataStore.edit { preferences ->
            preferences[REPEAT_MODE] = repeatMode.name
            preferences[SHUFFLE_MODE] = shuffleMode == ShuffleMode.ON
        }
    }

    suspend fun getPlayerState(): Pair<RepeatMode, ShuffleMode> {
        val preferences = dataStore.data.first()
        val repeatMode = try {
            RepeatMode.valueOf(preferences[REPEAT_MODE] ?: RepeatMode.OFF.name)
        } catch (e: IllegalArgumentException) {
            RepeatMode.OFF
        }
        val shuffleMode = if (preferences[SHUFFLE_MODE] == true) ShuffleMode.ON else ShuffleMode.OFF
        return Pair(repeatMode, shuffleMode)
    }

    suspend fun saveThemeMode(themeMode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode
        }
    }

    fun getThemeMode(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[THEME_MODE] ?: "system"
        }
    }
    
    suspend fun createPlaylist(name: String): Long {
        return withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Playlists.NAME, name)
                put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                values
            )
            
            uri?.lastPathSegment?.toLongOrNull() ?: -1L
        }
    }
    
    suspend fun addSongToPlaylist(songId: Long, playlistId: Long) {
        withContext(Dispatchers.IO) {
            val uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId)
            
            val values = ContentValues().apply {
                put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songId)
                put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, System.currentTimeMillis())
            }
            
            context.contentResolver.insert(uri, values)
        }
    }
}
