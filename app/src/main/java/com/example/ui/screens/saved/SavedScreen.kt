package com.example.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.VideoItem
import com.example.ui.screens.feed.VideoFeedViewModel

private val IG_TEXT = Color(0xFF000000)
private val IG_GRAY = Color(0xFF8E8E8E)
private val IG_BORDER = Color(0xFFDBDBDB)

private enum class SavedCategory(val label: String) {
    FAVORITES("پسندیده‌ها"),
    BOOKMARKS("ذخیره‌شده‌ها"),
    DOWNLOADS("دانلودها"),
    HISTORY("بازدیدها")
}

@Composable
fun SavedScreen(
    viewModel: VideoFeedViewModel,
    onVideoSelected: (VideoItem) -> Unit,
    onGoToFeed: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allVideos by viewModel.videos.collectAsState()
    var selectedCategory by remember { mutableStateOf(SavedCategory.FAVORITES) }

    val displayedVideos = when (selectedCategory) {
        SavedCategory.FAVORITES -> allVideos.filter { it.isFavorite }
        SavedCategory.BOOKMARKS -> allVideos.filter { it.isSaved }
        SavedCategory.DOWNLOADS -> allVideos.filter { it.isDownloaded }
        SavedCategory.HISTORY -> allVideos.filter { it.watchCount > 0 }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "بازگشت",
                tint = IG_TEXT,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onBack() }
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = "ذخیره‌شده‌ها",
                color = IG_TEXT,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        TabRow(
            selectedTabIndex = selectedCategory.ordinal,
            containerColor = Color.White,
            contentColor = IG_TEXT,
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(positions[selectedCategory.ordinal]),
                    color = IG_TEXT,
                    height = 1.5.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(IG_BORDER)
                )
            }
        ) {
            SavedCategory.values().forEach { cat ->
                Tab(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    text = {
                        Text(
                            text = cat.label,
                            fontSize = 12.sp,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategory == cat) IG_TEXT else IG_GRAY
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = when (cat) {
                                SavedCategory.FAVORITES -> Icons.Filled.Favorite
                                SavedCategory.BOOKMARKS -> Icons.Filled.Bookmark
                                SavedCategory.DOWNLOADS -> Icons.Filled.Download
                                SavedCategory.HISTORY -> Icons.Filled.History
                            },
                            contentDescription = cat.label,
                            tint = if (selectedCategory == cat) IG_TEXT else IG_GRAY,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        if (displayedVideos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = IG_GRAY,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = when (selectedCategory) {
                            SavedCategory.FAVORITES -> "هنوز چیزی نپسندیدی"
                            SavedCategory.BOOKMARKS -> "هنوز چیزی ذخیره نکردی"
                            SavedCategory.DOWNLOADS -> "هنوز چیزی دانلود نکردی"
                            SavedCategory.HISTORY -> "هنوز چیزی ندیدی"
                        },
                        color = IG_GRAY,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedVideos, key = { it.id }) { video ->
                    SavedTile(video = video, onClick = { onVideoSelected(video) })
                }
            }
        }
    }
}

@Composable
private fun SavedTile(video: VideoItem, onClick: () -> Unit) {
    val context = LocalContext.current
    val imageSource: Any? = remember(video.thumbnailUrl, video.isDownloaded, video.localFilePath) {
        when {
            video.thumbnailUrl.isNotBlank() -> video.thumbnailUrl
            video.isDownloaded && video.localFilePath != null -> video.localFilePath
            else -> null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(Color(0xFF111111))
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
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
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(text = emojiForCategory(video.category), fontSize = 36.sp)
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