package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BachegramBottomNav
import com.example.ui.components.BottomNavItem
import com.example.ui.screens.about.AboutUsScreen
import com.example.ui.screens.categories.CategoriesScreen
import com.example.ui.screens.feed.VideoFeedScreen
import com.example.ui.screens.feed.VideoFeedViewModel
import com.example.ui.screens.parent.ParentZoneScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.saved.SavedScreen
import com.example.ui.screens.search.SearchScreen

const val ROUTE_ABOUT_US = "about_us"
const val ROUTE_PARENT_ZONE = "parent_zone"

@Composable
fun BachegramApp(
    viewModel: VideoFeedViewModel = viewModel()
) {
    var currentRoute by remember { mutableStateOf(BottomNavItem.HOME.route) }

    // Enforce Persian RTL throughout the application
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BackHandler(enabled = currentRoute != BottomNavItem.HOME.route) {
            currentRoute = when (currentRoute) {
                ROUTE_ABOUT_US, ROUTE_PARENT_ZONE -> BottomNavItem.PROFILE.route
                else -> BottomNavItem.HOME.route
            }
        }

        val showBottomNav = currentRoute !in listOf(ROUTE_ABOUT_US, ROUTE_PARENT_ZONE)

        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    BachegramBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route -> currentRoute = route }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (showBottomNav) innerPadding else androidx.compose.foundation.layout.PaddingValues())
            ) {
                when (currentRoute) {
                    BottomNavItem.HOME.route -> {
                        VideoFeedScreen(
                            viewModel = viewModel,
                            onNavigateToSaved = { currentRoute = BottomNavItem.SAVED.route },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    BottomNavItem.CATEGORIES.route -> {
                        CategoriesScreen(
                            viewModel = viewModel,
                            onVideoSelected = { video ->
                                viewModel.selectVideo(video)
                                currentRoute = BottomNavItem.HOME.route
                            },
                            onSearchClick = {
                                currentRoute = BottomNavItem.SEARCH.route
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    BottomNavItem.SEARCH.route -> {
                        SearchScreen(
                            viewModel = viewModel,
                            onVideoSelected = { video ->
                                viewModel.selectVideo(video)
                                currentRoute = BottomNavItem.HOME.route
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    BottomNavItem.SAVED.route -> {
                        SavedScreen(
                            viewModel = viewModel,
                            onVideoSelected = { video ->
                                viewModel.selectVideo(video)
                                currentRoute = BottomNavItem.HOME.route
                            },
                            onGoToFeed = {
                                currentRoute = BottomNavItem.HOME.route
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    BottomNavItem.PROFILE.route -> {
                        ProfileScreen(
                            viewModel = viewModel,
                            onGoToAboutUs = { currentRoute = ROUTE_ABOUT_US },
                            onGoToParentZone = { currentRoute = ROUTE_PARENT_ZONE },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ROUTE_ABOUT_US -> {
                        AboutUsScreen(
                            onBack = { currentRoute = BottomNavItem.PROFILE.route },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ROUTE_PARENT_ZONE -> {
                        ParentZoneScreen(
                            viewModel = viewModel,
                            onBack = { currentRoute = BottomNavItem.PROFILE.route },
                            onGoToAbout = { currentRoute = ROUTE_ABOUT_US },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
