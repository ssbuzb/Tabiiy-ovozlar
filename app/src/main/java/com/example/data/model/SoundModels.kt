package com.example.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.R

enum class SoundId(val key: String) {
    GENTLE_RAIN("gentle_rain"),
    HEAVY_RAIN("heavy_rain"),
    FOREST("forest"),
    OCEAN("ocean"),
    RIVER("river"),
    WIND("wind"),
    BIRDS("birds"),
    FIREPLACE("fireplace"),
    NIGHT_NATURE("night_nature"),
    RAINY_CAFE("rainy_cafe"),
    RAIN_WINDOW("rain_window"),
    PEACEFUL_GARDEN("peaceful_garden");

    companion object {
        fun fromKey(key: String): SoundId = values().find { it.key == key } ?: GENTLE_RAIN
    }
}

enum class SoundCategory {
    ALL,
    RAIN,
    NATURE,
    AMBIENT
}

data class Soundscape(
    val id: SoundId,
    @StringRes val nameRes: Int,
    @StringRes val descRes: Int,
    val category: SoundCategory,
    val iconEmoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val accentColor: Color,
    val defaultVolume: Float = 0.7f
)

data class SoundPreset(
    val id: String,
    val name: String,
    @StringRes val nameRes: Int? = null,
    val volumes: Map<SoundId, Float>,
    val isCustom: Boolean = false,
    val iconEmoji: String = "✨"
)

data class SleepTimerState(
    val isActive: Boolean = false,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isFadeOutEnabled: Boolean = true
)

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    SYSTEM("system", "System Default", "🌐"),
    UZBEK("uz", "O'zbekcha", "🇺🇿"),
    RUSSIAN("ru", "Русский", "🇷🇺"),
    ENGLISH("en", "English", "🇬🇧")
}

enum class AppThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

data class AiSoundscapeResponse(
    val title: String,
    val description: String,
    val volumes: Map<SoundId, Float>,
    val recommendedTimerMinutes: Int,
    val meditationNote: String
)
