package com.example.ui.screens.categories

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.components.RainbowStrip
import com.example.ui.components.bouncyClick
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoItem
import com.example.ui.components.BachegramTopBar
import com.example.ui.components.PersianUtils
import com.example.ui.screens.feed.VideoFeedViewModel
import com.example.ui.theme.BadgeBlue
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.BadgePink
import com.example.ui.theme.BadgePurple
import com.example.ui.theme.BadgeYellow
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise

data class CategoryMeta(
    val name: String,
    val iconEmoji: String,
    val gradientColors: List<Color>,
    val description: String
)

val CATEGORIES_LIST = listOf(
    CategoryMeta("🎨 نقاشی و کاردستی", "🎨", listOf(Color(0xFFFF5D8F), Color(0xFFFF85A1)), "رنگ‌آمیزی، کاردستی، اوریگامی و نقاشی خلاق"),
    CategoryMeta("🔢 ریاضی", "🔢", listOf(Color(0xFF3A86FF), Color(0xFF60A5FA)), "شمارش، اعداد، بازی‌های هوش ریاضی و تمرینات"),
    CategoryMeta("🔤 فارسی و الفبا", "🔤", listOf(Color(0xFFFFB703), Color(0xFFFB8500)), "آموزش حروف، کلمات، شعر الفبا و خواندن"),
    CategoryMeta("🇬🇧 زبان انگلیسی", "🇬🇧", listOf(Color(0xFF7209B7), Color(0xFF9D4EDD)), "الفبا، آهنگ‌های شاد و مکالمه کودکانه انگلیسی"),
    CategoryMeta("🔬 علوم", "🔬", listOf(Color(0xFF06D6A0), Color(0xFF00BBF9)), "آزمایش‌های هیجان‌انگیز، شناخت بدن و دنیای اطراف"),
    CategoryMeta("🚀 نجوم", "🚀", listOf(Color(0xFF1D3557), Color(0xFF457B9D)), "سیارات منظومه شمسی، ستاره‌ها و فضا برای کودکان"),
    CategoryMeta("📚 قصه و داستان", "📚", listOf(Color(0xFFF77F00), Color(0xFFFCBF49)), "قصه‌های آموزنده، داستان‌های شب و مهدکودک"),
    CategoryMeta("🎵 شعر و موسیقی", "🎵", listOf(Color(0xFFE63946), Color(0xFFFF758F)), "سرودهای کودکانه، آموزش بلز، پیانو و نت‌ها"),
    CategoryMeta("🧠 هوش و خلاقیت", "🧠", listOf(Color(0xFF8338EC), Color(0xFFB5179E)), "معماهای تصویری، پازل و تقویت هوش کودک"),
    CategoryMeta("🤖 رباتیک و برنامه‌نویسی", "🤖", listOf(Color(0xFF2EC4B6), Color(0xFF20A4F3)), "تفکر الگوریتمی و آشنایی اولیه با دنیای کامپیوتر"),
    CategoryMeta("🏃 ورزش و حرکت", "🏃", listOf(Color(0xFFFF70A6), Color(0xFFFF9770)), "ورزش صبحگاهی، بازی‌های پرانرژی و ژیمناستیک"),
    CategoryMeta("🧼 مهارت‌های زندگی", "🧼", listOf(Color(0xFF48CAE4), Color(0xFF0096C7)), "نظم و انضباط، بهداشت شخصی و آداب معاشرت"),
    CategoryMeta("🛡️ آموزش ایمنی", "🛡️", listOf(Color(0xFFE76F51), Color(0xFFF4A261)), "ایمنی در خانه، خیابان، محافظت از خود و اینترنت"),
    CategoryMeta("🌱 طبیعت و محیط زیست", "🌱", listOf(Color(0xFF588157), Color(0xFF3A5A40)), "حیوانات، گیاهان، آب و هوا و نگهداری از زمین"),
    CategoryMeta("🧩 بازی‌های آموزشی", "🧩", listOf(Color(0xFFFF006E), Color(0xFFFF5C8A)), "بازی‌های مهدکودک، گروهی و فکری برای بچه‌ها"),
    CategoryMeta("🎭 سرگرمی", "🎭", listOf(Color(0xFF9B5DE5), Color(0xFFF15BB5)), "تئاترهای شاد، انیمیشن‌های کوتاه و نمایش عروسکی"),
    CategoryMeta("📖 آموزش مدرسه", "📖", listOf(Color(0xFF0077B6), Color(0xFF00B4D8)), "دروس پایه اول تا ششم ابتدایی و پیش‌دبستانی")
)

@Composable
fun CategoriesScreen(
    viewModel: VideoFeedViewModel,
    onVideoSelected: (VideoItem) -> Unit,
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allVideos by viewModel.videos.collectAsState()
    var selectedCategory by remember { mutableStateOf<CategoryMeta?>(null) }

    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (selectedCategory == null) {
            // Main Category Grid View
            BachegramTopBar(
                title = "دسته‌بندی‌ها",
                onSearchClick = onSearchClick
            )

            Text(
                text = "چه چیزی دوست داری یاد بگیری؟ 🌟",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("categories_grid")
            ) {
                items(CATEGORIES_LIST) { category ->
                    val videoCount = allVideos.count { it.category == category.name }

                    CategoryCard(
                        category = category,
                        count = videoCount,
                        onClick = { selectedCategory = category }
                    )
                }
            }
        } else {
            // Category Detail List View
            val category = selectedCategory!!
            val categoryVideos = allVideos.filter { it.category == category.name }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedCategory = null },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${PersianUtils.toPersianDigits(categoryVideos.size)} ویدیو",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (categoryVideos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هنوز ویدیویی در این دسته وجود ندارد.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categoryVideos) { video ->
                        CategoryVideoItemCard(
                            video = video,
                            onClick = { onVideoSelected(video) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryMeta,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .bouncyClick { onClick() }
            .testTag("category_card_${category.name}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            category.gradientColors[0].copy(alpha = 0.90f),
                            category.gradientColors[1].copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.28f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.iconEmoji,
                            fontSize = 24.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "${PersianUtils.toPersianDigits(count)} ویدیو",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    maxLines = 1
                )

                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CategoryVideoItemCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(listOf(PrimaryOrange, SecondaryTurquoise))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "پخش",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "رده سنی: ${PersianUtils.toPersianDigits(video.ageMin)} تا ${PersianUtils.toPersianDigits(video.ageMax)} سال",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = PersianUtils.formatDuration(video.duration),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
