package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class BachegramTab {
    HOME, SEARCH, CREATE, REELS, PROFILE
}

@Composable
fun BachegramBottomBar(
    selectedTab: BachegramTab,
    onTabSelected: (BachegramTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderGray = Color(0xFFDBDBDB)
    val iconInactive = Color(0xFF262626)

    Surface(
        color = Color.White,
        modifier = modifier.fillMaxWidth()
    ) {
        Box {
            // Instagram-style thin top divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(borderGray)
                    .align(Alignment.TopCenter)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BachegramTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        when (tab) {
                            BachegramTab.HOME -> Icon(
                                imageVector = if (isSelected) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "خانه",
                                tint = if (isSelected) Color.Black else iconInactive,
                                modifier = Modifier.size(25.dp)
                            )

                            BachegramTab.SEARCH -> Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "جستجو",
                                tint = if (isSelected) Color.Black else iconInactive,
                                modifier = Modifier.size(26.dp)
                            )

                            // ★ Instagram-style PLUS button (rounded square outline)
                            BachegramTab.CREATE -> Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .border(
                                        width = 1.8.dp,
                                        color = if (isSelected) Color.Black else iconInactive,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "ساخت پست",
                                    tint = if (isSelected) Color.Black else iconInactive,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            BachegramTab.REELS -> Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .border(
                                        width = 1.8.dp,
                                        color = if (isSelected) Color.Black else iconInactive,
                                        shape = RoundedCornerShape(7.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "ریلز",
                                    tint = if (isSelected) Color.Black else iconInactive,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            BachegramTab.PROFILE -> Box(
                                modifier = Modifier
                                    .size(27.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBDBDB))
                                    .border(
                                        width = if (isSelected) 1.8.dp else 1.dp,
                                        color = if (isSelected) Color.Black else borderGray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {}
                        }
                    }
                }
            }
        }
    }
}