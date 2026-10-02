package com.example.ui.screens.feed

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.VideoItem
import com.example.data.settings.DownloadMode
import com.example.download.DownloadStatus
import com.example.player.Media3PlayerManager
import com.example.ui.components.BoboGuideBadge
import com.example.ui.components.PersianUtils
import com.example.ui.components.RainbowStrip
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise
import com.example.ui.theme.StarYellow
import com.example.validator.ValidationResult
import com.example.validator.VideoLinkValidator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun VideoFeedScreen(
    viewModel: VideoFeedViewModel,
    onNavigateToSaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val videos by viewModel.videos.collectAsState()
    val downloadStates by viewModel.downloadStates.collectAsState()

    // Keep screen awake while watching video
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    if (videos.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = PrimaryOrange,
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "در حال آماده‌سازی ویدیوهای شاد بچه‌گرام...",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
                playerManager.playVideo(video)
            }
        }
    }

    playerManager.onVideoEnded = {
        coroutineScope.launch {
            if (pagerState.currentPage < videos.size - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            } else {
                pagerState.animateScrollToPage(0)
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            playerManager.updateProgress()
            delay(400)
        }
    }

    // Dialog states
    var showReportDialog by remember { mutableStateOf<VideoItem?>(null) }
    var showCommentSheet by remember { mutableStateOf<VideoItem?>(null) }
    var showCellularWarningDialog by remember { mutableStateOf<VideoItem?>(null) }
    var showDeleteDownloadDialog by remember { mutableStateOf<VideoItem?>(null) }
    var boboMessage by remember { mutableStateOf<String?>("امروز چی یاد بگیریم؟ ⭐") }

    // ============ NEW LAYOUT: video and info are stacked, not overlaid ============
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Rainbow bar (not on video, it's the top edge)
        RainbowStrip(height = 3.dp)

        // Greeting bar (NOT on video — sits above the pager)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سلام دوست کوچولو 🌈",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            BoboGuideBadge(customMessage = boboMessage)
        }

        // Vertical pager takes the rest of the screen
        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("video_vertical_pager")
        ) { pageIndex ->
            val video = videos.getOrNull(pageIndex) ?: return@VerticalPager
            val isCurrentPage = pagerState.currentPage == pageIndex

            val downloadStatus = downloadStates[video.id]
            val progressPercent = (downloadStatus as? DownloadStatus.Downloading)?.progressPercent

            VideoPageItem(
                video = video,
                isCurrentPage = isCurrentPage,
                playerManager = playerManager,
                downloadProgress = progressPercent,
                onLike = {
                    viewModel.likeVideo(video)
                    boboMessage = "آفرین قهرمان! پسندیدی ⭐"
                },
                onToggleFavorite = {
                    viewModel.toggleFavorite(video)
                    boboMessage = "به علاقه‌مندی‌ها اضافه شد ❤️"
                },
                onToggleSaved = {
                    viewModel.toggleSaved(video)
                    boboMessage = "ویدیو نشان شد 🔖"
                },
                onDownloadClick = {
                    if (video.isDownloaded) {
                        showDeleteDownloadDialog = video
                    } else if (viewModel.networkHelper.isCellular() &&
                        viewModel.networkSettingsManager.downloadMode.value == DownloadMode.PROMPT
                    ) {
                        showCellularWarningDialog = video
                    } else {
                        viewModel.startDownload(video)
                        boboMessage = "در حال دانلود برای تماشای آفلاین... 💾"
                    }
                },
                onShare = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, video.title)
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "ویدیوی زیبای «${video.title}» را در اپلیکیشن بچه‌گرام ببینید:\n${video.videoUrl}"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری ویدیو"))
                },
                onReport = { showReportDialog = video },
                onComment = { showCommentSheet = video },
                onNavigateToOffline = onNavigateToSaved,
                onNextVideo = {
                    coroutineScope.launch {
                        if (pageIndex < videos.size - 1) {
                            pagerState.animateScrollToPage(pageIndex + 1)
                        }
                    }
                }
            )
        }
    }

    // ============ DIALOGS (unchanged) ============

    showCellularWarningDialog?.let { targetVideo ->
        val approxMB = (viewModel.downloadManager.getEstimatedSizeBytes(targetVideo.duration) / (1024.0 * 1024.0))
        AlertDialog(
            onDismissRequest = { showCellularWarningDialog = null },
            title = {
                Text(
                    text = "دانلود با اینترنت همراه",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "این دانلود حدود ${PersianUtils.toPersianDigits(String.format("%.1f", approxMB))} مگابایت از اینترنت همراه شما استفاده می‌کند. آیا ادامه می‌دهید؟"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startDownload(targetVideo)
                        showCellularWarningDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) { Text("تأیید و دانلود") }
            },
            dismissButton = {
                TextButton(onClick = { showCellularWarningDialog = null }) { Text("انصراف") }
            }
        )
    }

    showDeleteDownloadDialog?.let { targetVideo ->
        AlertDialog(
            onDismissRequest = { showDeleteDownloadDialog = null },
            title = {
                Text(
                    text = "مدیریت فایل آفلاین",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text("این ویدیو روی حافظه دستگاه ذخیره است. آیا مایلید آن را پاک کنید تا حافظه گوشی آزاد شود؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDownloadedVideo(targetVideo.id)
                        showDeleteDownloadDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946))
                ) { Text("پاک کردن از حافظه") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDownloadDialog = null }) { Text("نگه داشتن") }
            }
        )
    }

    showReportDialog?.let { reportedVideo ->
        AlertDialog(
            onDismissRequest = { showReportDialog = null },
            title = {
                Text(
                    text = "گزارش محتوای ویدیویی",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text("آیا این ویدیو را نامناسب یا دارای مشکل می‌دانید؟ تیم بررسی بچه‌گرام فوراً آن را بازبینی می‌کند.")
            },
            confirmButton = {
                Button(
                    onClick = { showReportDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) { Text("ارسال گزارش") }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = null }) { Text("انصراف") }
            }
        )
    }

    showCommentSheet?.let { video ->
        var commentText by remember { mutableStateOf("") }
        var submitted by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCommentSheet = null },
            title = {
                Text(
                    text = "نظرات و بازخورد",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "نظر یا تجربه فرزندتان درباره «${video.title}»:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (submitted) {
                        Text(
                            text = "نظر شما با موفقیت ثبت شد! ⭐",
                            color = SecondaryTurquoise,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("اینجا بنویسید...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!submitted) {
                    Button(
                        onClick = { submitted = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTurquoise)
                    ) { Text("ارسال") }
                } else {
                    Button(onClick = { showCommentSheet = null }) { Text("بستن") }
                }
            },
            dismissButton = {
                if (!submitted) {
                    TextButton(onClick = { showCommentSheet = null }) { Text("انصراف") }
                }
            }
        )
    }
}

