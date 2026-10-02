package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise
import kotlinx.coroutines.launch

@Composable
fun VideoActionColumn(
    isFavorite: Boolean,
    isSaved: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Int?,
    likesCount: Int,
    isMuted: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    onReportClick: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val likeScale = remember { Animatable(1f) }
    val saveScale = remember { Animatable(1f) }

    Column(
        modifier = modifier.padding(end = 12.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mute / Unmute
        ActionBubble(
            icon = if (isMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
            label = if (isMuted) "بی‌صدا" else "صدا",
            tint = Color.White,
            onClick = onToggleMute,
            tag = "video_mute_button"
        )

        // Like / Favorite
        ActionBubble(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            label = PersianUtils.formatCount(likesCount + if (isFavorite) 1 else 0),
            tint = if (isFavorite) AccentPink else Color.White,
            scale = likeScale.value,
            onClick = {
                coroutineScope.launch {
                    likeScale.animateTo(1.4f, spring(stiffness = Spring.StiffnessHigh))
                    likeScale.animateTo(1.0f, spring(stiffness = Spring.StiffnessMedium))
                }
                onLikeClick()
            },
            tag = "video_like_button"
        )

        // Download / Offline Cache
        if (downloadProgress != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { downloadProgress / 100f },
                        color = SecondaryTurquoise,
                        modifier = Modifier.size(34.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = PersianUtils.toPersianDigits(downloadProgress),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "دانلود...",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White
                )
            }
        } else if (isDownloaded) {
            ActionBubble(
                icon = Icons.Filled.CheckCircle,
                label = "آفلاین",
                tint = BadgeGreen,
                onClick = onDownloadClick,
                tag = "video_downloaded_button"
            )
        } else {
            ActionBubble(
                icon = Icons.Outlined.Download,
                label = "دانلود",
                tint = Color.White,
                onClick = onDownloadClick,
                tag = "video_download_button"
            )
        }

        // Comment / Parent note
        ActionBubble(
            icon = Icons.Outlined.ChatBubbleOutline,
            label = "نظر",
            tint = Color.White,
            onClick = onCommentClick,
            tag = "video_comment_button"
        )

        // Save / Bookmark
        ActionBubble(
            icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            label = "ذخیره",
            tint = if (isSaved) PrimaryOrange else Color.White,
            scale = saveScale.value,
            onClick = {
                coroutineScope.launch {
                    saveScale.animateTo(1.3f, spring(stiffness = Spring.StiffnessHigh))
                    saveScale.animateTo(1.0f, spring(stiffness = Spring.StiffnessMedium))
                }
                onSaveClick()
            },
            tag = "video_save_button"
        )

        // Share
        ActionBubble(
            icon = Icons.Filled.Share,
            label = "اشتراک",
            tint = Color.White,
            onClick = onShareClick,
            tag = "video_share_button"
        )

        // Report inappropriate
        ActionBubble(
            icon = Icons.Filled.Security,
            label = "گزارش",
            tint = Color.White.copy(alpha = 0.8f),
            onClick = onReportClick,
            tag = "video_report_button"
        )
    }
}

@Composable
fun ActionBubble(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String,
    scale: Float = 1f
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
}
