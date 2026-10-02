package com.example.ui.screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VideoItem
import com.example.data.model.VideoStatus
import com.example.ui.screens.feed.VideoFeedViewModel

private val IG_TEXT = Color(0xFF000000)
private val IG_GRAY = Color(0xFF8E8E8E)
private val IG_BORDER = Color(0xFFDBDBDB)

/**
 * Instagram-style "New post" screen.
 * Lets the child pick a video from their device, add a caption,
 * and save it as a local post that only they can see.
 */
@Composable
fun CreatePostScreen(
    viewModel: VideoFeedViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
    ) {
        // ============== TOP BAR ==============
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
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
                text = "پست جدید",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = IG_TEXT
            )
            Spacer(Modifier.weight(1f))

            // Share button
            if (selectedUri != null) {
                Text(
                    text = if (isSaving) "..." else "اشتراک",
                    color = if (isSaving) IG_GRAY else Color(0xFF0095F6),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(enabled = !isSaving) {
                            isSaving = true
                            errorMessage = null
                            val uri = selectedUri
                            if (uri != null) {
                                viewModel.addLocalPost(
                                    uri = uri,
                                    caption = caption,
                                    onResult = { success, msg ->
                                        isSaving = false
                                        if (success) onCreated()
                                        else errorMessage = msg
                                    }
                                )
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(IG_BORDER)
        )

        // ============== BODY ==============
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectedUri == null) {
                // Empty state: big add button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF2F2F2))
                        .clickable {
                            picker.launch("video/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "انتخاب ویدیو",
                                tint = IG_TEXT,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = "انتخاب ویدیو از گالری",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = IG_TEXT
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "فایل ویدیویی از گوشی انتخاب کن",
                            fontSize = 12.sp,
                            color = IG_GRAY
                        )
                    }
                }
            } else {
                // Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "پیش‌نمایش",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Caption input
                TextField(
                    value = caption,
                    onValueChange = { caption = it },
                    placeholder = {
                        Text(
                            text = "کپشن بنویس...",
                            color = IG_GRAY,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = false,
                    maxLines = 4,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = IG_TEXT
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F2F2), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Change video button
                Text(
                    text = "تغییر ویدیو",
                    color = Color(0xFF0095F6),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { picker.launch("video/*") }
                        .padding(6.dp)
                )
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFE63946),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}