package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.VideoItem
import com.example.ui.components.PersianUtils
import com.example.ui.screens.feed.VideoFeedViewModel

private val IG_TEXT = Color(0xFF000000)
private val IG_GRAY = Color(0xFF8E8E8E)
private val IG_BORDER = Color(0xFFDBDBDB)
private val IG_GRADIENT = listOf(
    Color(0xFFFFDD55),
    Color(0xFFFF5433),
    Color(0xFFC837AB),
    Color(0xFF5B51D8)
)

private enum class ProfileTab(val label: String) {
    POSTS("پست‌ها"),
    REELS("ریلز"),
    SAVED("ذخیره")
}

@Composable
fun ProfileScreen(
    viewModel: VideoFeedViewModel,
    onGoToAboutUs: () -> Unit = {},
    onGoToParentZone: () -> Unit = {},
    onGoToSaved: () -> Unit = {},
    onVideoSelected: (VideoItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allVideos by viewModel.videos.collectAsState()
    var selectedTab by remember { mutableStateOf(ProfileTab.POSTS) }

    val posts = allVideos
    val reels = allVideos.filter { it.duration in 30..180 }
    val saved = allVideos.filter { it.isSaved }

    val displayList = when (selectedTab) {
        ProfileTab.POSTS -> posts
        ProfileTab.REELS -> reels
        ProfileTab.SAVED -> saved
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ============== HEADER ==============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "bachegram_kid",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = IG_TEXT
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "▾",
                fontSize = 12.sp,
                color = IG_TEXT
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "تنظیمات",
                tint = IG_TEXT,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onGoToParentZone() }
            )
            Spacer(Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "منو",
                tint = IG_TEXT,
                modifier = Modifier.size(24.dp)
            )
        }

        // ============== AVATAR + STATS ==============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with gradient ring
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Brush.sweepGradient(IG_GRADIENT)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF2F2F2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌟", fontSize = 40.sp)
                    }
                }
            }

            Spacer(Modifier.width(20.dp))

            // Stats
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    number = PersianUtils.toPersianDigits(posts.size.toString()),
                    label = "پست"
                )
                ProfileStat(
                    number = PersianUtils.toPersianDigits(
                        PersianUtils.toPersianDigits(1234).toString()
                    ).let { PersianUtils.toPersianDigits("1234") },
                    label = "دنبال‌کننده"
                )
                ProfileStat(
                    number = PersianUtils.toPersianDigits("42"),
                    label = "دنبال‌شونده"
                )
            }
        }

        // ============== BIO ==============
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = "قهرمان کوچولو 🌈",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = IG_TEXT
            )
            Text(
                text = "دنیای شاد و آموزنده بچه‌ها",
                fontSize = 13.sp,
                color = IG_TEXT
            )
            Text(
                text = "🎨 یادگیری با بازی و سرگرمی",
                fontSize = 13.sp,
                color = IG_TEXT
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "bachegram.ir",
                fontSize = 13.sp,
                color = Color(0xFF00376B),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        // ============== ACTION BUTTONS ==============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProfileButton(
                label = "ویرایش پروفایل",
                onClick = onGoToParentZone,
                modifier = Modifier.weight(1f)
            )
            ProfileButton(
                label = "اشتراک پروفایل",
                onClick = { },
                modifier = Modifier.weight(1f)
            )
            ProfileButton(
                label = "ذخیره‌ها",
                onClick = onGoToSaved,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // ============== HIGHLIGHTS ROW ==============
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(listOf("🎨", "🔢", "📚", "🔬", "🎵")) { emoji ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, IG_BORDER, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2F2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 26.sp)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "هایلایت",
                        fontSize = 11.sp,
                        color = IG_TEXT,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ============== TAB ROW ==============
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when (tab) {
                            ProfileTab.POSTS -> if (isSelected) Icons.Filled.GridOn else Icons.Outlined.GridOn
                            ProfileTab.REELS -> if (isSelected) Icons.Filled.PlayArrow else Icons.Outlined.PlayArrow
                            ProfileTab.SAVED -> if (isSelected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
                        },
                        contentDescription = tab.label,
                        tint = if (isSelected) IG_TEXT else IG_GRAY,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        color = if (isSelected) IG_TEXT else IG_GRAY,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(IG_BORDER)
        )

        // ============== GRID (3 columns, square, CROP) ==============
        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = IG_GRAY,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "هنوز چیزی اینجا نیست",
                        color = IG_GRAY,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // We use a manual grid via chunked rows so it fits inside a verticalScroll Column
            Column(modifier = Modifier.fillMaxWidth()) {
                displayList.chunked(3).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { video ->
                            ProfileGridTile(
                                video = video,
                                modifier = Modifier.weight(1f),
                                onClick = { onVideoSelected(video) }
                            )
                        }
                        // Fill empty space when the last row has < 3 items
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileStat(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = IG_TEXT
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = IG_TEXT
        )
    }
}

@Composable
private fun ProfileButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFEFEF))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = IG_TEXT,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileGridTile(
    video: VideoItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageSource: Any? = remember(video.thumbnailUrl, video.isDownloaded, video.localFilePath) {
        when {
            video.thumbnailUrl.isNotBlank() -> video.thumbnailUrl
            video.isDownloaded && video.localFilePath != null -> video.localFilePath
            else -> null
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(0.5.dp)
            .background(Color(0xFF111111))
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

        // Small play icon bottom-right if it's a reel
        if (video.duration in 30..180) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
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