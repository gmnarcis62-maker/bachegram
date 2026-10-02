package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.VideoItem
import com.example.ui.components.BachegramBottomBar
import com.example.ui.components.BachegramTab
import com.example.ui.components.InstagramTopBar
import com.example.ui.screens.about.AboutUsScreen
import com.example.ui.screens.create.CreatePostScreen
import com.example.ui.screens.detail.PostDetailScreen
import com.example.ui.screens.direct.DirectMessagesScreen
import com.example.ui.screens.feed.VideoFeedScreen
import com.example.ui.screens.feed.VideoFeedViewModel
import com.example.ui.screens.home.HomeFeedScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.parent.ParentZoneScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.saved.SavedScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.stories.StoryViewerScreen

const val ROUTE_ABOUT_US = "about_us"
const val ROUTE_PARENT_ZONE = "parent_zone"
const val ROUTE_SAVED = "saved"
const val ROUTE_NOTIFICATIONS = "notifications"
const val ROUTE_DIRECT = "direct"
const val ROUTE_POST_DETAIL = "post_detail"
const val ROUTE_STORY = "story"

@Composable
fun BachegramApp(
    viewModel: VideoFeedViewModel = viewModel()
) {
    var nestedRoute by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(BachegramTab.HOME) }

    // Selected video for Post Detail and Story viewer
    var detailVideo by remember { mutableStateOf<VideoItem?>(null) }
    var storyVideo by remember { mutableStateOf<VideoItem?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        BackHandler(enabled = nestedRoute != null) {
            nestedRoute = null
            detailVideo = null
            storyVideo = null
        }

        // ============================================================
        //  NESTED / FULL-SCREEN SCREENS
        // ============================================================
        if (nestedRoute != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                when (nestedRoute) {
                    ROUTE_ABOUT_US -> AboutUsScreen(
                        onBack = { nestedRoute = null },
                        modifier = Modifier.fillMaxSize()
                    )

                    ROUTE_PARENT_ZONE -> ParentZoneScreen(
                        viewModel = viewModel,
                        onBack = { nestedRoute = null },
                        onGoToAbout = { nestedRoute = ROUTE_ABOUT_US },
                        modifier = Modifier.fillMaxSize()
                    )

                    ROUTE_SAVED -> SavedScreen(
                        viewModel = viewModel,
                        onVideoSelected = { v ->
                            detailVideo = v
                            nestedRoute = ROUTE_POST_DETAIL
                        },
                        onGoToFeed = { nestedRoute = null },
                        onBack = { nestedRoute = null },
                        modifier = Modifier.fillMaxSize()
                    )

                    ROUTE_NOTIFICATIONS -> NotificationsScreen(
                        onBack = { nestedRoute = null },
                        modifier = Modifier.fillMaxSize()
                    )

                    ROUTE_DIRECT -> DirectMessagesScreen(
                        onBack = { nestedRoute = null },
                        modifier = Modifier.fillMaxSize()
                    )

                    ROUTE_POST_DETAIL -> {
                        val v = detailVideo
                        if (v != null) {
                            PostDetailScreen(
                                video = v,
                                viewModel = viewModel,
                                onBack = {
                                    nestedRoute = null
                                    detailVideo = null
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            nestedRoute = null
                        }
                    }

                    ROUTE_STORY -> {
                        val v = storyVideo
                        if (v != null) {
                            StoryViewerScreen(
                                video = v,
                                onDismiss = {
                                    nestedRoute = null
                                    storyVideo = null
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            nestedRoute = null
                        }
                    }
                }
            }
            return@CompositionLocalProvider
        }

        // ============================================================
        //  MAIN INSTAGRAM LAYOUT
        // ============================================================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
        ) {
            AnimatedVisibility(
                visible = selectedTab != BachegramTab.CREATE &&
                    selectedTab != BachegramTab.REELS,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                InstagramTopBar(
                    onNotificationsClick = { nestedRoute = ROUTE_NOTIFICATIONS },
                    onMessagesClick = { nestedRoute = ROUTE_DIRECT }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    BachegramTab.HOME -> HomeFeedScreen(
                        viewModel = viewModel,
                        onVideoSelected = { v ->
                            detailVideo = v
                            nestedRoute = ROUTE_POST_DETAIL
                        }
                    )

                    BachegramTab.SEARCH -> SearchScreen(
                        viewModel = viewModel,
                        onVideoSelected = { v ->
                            detailVideo = v
                            nestedRoute = ROUTE_POST_DETAIL
                        }
                    )

                    BachegramTab.CREATE -> CreatePostScreen(
                        viewModel = viewModel,
                        onBack = { selectedTab = BachegramTab.HOME },
                        onCreated = { selectedTab = BachegramTab.PROFILE }
                    )

                    BachegramTab.REELS -> VideoFeedScreen(
                        viewModel = viewModel,
                        onNavigateToSaved = { nestedRoute = ROUTE_SAVED },
                        onBackClick = { selectedTab = BachegramTab.HOME },
                        modifier = Modifier.fillMaxSize()
                    )

                    BachegramTab.PROFILE -> ProfileScreen(
                        viewModel = viewModel,
                        onGoToAboutUs = { nestedRoute = ROUTE_ABOUT_US },
                        onGoToParentZone = { nestedRoute = ROUTE_PARENT_ZONE },
                        onGoToSaved = { nestedRoute = ROUTE_SAVED },
                        onVideoSelected = { v ->
                            detailVideo = v
                            nestedRoute = ROUTE_POST_DETAIL
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Box(modifier = Modifier.navigationBarsPadding()) {
                BachegramBottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { newTab -> selectedTab = newTab }
                )
            }
        }
    }
}