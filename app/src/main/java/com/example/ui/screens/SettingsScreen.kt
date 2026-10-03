package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.ui.NatureCalmViewModel

@Composable
fun SettingsScreen(
    viewModel: NatureCalmViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.appLanguage.collectAsState()
    val currentTheme by viewModel.appThemeMode.collectAsState()
    val isBatterySaver by viewModel.isBatterySaver.collectAsState()
    val isAnimationsEnabled by viewModel.isAnimationsEnabled.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Preferences
        item {
            Text(
                text = stringResource(R.string.section_preferences),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Language Picker Tile
        item {
            SettingsClickableTile(
                icon = Icons.Default.Language,
                title = stringResource(R.string.setting_language),
                subtitle = "${currentLang.flag} ${currentLang.displayName}",
                onClick = { showLanguageDialog = true },
                tag = "settings_language"
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Theme Picker Tile
        item {
            val themeTitle = when (currentTheme) {
                AppThemeMode.DARK -> stringResource(R.string.theme_dark)
                AppThemeMode.LIGHT -> stringResource(R.string.theme_light)
                AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                else -> stringResource(R.string.theme_system)
            }
            SettingsClickableTile(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.setting_theme),
                subtitle = themeTitle,
                onClick = { showThemeDialog = true },
                tag = "settings_theme"
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Audio & Playback
        item {
            Text(
                text = stringResource(R.string.section_audio),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Battery Saver Mode Switch
        item {
            SettingsSwitchTile(
                icon = Icons.Default.BatterySaver,
                title = stringResource(R.string.setting_battery_saver),
                subtitle = stringResource(R.string.setting_battery_saver_desc),
                checked = isBatterySaver,
                onCheckedChange = { viewModel.setBatterySaver(it) },
                tag = "settings_battery_saver"
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Ambient Animations Switch
        item {
            SettingsSwitchTile(
                icon = Icons.Default.Animation,
                title = stringResource(R.string.setting_animations),
                subtitle = stringResource(R.string.setting_animations_desc),
                checked = isAnimationsEnabled,
                onCheckedChange = { viewModel.setAnimationsEnabled(it) },
                tag = "settings_animations"
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Audio Engine Info Tile
        item {
            SettingsClickableTile(
                icon = Icons.Default.GraphicEq,
                title = stringResource(R.string.setting_audio_engine),
                subtitle = stringResource(R.string.setting_audio_engine_desc),
                onClick = { },
                tag = "settings_audio_engine"
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: System & About
        item {
            Text(
                text = stringResource(R.string.section_system),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Privacy Policy Tile
        item {
            SettingsClickableTile(
                icon = Icons.Default.Security,
                title = stringResource(R.string.setting_privacy),
                subtitle = stringResource(R.string.setting_privacy_desc),
                onClick = { showPrivacyDialog = true },
                tag = "settings_privacy"
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // About Nature Calm Tile
        item {
            SettingsClickableTile(
                icon = Icons.Default.Info,
                title = stringResource(R.string.setting_about),
                subtitle = stringResource(R.string.setting_version),
                onClick = { showAboutDialog = true },
                tag = "settings_about"
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.setting_language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${lang.flag} ${lang.displayName}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (currentLang == lang) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.setting_theme)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.values().forEach { mode ->
                        val label = when (mode) {
                            AppThemeMode.DARK -> stringResource(R.string.theme_dark)
                            AppThemeMode.LIGHT -> stringResource(R.string.theme_light)
                            AppThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (currentTheme == mode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(stringResource(R.string.setting_privacy)) },
            text = {
                Text(
                    text = "Nature Calm is committed to protecting your privacy.\n\n" +
                            "• Zero personal telemetry or trackers are collected.\n" +
                            "• All sounds are mathematically synthesized offline on your device in real-time.\n" +
                            "• Presets, timers, and favorites remain strictly on your local device.\n" +
                            "• AI Soundscape Alchemist calls Gemini securely without saving sensitive data.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.setting_about)) },
            text = {
                Column {
                    Text(
                        text = "Nature Calm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.setting_version),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Designed for deep sleep, peaceful focus, meditation, and pure rest through organic nature soundscapes.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsClickableTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
            )
        }
    }
}
