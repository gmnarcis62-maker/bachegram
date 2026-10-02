package com.example.ui.screens.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.VideoItem
import com.example.data.repository.VideoRepository
import com.example.data.settings.NetworkSettingsManager
import com.example.download.DownloadStatus
import com.example.download.VideoDownloadManager
import com.example.network.NetworkHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VideoFeedViewModel(application: Application) : AndroidViewModel(application) {

    val repository = VideoRepository(application)
    val downloadManager = VideoDownloadManager(application, repository)
    val networkSettingsManager = NetworkSettingsManager(application)
    val networkHelper = NetworkHelper(application)

    val videos: StateFlow<List<VideoItem>> = repository.getAllVideos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val downloadedVideos: StateFlow<List<VideoItem>> = repository.getDownloadedVideos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val downloadStates: StateFlow<Map<String, DownloadStatus>> = downloadManager.downloadStates

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfNeeded()
        }
    }

    fun onPageChanged(index: Int) {
        _currentIndex.value = index
        val currentList = videos.value
        if (index in currentList.indices) {
            viewModelScope.launch {
                repository.recordWatch(currentList[index].id)
            }
        }
    }

    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleFavorite(video.id, video.isFavorite)
        }
    }

    fun toggleSaved(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleSaved(video.id, video.isSaved)
        }
    }

    fun likeVideo(video: VideoItem) {
        viewModelScope.launch {
            repository.likeVideo(video.id)
            if (!video.isFavorite) {
                repository.toggleFavorite(video.id, false)
            }
        }
    }

    fun startDownload(video: VideoItem) {
        downloadManager.startDownload(video)
    }

    fun cancelDownload(videoId: String) {
        downloadManager.cancelDownload(videoId)
    }

    fun deleteDownloadedVideo(videoId: String) {
        downloadManager.deleteDownloadedVideo(videoId)
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun selectVideo(video: VideoItem) {
        val index = videos.value.indexOfFirst { it.id == video.id }
        if (index >= 0) {
            _currentIndex.value = index
        }
    }

    val allVideosForAdmin: StateFlow<List<VideoItem>> = repository.getAllVideosForAdmin()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isScanningHealth = MutableStateFlow(false)
    val isScanningHealth: StateFlow<Boolean> = _isScanningHealth.asStateFlow()

    private val _scanProgress = MutableStateFlow(Pair(0, 0))
    val scanProgress: StateFlow<Pair<Int, Int>> = _scanProgress.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow<String?>(null)
    val scanStatusMessage: StateFlow<String?> = _scanStatusMessage.asStateFlow()

    fun runHealthCheckOnAllVideos() {
        if (_isScanningHealth.value) return
        viewModelScope.launch {
            _isScanningHealth.value = true
            val list = allVideosForAdmin.value
            _scanProgress.value = Pair(0, list.size)
            var verifiedCount = 0
            var failedCount = 0

            for ((index, video) in list.withIndex()) {
                _scanStatusMessage.value = "در حال بررسی (${index + 1}/${list.size}): ${video.title.take(20)}..."
                _scanProgress.value = Pair(index + 1, list.size)

                val result = com.example.validator.VideoLinkValidator.validateVideoUrl(video.videoUrl)
                when (result) {
                    is com.example.validator.ValidationResult.Success -> {
                        repository.updateVideoStatus(video.id, com.example.data.model.VideoStatus.VERIFIED)
                        verifiedCount++
                    }
                    is com.example.validator.ValidationResult.Failed -> {
                        repository.updateVideoStatus(video.id, com.example.data.model.VideoStatus.FAILED)
                        failedCount++
                    }
                }
            }

            _isScanningHealth.value = false
            _scanStatusMessage.value = "بررسی به پایان رسید: $verifiedCount سالم، $failedCount نیازمند اصلاح"
        }
    }

    fun deleteFailedVideos() {
        viewModelScope.launch {
            val count = repository.deleteFailedVideos()
            _scanStatusMessage.value = "$count ویدیوی خراب با موفقیت حذف شدند"
        }
    }

    fun addNewVerifiedVideo(
        title: String,
        description: String,
        category: String,
        subCategory: String,
        videoUrl: String,
        ageMin: Int,
        ageMax: Int,
        duration: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            // Step 1: Duplicate check
            val existing = allVideosForAdmin.value
            val duplicateError = com.example.validator.VideoLinkValidator.isDuplicate(videoUrl, title, existing)
            if (duplicateError != null) {
                onResult(false, duplicateError)
                return@launch
            }

            // Step 2: Real HTTP & MimeType health validation
            val validation = com.example.validator.VideoLinkValidator.validateVideoUrl(videoUrl)
            when (validation) {
                is com.example.validator.ValidationResult.Failed -> {
                    onResult(false, "لینک نامعتبر است: ${validation.reason}")
                }
                is com.example.validator.ValidationResult.Success -> {
                    val newVideo = VideoItem(
                        id = "video_custom_${System.currentTimeMillis()}",
                        title = title.trim(),
                        description = description.trim(),
                        category = category.trim(),
                        subCategory = subCategory.trim(),
                        ageMin = ageMin,
                        ageMax = ageMax,
                        thumbnailUrl = "",
                        videoUrl = videoUrl.trim(),
                        duration = duration,
                        language = "فارسی",
                        sourceName = "مدیر بچه‌گرام",
                        sourceUrl = videoUrl.trim(),
                        isDirectMedia = true,
                        isVerified = true,
                        videoStatus = com.example.data.model.VideoStatus.VERIFIED.name
                    )
                    repository.addVerifiedVideo(newVideo)
                    onResult(true, "ویدیوی جدید پس از بررسی سلامت و تأیید فنی با موفقیت اضافه شد! ✅")
                }
            }
        }
    }
}
