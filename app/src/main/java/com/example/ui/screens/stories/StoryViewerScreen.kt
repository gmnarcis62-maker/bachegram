package com.example.ui.screens.stories

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.VideoItem
import kotlinx.coroutines.delay

private val IG_GRADIENT = listOf(
    Color(0xFFFFDD55),
    Color(0xFFFF5433),
    Color(0xFFC837AB),
    Color(0xFF5B51D8)
)

/**
 * Simple Instagram-style story viewer.
 * Shows the story media with a top progress bar and auto-dismisses after 5 seconds.
 */
@Composable
fun StoryViewerScreen(
    video: VideoItem,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 5000, easing = LinearEasing),
        label = "storyProgress"
    )

    // Auto-advance the progress bar and dismiss
    LaunchedEffect(video.id) {
        progress = 1f
        delay(5000)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() }
    ) {
        // Media (thumbnail)
        val imageSource: Any? = remember(video.thumbnailUrl, video.isDownloaded, video.localFilePath) {
            when {
                video.thumbnailUrl.isNotBlank() -> video.thumbnailUrl
                video.isDownloaded && video.localFilePath != null -> video.localFilePath
                else -> null
            }
        }

        if (imageSource != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageSource)
                    .apply {
                        if (video.isDownloaded && video.localFilePath != null) {
                            videoFrameMillis(1000L)
                        }
                    }
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎬", fontSize = 80.sp)
            }
        }

        // Top progress bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.35f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(2.5.dp)
                        .background(Color.White)
                )
            }
        }

        // Header: avatar + username + close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.sweepGradient(IG_GRADIENT)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emojiForCategory(video.category), fontSize = 16.sp)
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = video.sourceName.ifBlank { "بچه‌گرام" },
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "بستن",
                tint = Color.White,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onDismiss() }
            )
        }
    }
}

private fun emojiForCategory(category: String): String = when {
    category.contains("نقاشی") -> "🎨"
    category.contains("ریاضی") -> "🔢"
    category.contains("قصه") || category.contains("داستان") -> "📚"
    category.contains("علوم") -> "🔬"
    category.contains("موسیقی") || category.contains("شعر") -> "🎵"
    category.contains("زبان") -> "🇬🇧"
    category.contains("ورزش") -> "🏃"
    category.contains("طبیعت") -> "🌱"
    category.contains("رباتیک") -> "🤖"
    category.contains("مهارت") -> "🧼"
    else -> "🎬"
}