package com.example.data.model

enum class VideoStatus {
    VERIFIED,       // تأیید شده و سالم (فقط این وضعیت در فید نمایش داده می‌شود)
    FAILED,         // خراب (۴۰۴، ۴۰۳ یا خطای دیکودر)
    EXPIRED,        // منقضی شده
    CHECK_REQUIRED  // نیازمند بررسی مجدد
}

data class VideoItem(
    val id: String,
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
    val isDirectMedia: Boolean = true,
    val isVerified: Boolean = true,
    val isFavorite: Boolean = false,
    val isSaved: Boolean = false,
    val likesCount: Int = 120,
    val watchCount: Int = 450,
    val createdAt: Long = System.currentTimeMillis(),
    val localFilePath: String? = null,
    val isDownloaded: Boolean = false,
    val fileSizeBytes: Long = 0L,
    val videoStatus: String = VideoStatus.VERIFIED.name
)
