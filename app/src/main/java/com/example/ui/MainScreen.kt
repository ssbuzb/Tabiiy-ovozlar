package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.screens.AiAlchemistScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.MixerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.util.LocaleHelper

enum class ScreenTab(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME(R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    MIXER(R.string.nav_mixer, Icons.Filled.Tune, Icons.Outlined.Tune, "tab_mixer"),
    LIBRARY(R.string.nav_library, Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic, "tab_library"),
    FAVORITES(R.string.nav_favorites, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "tab_favorites"),
    AI(R.string.nav_ai_alchemist, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_ai"),
    SETTINGS(R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

@Composable
fun MainScreen(viewModel: NatureCalmViewModel) {
    val context = LocalContext.current
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

    // Localized Context provider
    val localizedContext = remember(appLanguage, context) {
        LocaleHelper.setLocale(context, appLanguage)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        if (!isOnboardingCompleted) {
            OnboardingScreen(
                onComplete = {
                    viewModel.setOnboardingCompleted(true)
                }
            )
        } else {
            var currentTab by remember { mutableStateOf(ScreenTab.HOME) }

            BackHandler(enabled = currentTab != ScreenTab.HOME) {
                currentTab = ScreenTab.HOME
            }

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        val navTabs = listOf(
                            ScreenTab.HOME,
                            ScreenTab.MIXER,
                            ScreenTab.LIBRARY,
                            ScreenTab.FAVORITES,
                            ScreenTab.AI,
                            ScreenTab.SETTINGS
                        )

                        navTabs.forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.titleRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(tab.titleRes),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                ),
                                modifier = Modifier.testTag(tab.tag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition"
                    ) { target ->
                        when (target) {
                            ScreenTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToMixer = { currentTab = ScreenTab.MIXER }
                            )
                            ScreenTab.MIXER -> MixerScreen(viewModel = viewModel)
                            ScreenTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                            ScreenTab.FAVORITES -> FavoritesScreen(viewModel = viewModel)
                            ScreenTab.AI -> AiAlchemistScreen(viewModel = viewModel)
                            ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
