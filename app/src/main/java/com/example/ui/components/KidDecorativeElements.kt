package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise
import com.example.ui.theme.StarYellow

/**
 * Animated twinkling star particles background for playful kid screens
 */
@Composable
fun TwinklingStarsOverlay(
    modifier: Modifier = Modifier,
    starsCount: Int = 12
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars_twinkle")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_alpha"
    )
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_float"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Deterministic star positions
        val starCoords = listOf(
            Offset(width * 0.12f, height * 0.08f + floatAnim),
            Offset(width * 0.85f, height * 0.06f - floatAnim),
            Offset(width * 0.45f, height * 0.12f + floatAnim * 0.7f),
            Offset(width * 0.90f, height * 0.25f - floatAnim),
            Offset(width * 0.08f, height * 0.35f + floatAnim),
            Offset(width * 0.30f, height * 0.45f - floatAnim),
            Offset(width * 0.78f, height * 0.60f + floatAnim),
            Offset(width * 0.15f, height * 0.75f - floatAnim),
            Offset(width * 0.88f, height * 0.82f + floatAnim),
            Offset(width * 0.40f, height * 0.90f - floatAnim)
        )

        starCoords.take(starsCount).forEachIndexed { index, pos ->
            val starSize = if (index % 2 == 0) 5.dp.toPx() else 3.5.dp.toPx()
            val starColor = if (index % 3 == 0) StarYellow else Color.White
            val currentAlpha = (alphaAnim + (index * 0.1f)) % 0.8f + 0.2f

            drawCircle(
                color = starColor.copy(alpha = currentAlpha),
                radius = starSize,
                center = pos
            )
        }
    }
}

/**
 * Soft decorative cloud shape header
 */
@Composable
fun KidCloudHeader(
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    cloudColor: Color = Color.White.copy(alpha = 0.95f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(w, h * 0.6f)
            cubicTo(
                w * 0.85f, h * 1.1f,
                w * 0.65f, h * 0.4f,
                w * 0.5f, h * 0.8f
            )
            cubicTo(
                w * 0.35f, h * 1.1f,
                w * 0.15f, h * 0.5f,
                0f, h * 0.7f
            )
            close()
        }
        drawPath(path = path, color = cloudColor)
    }
}

/**
 * Rainbow accent strip for child appeal
 */
@Composable
fun RainbowStrip(
    modifier: Modifier = Modifier,
    height: Dp = 5.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFF5D8F), // Pink
                        Color(0xFFFFB703), // Yellow
                        Color(0xFF57CC99), // Green
                        Color(0xFF00B4D8), // Turquoise
                        Color(0xFF8338EC)  // Purple
                    )
                )
            )
    )
}

/**
 * Bouncy click scale modifier for tactile child interaction
 */
fun Modifier.bouncyClick(
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.93f else 1.0f

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
