package com.example.ui.screens.direct

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IG_TEXT = Color(0xFF000000)
private val IG_GRAY = Color(0xFF8E8E8E)
private val IG_BORDER = Color(0xFFDBDBDB)
private val IG_GRADIENT = listOf(
    Color(0xFFFFDD55),
    Color(0xFFFF5433),
    Color(0xFFC837AB),
    Color(0xFF5B51D8)
)

private data class DMItem(
    val id: String,
    val name: String,
    val emoji: String,
    val lastMessage: String,
    val time: String,
    val unread: Boolean = false
)

@Composable
fun DirectMessagesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val conversations = listOf(
        DMItem("d1", "مامانِ سارا", "🌸", "ویدیوی جدید دیدی؟ 😍", "۲ دقیقه", unread = true),
        DMItem("d2", "بابای علی", "🚀", "عالی بود دخترم", "۱ ساعت"),
        DMItem("d3", "خاله نازنین", "🌷", "چه خبر؟", "۳ ساعت", unread = true),
        DMItem("d4", "دایی رضا", "📚", "کتاب جدید خریدم برات", "دیروز"),
        DMItem("d5", "خانم معلم", "🔢", "آفرین به تو 👏", "دیروز"),
        DMItem("d6", "بچه‌گرام", "⭐", "خوش آمدی به بچه‌گرام!", "۲ روز پیش")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
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
                text = "bachegram_kid",
                color = IG_TEXT,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = "پیام جدید",
                tint = IG_TEXT,
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(IG_BORDER)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(conversations, key = { it.id }) { c ->
                DMRow(c)
            }
        }
    }
}

@Composable
private fun DMRow(c: DMItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Brush.sweepGradient(IG_GRADIENT)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = c.emoji, fontSize = 26.sp)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = c.name,
                fontSize = 14.sp,
                fontWeight = if (c.unread) FontWeight.Bold else FontWeight.SemiBold,
                color = IG_TEXT
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = c.lastMessage,
                fontSize = 12.sp,
                color = if (c.unread) IG_TEXT else IG_GRAY,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = c.time,
                fontSize = 11.sp,
                color = IG_GRAY
            )
            if (c.unread) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0095F6))
                )
            }
        }
    }
}