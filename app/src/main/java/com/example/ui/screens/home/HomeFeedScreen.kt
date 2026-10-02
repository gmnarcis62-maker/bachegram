@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.media3.common.util.UnstableApi::class
)
package com.example.ui.screens.home

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.data.model.VideoItem
import com.example.player.GridVideoPreviewManager
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.PersianUtils
import com.example.ui.screens.feed.VideoFeedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private val IG_BG = Color.White
private val IG_TEXT = Color(0xFF000000)
private val IG_TEXT_SOFT = Color(0xFF262626)
private val IG_GRAY = Color(0xFF8E8E8E)
private val IG_BORDER = Color(0xFFDBDBDB)
private val IG_LIKE = Color(0xFFED4956)

private val IG_GRADIENT = listOf(
    Color(0xFFFFDD55),
    Color(0xFFFF5433),
    Color(0xFFC837AB),
    Color(0xFF5B51D8)
)

@Composable
fun HomeFeedScreen(
    viewModel: VideoFeedViewModel,
    onVideoSelected: (VideoItem) -> Unit,
    onOpenComments: (VideoItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videos by viewModel.videos.collectAsState()
    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewManager = remember { GridVideoPreviewManager(context) }
    val activePreviewId by previewManager.activePreviewId.collectAsState()
    val isPreviewReady by previewManager.isPreviewReady.collectAsState()

    val commentsMap by viewModel.commentsByVideo.collectAsState()
    val commentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentsVideo by remember { mutableStateOf<VideoItem?>(null) }

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

    LaunchedEffect(videos, listState) {
        if (videos.isEmpty()) return@LaunchedEffect
        delay(500)
        triggerFeedPreview(listState, videos, previewManager)

        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { scrolling ->
                if (scrolling) previewManager.stopPreview()
                else {
                    delay(400)
                    triggerFeedPreview(listState, videos, previewManager)
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(IG_BG),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        item(key = "stories") {
            InstagramStoriesRow(videos = videos.take(12))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(IG_BORDER)
            )
        }

        itemsIndexed(
            items = videos,
            key = { _, v -> v.id }
        ) { _, video ->
            val isPreviewing = activePreviewId == video.id
            InstagramPostCard(
                video = video,
                isPreviewing = isPreviewing,
                isPreviewReady = isPreviewReady,
                previewManager = previewManager,
                onLikeClick = { viewModel.likeVideo(video) },
                onSaveClick = { viewModel.toggleSaved(video) },
                onCommentClick = {
                    commentsVideo = video
                    viewModel.ensureCommentsLoaded(video.id)
                    onOpenComments(video)
                },
                onShareClick = { },
                onHeaderClick = { onVideoSelected(video) },
                onMediaClick = { onVideoSelected(video) }
            )
        }
    }

    commentsVideo?.let { v ->
        CommentsBottomSheet(
            video = v,
            comments = commentsMap[v.id].orEmpty(),
            sheetState = commentSheetState,
            onDismiss = { commentsVideo = null },
            onSubmitComment = { viewModel.addComment(v.id, it) },
            onToggleLike = { comment -> viewModel.toggleCommentLike(v.id, comment.id) }
        )
    }
}

private fun triggerFeedPreview(
    listState: androidx.compose.foundation.lazy.LazyListState,
    videos: List<VideoItem>,
    previewManager: GridVideoPreviewManager
) {
    val layout = listState.layoutInfo
    if (layout.visibleItemsInfo.isEmpty() || videos.isEmpty()) return
    val center = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
    val item = layout.visibleItemsInfo
        .filter { it.index > 0 }
        .minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - center) } ?: return
    val videoIndex = item.index - 1
    if (videoIndex in videos.indices) {
        previewManager.playPreview(videos[videoIndex])
    }
}

@Composable
private fun InstagramStoriesRow(videos: List<VideoItem>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(IG_BG)
            .padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(66.dp)
            ) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBDBDB))
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0095F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "داستان تو",
                    fontSize = 11.sp,
                    color = IG_TEXT_SOFT,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        items(videos) { v ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(66.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(IG_GRADIENT)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emojiForCategory(v.category), fontSize = 26.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = v.category.replace(Regex("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\\p{Cn}]+\\s*"), ""),
                    fontSize = 11.sp,
                    color = IG_TEXT_SOFT,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun InstagramPostCard(
    video: VideoItem,
    isPreviewing: Boolean,
    isPreviewReady: Boolean,
    previewManager: GridVideoPreviewManager,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onHeaderClick: () -> Unit,
    onMediaClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showHeart by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IG_BG)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Brush.sweepGradient(IG_GRADIENT)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emojiForCategory(video.category), fontSize = 18.sp)
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onHeaderClick() }
            ) {
                Text(
                    text = video.sourceName.ifBlank { "بچه‌گرام" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = IG_TEXT
                )
                Text(
                    text = video.category,
                    fontSize = 11.sp,
                    color = IG_GRAY,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "منو",
                tint = IG_TEXT,
                modifier = Modifier.size(20.dp)
            )
        }

        // Media with single-tap = detail, double-tap = like
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onMediaClick() },
                        onDoubleTap = {
                            onLikeClick()
                            coroutineScope.launch {
                                showHeart = true
                                heartScale.snapTo(0.2f)
                                heartScale.animateTo(1.4f, spring(stiffness = Spring.StiffnessHigh))
                                heartScale.animateTo(1.0f)
                                delay(600)
                                showHeart = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
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
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = emojiForCategory(video.category), fontSize = 56.sp)
            }

            if (isPreviewing && isPreviewReady) {
                AndroidView(
                    factory = { ctx ->
                        val view = android.view.LayoutInflater.from(ctx)
                            .inflate(com.example.R.layout.player_view_texture, null, false) as PlayerView
                        view.apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                            player = previewManager.getPlayer()
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
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

            // Big heart on double-tap
            if (showHeart) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = IG_LIKE,
                    modifier = Modifier
                        .size(110.dp)
                        .scale(heartScale.value)
                        .align(Alignment.Center)
                )
            }
        }

        // Action row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (video.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "پسندیدن",
                    tint = if (video.isFavorite) IG_LIKE else IG_TEXT,
                    modifier = Modifier.size(26.dp)
                )
            }
            IconButton(onClick = onCommentClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Comment,
                    contentDescription = "نظر",
                    tint = IG_TEXT,
                    modifier = Modifier.size(25.dp)
                )
            }
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "ارسال",
                    tint = IG_TEXT,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(onClick = onSaveClick) {
                Icon(
                    imageVector = if (video.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "ذخیره",
                    tint = IG_TEXT,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        // Likes
        Text(
            text = "${PersianUtils.toPersianDigits(video.likesCount.toString())} پسند",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = IG_TEXT,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
        )

        // Caption
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
        ) {
            Text(
                text = video.sourceName.ifBlank { "بچه‌گرام" },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = IG_TEXT
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = video.title,
                fontSize = 13.sp,
                color = IG_TEXT_SOFT,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Comments link
        Text(
            text = "مشاهده همه نظرات",
            fontSize = 12.sp,
            color = IG_GRAY,
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 3.dp)
                .clickable { onCommentClick() }
        )

        // Timestamp
        Text(
            text = PersianUtils.formatDuration(video.duration) + " پیش",
            fontSize = 10.sp,
            color = IG_GRAY,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(IG_BORDER)
        )
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