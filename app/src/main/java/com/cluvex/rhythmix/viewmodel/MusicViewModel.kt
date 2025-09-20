package com.cluvex.rhythmix.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.cluvex.rhythmix.model.*
import com.cluvex.rhythmix.repository.MusicRepository
import com.cluvex.rhythmix.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    
    private val musicRepository = MusicRepository(application)
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    
    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()
    
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()
    
    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()
    
    private val _currentQueueIndex = MutableStateFlow(-1)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    init {
        loadInitialData()
        initializeMediaController()
        observeThemeMode()
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), MusicService::class.java)
        )
        
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(playerListener)
            setupPlayerStateTracking()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let {
                updateCurrentSongFromMediaItem(it)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> _isLoading.value = true
                else -> _isLoading.value = false
            }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = when (repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleMode.value = if (shuffleModeEnabled) ShuffleMode.ON else ShuffleMode.OFF
        }
    }

    private fun setupPlayerStateTracking() {
        viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    _playbackPosition.value = controller.currentPosition
                    _duration.value = controller.duration.takeIf { it != C.TIME_UNSET } ?: 0L
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun updateCurrentSongFromMediaItem(mediaItem: MediaItem) {
        val songId = mediaItem.mediaId.toLongOrNull() ?: return
        val currentQueue = _queue.value
        val song = currentQueue.find { it.id == songId }
        _currentSong.value = song
        _currentQueueIndex.value = currentQueue.indexOfFirst { it.id == songId }
    }

    fun refreshData() {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val songs = musicRepository.getAllSongs()
                val albums = musicRepository.getAlbums()
                val artists = musicRepository.getArtists()
                val favorites = musicRepository.getFavoriteSongs().first()
                
                _uiState.value = _uiState.value.copy(
                    songs = songs,
                    albums = albums,
                    artists = artists,
                    favorites = favorites
                )
                
                val (lastSongId, lastPosition) = musicRepository.getLastPlayedSong()
                if (lastSongId != -1L) {
                    val lastSong = songs.find { it.id == lastSongId }
                    lastSong?.let {
                        _currentSong.value = it
                        seekTo(lastPosition)
                    }
                }
                
                val (repeatMode, shuffleMode) = musicRepository.getPlayerState()
                _repeatMode.value = repeatMode
                _shuffleMode.value = shuffleMode
                
            } catch (e: Exception) {
                
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeThemeMode() {
        viewModelScope.launch {
            musicRepository.getThemeMode().collect { mode ->
                _themeMode.value = mode
            }
        }
    }

    fun playPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        _queue.value = songs
        _currentQueueIndex.value = startIndex
        
        val serviceIntent = android.content.Intent(getApplication(), MusicService::class.java)
        getApplication<Application>().startService(serviceIntent)
        
        viewModelScope.launch {
            controllerFuture?.get()?.let { controller ->
                val mediaItems = songs.map { song ->
                    MediaItem.Builder()
                        .setMediaId(song.id.toString())
                        .setUri(song.uri)
                        .build()
                }
                controller.setMediaItems(mediaItems, startIndex, 0L)
                controller.prepare()
                if (startIndex >= 0) {
                    _currentSong.value = songs.getOrNull(startIndex)
                }
            }
        }
    }

    fun playSong(song: Song, playlist: List<Song> = listOf(song)) {
        val index = playlist.indexOf(song)
        setPlaylist(playlist, index)
        mediaController?.play()
    }

    fun addToQueue(song: Song) {
        val currentQueue = _queue.value.toMutableList()
        currentQueue.add(song)
        _queue.value = currentQueue
        
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.uri)
            .build()
        mediaController?.addMediaItem(mediaItem)
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (index >= 0 && index < currentQueue.size) {
            currentQueue.removeAt(index)
            _queue.value = currentQueue
            mediaController?.removeMediaItem(index)
        }
    }

    fun toggleRepeat() {
        val newMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = newMode
        
        val playerRepeatMode = when (newMode) {
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = playerRepeatMode
        
        viewModelScope.launch {
            musicRepository.savePlayerState(newMode, _shuffleMode.value)
        }
    }

    fun toggleShuffle() {
        val newMode = if (_shuffleMode.value == ShuffleMode.ON) ShuffleMode.OFF else ShuffleMode.ON
        _shuffleMode.value = newMode
        mediaController?.shuffleModeEnabled = newMode == ShuffleMode.ON
        
        viewModelScope.launch {
            musicRepository.savePlayerState(_repeatMode.value, newMode)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val currentFavorites = _uiState.value.favorites
            if (currentFavorites.contains(song.id.toString())) {
                musicRepository.removeFromFavorites(song.id)
            } else {
                musicRepository.addToFavorites(song.id)
            }
            
            val updatedFavorites = musicRepository.getFavoriteSongs().first()
            _uiState.value = _uiState.value.copy(favorites = updatedFavorites)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterContent(query)
    }

    private fun filterContent(query: String) {
        if (query.isBlank()) {
            loadInitialData()
            return
        }
        
        viewModelScope.launch {
            val allSongs = musicRepository.getAllSongs()
            val filteredSongs = allSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
            
            val allAlbums = musicRepository.getAlbums()
            val filteredAlbums = allAlbums.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true)
            }
            
            val allArtists = musicRepository.getArtists()
            val filteredArtists = allArtists.filter {
                it.name.contains(query, ignoreCase = true)
            }
            
            _uiState.value = _uiState.value.copy(
                songs = filteredSongs,
                albums = filteredAlbums,
                artists = filteredArtists
            )
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            musicRepository.saveThemeMode(mode)
            _themeMode.value = mode
        }
    }
    
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                musicRepository.createPlaylist(name)
                refreshData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun addSongToPlaylist(song: Song, playlistId: Long) {
        viewModelScope.launch {
            try {
                musicRepository.addSongToPlaylist(song.id, playlistId)
                refreshData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}

data class MusicUiState(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)
