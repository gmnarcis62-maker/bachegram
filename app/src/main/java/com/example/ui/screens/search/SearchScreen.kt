package com.example.ui.screens.search

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.R
import com.example.data.model.VideoItem
import com.example.data.settings.PlaybackMode
import com.example.player.GridVideoPreviewManager
import com.example.ui.components.PersianUtils
import com.example.ui.components.RainbowStrip
import com.example.ui.components.bouncyClick
import com.example.ui.screens.feed.VideoFeedViewModel
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise
import com.example.ui.theme.StarYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

private val EXPLORE_REAL_CATEGORIES = listOf(
    "همه",
    "⭐ محبوب‌ترین‌ها",
    "🎨 نقاشی و کاردستی",
    "🎭 سرگرمی",
    "📚 قصه و داستان",
    "🔬 علوم",
    "🎵 شعر و موسیقی",
    "🔤 فارسی و الفبا",
    "🔢 ریاضی",
    "🇬🇧 زبان انگلیسی",
    "🤖 رباتیک و برنامه‌نویسی",
    "🧼 مهارت‌های زندگی",
    "🧠 هوش و خلاقیت",
    "🏃 ورزش و حرکت"
)

fun normalizePersian(text: String): String {
    return text
        .replace('ي', 'ی')
        .replace('ك', 'ک')
        .replace('ة', 'ه')
        .replace('\u200C', ' ')
        .replace('آ', 'ا')
        .replace('إ', 'ا')
        .replace('أ', 'ا')
        .replace('ئ', 'ی')
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase()
}

