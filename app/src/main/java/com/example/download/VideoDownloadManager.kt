package com.example.download

import android.content.Context
import com.example.data.model.VideoItem
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(val progressPercent: Int) : DownloadStatus()
    data class Completed(val filePath: String, val sizeBytes: Long) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}

class VideoDownloadManager(
    private val context: Context,
    private val repository: VideoRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val downloadJobs = mutableMapOf<String, Job>()

    private val _downloadStates = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadStatus>> = _downloadStates.asStateFlow()

    fun getDownloadDir(): File {
        val dir = File(context.filesDir, "bachegram_offline_videos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun isVideoDownloadedLocally(video: VideoItem): Boolean {
        if (!video.isDownloaded || video.localFilePath == null) return false
        val file = File(video.localFilePath)
        return file.exists() && file.length() > 0
    }

    fun getEstimatedSizeBytes(durationSeconds: Int): Long {
        // Estimate approx 600 KB per minute for kid mobile video
        val minutes = (durationSeconds / 60.0).coerceAtLeast(1.0)
        return (minutes * 3.5 * 1024 * 1024).toLong()
    }

    fun startDownload(video: VideoItem) {
        if (downloadJobs[video.id]?.isActive == true) return

        val job = scope.launch {
            try {
                updateStatus(video.id, DownloadStatus.Downloading(0))

                val outputFile = File(getDownloadDir(), "${video.id}.mp4")
                val url = URL(video.videoUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 25000
                    setRequestProperty("User-Agent", "BachegramApp/1.0")
                    connect()
                }

                val totalLength = connection.contentLengthLong.let { if (it > 0) it else getEstimatedSizeBytes(video.duration) }
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(outputFile)

                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead = 0L
                var lastProgress = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress = ((totalBytesRead * 100) / totalLength).toInt().coerceIn(0, 99)
                    if (progress != lastProgress) {
                        lastProgress = progress
                        updateStatus(video.id, DownloadStatus.Downloading(progress))
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                connection.disconnect()

                val finalSize = outputFile.length()
                updateStatus(video.id, DownloadStatus.Completed(outputFile.absolutePath, finalSize))
                repository.markDownloaded(video.id, outputFile.absolutePath, finalSize)

            } catch (e: Exception) {
                e.printStackTrace()
                updateStatus(video.id, DownloadStatus.Error("خطا در دانلود ویدیو"))
            } finally {
                downloadJobs.remove(video.id)
            }
        }

        downloadJobs[video.id] = job
    }

    fun cancelDownload(videoId: String) {
        downloadJobs[videoId]?.cancel()
        downloadJobs.remove(videoId)
        updateStatus(videoId, DownloadStatus.Idle)
        val file = File(getDownloadDir(), "$videoId.mp4")
        if (file.exists()) {
            file.delete()
        }
    }

    fun deleteDownloadedVideo(videoId: String) {
        scope.launch {
            cancelDownload(videoId)
            val file = File(getDownloadDir(), "$videoId.mp4")
            if (file.exists()) {
                file.delete()
            }
            repository.removeDownload(videoId)
            updateStatus(videoId, DownloadStatus.Idle)
        }
    }

    private fun updateStatus(videoId: String, status: DownloadStatus) {
        val current = _downloadStates.value.toMutableMap()
        current[videoId] = status
        _downloadStates.value = current
    }
}
