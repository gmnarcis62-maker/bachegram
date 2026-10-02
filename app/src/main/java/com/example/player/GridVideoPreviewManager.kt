package com.example.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

@OptIn(UnstableApi::class)
class GridVideoPreviewManager(private val context: Context) {

    companion object {
        const val TAG = "BachegramGridPreview"
    }

    private var previewPlayer: ExoPlayer? = null

    private val _activePreviewId = MutableStateFlow<String?>(null)
    val activePreviewId: StateFlow<String?> = _activePreviewId.asStateFlow()

    private val _isPreviewReady = MutableStateFlow(false)
    val isPreviewReady: StateFlow<Boolean> = _isPreviewReady.asStateFlow()

    private val _hasPreviewError = MutableStateFlow(false)
    val hasPreviewError: StateFlow<Boolean> = _hasPreviewError.asStateFlow()

    init {
        createPlayer()
    }

    fun getPlayer(): ExoPlayer? = previewPlayer

    private fun createPlayer() {
        Log.i(TAG, "Initializing lightweight ExoPlayer for Instagram Explore Grid Previews...")

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)
            .setUserAgent(
                "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            )

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1500, // minBufferMs
                12000, // maxBufferMs
                400,  // bufferForPlaybackMs (instant start)
                1000  // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        previewPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                Log.d(TAG, "Grid preview STATE_READY for ${_activePreviewId.value}")
                                _isPreviewReady.value = true
                                _hasPreviewError.value = false
                            }
                            Player.STATE_BUFFERING -> {
                                _isPreviewReady.value = false
                            }
                            Player.STATE_IDLE, Player.STATE_ENDED -> {
                                _isPreviewReady.value = false
                            }
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        Log.i(TAG, "Grid preview first frame rendered for ${_activePreviewId.value}!")
                        _isPreviewReady.value = true
                        _hasPreviewError.value = false
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.w(TAG, "Grid preview error for ${_activePreviewId.value}: ${error.errorCodeName}")
                        _hasPreviewError.value = true
                        _isPreviewReady.value = false
                    }
                })
            }
    }

    fun playPreview(video: VideoItem) {
        // Already playing this exact video → no-op
        if (_activePreviewId.value == video.id && previewPlayer?.isPlaying == true) {
            return
        }

        val player = previewPlayer ?: run {
            createPlayer()
            return
        }

        _activePreviewId.value = video.id
        _isPreviewReady.value = false
        _hasPreviewError.value = false

        player.stop()
        player.clearMediaItems()

        val mediaItem = if (video.isDownloaded && video.localFilePath != null &&
            File(video.localFilePath).exists()
        ) {
            MediaItem.fromUri(Uri.fromFile(File(video.localFilePath)))
        } else {
            val normalizedUrl = Media3PlayerManager.safeNormalizeUrl(video.videoUrl)
            MediaItem.fromUri(Uri.parse(normalizedUrl))
        }

        try {
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            Log.d(TAG, "Started grid preview request for: ${video.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start grid preview", e)
            _hasPreviewError.value = true
            _isPreviewReady.value = false
        }
    }

    /**
     * Pauses the current preview and clears the active id, but does NOT
     * clear the video surface. Keeping the surface attached prevents the
     * "only first preview works" bug when switching between cards.
     */
    fun stopPreview() {
        _activePreviewId.value = null
        _isPreviewReady.value = false
        _hasPreviewError.value = false
        previewPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            // ⚠️ DO NOT call clearVideoSurface() here!
            // Doing so detaches the surface and breaks subsequent previews.
        }
    }

    fun pause() {
        previewPlayer?.pause()
    }

    fun resume() {
        // Only resume if there's an active preview
        if (_activePreviewId.value != null) {
            previewPlayer?.play()
        }
    }

    fun release() {
        Log.i(TAG, "Releasing GridVideoPreviewManager ExoPlayer...")
        previewPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.clearVideoSurface()
            player.release()
        }
        previewPlayer = null
        _activePreviewId.value = null
        _isPreviewReady.value = false
    }
}