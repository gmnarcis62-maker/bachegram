@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.media3.common.util.UnstableApi::class
)
package com.example.ui.screens.feed

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.CommentItem
import com.example.data.model.VideoItem
import com.example.player.Media3PlayerManager
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.PersianUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val IG_LIKE = Color(0xFFED4956)
private val IG_GRADIENT = listOf(
    Color(0xFFFFDD55),
    Color(0xFFFF5433),
    Color(0xFFC837AB),
    Color(0xFF5B51D8)
)

@Composable
fun VideoFeedScreen(
    viewModel: VideoFeedViewModel,
    onNavigateToSaved: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Reels uses the shuffled list — a new random order is created on every app launch.
    val videos by viewModel.shuffledVideos.collectAsState()
    val commentsMap by viewModel.commentsByVideo.collectAsState()

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    if (videos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    val currentActiveIndex by viewModel.currentIndex.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = currentActiveIndex.coerceIn(0, (videos.size - 1).coerceAtLeast(0)),
        pageCount = { videos.size }
    )

    LaunchedEffect(currentActiveIndex) {
        if (currentActiveIndex in videos.indices && pagerState.currentPage != currentActiveIndex) {
            pagerState.scrollToPage(currentActiveIndex)
        }
    }

    val playerManager = remember { Media3PlayerManager(context) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE,
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> playerManager.pause()
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> playerManager.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerManager.release()
        }
    }

    var lastPlayedVideoId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pagerState.currentPage, videos.isNotEmpty()) {
        if (videos.isNotEmpty() && pagerState.currentPage in videos.indices) {
            val video = videos[pagerState.currentPage]
            if (video.id != lastPlayedVideoId) {
                lastPlayedVideoId = video.id
                viewModel.onPageChanged(pagerState.currentPage)
                viewModel.ensureCommentsLoaded(video.id)
                playerManager.playVideo(video)
            }
        }
    }

    // When a video ends:
    //   - if not at the end, go to next video
    //   - if at the end, reshuffle the list and start over from index 0
    playerManager.onVideoEnded = {
        coroutineScope.launch {
            val total = viewModel.shuffledVideos.value.size
            if (total <= 0) return@launch
            if (pagerState.currentPage < total - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            } else {
                viewModel.reshuffleAndRestart()
                pagerState.scrollToPage(0)
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            playerManager.updateProgress()
            delay(400)
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().testTag("reels_pager")
        ) { pageIndex ->
            val video = videos.getOrNull(pageIndex) ?: return@VerticalPager
            val isCurrentPage = pagerState.currentPage == pageIndex
            val comments = commentsMap[video.id].orEmpty()

            ReelsPageItem(
                video = video,
                isCurrentPage = isCurrentPage,
                playerManager = playerManager,
                comments = comments,
                onLike = { viewModel.likeVideo(video) },
                onToggleFavorite = { viewModel.toggleFavorite(video) },
                onToggleSaved = { viewModel.toggleSaved(video) },
                onSubmitComment = { text -> viewModel.addComment(video.id, text) },
                onToggleCommentLike = { commentId -> viewModel.toggleCommentLike(video.id, commentId) },
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${video.title}\n${video.videoUrl}")
                    }
                    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری"))
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "بازگشت",
                tint = Color.White,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onBackClick() }
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = "ریلز",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ReelsPageItem(
    video: VideoItem,
    isCurrentPage: Boolean,
    playerManager: Media3PlayerManager,
    comments: List<CommentItem>,
    onLike: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleSaved: () -> Unit,
    onSubmitComment: (String) -> Unit,
    onToggleCommentLike: (String) -> Unit,
    onShare: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val isBuffering by playerManager.isBuffering.collectAsState()

    var showPlayIcon by remember { mutableStateOf(false) }
    var showBigHeart by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showComments by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isCurrentPage) {
            AndroidView(
                factory = { ctx ->
                    val view = android.view.LayoutInflater.from(ctx)
                        .inflate(com.example.R.layout.player_view_texture, null, false) as PlayerView
                    view.apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        setShutterBackgroundColor(android.graphics.Color.BLACK)
                        player = playerManager.getPlayer()
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { v ->
                    v.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    v.player = if (isCurrentPage) playerManager.getPlayer() else null
                },
                onRelease = { v -> v.player = null },
                onReset = { v -> v.player = null },
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            playerManager.togglePlayPause()
                            coroutineScope.launch {
                                showPlayIcon = true
                                delay(600)
                                showPlayIcon = false
                            }
                        },
                        onDoubleTap = {
                            onLike()
                            coroutineScope.launch {
                                showBigHeart = true
                                heartScale.snapTo(0.2f)
                                heartScale.animateTo(1.4f, spring(stiffness = Spring.StiffnessHigh))
                                heartScale.animateTo(1.0f)
                                delay(600)
                                showBigHeart = false
                            }
                        }
                    )
                }
        )

        if (showBigHeart) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = IG_LIKE,
                modifier = Modifier
                    .size(120.dp)
                    .scale(heartScale.value)
                    .align(Alignment.Center)
            )
        }

        if (showPlayIcon) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        if (isBuffering && isCurrentPage) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(46.dp).align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ReelsActionButton(
                icon = if (video.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = PersianUtils.toPersianDigits(video.likesCount.toString()),
                tint = if (video.isFavorite) IG_LIKE else Color.White,
                onClick = onToggleFavorite
            )
            ReelsActionButton(
                icon = Icons.AutoMirrored.Filled.Comment,
                label = "نظر",
                onClick = { showComments = true }
            )
            ReelsActionButton(
                icon = Icons.AutoMirrored.Filled.Send,
                label = "ارسال",
                onClick = onShare
            )
            ReelsActionButton(
                icon = if (video.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                label = if (video.isSaved) "ذخیره شد" else "ذخیره",
                tint = if (video.isSaved) Color(0xFFFFCC00) else Color.White,
                onClick = onToggleSaved
            )
            ReelsActionButton(
                icon = Icons.Filled.MoreHoriz,
                label = "",
                onClick = { }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.76f)
                .padding(start = 14.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(Modifier.width(8.dp))
                Text(
                    text = video.sourceName.ifBlank { "بچه‌گرام" },
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.White, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .clickable { }
                ) {
                    Text("دنبال کردن", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = video.title,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "صدای اصلی — بچه‌گرام",
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (showComments) {
            CommentsBottomSheet(
                video = video,
                comments = comments,
                sheetState = sheetState,
                onDismiss = { showComments = false },
                onSubmitComment = onSubmitComment,
                onToggleLike = { comment -> onToggleCommentLike(comment.id) }
            )
        }
    }
}

@Composable
private fun ReelsActionButton(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(30.dp)
        )
        if (label.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
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