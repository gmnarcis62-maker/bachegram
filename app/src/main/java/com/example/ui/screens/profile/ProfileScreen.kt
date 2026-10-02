package com.example.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.PersianUtils
import com.example.ui.screens.feed.VideoFeedViewModel
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise
import com.example.ui.theme.StarYellow

@Composable
fun ProfileScreen(
    viewModel: VideoFeedViewModel,
    onGoToAboutUs: () -> Unit = {},
    onGoToParentZone: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allVideos by viewModel.videos.collectAsState()
    val totalWatched = allVideos.count { it.watchCount > 0 }
    val totalFavorites = allVideos.count { it.isFavorite }

    var kidName by remember { mutableStateOf("قهرمان کوچولو") }
    var selectedAgeRange by remember { mutableStateOf("۶ تا ۸ سال") }
    var showParentalGate by remember { mutableStateOf(false) }
    var parentalGateUnlocked by remember { mutableStateOf(false) }
    var maxDailyMinutes by remember { mutableStateOf("۳۰") }
    var safeSearchActive by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "پروفایل من 🐻",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Mascot Avatar Card
        Box(
            modifier = Modifier
                .size(105.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(PrimaryOrange, SecondaryTurquoise))
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bobo_profile),
                contentDescription = "آواتار بوبو",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = kidName,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "رده سنی: $selectedAgeRange",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Stats Row (Stars, Watched, Favorites)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "ستاره‌ها",
                value = PersianUtils.toPersianDigits(totalWatched * 5 + totalFavorites * 10 + 25),
                icon = Icons.Default.Star,
                iconTint = StarYellow,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "ویدیو دیده شده",
                value = PersianUtils.toPersianDigits(totalWatched),
                icon = Icons.Default.ChildCare,
                iconTint = SecondaryTurquoise,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "پسندیده‌ها",
                value = PersianUtils.toPersianDigits(totalFavorites),
                icon = Icons.Default.Check,
                iconTint = PrimaryOrange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Parental Gate / Safety Controls Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SecondaryTurquoise.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SecondaryTurquoise,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "کنترل والدین (فضای امن)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Button(
                        onClick = { showParentalGate = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (parentalGateUnlocked) BadgeGreen else PrimaryOrange
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (parentalGateUnlocked) "باز شده" else "قفل والدین")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "فیلتر ایمن کودک",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "جلوگیری خودکار از محتوای نامناسب و لینک‌های خارجی",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = safeSearchActive,
                        onCheckedChange = { if (parentalGateUnlocked) safeSearchActive = it else showParentalGate = true },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryOrange)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "محدودیت زمان استفاده در روز",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$maxDailyMinutes دقیقه در روز",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Open Parent Zone Dedicated Screen
        Button(
            onClick = {
                if (parentalGateUnlocked) {
                    onGoToParentZone()
                } else {
                    showParentalGate = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTurquoise),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_parent_zone_button")
        ) {
            Icon(imageVector = Icons.Default.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ورود به محیط اختصاصی والدین 🛡️")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // About Us Button
        Button(
            onClick = onGoToAboutUs,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_about_us_button")
        ) {
            Icon(imageVector = Icons.Default.Phone, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("درباره ما و تیم ردلاین سافت البرز 📞")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Bachegram
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "درباره اپلیکیشن بچه‌گرام",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "بچه‌گرام یک شبکه اجتماعی ویدیویی سالم، پرانرژی و سرشار از آموزش برای کودکان ایرانی است. تمامی ویدیوها از منابع معتبر آموزشی بررسی و بدون تبلیغات ارائه می‌شوند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }

    // Parental Gate Math Challenge Dialog (Safety Gate for parents)
    if (showParentalGate) {
        var answerInput by remember { mutableStateOf("") }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showParentalGate = false },
            title = {
                Text(
                    text = "ورود والدین",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text("برای ورود به تنظیمات والدین، پاسخ این مسئله ریاضی را وارد کنید:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "۷ × ۸ = ؟",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryOrange
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = { answerInput = it },
                        placeholder = { Text("پاسخ عددی (۵۶)") },
                        isError = isError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isError) {
                        Text(
                            text = "پاسخ صحیح نیست. دوباره تلاش کنید.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleaned = answerInput.replace("۵", "5").replace("۶", "6").trim()
                        if (cleaned == "56") {
                            parentalGateUnlocked = true
                            showParentalGate = false
                            onGoToParentZone()
                        } else {
                            isError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("تایید")
                }
            },
            dismissButton = {
                TextButton(onClick = { showParentalGate = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
