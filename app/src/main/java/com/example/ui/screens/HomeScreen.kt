package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SoundCatalog
import com.example.data.model.Soundscape
import com.example.ui.NatureCalmViewModel
import com.example.ui.components.AmbientSoundVisualizer
import com.example.ui.components.AtmosphericBackground
import com.example.ui.components.PlayPauseButton
import com.example.ui.components.SleepTimerSheet

@Composable
fun HomeScreen(
    viewModel: NatureCalmViewModel,
    onNavigateToMixer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primarySoundId by viewModel.primarySoundId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val soundName by viewModel.currentSoundOrPresetName.collectAsState()
    val masterVolume by viewModel.masterVolume.collectAsState()
    val visualizerAmp by viewModel.visualizerAmp.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val animationsEnabled by viewModel.isAnimationsEnabled.collectAsState()
    val isBatterySaver by viewModel.isBatterySaver.collectAsState()

    val currentSound = SoundCatalog.getSound(primarySoundId)
    val isFav = favorites.contains("sound:${primarySoundId.key}")

    var showTimerSheet by remember { mutableStateOf(false) }

    AtmosphericBackground(
        primarySoundId = primarySoundId,
        isPlaying = isPlaying,
        animationsEnabled = animationsEnabled,
        isBatterySaver = isBatterySaver,
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Greeting
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = stringResource(viewModel.greetingRes),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            // Central Hero Soundscape & Playback Hub
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient resonance visualizer
                    AmbientSoundVisualizer(
                        amplitude = visualizerAmp,
                        accentColor = currentSound.accentColor,
                        isPlaying = isPlaying,
                        size = 250.dp
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = soundName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isPlaying) stringResource(R.string.loop_continuous) else stringResource(R.string.action_pause),
                            style = MaterialTheme.typography.labelMedium,
                            color = currentSound.accentColor.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Big Play/Pause Button
                        PlayPauseButton(
                            isPlaying = isPlaying,
                            onToggle = { viewModel.togglePlayPause() },
                            accentColor = currentSound.accentColor
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Controls Row (Timer, Favorite, Mixer)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            // Sleep Timer
                            IconButton(
                                onClick = { showTimerSheet = true },
                                modifier = Modifier.testTag("home_timer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = stringResource(R.string.sleep_timer),
                                    tint = if (timerState.isActive) currentSound.accentColor else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Favorite
                            IconButton(
                                onClick = {
                                    viewModel.toggleFavorite("sound:${primarySoundId.key}", "SOUND")
                                },
                                modifier = Modifier.testTag("home_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = stringResource(R.string.action_favorite),
                                    tint = if (isFav) Color(0xFFFF5252) else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Sound Mixer Shortcut
                            IconButton(
                                onClick = onNavigateToMixer,
                                modifier = Modifier.testTag("home_mixer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = stringResource(R.string.mixer_title),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Master Volume Slider
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Slider(
                        value = masterVolume,
                        onValueChange = { viewModel.setMasterVolume(it) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .testTag("master_volume_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = currentSound.accentColor,
                            activeTrackColor = currentSound.accentColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Sound of the Day ("Today's Atmosphere")
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.sound_of_the_day),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DailySoundCard(
                        sound = viewModel.recommendedSound,
                        onPlay = {
                            viewModel.playSingleSound(viewModel.recommendedSound.id)
                        }
                    )
                }
            }

            // Quick Mixes Carousel
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.quick_mixes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(SoundCatalog.defaultPresets) { preset ->
                            Surface(
                                onClick = { viewModel.applyPreset(preset) },
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                modifier = Modifier.testTag("preset_${preset.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = preset.iconEmoji,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = preset.nameRes?.let { stringResource(it) } ?: preset.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recommended Soundscapes Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.recommended_for_you),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(SoundCatalog.allSounds.take(6)) { sound ->
                            AtmospherePreviewCard(
                                sound = sound,
                                isPlaying = isPlaying && primarySoundId == sound.id,
                                onPlay = { viewModel.playSingleSound(sound.id) }
                            )
                        }
                    }
                }
            }
        }

        // Sleep Timer Sheet
        if (showTimerSheet) {
            SleepTimerSheet(
                timerState = timerState,
                onSetTimer = { minutes, fadeOut ->
                    viewModel.setSleepTimer(minutes, fadeOut)
                },
                onCancelTimer = {
                    viewModel.cancelSleepTimer()
                },
                onDismiss = { showTimerSheet = false }
            )
        }
    }
}

@Composable
fun DailySoundCard(
    sound: Soundscape,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .testTag("daily_sound_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(sound.accentColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = sound.iconEmoji, fontSize = 26.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = stringResource(R.string.todays_atmosphere),
                        style = MaterialTheme.typography.labelSmall,
                        color = sound.accentColor
                    )
                    Text(
                        text = stringResource(sound.nameRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(sound.descRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(sound.accentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.action_play),
                    tint = Color(0xFF071F18)
                )
            }
        }
    }
}

@Composable
fun AtmospherePreviewCard(
    sound: Soundscape,
    isPlaying: Boolean,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(170.dp)
            .clickable(onClick = onPlay)
            .testTag("preview_sound_${sound.id.key}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = sound.iconEmoji, fontSize = 28.sp)
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(sound.accentColor)
                    )
                }
            }

            Column {
                Text(
                    text = stringResource(sound.nameRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.loop_continuous),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
