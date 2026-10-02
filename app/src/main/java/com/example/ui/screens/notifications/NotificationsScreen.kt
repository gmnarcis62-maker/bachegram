package com.example.ui.screens.notifications

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val IG_TEXT = Color(0xFF000000)
private val IG_GRAY = Color(0xFF8E8E8E)
private val IG_BORDER = Color(0xFFDBDBDB)
private val IG_LIKE = Color(0xFFED4956)
private val IG_GRADIENT = listOf(
    Color(0xFFFFDD55),
    Color(0xFFFF5433),
    Color(0xFFC837AB),
    Color(0xFF5B51D8)
)

private data class NotifItem(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val ago: String,
    val type: String
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NotifItem("n1", "🌸", "مامانِ سارا", "پست شما را پسندید", "۲ دقیقه", "like"),
        NotifItem("n2", "🚀", "بابای علی", "برای شما کامنت گذاشت: «عالی بود!»", "۱۰ دقیقه", "comment"),
        NotifItem("n3", "🌷", "خاله نازنین", "شما را دنبال کرد", "۱ ساعت", "follow"),
        NotifItem("n4", "🎨", "آموزشگاه رنگین‌کمان", "ویدیوی جدیدی منتشر کرد", "۳ ساعت", "new"),
        NotifItem("n5", "⭐", "بچه‌گرام", "ویدیو شما ۱۰۰ بار دیده شد 🎉", "دیروز", "like"),
        NotifItem("n6", "🔢", "خانم معلم", "به ۵ پست شما پسند داد", "دیروز", "like"),
        NotifItem("n7", "📚", "دایی رضا", "شما را دنبال کرد", "۲ روز پیش", "follow")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
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
                text = "اعلان‌ها",
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(items, key = { it.id }) { item ->
                NotifRow(item)
            }
        }
    }
}

@Composable
private fun NotifRow(item: NotifItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with gradient ring
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Brush.sweepGradient(IG_GRADIENT)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 22.sp)
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = IG_TEXT
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                fontSize = 12.sp,
                color = IG_TEXT
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.ago,
                fontSize = 11.sp,
                color = IG_GRAY
            )
        }

        // Right side visual hint
        when (item.type) {
            "follow" -> Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0095F6))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "دنبال کردن",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            "like" -> Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF2F2F2))
            )
            "comment" -> Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF2F2F2))
            )
            else -> Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF2F2F2))
            )
        }
    }
}