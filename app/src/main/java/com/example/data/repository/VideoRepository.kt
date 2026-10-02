package com.example.data.repository

import android.content.Context
import com.example.data.local.BachegramDatabase
import com.example.data.local.VideoDao
import com.example.data.local.VideoEntity
import com.example.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStreamReader

class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao = BachegramDatabase.getInstance(context).videoDao()
) {
    suspend fun seedDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val count = videoDao.getCount()
        if (count == 0 || videoDao.getCountByStatus("VERIFIED") == 0) {
            try {
                val jsonString = context.assets.open("initial_videos.json").use { inputStream ->
                    InputStreamReader(inputStream).readText()
                }
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<VideoEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        VideoEntity(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            category = obj.getString("category"),
                            subCategory = obj.optString("subCategory", "آموزشی"),
                            ageMin = obj.optInt("ageMin", 4),
                            ageMax = obj.optInt("ageMax", 8),
                            thumbnailUrl = obj.optString("thumbnailUrl", ""),
                            videoUrl = com.example.player.Media3PlayerManager.safeNormalizeUrl(obj.getString("videoUrl")),
                            duration = obj.optInt("duration", 180),
                            language = obj.optString("language", "fa"),
                            sourceName = obj.optString("sourceName", "اینترنت آموزشی"),
                            sourceUrl = obj.optString("sourceUrl", ""),
                            isDirectMedia = obj.optBoolean("isDirectMedia", true),
                            isVerified = obj.optBoolean("isVerified", true),
                            isFavorite = false,
                            isSaved = false,
                            likesCount = 50 + (i * 7) % 350,
                            watchCount = 120 + (i * 13) % 900,
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis() - i * 3600000L),
                            videoStatus = com.example.data.model.VideoStatus.VERIFIED.name
                        )
                    )
                }
                videoDao.insertVideos(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAllVideos(): Flow<List<VideoItem>> =
        videoDao.getAllVideos().map { entities -> entities.map { it.toVideoItem() } }

    fun getFavoriteVideos(): Flow<List<VideoItem>> =
        videoDao.getFavoriteVideos().map { entities -> entities.map { it.toVideoItem() } }

    fun getSavedVideos(): Flow<List<VideoItem>> =
        videoDao.getSavedVideos().map { entities -> entities.map { it.toVideoItem() } }

    fun getHistoryVideos(): Flow<List<VideoItem>> =
        videoDao.getHistoryVideos().map { entities -> entities.map { it.toVideoItem() } }

    fun getVideosByCategory(category: String): Flow<List<VideoItem>> =
        videoDao.getVideosByCategory(category).map { entities -> entities.map { it.toVideoItem() } }

    fun searchVideos(query: String): Flow<List<VideoItem>> =
        videoDao.searchVideos(query).map { entities -> entities.map { it.toVideoItem() } }

    fun getDownloadedVideos(): Flow<List<VideoItem>> =
        videoDao.getDownloadedVideos().map { entities -> entities.map { it.toVideoItem() } }

    suspend fun markDownloaded(id: String, path: String, sizeBytes: Long) = withContext(Dispatchers.IO) {
        videoDao.setDownloaded(id, true, path, sizeBytes)
    }

    suspend fun removeDownload(id: String) = withContext(Dispatchers.IO) {
        videoDao.setDownloaded(id, false, null, 0L)
    }

    suspend fun toggleFavorite(id: String, currentState: Boolean) = withContext(Dispatchers.IO) {
        videoDao.setFavorite(id, !currentState)
    }

    suspend fun toggleSaved(id: String, currentState: Boolean) = withContext(Dispatchers.IO) {
        videoDao.setSaved(id, !currentState)
    }

    suspend fun recordWatch(id: String) = withContext(Dispatchers.IO) {
        videoDao.incrementWatch(id)
    }

    fun getAllVideosForAdmin(): Flow<List<VideoItem>> =
        videoDao.getAllVideosForAdmin().map { entities -> entities.map { it.toVideoItem() } }

    suspend fun updateVideoStatus(id: String, status: com.example.data.model.VideoStatus) = withContext(Dispatchers.IO) {
        videoDao.updateVideoStatus(id, status.name)
    }

    suspend fun deleteVideo(id: String) = withContext(Dispatchers.IO) {
        videoDao.deleteVideo(id)
    }

    suspend fun deleteFailedVideos(): Int = withContext(Dispatchers.IO) {
        videoDao.deleteFailedVideos()
    }

    suspend fun addVerifiedVideo(video: VideoItem) = withContext(Dispatchers.IO) {
        videoDao.insertVideo(com.example.data.local.VideoEntity.fromVideoItem(video))
    }

    suspend fun likeVideo(id: String) = withContext(Dispatchers.IO) {
        videoDao.incrementLike(id)
    }
}
