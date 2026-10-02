package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
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
import com.example.data.model.VideoItem
import com.example.player.GridVideoPreviewManager
import com.example.ui.screens.feed.VideoFeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

private val IG_GRAY = Color(0xFF8E8E8E)
private val IG_TEXT = Color(0xFF000000)

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

    val gridState = rememberLazyGridState()
    val previewManager = remember { GridVideoPreviewManager(context) }
    val activePreviewId by previewManager.activePreviewId.collectAsState()
    val isPreviewReady by previewManager.isPreviewReady.collectAsState()

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

    val normalizedQuery = remember(searchQuery) { normalizePersian(searchQuery) }
    val filteredVideos = remember(allVideos, normalizedQuery) {
        allVideos.filter { video ->
            if (normalizedQuery.isBlank()) true
            else {
                val n = normalizePersian(
                    video.title + " " + video.description + " " + video.category
                )
                normalizedQuery.split(" ").filter { it.isNotBlank() }.all { n.contains(it) }
            }
        }
    }

    LaunchedEffect(filteredVideos) {
        if (filteredVideos.isEmpty()) return@LaunchedEffect
        delay(500)
        triggerExplorePreview(gridState, filteredVideos, previewManager)

        snapshotFlow { gridState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { isScrolling ->
                if (isScrolling) previewManager.stopPreview()
                else {
                    delay(400)
                    triggerExplorePreview(gridState, filteredVideos, previewManager)
                }
            }
    }

    Column(
        modifier = modifier.fillMaxSize().background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(text = "جستجو", color = IG_GRAY, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "جستجو",
                        tint = IG_GRAY,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "پاک",
                                tint = IG_GRAY
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFEFEFEF),
                    unfocusedContainerColor = Color(0xFFEFEFEF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = IG_TEXT
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_field")
            )
        }

        if (filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "چیزی پیدا نشد 🔍",
                    color = IG_GRAY,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_grid")
            ) {
                items(filteredVideos, key = { it.id }) { video ->
                    ExploreTile(
                        video = video,
                        isPreviewing = activePreviewId == video.id,
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

private fun triggerExplorePreview(
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    videos: List<VideoItem>,
    previewManager: GridVideoPreviewManager
) {
    val layout = gridState.layoutInfo
    if (layout.visibleItemsInfo.isEmpty() || videos.isEmpty()) return
    val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
    val item = layout.visibleItemsInfo.minByOrNull {
        kotlin.math.abs((it.offset.y + it.size.height / 2) - center)
    } ?: return
    if (item.index in videos.indices) {
        previewManager.playPreview(videos[item.index])
    }
}

@Composable
private fun ExploreTile(
    video: VideoItem,
    isPreviewing: Boolean,
    isPreviewReady: Boolean,
    previewManager: GridVideoPreviewManager,
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
            Text(text = emojiForCategory(video.category), fontSize = 40.sp)
        }

        if (isPreviewing && isPreviewReady) {
            key(video.id) {
                AndroidView(
                    factory = { ctx ->
                        val view = android.view.LayoutInflater.from(ctx)
                            .inflate(com.example.R.layout.player_view_texture, null, false) as PlayerView
                        view.apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                            player = previewManager.getPlayer()
                        }
                    },
                    update = { v ->
                        v.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        v.player = previewManager.getPlayer()
                    },
                    onRelease = { v -> v.player = null },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun normalizePersian(text: String): String = text
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