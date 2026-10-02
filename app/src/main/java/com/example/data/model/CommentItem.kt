package com.example.data.model

data class CommentItem(
    val id: String,
    val videoId: String,
    val authorName: String,
    val authorEmoji: String,
    val text: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val isFromCreator: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val timeAgoLabel: String = "الان"
)