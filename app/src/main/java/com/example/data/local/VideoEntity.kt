package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.VideoItem
import com.example.data.model.VideoStatus

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val subCategory: String,
    val ageMin: Int,
    val ageMax: Int,
    val thumbnailUrl: String,
    val videoUrl: String,
    val duration: Int,
    val language: String,
    val sourceName: String,
    val sourceUrl: String,
    val isDirectMedia: Boolean,
    val isVerified: Boolean,
    val isFavorite: Boolean = false,
    val isSaved: Boolean = false,
    val likesCount: Int = 0,
    val watchCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false,
    val fileSizeBytes: Long = 0L,
    val videoStatus: String = VideoStatus.VERIFIED.name
) {
    fun toVideoItem(): VideoItem = VideoItem(
        id = id,
        title = title,
        description = description,
        category = category,
        subCategory = subCategory,
        ageMin = ageMin,
        ageMax = ageMax,
        thumbnailUrl = thumbnailUrl,
        videoUrl = videoUrl,
        duration = duration,
        language = language,
        sourceName = sourceName,
        sourceUrl = sourceUrl,
        isDirectMedia = isDirectMedia,
        isVerified = isVerified,
        isFavorite = isFavorite,
        isSaved = isSaved,
        likesCount = likesCount,
        watchCount = watchCount,
        createdAt = createdAt,
        localFilePath = localFilePath,
        isDownloaded = isDownloaded,
        fileSizeBytes = fileSizeBytes,
        videoStatus = videoStatus
    )

    companion object {
        fun fromVideoItem(item: VideoItem): VideoEntity = VideoEntity(
            id = item.id,
            title = item.title,
            description = item.description,
            category = item.category,
            subCategory = item.subCategory,
            ageMin = item.ageMin,
            ageMax = item.ageMax,
            thumbnailUrl = item.thumbnailUrl,
            videoUrl = item.videoUrl,
            duration = item.duration,
            language = item.language,
            sourceName = item.sourceName,
            sourceUrl = item.sourceUrl,
            isDirectMedia = item.isDirectMedia,
            isVerified = item.isVerified,
            isFavorite = item.isFavorite,
            isSaved = item.isSaved,
            likesCount = item.likesCount,
            watchCount = item.watchCount,
            createdAt = item.createdAt,
            localFilePath = item.localFilePath,
            isDownloaded = item.isDownloaded,
            fileSizeBytes = item.fileSizeBytes,
            videoStatus = item.videoStatus
        )
    }
}
