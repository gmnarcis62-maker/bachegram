package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlaybackMode {
    WIFI_AND_MOBILE, // Default: plays freely on WiFi and Mobile Data
    WIFI_ONLY,       // Only plays on WiFi
    PROMPT           // Ask user
}

enum class DownloadMode {
    WIFI_AND_MOBILE,
    WIFI_ONLY,       // Default: saves cellular data
    PROMPT           // Always prompt on cellular data
}

class NetworkSettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bachegram_network_prefs", Context.MODE_PRIVATE)

    private val _playbackMode = MutableStateFlow(
        PlaybackMode.valueOf(prefs.getString("playback_mode", PlaybackMode.WIFI_AND_MOBILE.name) ?: PlaybackMode.WIFI_AND_MOBILE.name)
    )
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    private val _downloadMode = MutableStateFlow(
        DownloadMode.valueOf(prefs.getString("download_mode", DownloadMode.PROMPT.name) ?: DownloadMode.PROMPT.name)
    )
    val downloadMode: StateFlow<DownloadMode> = _downloadMode.asStateFlow()

    private val _autoCacheEnabled = MutableStateFlow(
        prefs.getBoolean("auto_cache_enabled", true)
    )
    val autoCacheEnabled: StateFlow<Boolean> = _autoCacheEnabled.asStateFlow()

    fun setPlaybackMode(mode: PlaybackMode) {
        _playbackMode.value = mode
        prefs.edit().putString("playback_mode", mode.name).apply()
    }

    fun setDownloadMode(mode: DownloadMode) {
        _downloadMode.value = mode
        prefs.edit().putString("download_mode", mode.name).apply()
    }

    fun setAutoCacheEnabled(enabled: Boolean) {
        _autoCacheEnabled.value = enabled
        prefs.edit().putBoolean("auto_cache_enabled", enabled).apply()
    }
}
