package com.example.ui.screens.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.BachegramTopBar
import com.example.ui.components.PersianUtils
import com.example.ui.screens.categories.CategoryVideoItemCard
import com.example.ui.screens.feed.VideoFeedViewModel
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise

enum class SavedTab(val title: String) {
    FAVORITES("❤️ علاقه‌مندی‌ها"),
    BOOKMARKS("🔖 ذخیره‌شده‌ها"),
    OFFLINE_DOWNLOADS("💾 دانلودهای آفلاین"),
    HISTORY("🕒 تاریخچه تماشا")
}

@Composable
fun SavedScreen(
    viewModel: VideoFeedViewModel,
    onVideoSelected: (VideoItem) -> Unit,
    onGoToFeed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allVideos by viewModel.videos.collectAsState()
    var selectedTab by remember { mutableStateOf(SavedTab.FAVORITES) }

    val displayedVideos = when (selectedTab) {
        SavedTab.FAVORITES -> allVideos.filter { it.isFavorite }
        SavedTab.BOOKMARKS -> allVideos.filter { it.isSaved }
        SavedTab.OFFLINE_DOWNLOADS -> allVideos.filter { it.isDownloaded }
        SavedTab.HISTORY -> allVideos.filter { it.watchCount > 0 }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BachegramTopBar(title = "صندوق من")

        // Scrollable Tab Row for all 4 tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryOrange,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = PrimaryOrange,
                    height = 3.dp
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            SavedTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name}")
                )
            }
        }

        if (displayedVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = when (selectedTab) {
                            SavedTab.FAVORITES -> "هنوز ویدیویی را پسند نکرده‌اید ❤️"
                            SavedTab.BOOKMARKS -> "ویدیویی ذخیره نکرده‌اید 🔖"
                            SavedTab.OFFLINE_DOWNLOADS -> "هنوز ویدیویی برای تماشای آفلاین دانلود نشده است 💾"
                            SavedTab.HISTORY -> "هنوز ویدیویی تماشا نکرده‌اید 🕒"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = when (selectedTab) {
                            SavedTab.OFFLINE_DOWNLOADS -> "در صفحه اصلی ویدیو، روی دکمه دانلود (📥) بزنید تا ویدیو در حافظه ذخیره و بدون نیاز به اینترنت قابل پخش شود."
                            else -> "با تماشای ویدیوها در خانه و دو بار ضربه روی تصویر، آنها را در این بخش نگه دارید."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onGoToFeed,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("تماشای ویدیوها")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedVideos) { video ->
                    if (selectedTab == SavedTab.OFFLINE_DOWNLOADS) {
                        DownloadedVideoCard(
                            video = video,
                            onClick = { onVideoSelected(video) },
                            onDelete = { viewModel.deleteDownloadedVideo(video.id) }
                        )
                    } else {
                        CategoryVideoItemCard(
                            video = video,
                            onClick = { onVideoSelected(video) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadedVideoCard(
    video: VideoItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val sizeMB = if (video.fileSizeBytes > 0) {
        String.format("%.1f مگابایت", video.fileSizeBytes / (1024.0 * 1024.0))
    } else {
        "دانلود شده"
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(BadgeGreen, SecondaryTurquoise))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "پخش آفلاین",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BadgeGreen.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = BadgeGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "آماده تماشای آفلاین",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = BadgeGreen
                            )
                        }
                    }

                    Text(
                        text = PersianUtils.toPersianDigits(sizeMB),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "حذف از حافظه",
                    tint = Color(0xFFE63946)
                )
            }
        }
    }
}
