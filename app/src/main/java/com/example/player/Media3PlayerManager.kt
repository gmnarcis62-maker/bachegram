package com.example.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.model.VideoItem
import com.example.network.NetworkHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

@OptIn(UnstableApi::class)
class Media3PlayerManager(private val context: Context) {

    companion object {
        const val TAG = "BachegramExoPlayer"

        // Official public benchmark test streams for diagnostics only
        const val TEST_MP4_URL = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4"
        const val TEST_HLS_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"

        /**
         * Safely normalizes URLs by decoding first, then cleanly encoding only necessary
         * Unicode/Persian path characters. Idempotent: NEVER double-encodes '%xx' into '%25xx'.
         */
        fun safeNormalizeUrl(rawUrl: String): String {
            if (rawUrl.isBlank()) return rawUrl
            return try {
                val trimmed = rawUrl.trim()
                val uri = URI(trimmed)
                val scheme = uri.scheme ?: "https"
                val host = uri.host ?: ""
                val port = uri.port
                val authority = if (port != -1) "$host:$port" else host

                val rawPath = uri.rawPath ?: uri.path ?: ""
                // Decode first to handle already-encoded strings cleanly
                val decodedPath = URLDecoder.decode(rawPath, "UTF-8")

                // Re-encode segments safely
                val normalizedPath = decodedPath.split("/").joinToString("/") { segment ->
                    URLEncoder.encode(segment, "UTF-8")
                        .replace("+", "%20")
                        .replace("%21", "!")
                        .replace("%27", "'")
                        .replace("%28", "(")
                        .replace("%29", ")")
                        .replace("%7E", "~")
                }

                val query = uri.rawQuery
                val fragment = uri.rawFragment
                val queryPart = if (!query.isNullOrEmpty()) "?$query" else ""
                val fragmentPart = if (!fragment.isNullOrEmpty()) "#$fragment" else ""

                "$scheme://$authority$normalizedPath$queryPart$fragmentPart"
            } catch (e: Exception) {
                rawUrl.trim()
            }
        }

        fun safeEncodeUrl(rawUrl: String): String = safeNormalizeUrl(rawUrl)
    }

    private var exoPlayer: ExoPlayer? = null
    private val networkHelper = NetworkHelper(context)

    // Player State Flows
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _hasError = MutableStateFlow<String?>(null)
    val hasError: StateFlow<String?> = _hasError.asStateFlow()

    private val _hasRenderedFirstFrame = MutableStateFlow(false)
    val hasRenderedFirstFrame: StateFlow<Boolean> = _hasRenderedFirstFrame.asStateFlow()

    private val _isOfflineWithoutCache = MutableStateFlow(false)
    val isOfflineWithoutCache: StateFlow<Boolean> = _isOfflineWithoutCache.asStateFlow()

    private val _isPlayingLocally = MutableStateFlow(false)
    val isPlayingLocally: StateFlow<Boolean> = _isPlayingLocally.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _currentPlayingUrl = MutableStateFlow<String?>(null)
    val currentPlayingUrl: StateFlow<String?> = _currentPlayingUrl.asStateFlow()

    // Structured Telemetry for Technical Diagnostics
    private var requestStartTimeMs: Long = 0L
    private var bufferingStartTimeMs: Long = 0L
    private var readyTimeMs: Long = 0L
    private var firstFrameTimeMs: Long = 0L
    private var lastDetailedError: String? = null
    private var lastPlayerStateName: String = "IDLE"

    private val _diagnosticReport = MutableStateFlow<String>("آماده برای اجرای تست")
    val diagnosticReport: StateFlow<String> = _diagnosticReport.asStateFlow()

    var onVideoEnded: (() -> Unit)? = null

