package com.example.ui.screens.parent

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.settings.DownloadMode
import com.example.data.settings.PlaybackMode
import com.example.player.BachegramCache
import com.example.ui.components.PersianUtils
import com.example.ui.components.RainbowStrip
import com.example.ui.screens.feed.VideoFeedViewModel
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BadgeGreen
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTurquoise
import com.example.ui.theme.StarYellow

@Composable
fun ParentZoneScreen(
    viewModel: VideoFeedViewModel,
    onBack: () -> Unit,
    onGoToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allVideos by viewModel.videos.collectAsState()
    val allVideosForAdmin by viewModel.allVideosForAdmin.collectAsState()
    val downloadedVideos by viewModel.downloadedVideos.collectAsState()

    val totalWatched = allVideos.count { it.watchCount > 0 }
    val totalFavorites = allVideos.count { it.isFavorite }

    var dailyMinutesLimit by remember { mutableFloatStateOf(45f) }
    var strictAgeEnforcement by remember { mutableStateOf(true) }
    var autoPauseAtNight by remember { mutableStateOf(true) }

    val playbackMode by viewModel.networkSettingsManager.playbackMode.collectAsState()
    val downloadMode by viewModel.networkSettingsManager.downloadMode.collectAsState()
    val autoCacheEnabled by viewModel.networkSettingsManager.autoCacheEnabled.collectAsState()

    var cacheSizeMB by remember { mutableDoubleStateOf(BachegramCache.getCacheSizeMB(context)) }
    var cacheClearedMessage by remember { mutableStateOf(false) }

    // Admin link health state
    val isScanningHealth by viewModel.isScanningHealth.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsState()

    var showAddVideoDialog by remember { mutableStateOf(false) }

    val verifiedCount = allVideosForAdmin.count { it.videoStatus == "VERIFIED" }
    val failedCount = allVideosForAdmin.count { it.videoStatus == "FAILED" }

    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RainbowStrip()

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("parent_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت"
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "مرکز مدیریت و نظارت والدین 🛡️",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Content & Link Health Management Card (Rule 44)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BadgeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = BadgeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "مدیریت و اعتبارسنجی سلامت لینک‌ها",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "هیچ ویدیوی خراب یا تکراری به کودک نشان داده نمی‌شود. فقط لینک‌های با وضعیت VERIFIED نمایش داده می‌شوند.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = PersianUtils.toPersianDigits(allVideosForAdmin.size),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "کل ویدیوها",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BadgeGreen.copy(alpha = 0.12f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = PersianUtils.toPersianDigits(verifiedCount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = BadgeGreen
                                )
                                Text(
                                    text = "تأیید شده (سالم)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BadgeGreen
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE63946).copy(alpha = 0.12f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = PersianUtils.toPersianDigits(failedCount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFE63946)
                                )
                                Text(
                                    text = "خراب / رد شده",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE63946)
                                )
                            }
                        }
                    }

                    // Scan Progress Indicator
                    if (isScanningHealth) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val (current, total) = scanProgress
                            val progressFloat = if (total > 0) (current.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
                            LinearProgressIndicator(
                                progress = { progressFloat },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PrimaryOrange
                            )
                            Text(
                                text = scanStatusMessage ?: "در حال بررسی سلامت لینک‌ها...",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryOrange
                            )
                        }
                    } else if (scanStatusMessage != null) {
                        Text(
                            text = scanStatusMessage!!,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = BadgeGreen
                        )
                    }

                    // Health Check Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.runHealthCheckOnAllVideos() },
                            enabled = !isScanningHealth,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بررسی سلامت لینک‌ها", fontSize = 12.sp)
                        }

                        if (failedCount > 0) {
                            Button(
                                onClick = { viewModel.deleteFailedVideos() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE63946)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حذف خراب‌ها", fontSize = 11.sp)
                            }
                        }
                    }

                    // Add new video button
                    Button(
                        onClick = { showAddVideoDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTurquoise),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("افزودن ویدیوی جدید (همراه با تست خودکار سلامت)")
                    }
                }
            }

            // Internet & Data Settings Card (Rule 36, 38)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تنظیمات مصرف اینترنت و دیتا",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Playback on Cellular + WiFi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "پخش آنلاین با اینترنت سیم‌کارت و وای‌فای",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "تماشای بدون محدودیت ویدیوها روی اینترنت همراه و WiFi (پیش‌فرض فعال)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = playbackMode == PlaybackMode.WIFI_AND_MOBILE,
                            onCheckedChange = { isChecked ->
                                viewModel.networkSettingsManager.setPlaybackMode(
                                    if (isChecked) PlaybackMode.WIFI_AND_MOBILE else PlaybackMode.WIFI_ONLY
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryOrange)
                        )
                    }

                    // Auto Cache Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ذخیره خودکار در کش هوشمند (Media3)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ویدیوهایی که تماشا می‌شوند در کش ذخیره شده تا در دفعات بعد اینترنت مصرف نشود",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoCacheEnabled,
                            onCheckedChange = { viewModel.networkSettingsManager.setAutoCacheEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryOrange)
                        )
                    }

                    // Cellular Download Warning Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "هشدار قبل از دانلود با اینترنت همراه",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "قبل از دانلود ویدیو روی اینترنت سیم‌کارت، تاییدیه حجم گرفته شود",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = downloadMode == DownloadMode.PROMPT,
                            onCheckedChange = { isChecked ->
                                viewModel.networkSettingsManager.setDownloadMode(
                                    if (isChecked) DownloadMode.PROMPT else DownloadMode.WIFI_AND_MOBILE
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryOrange)
                        )
                    }
                }
            }

            // Storage & Cache Manager Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SecondaryTurquoise.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = SecondaryTurquoise,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "مدیریت حافظه دستگاه و کش",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "حجم کش موقت ویدیوها:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${PersianUtils.toPersianDigits(String.format("%.1f", cacheSizeMB))} مگابایت",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تعداد ویدیوهای دانلود شده آفلاین:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${PersianUtils.toPersianDigits(downloadedVideos.size)} ویدیو",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SecondaryTurquoise
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (cacheClearedMessage) {
                        Text(
                            text = "حافظه موقت با موفقیت آزاد شد! ✨",
                            color = BadgeGreen,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            BachegramCache.clearCache(context)
                            cacheSizeMB = 0.0
                            cacheClearedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پاکسازی حافظه موقت (کش)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Screen Time Manager Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "مدیریت زمان استفاده در روز",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "سقف مجاز تماشا: ${PersianUtils.toPersianDigits(dailyMinutesLimit.toInt())} دقیقه",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryOrange
                        )
                    )

                    Slider(
                        value = dailyMinutesLimit,
                        onValueChange = { dailyMinutesLimit = it },
                        valueRange = 15f..120f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryOrange,
                            activeTrackColor = PrimaryOrange
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "پس از اتمام زمان، برنامه با پیامی محبت‌آمیز به کودک پیشنهاد بازی و استراحت می‌دهد.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Learning Activity Report Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SecondaryTurquoise.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = SecondaryTurquoise,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "گزارش یادگیری فرزند شما",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تعداد ویدیوهای آموزشی دیده شده:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${PersianUtils.toPersianDigits(totalWatched)} ویدیو",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SecondaryTurquoise
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مجموع امتیازات و ستاره‌های کسب شده:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${PersianUtils.toPersianDigits(totalWatched * 5 + totalFavorites * 10)} ⭐",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = StarYellow
                        )
                    }
                }
            }

            // About Developer & Contact Button
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "شناسنامه و توسعه‌دهنده برنامه",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "توسعه داده شده توسط تیم نرم افزاری ردلاین سافت البرز به مدیریت مهندس مهدی رضایی",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onGoToAbout,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مشاهده اطلاعات تماس و سازندگان")
                    }
                }
            }
        }
    }

    // Add Video Dialog with Automated Health Check
    if (showAddVideoDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf("آموزش و خلاقیت") }
        var newDurationText by remember { mutableStateOf("180") }
        var isSubmitting by remember { mutableStateOf(false) }
        var submitStatusMessage by remember { mutableStateOf<String?>(null) }
        var isSuccess by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) showAddVideoDialog = false
            },
            title = {
                Text(
                    text = "افزودن ویدیوی جدید با تست سلامت 🎬",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "لینک قبل از ذخیره تست می‌شود (پاسخ سرور، Content-Type ویدیو و عدم تکراری بودن).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("عنوان ویدیو") },
                        placeholder = { Text("مثلاً: آموزش نقاشی خرس مهربان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("آدرس مستقیم ویدیو (MP4 یا m3u8)") },
                        placeholder = { Text("https://example.com/video.mp4") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("دسته‌بندی") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newDurationText,
                        onValueChange = { newDurationText = it },
                        label = { Text("مدت زمان تقریبی (ثانیه)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (submitStatusMessage != null) {
                        Text(
                            text = submitStatusMessage!!,
                            color = if (isSuccess) BadgeGreen else Color(0xFFE63946),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    if (isSubmitting) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("در حال ارسال درخواست و بررسی پاسخ سرور...", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                if (!isSuccess) {
                    Button(
                        onClick = {
                            if (newTitle.isBlank() || newUrl.isBlank()) {
                                submitStatusMessage = "لطفاً عنوان و آدرس ویدیو را وارد کنید"
                                return@Button
                            }
                            isSubmitting = true
                            submitStatusMessage = null
                            val durationSec = newDurationText.toIntOrNull() ?: 180

                            viewModel.addNewVerifiedVideo(
                                title = newTitle,
                                description = newTitle,
                                category = newCategory,
                                subCategory = newCategory,
                                videoUrl = newUrl,
                                ageMin = 3,
                                ageMax = 9,
                                duration = durationSec
                            ) { success, msg ->
                                isSubmitting = false
                                submitStatusMessage = msg
                                isSuccess = success
                            }
                        },
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Text("تست و ذخیره")
                    }
                } else {
                    Button(onClick = { showAddVideoDialog = false }) {
                        Text("تأیید و بستن")
                    }
                }
            },
            dismissButton = {
                if (!isSubmitting && !isSuccess) {
                    TextButton(onClick = { showAddVideoDialog = false }) {
                        Text("انصراف")
                    }
                }
            }
        )
    }
}