@Composable
fun SearchScreen(
    viewModel: VideoFeedViewModel,
    onVideoSelected: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val allVideos by viewModel.videos.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryChip by rememberSaveable { mutableStateOf("همه") }

    val gridState = rememberLazyGridState()
    val previewManager = remember { GridVideoPreviewManager(context) }
    val activePreviewId by previewManager.activePreviewId.collectAsState()
    val isPreviewReady by previewManager.isPreviewReady.collectAsState()

    val playbackMode by viewModel.networkSettingsManager.playbackMode.collectAsState()
    val isDataSaverActive = (playbackMode == PlaybackMode.WIFI_ONLY && !viewModel.networkHelper.isWiFi())

    // Lifecycle observer for grid preview player
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> previewManager.pause()
                Lifecycle.Event.ON_RESUME -> previewManager.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            previewManager.release()
        }
    }

    // Filter videos
    val normalizedQuery = remember(searchQuery) { normalizePersian(searchQuery) }

    val filteredVideos = remember(allVideos, normalizedQuery, selectedCategoryChip) {
        allVideos.filter { video ->
            val matchesQuery = if (normalizedQuery.isBlank()) {
                true
            } else {
                val normTitle = normalizePersian(video.title)
                val normDesc = normalizePersian(video.description)
                val normCat = normalizePersian(video.category)
                val normSub = normalizePersian(video.subCategory)

                val queryWords = normalizedQuery.split(" ").filter { it.isNotBlank() }
                queryWords.all { word ->
                    normTitle.contains(word) || normDesc.contains(word) ||
                        normCat.contains(word) || normSub.contains(word)
                }
            }

            val matchesCategory = when (selectedCategoryChip) {
                "همه" -> true
                "⭐ محبوب‌ترین‌ها" -> video.likesCount >= 50 || video.isFavorite
                else -> {
                    val cleanChip = selectedCategoryChip.replace(
                        Regex("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\\p{Cn}]+\\s*"), ""
                    )
                    video.category.contains(cleanChip, ignoreCase = true)
                }
            }

            matchesQuery && matchesCategory
        }
    }

    // ============================================================
    //  FIXED PREVIEW TRIGGER LOGIC
    //  - Fires on initial load
    //  - Fires whenever scroll settles
    //  - Fires whenever the filtered list changes
    // ============================================================
    LaunchedEffect(filteredVideos, isDataSaverActive) {
        if (isDataSaverActive) return@LaunchedEffect
        if (filteredVideos.isEmpty()) return@LaunchedEffect

        // Initial trigger: play preview for the topmost centered item after layout
        delay(450L)
        triggerCenterPreview(gridState, filteredVideos, previewManager)

        // Then observe scroll state transitions
        snapshotFlow { gridState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { isScrolling ->
                if (isScrolling) {
                    previewManager.stopPreview()
                } else {
                    // Wait for scroll to fully settle, then re-check center item
                    delay(400L)
                    triggerCenterPreview(gridState, filteredVideos, previewManager)
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RainbowStrip(height = 3.dp)

        // Search Bar + Category Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "جستجوی کلیپ، آموزش، موضوع... 🔍",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "جستجو",
                        tint = PrimaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "پاک کردن متن",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_field")
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(EXPLORE_REAL_CATEGORIES) { chipTitle ->
                    val isSelected = selectedCategoryChip == chipTitle
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryChip = chipTitle },
                        label = {
                            Text(
                                text = chipTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryOrange,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PrimaryOrange
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        // Grid or Empty state
        if (filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bobo_idle),
                        contentDescription = "بوبو",
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "هیچ ویدیویی با این مشخصات پیدا نشد!",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "می‌تونی عبارت دیگه‌ای رو جستجو کنی یا فیلتر دسته‌بندی رو پاک کنی.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedCategoryChip = "همه"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTurquoise),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("نمایش همه ویدیوها", color = Color.White)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                contentPadding = PaddingValues(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_grid")
            ) {
                itemsIndexed(
                    items = filteredVideos,
                    key = { _, video -> video.id },
                    span = { index, _ ->
                        if (index % 9 == 0 && index > 0) GridItemSpan(2) else GridItemSpan(1)
                    }
                ) { index, video ->
                    val isLargeTile = (index % 9 == 0 && index > 0)
                    val isCurrentlyPreviewing = (activePreviewId == video.id)

                    InstagramExploreLiveCard(
                        video = video,
                        isLargeTile = isLargeTile,
                        isCurrentlyPreviewing = isCurrentlyPreviewing,
                        isPreviewReady = isPreviewReady,
                        previewManager = previewManager,
                        onClick = {
                            previewManager.stopPreview()
                            onVideoSelected(video)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Helper: finds the topmost centered item and asks the preview manager to play it.
 * This function is safe to call repeatedly — the manager dedupes same-video requests.
 */
private fun triggerCenterPreview(
    gridState: LazyGridState,
    videos: List<VideoItem>,
    previewManager: GridVideoPreviewManager
) {
    val layout = gridState.layoutInfo
    if (layout.visibleItemsInfo.isEmpty() || videos.isEmpty()) return

    val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
    val centerItem = layout.visibleItemsInfo.minByOrNull { item ->
        val itemCenter = item.offset.y + item.size.height / 2
        kotlin.math.abs(itemCenter - viewportCenter)
    } ?: return

    if (centerItem.index in videos.indices) {
        previewManager.playPreview(videos[centerItem.index])
    }
}

/**
 * Instagram-Explore-style grid card.
 * - Thumbnail / video preview uses RESIZE_MODE_FIT (no zoom / crop).
 * - NO overlays on the video area (no icons, no text).
 * - All info (title / category / duration) sits BELOW the video.
 */
@Composable
fun InstagramExploreLiveCard(
    video: VideoItem,
    isLargeTile: Boolean,
    isCurrentlyPreviewing: Boolean,
    isPreviewReady: Boolean,
    previewManager: GridVideoPreviewManager,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val fallbackGradient = remember(video.category) {
        when {
            video.category.contains("نقاشی") -> listOf(Color(0xFFFF9F1C), Color(0xFFFF4081))
            video.category.contains("ریاضی") -> listOf(Color(0xFF2EC4B6), Color(0xFF011627))
            video.category.contains("قصه") || video.category.contains("داستان") ->
                listOf(Color(0xFF8338EC), Color(0xFF3A86FF))
            video.category.contains("علوم") -> listOf(Color(0xFF06D6A0), Color(0xFF118AB2))
            video.category.contains("موسیقی") -> listOf(Color(0xFFFF006E), Color(0xFFFB5607))
            video.category.contains("زبان") -> listOf(Color(0xFF4361EE), Color(0xFF7209B7))
            else -> listOf(Color(0xFF4361EE), Color(0xFF3F37C9))
        }
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClick { onClick() }
            .testTag("explore_item_${video.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ============ PURE VIDEO AREA ============
            // No icons, no text, no overlays. Only thumbnail or live preview.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isLargeTile) 1.25f else 0.85f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val imageSource: Any? = remember(video.thumbnailUrl, video.isDownloaded, video.localFilePath) {
                    when {
                        video.thumbnailUrl.isNotBlank() -> video.thumbnailUrl
                        video.isDownloaded && video.localFilePath != null -> video.localFilePath
                        else -> null
                    }
                }

                // Layer 1: thumbnail / fallback gradient
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
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(fallbackGradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        val categoryEmoji = remember(video.category) {
                            when {
                                video.category.contains("نقاشی") -> "🎨"
                                video.category.contains("ریاضی") -> "🔢"
                                video.category.contains("قصه") || video.category.contains("داستان") -> "📚"
                                video.category.contains("علوم") -> "🔬"
                                video.category.contains("موسیقی") || video.category.contains("شعر") -> "🎵"
                                video.category.contains("زبان") -> "🇬🇧"
                                video.category.contains("ورزش") -> "🏃"
                                video.category.contains("طبیعت") -> "🌱"
                                video.category.contains("رباتیک") -> "🤖"
                                video.category.contains("مهارت") -> "🧼"
                                else -> "🎬"
                            }
                        }
                        Text(
                            text = categoryEmoji,
                            fontSize = if (isLargeTile) 36.sp else 24.sp
                        )
                    }
                }

                // Layer 2: live muted preview (RESIZE_MODE_FIT → whole video visible)
                if (isCurrentlyPreviewing && isPreviewReady) {
                    key(video.id) {
                        AndroidView(
                            factory = { ctx ->
                                val view = android.view.LayoutInflater.from(ctx)
                                    .inflate(R.layout.player_view_texture, null, false) as PlayerView
                                view.apply {
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    player = previewManager.getPlayer()
                                }
                            },
                            update = { view ->
                                view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                val target = previewManager.getPlayer()
                                if (view.player !== target) {
                                    view.player = target
                                }
                            },
                            onRelease = { view ->
                                val target = previewManager.getPlayer()
                                if (view.player === target) {
                                    view.player = null
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // ============ INFO BELOW VIDEO (not on top of it) ============
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = video.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isLargeTile) 11.sp else 9.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.category,
                        color = PrimaryOrange,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = PersianUtils.formatDuration(video.duration),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp)
                    )
                }
            }
        }
    }
}