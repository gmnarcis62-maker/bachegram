package com.example.ui.screens.detail

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.data.model.CommentItem
import com.example.data.model.VideoItem
import com.example.player.Media3PlayerManager
import com.example.ui.components.PersianUtils
import com.example.ui.screens.feed.VideoFeedViewModel

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

@OptIn(UnstableApi::class)
@Composable
fun PostDetailScreen(
    video: VideoItem,
    viewModel: VideoFeedViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val playerManager = remember { Media3PlayerManager(context) }

    val commentsMap by viewModel.commentsByVideo.collectAsState()
    val comments = commentsMap[video.id].orEmpty()

    val isPlaying by playerManager.isPlaying.collectAsState()

    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(video.id) {
        viewModel.ensureCommentsLoaded(video.id)
        playerManager.playVideo(video)
    }

    DisposableEffect(Unit) {
        onDispose { playerManager.release() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        // Top bar
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
                text = "پست",
                color = IG_TEXT,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(IG_BORDER)
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.sourceName.ifBlank { "بچه‌گرام" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IG_TEXT
                    )
                    Text(
                        text = video.category,
                        fontSize = 11.sp,
                        color = IG_GRAY
                    )
                }
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "منو",
                    tint = IG_TEXT,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Media
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
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
                        v.player = playerManager.getPlayer()
                    },
                    onRelease = { v -> v.player = null },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { viewModel.likeVideo(video) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (video.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "پسندیدن",
                        tint = if (video.isFavorite) IG_LIKE else IG_TEXT,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "نظر",
                        tint = IG_TEXT,
                        modifier = Modifier.size(25.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "ارسال",
                        tint = IG_TEXT,
                        modifier = Modifier.size(25.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { viewModel.toggleSaved(video) },
                    contentAlignment = Alignment.Center
                ) {
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
                    color = IG_TEXT_SOFT
                )
            }

            Spacer(Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(IG_BORDER)
            )

            // Comments header
            Text(
                text = "نظرات",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = IG_TEXT,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )

            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هنوز نظری نیست. اولین نفر باش!",
                        color = IG_GRAY,
                        fontSize = 13.sp
                    )
                }
            } else {
                comments.forEach { c ->
                    PostDetailCommentRow(
                        comment = c,
                        onToggleLike = { viewModel.toggleCommentLike(video.id, c.id) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }

        // Bottom input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(IG_BORDER)
                    .align(Alignment.TopCenter)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(IG_GRADIENT)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌟", fontSize = 16.sp)
                    }
                }
                Spacer(Modifier.width(10.dp))

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(text = "افزودن نظر...", color = IG_GRAY, fontSize = 13.sp)
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = IG_TEXT
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "ارسال",
                    color = if (inputText.isBlank()) IG_GRAY else Color(0xFF0095F6),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(enabled = inputText.isNotBlank()) {
                            viewModel.addComment(video.id, inputText)
                            inputText = ""
                        }
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun PostDetailCommentRow(
    comment: CommentItem,
    onToggleLike: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Brush.sweepGradient(IG_GRADIENT)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = comment.authorEmoji, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.authorName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = IG_TEXT
                )
                if (comment.isFromCreator) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00376B))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "سازنده",
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = comment.text,
                fontSize = 13.sp,
                color = IG_TEXT_SOFT,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = comment.timeAgoLabel, fontSize = 11.sp, color = IG_GRAY)
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "پاسخ",
                    fontSize = 11.sp,
                    color = IG_GRAY,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onToggleLike() }
        ) {
            Icon(
                imageVector = if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (comment.isLiked) IG_LIKE else IG_GRAY,
                modifier = Modifier.size(16.dp)
            )
            if (comment.likesCount > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = PersianUtils.toPersianDigits(comment.likesCount.toString()),
                    fontSize = 10.sp,
                    color = IG_GRAY
                )
            }
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