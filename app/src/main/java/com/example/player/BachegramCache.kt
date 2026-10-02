package com.example.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

@OptIn(UnstableApi::class)
object BachegramCache {

    private const val CACHE_SIZE = 500L * 1024L * 1024L // 500 MB LRU video cache
    private var simpleCacheInstance: SimpleCache? = null

    @Synchronized
    fun getSimpleCache(context: Context): SimpleCache {
        if (simpleCacheInstance == null) {
            val cacheFolder = File(context.cacheDir, "bachegram_video_cache")
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs()
            }
            val databaseProvider = StandaloneDatabaseProvider(context)
            val evictor = LeastRecentlyUsedCacheEvictor(CACHE_SIZE)
            simpleCacheInstance = SimpleCache(cacheFolder, evictor, databaseProvider)
        }
        return simpleCacheInstance!!
    }

    fun buildCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        val cache = getSimpleCache(context)
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")

        val upstreamFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    fun getCacheSizeMB(context: Context): Double {
        val cacheFolder = File(context.cacheDir, "bachegram_video_cache")
        if (!cacheFolder.exists()) return 0.0
        val bytes = getFolderSize(cacheFolder)
        return bytes / (1024.0 * 1024.0)
    }

    fun clearCache(context: Context) {
        try {
            val cacheFolder = File(context.cacheDir, "bachegram_video_cache")
            if (cacheFolder.exists()) {
                cacheFolder.deleteRecursively()
                cacheFolder.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFolderSize(file: File): Long {
        if (file.isFile) return file.length()
        var size: Long = 0
        file.listFiles()?.forEach { child ->
            size += getFolderSize(child)
        }
        return size
    }
}