    init {
        createPlayer()
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    private fun createPlayer() {
        Log.i(TAG, "[Player Lifecycle] Initializing Media3 ExoPlayer instance...")

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(25000)
            .setReadTimeoutMs(30000)
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            .setDefaultRequestProperties(mapOf("Accept" to "*/*", "Connection" to "keep-alive"))

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)

        // Low-latency buffer tuning for instant start
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2000,  // minBufferMs
                30000, // maxBufferMs
                500,   // bufferForPlaybackMs
                1500   // bufferForPlaybackAfterRebufferMs
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val stateName = when (playbackState) {
                            Player.STATE_IDLE -> "STATE_IDLE"
                            Player.STATE_BUFFERING -> "STATE_BUFFERING"
                            Player.STATE_READY -> "STATE_READY"
                            Player.STATE_ENDED -> "STATE_ENDED"
                            else -> "STATE_UNKNOWN($playbackState)"
                        }
                        lastPlayerStateName = stateName
                        val elapsedSinceRequest = if (requestStartTimeMs > 0) System.currentTimeMillis() - requestStartTimeMs else 0

                        Log.d(TAG, "[PlaybackState] Transitioned to $stateName (${elapsedSinceRequest}ms elapsed since request)")

                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                bufferingStartTimeMs = System.currentTimeMillis()
                                _isBuffering.value = true
                                _hasError.value = null
                                _isOfflineWithoutCache.value = false
                            }
                            Player.STATE_READY -> {
                                readyTimeMs = System.currentTimeMillis()
                                val bufferingDuration = if (bufferingStartTimeMs > 0) readyTimeMs - bufferingStartTimeMs else 0
                                Log.i(TAG, "[PlaybackState] STATE_READY reached! Total buffering time: ${bufferingDuration}ms, Duration: ${duration}ms")
                                _isBuffering.value = false
                                _hasError.value = null
                                _isOfflineWithoutCache.value = false
                                _duration.value = duration.coerceAtLeast(0L)
                            }
                            Player.STATE_ENDED -> {
                                _isBuffering.value = false
                                onVideoEnded?.invoke()
                            }
                            Player.STATE_IDLE -> {
                                _isBuffering.value = false
                            }
                        }
                    }

                    override fun onRenderedFirstFrame() {
                        firstFrameTimeMs = System.currentTimeMillis()
                        val ttff = if (requestStartTimeMs > 0) firstFrameTimeMs - requestStartTimeMs else 0
                        Log.i(TAG, "[Render] SUCCESS! First video frame rendered on screen! Time-To-First-Frame (TTFF): ${ttff}ms")
                        _isBuffering.value = false
                        _hasError.value = null
                        _hasRenderedFirstFrame.value = true
                    }

                    override fun onTracksChanged(tracks: Tracks) {
                        Log.d(TAG, "[Tracks] Tracks changed, count: ${tracks.groups.size}")
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        _isPlaying.value = playing
                        Log.d(TAG, "[PlayingState] isPlaying: $playing")
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        _isBuffering.value = false
                        val cause = error.cause
                        val codeName = error.errorCodeName
                        val codeNumber = error.errorCode
                        val elapsedSinceStart = if (requestStartTimeMs > 0) System.currentTimeMillis() - requestStartTimeMs else 0

                        // Extract full chained root cause
                        var rootCauseMessage: String = cause?.message ?: error.message ?: "علت نامشخص"
                        var currentCause = cause
                        while (currentCause?.cause != null) {
                            currentCause = currentCause.cause
                            rootCauseMessage += " -> " + (currentCause?.message ?: "")
                        }

                        Log.e(TAG, "[PlayerError] Code: $codeName ($codeNumber), Elapsed: ${elapsedSinceStart}ms, RootCause: $rootCauseMessage", error)

                        val userFriendlyMessage = when {
                            cause is HttpDataSource.InvalidResponseCodeException -> {
                                val httpCode = cause.responseCode
                                Log.e(TAG, "[HTTP Error] Status: $httpCode for URL: ${_currentPlayingUrl.value}")
                                when (httpCode) {
                                    404 -> "خطای سرور ۴۰۴: فایل ویدیویی پیدا نشد یا از سرور حذف شده است."
                                    403 -> "خطای ۴۰۳: دسترسی توسط سرور یا فایروال مسدود شده است."
                                    500, 502, 503 -> "خطای موقت سرور ($httpCode): سرور ویدیو در دسترس نیست."
                                    else -> "پاسخ ناموفق از سرور ویدیو (کد $httpCode)"
                                }
                            }
                            cause is HttpDataSource.HttpDataSourceException -> {
                                Log.e(TAG, "[Network Error] HttpDataSourceException: ${cause.message}")
                                "خطا در اتصال به سرور ویدیو. اتصال اینترنت خود را بررسی نمایید."
                            }
                            !networkHelper.isNetworkAvailable() && !_isPlayingLocally.value -> {
                                _isOfflineWithoutCache.value = true
                                "اینترنت قطع است و این ویدیو هنوز در حافظه دستگاه ذخیره نشده است."
                            }
                            else -> {
                                "خطای پخش مدیا ($codeName): $rootCauseMessage"
                            }
                        }

                        lastDetailedError = "[$codeName / $codeNumber]: $rootCauseMessage"
                        _hasError.value = userFriendlyMessage
                        _diagnosticReport.value = buildTechnicalReport()
                    }
                })
            }
    }

    fun playVideo(video: VideoItem) {
        val player = exoPlayer ?: return
        _hasError.value = null
        _isOfflineWithoutCache.value = false
        _isBuffering.value = true
        _hasRenderedFirstFrame.value = false
        requestStartTimeMs = System.currentTimeMillis()
        bufferingStartTimeMs = requestStartTimeMs
        readyTimeMs = 0L
        firstFrameTimeMs = 0L
        lastDetailedError = null

        // Stop prior media cleanly before loading new item
        player.stop()
        player.clearMediaItems()

        // Priority 1: Check offline file on disk
        if (video.isDownloaded && video.localFilePath != null) {
            val localFile = File(video.localFilePath)
            if (localFile.exists() && localFile.length() > 0) {
                Log.i(TAG, "[Local Playback] Playing downloaded file: ${localFile.absolutePath} (${localFile.length()} bytes)")
                _isPlayingLocally.value = true
                _currentPlayingUrl.value = localFile.absolutePath
                val localMediaItem = MediaItem.fromUri(Uri.fromFile(localFile))
                player.setMediaItem(localMediaItem)
                player.prepare()
                player.playWhenReady = true
                return
            }
        }

        // Priority 2: Online playback with safe URL normalization
        _isPlayingLocally.value = false
        val normalizedUrl = safeNormalizeUrl(video.videoUrl)
        _currentPlayingUrl.value = normalizedUrl
        Log.i(TAG, "[Online Request] Initiating playback for: \"${video.title}\" -> $normalizedUrl")

        try {
            val uri = Uri.parse(normalizedUrl)
            val onlineMediaItem = MediaItem.Builder()
                .setUri(uri)
                .build()

            player.setMediaItem(onlineMediaItem)
            player.prepare()
            player.playWhenReady = true
            _diagnosticReport.value = buildTechnicalReport()
        } catch (e: Exception) {
            Log.e(TAG, "[Setup Error] Failed to configure MediaItem for $normalizedUrl", e)
            _isBuffering.value = false
            _hasError.value = "خطا در تنظیم آدرس ویدیو: ${e.message}"
            lastDetailedError = "SetupException: ${e.message}"
        }
    }

    /**
     * Plays a direct URL directly (for diagnostic testing of MP4 and HLS streams)
     */
    fun playDirectUrl(url: String, label: String = "آزمایشی") {
        val player = exoPlayer ?: return
        _hasError.value = null
        _isBuffering.value = true
        _isPlayingLocally.value = false
        _hasRenderedFirstFrame.value = false
        requestStartTimeMs = System.currentTimeMillis()
        bufferingStartTimeMs = requestStartTimeMs
        readyTimeMs = 0L
        firstFrameTimeMs = 0L
        lastDetailedError = null

        player.stop()
        player.clearMediaItems()

        val normalized = safeNormalizeUrl(url)
        _currentPlayingUrl.value = normalized
        Log.i(TAG, "[Diagnostic Test] Playing direct URL ($label): $normalized")

        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(normalized))
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
            _diagnosticReport.value = "شروع تست ($label):\n$normalized\nوضعیت: در حال اتصال..."
        } catch (e: Exception) {
            Log.e(TAG, "[Diagnostic Test Error] playDirectUrl failed: ${e.message}", e)
            _isBuffering.value = false
            _hasError.value = "خطای بارگذاری ویدیوی تست: ${e.message}"
            lastDetailedError = "DirectUrlException: ${e.message}"
        }
    }

    fun buildTechnicalReport(): String {
        val now = System.currentTimeMillis()
        val elapsed = if (requestStartTimeMs > 0) now - requestStartTimeMs else 0
        val ttff = if (firstFrameTimeMs > 0) firstFrameTimeMs - requestStartTimeMs else -1
        return buildString {
            appendLine("=== گزارش فنی وضعیت پلیر بچه گرام ===")
            appendLine("آدرس فعال: ${_currentPlayingUrl.value ?: "هیچ"}")
            appendLine("وضعیت داخلی پلیر: $lastPlayerStateName")
            appendLine("در حال بافرینگ: ${_isBuffering.value}")
            appendLine("فریم اول رندر شده: ${_hasRenderedFirstFrame.value}")
            if (ttff >= 0) appendLine("زمان تا فریم اول (TTFF): ${ttff}ms")
            appendLine("مدت زمان سپری‌شده: ${elapsed}ms")
            appendLine("در حال پخش زنده: ${_isPlaying.value}")
            appendLine("اتصال اینترنت دستگاه: ${if (networkHelper.isNetworkAvailable()) "متصل" else "قطع"}")
            appendLine("خطای ثبت‌شده: ${lastDetailedError ?: "بدون خطا"}")
        }
    }

    fun retryCurrentVideo(video: VideoItem) {
        Log.i(TAG, "[Retry] Retrying video: \"${video.title}\"")
        playVideo(video)
    }

    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun toggleMute() {
        exoPlayer?.let { player ->
            val newMuted = !_isMuted.value
            _isMuted.value = newMuted
            player.volume = if (newMuted) 0f else 1f
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun resume() {
        exoPlayer?.play()
    }

    fun updateProgress() {
        exoPlayer?.let { player ->
            if (player.playbackState == Player.STATE_READY) {
                _currentPosition.value = player.currentPosition
            }
        }
    }

    fun release() {
        Log.i(TAG, "[Player Lifecycle] Releasing ExoPlayer instance...")
        exoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.clearVideoSurface()
            player.release()
        }
        exoPlayer = null
    }
}
