package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise
import com.example.ui.theme.StarYellow
import kotlinx.coroutines.launch

val BOBO_MESSAGES = listOf(
    "سلام دوست کوچولو! 🌈",
    "امروز چی یاد بگیریم؟ ⭐",
    "آفرین! یک چیز جدید یاد گرفتی ⭐",
    "بریم یک ویدیوی جدید ببینیم؟ 🚀",
    "تو خیلی کنجکاو و باهوشی! 🐻",
    "یادگیری با کارتون و بازی خیلی خوش می‌گذره! 🎨"
)

@Composable
fun BoboGuideBadge(
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    onMascotClick: (() -> Unit)? = null
) {
    var messageIndex by remember { mutableIntStateOf(0) }
    var isBubbleVisible by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val bounceScale = remember { Animatable(1f) }

    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "bobo_float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobo_y"
    )

    Row(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .graphicsLayer { translationY = offsetY },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Speech Bubble
        AnimatedVisibility(
            visible = isBubbleVisible,
            enter = fadeIn() + slideInHorizontally { it / 2 },
            exit = fadeOut() + slideOutHorizontally { it / 2 }
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable {
                        messageIndex = (messageIndex + 1) % BOBO_MESSAGES.size
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customMessage ?: BOBO_MESSAGES[messageIndex],
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1D2D44)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { isBubbleVisible = false }
                    )
                }
            }
        }

        // Bobo Avatar with cheerful bounce
        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(bounceScale.value)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(PrimaryOrange, StarYellow))
                )
                .padding(2.5.dp)
                .clickable {
                    isBubbleVisible = true
                    messageIndex = (messageIndex + 1) % BOBO_MESSAGES.size
                    coroutineScope.launch {
                        bounceScale.animateTo(1.3f, spring(stiffness = Spring.StiffnessHigh))
                        bounceScale.animateTo(1.0f, spring(stiffness = Spring.StiffnessMedium))
                    }
                    onMascotClick?.invoke()
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bobo_happy),
                contentDescription = "شخصیت راهنما بوبو",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}