/**
 * One full page in the vertical feed.
 *
 * Layout:
 *   ┌───────────────────────────┐
 *   │                           │
 *   │         VIDEO             │  ← pure video, RESIZE_MODE_FIT, no persistent UI
 *   │      (letterboxed)        │     only ephemeral/contextual states shown here
 *   │                           │
 *   ├───────────────────────────┤
 *   │  badges / title / desc    │  ← info panel (dark surface)
 *   │  actions / progress bar   │
 *   └───────────────────────────┘
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPageItem(
    video: VideoItem,
    isCurrentPage: Boolean,
    playerManager: Media3PlayerManager,
    downloadProgress: Int?,
    onLike: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleSaved: () -> Unit,
    onDownloadClick: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onComment: () -> Unit,
    onNavigateToOffline: () -> Unit,
    onNextVideo: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isPlaying by playerManager.isPlaying.collectAsState()
    val isBuffering by playerManager.isBuffering.collectAsState()
    val hasError by playerManager.hasError.collectAsState()
    val isOfflineWithoutCache by playerManager.isOfflineWithoutCache.collectAsState()
    val isPlayingLocally by playerManager.isPlayingLocally.collectAsState()
    val currentPos by playerManager.currentPosition.collectAsState()
    val totalDuration by playerManager.duration.collectAsState()
    val isMuted by playerManager.isMuted.collectAsState()
    val hasRenderedFirstFrame by playerManager.hasRenderedFirstFrame.collectAsState()

    var showPlayPauseIcon by remember { mutableStateOf(false) }
    var showBigHeart by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    var isBufferingTimedOut by remember { mutableStateOf(false) }
    var showDiagnosticDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isBuffering, isCurrentPage, video.id, hasRenderedFirstFrame) {
        isBufferingTimedOut = false
        if (isBuffering && isCurrentPage && !hasRenderedFirstFrame) {
            delay(20000)
            if (isBuffering && isCurrentPage && !hasRenderedFirstFrame) {
                isBufferingTimedOut = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ============================================================
        //  VIDEO AREA — pure video, no persistent UI on top of it
        // ============================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            onLike()
                            coroutineScope.launch {
                                showBigHeart = true
                                heartScale.snapTo(0.2f)
                                heartScale.animateTo(1.4f, spring(stiffness = Spring.StiffnessHigh))
                                heartScale.animateTo(1.0f)
                                delay(400)
                                showBigHeart = false
                            }
                        },
                        onTap = {
                            playerManager.togglePlayPause()
                            coroutineScope.launch {
                                showPlayPauseIcon = true
                                delay(600)
                                showPlayPauseIcon = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isCurrentPage) {
                AndroidView(
                    factory = { ctx ->
                        val view = android.view.LayoutInflater.from(ctx)
                            .inflate(com.example.R.layout.player_view_texture, null, false) as PlayerView
                        view.apply {
                            useController = false
                            // ⭐ KEY FIX: FIT so the whole video is visible, no zoom
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            setShutterBackgroundColor(android.graphics.Color.BLACK)
                            player = playerManager.getPlayer()
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { view ->
                        // Re-assert FIT on every update (in case XML or lifecycle overrode it)
                        view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        view.player = if (isCurrentPage) playerManager.getPlayer() else null
                    },
                    onRelease = { view -> view.player = null },
                    onReset = { view -> view.player = null },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ---- Ephemeral feedback: heart on double tap ----
            AnimatedVisibility(
                visible = showBigHeart,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = AccentPink,
                    modifier = Modifier
                        .size(100.dp)
                        .scale(heartScale.value)
                )
            }

            // ---- Ephemeral feedback: play / pause on tap ----
            AnimatedVisibility(
                visible = showPlayPauseIcon,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f)),
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

            // ---- Contextual: buffering spinner ----
            if (isBuffering && isCurrentPage && !hasRenderedFirstFrame && !isBufferingTimedOut) {
                CircularProgressIndicator(
                    color = PrimaryOrange,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            // ---- Contextual: buffering timeout ----
            if (isBufferingTimedOut && isCurrentPage) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "پاسخ از سرور ویدیو بیش از ۲۰ ثانیه طول کشید",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { playerManager.playVideo(video) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("تلاش مجدد", fontSize = 11.sp) }
                        Button(
                            onClick = { showDiagnosticDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTurquoise),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("تست سرور", fontSize = 11.sp) }
                    }
                }
            }

            // ---- Contextual: offline without cache ----
            if (isOfflineWithoutCache && isCurrentPage) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "اینترنت قطع شده، اما می‌توانی ویدیوهای ذخیره‌شده را ببینی 🌈",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = onNavigateToOffline,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاهده ویدیوهای آفلاین")
                    }
                }
            } else if (hasError != null && isCurrentPage) {
                // ---- Contextual: real error ----
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = PrimaryOrange,
                        modifier = Modifier.size(42.dp)
                    )
                    Text(
                        text = hasError ?: "خطا در پخش ویدیو",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { playerManager.playVideo(video) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تلاش مجدد")
                        }
                        Button(
                            onClick = { showDiagnosticDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTurquoise),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("عیب‌یابی") }
                    }
                }
            }
        }

        // ============================================================
        //  INFO PANEL — everything persistent lives BELOW the video
        // ============================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111418))
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // ---- Badges row ----
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isPlayingLocally) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BadgeGreen.copy(alpha = 0.9f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "آفلاین",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryOrange.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = video.category,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SecondaryTurquoise.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = "${PersianUtils.toPersianDigits(video.ageMin)} تا ${PersianUtils.toPersianDigits(video.ageMax)} سال",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Title ----
            Text(
                text = video.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                ),
                maxLines = if (isDescriptionExpanded) 3 else 2
            )

            // ---- Description (tap to expand) ----
            if (video.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    ),
                    maxLines = if (isDescriptionExpanded) 5 else 2,
                    modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---- Horizontal action row ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeedActionButton(
                    icon = if (video.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    label = PersianUtils.toPersianDigits(video.likesCount.toString()),
                    tint = if (video.isFavorite) AccentPink else Color.White,
                    onClick = onToggleFavorite
                )
                FeedActionButton(
                    icon = Icons.AutoMirrored.Filled.Comment,
                    label = "نظر",
                    onClick = onComment
                )
                FeedActionButton(
                    icon = if (video.isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    label = if (video.isSaved) "نشان شد" else "نشان",
                    tint = if (video.isSaved) StarYellow else Color.White,
                    onClick = onToggleSaved
                )
                FeedActionButton(
                    icon = Icons.Filled.Download,
                    label = when {
                        video.isDownloaded -> "ذخیره‌شده"
                        downloadProgress != null -> "${downloadProgress}%"
                        else -> "دانلود"
                    },
                    tint = if (video.isDownloaded) BadgeGreen else Color.White,
                    progress = downloadProgress,
                    onClick = onDownloadClick
                )
                FeedActionButton(
                    icon = Icons.Filled.Share,
                    label = "ارسال",
                    onClick = onShare
                )
                FeedActionButton(
                    icon = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    label = if (isMuted) "بی‌صدا" else "صدا",
                    onClick = { playerManager.toggleMute() }
                )
                FeedActionButton(
                    icon = Icons.Filled.Flag,
                    label = "گزارش",
                    onClick = onReport
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---- Progress bar with timers ----
            val progress = if (totalDuration > 0)
                (currentPos.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = PersianUtils.formatMillis(currentPos),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = PrimaryOrange,
                    trackColor = Color.White.copy(alpha = 0.25f)
                )
                Text(
                    text = PersianUtils.formatMillis(totalDuration),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                )
            }

            // ---- Small diagnostic link (not on video) ----
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🧪 تست و عیب‌یابی پخش",
                color = SecondaryTurquoise,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { showDiagnosticDialog = true }
                    .padding(4.dp)
            )
        }
    }

    // ============ Diagnostic dialog (unchanged logic) ============
    if (showDiagnosticDialog) {
        var isRunningHttpTest by remember { mutableStateOf(false) }
        var testStatusResult by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showDiagnosticDialog = false },
            title = {
                Text(
                    text = "ابزار عیب‌یابی و تست پخش زنده 🧪",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "عنوان: ${video.title}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "آدرس استریم:\n${video.videoUrl}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (testStatusResult != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = testStatusResult!!,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (isRunningHttpTest) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("در حال اجرای تست شبکه...", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Button(
                        onClick = {
                            isRunningHttpTest = true
                            testStatusResult = null
                            coroutineScope.launch {
                                val isOk = try {
                                    val u = java.net.URL("https://www.google.com")
                                    val conn = (u.openConnection() as java.net.HttpURLConnection).apply {
                                        connectTimeout = 5000
                                        readTimeout = 5000
                                        requestMethod = "HEAD"
                                    }
                                    val code = conn.responseCode
                                    conn.disconnect()
                                    code in 200..399
                                } catch (e: Exception) { false }
                                isRunningHttpTest = false
                                testStatusResult = if (isOk) "✅ اتصال به اینترنت جهانی برقرار است"
                                else "❌ عدم دسترسی به اینترنت جهانی"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("📡 ۱. تست اتصال اینترنت", fontSize = 11.sp) }

                    Button(
                        onClick = {
                            isRunningHttpTest = true
                            testStatusResult = null
                            coroutineScope.launch {
                                val res = VideoLinkValidator.validateVideoUrl(video.videoUrl)
                                isRunningHttpTest = false
                                testStatusResult = when (res) {
                                    is ValidationResult.Success -> "✅ پاسخ سرور: Content-Type=${res.contentType}"
                                    is ValidationResult.Failed -> "❌ ${res.reason}"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTurquoise),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🔍 ۲. تست زنده سرور این ویدیو", fontSize = 11.sp) }

                    Button(
                        onClick = {
                            showDiagnosticDialog = false
                            playerManager.playDirectUrl(
                                "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
                                "MP4 Big Buck Bunny"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🎬 ۳. تست پخش MP4 عمومی", fontSize = 11.sp) }

                    Button(
                        onClick = {
                            showDiagnosticDialog = false
                            playerManager.playDirectUrl(
                                "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                                "HLS Mux"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StarYellow.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("⚡ ۴. تست پخش HLS عمومی", color = Color.Black, fontSize = 11.sp) }

                    Button(
                        onClick = {
                            showDiagnosticDialog = false
                            playerManager.retryCurrentVideo(video)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("🔄 ۵. بارگذاری مجدد همین ویدیو", fontSize = 11.sp) }

                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                    Button(
                        onClick = {
                            val report = playerManager.buildTechnicalReport() + "\nآخرین تست: ${testStatusResult ?: "انجام نشده"}"
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(report))
                            testStatusResult = "📋 گزارش فنی در کلیپ‌بورد کپی شد!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "📋 ۶. کپی گزارش فنی",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDiagnosticDialog = false }) { Text("بستن") }
            }
        )
    }
}

/**
 * Small action button used in the horizontal bottom row.
 * Optionally shows a circular download-progress ring over the icon.
 */
@Composable
private fun FeedActionButton(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    progress: Int? = null,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            if (progress != null && progress in 1..99) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    color = PrimaryOrange,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}