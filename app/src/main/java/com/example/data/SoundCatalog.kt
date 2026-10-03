package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.data.model.SoundCategory
import com.example.data.model.SoundId
import com.example.data.model.SoundPreset
import com.example.data.model.Soundscape

object SoundCatalog {

    val allSounds: List<Soundscape> = listOf(
        Soundscape(
            id = SoundId.GENTLE_RAIN,
            nameRes = R.string.sound_gentle_rain,
            descRes = R.string.sound_gentle_rain_desc,
            category = SoundCategory.RAIN,
            iconEmoji = "🌧️",
            primaryColor = Color(0xFF1B2F38),
            secondaryColor = Color(0xFF0F1E24),
            accentColor = Color(0xFF5AB6D8),
            defaultVolume = 0.75f
        ),
        Soundscape(
            id = SoundId.HEAVY_RAIN,
            nameRes = R.string.sound_heavy_rain,
            descRes = R.string.sound_heavy_rain_desc,
            category = SoundCategory.RAIN,
            iconEmoji = "⛈️",
            primaryColor = Color(0xFF19232F),
            secondaryColor = Color(0xFF0B1119),
            accentColor = Color(0xFF789EC9),
            defaultVolume = 0.65f
        ),
        Soundscape(
            id = SoundId.FOREST,
            nameRes = R.string.sound_forest,
            descRes = R.string.sound_forest_desc,
            category = SoundCategory.NATURE,
            iconEmoji = "🌲",
            primaryColor = Color(0xFF183323),
            secondaryColor = Color(0xFF0B1C12),
            accentColor = Color(0xFF57B882),
            defaultVolume = 0.70f
        ),
        Soundscape(
            id = SoundId.OCEAN,
            nameRes = R.string.sound_ocean,
            descRes = R.string.sound_ocean_desc,
            category = SoundCategory.NATURE,
            iconEmoji = "🌊",
            primaryColor = Color(0xFF122C3D),
            secondaryColor = Color(0xFF071722),
            accentColor = Color(0xFF4FAADB),
            defaultVolume = 0.70f
        ),
        Soundscape(
            id = SoundId.RIVER,
            nameRes = R.string.sound_river,
            descRes = R.string.sound_river_desc,
            category = SoundCategory.NATURE,
            iconEmoji = "🏞️",
            primaryColor = Color(0xFF133636),
            secondaryColor = Color(0xFF071E1E),
            accentColor = Color(0xFF4AC4B8),
            defaultVolume = 0.65f
        ),
        Soundscape(
            id = SoundId.WIND,
            nameRes = R.string.sound_wind,
            descRes = R.string.sound_wind_desc,
            category = SoundCategory.NATURE,
            iconEmoji = "🍃",
            primaryColor = Color(0xFF1C3138),
            secondaryColor = Color(0xFF0D1C21),
            accentColor = Color(0xFF76C4BA),
            defaultVolume = 0.50f
        ),
        Soundscape(
            id = SoundId.BIRDS,
            nameRes = R.string.sound_birds,
            descRes = R.string.sound_birds_desc,
            category = SoundCategory.NATURE,
            iconEmoji = "🐦",
            primaryColor = Color(0xFF22361E),
            secondaryColor = Color(0xFF101F0D),
            accentColor = Color(0xFF90D462),
            defaultVolume = 0.55f
        ),
        Soundscape(
            id = SoundId.FIREPLACE,
            nameRes = R.string.sound_fireplace,
            descRes = R.string.sound_fireplace_desc,
            category = SoundCategory.AMBIENT,
            iconEmoji = "🔥",
            primaryColor = Color(0xFF381F17),
            secondaryColor = Color(0xFF210E09),
            accentColor = Color(0xFFE28148),
            defaultVolume = 0.65f
        ),
        Soundscape(
            id = SoundId.NIGHT_NATURE,
            nameRes = R.string.sound_night_nature,
            descRes = R.string.sound_night_nature_desc,
            category = SoundCategory.NATURE,
            iconEmoji = "🌙",
            primaryColor = Color(0xFF1B1933),
            secondaryColor = Color(0xFF0B091B),
            accentColor = Color(0xFF9B8CE6),
            defaultVolume = 0.60f
        ),
        Soundscape(
            id = SoundId.RAINY_CAFE,
            nameRes = R.string.sound_rainy_cafe,
            descRes = R.string.sound_rainy_cafe_desc,
            category = SoundCategory.AMBIENT,
            iconEmoji = "☕",
            primaryColor = Color(0xFF2E231C),
            secondaryColor = Color(0xFF19120E),
            accentColor = Color(0xFFC7956D),
            defaultVolume = 0.60f
        ),
        Soundscape(
            id = SoundId.RAIN_WINDOW,
            nameRes = R.string.sound_rain_window,
            descRes = R.string.sound_rain_window_desc,
            category = SoundCategory.RAIN,
            iconEmoji = "🌧️",
            primaryColor = Color(0xFF1B2936),
            secondaryColor = Color(0xFF0D1720),
            accentColor = Color(0xFF67A4D0),
            defaultVolume = 0.70f
        ),
        Soundscape(
            id = SoundId.PEACEFUL_GARDEN,
            nameRes = R.string.sound_peaceful_garden,
            descRes = R.string.sound_peaceful_garden_desc,
            category = SoundCategory.AMBIENT,
            iconEmoji = "🌿",
            primaryColor = Color(0xFF1D3328),
            secondaryColor = Color(0xFF0A1B13),
            accentColor = Color(0xFF66C898),
            defaultVolume = 0.60f
        )
    )

    fun getSound(id: SoundId): Soundscape = allSounds.find { it.id == id } ?: allSounds.first()

    val defaultPresets: List<SoundPreset> = listOf(
        SoundPreset(
            id = "preset_deep_sleep",
            name = "Deep Sleep",
            nameRes = R.string.preset_deep_sleep,
            volumes = mapOf(
                SoundId.GENTLE_RAIN to 0.75f,
                SoundId.NIGHT_NATURE to 0.35f,
                SoundId.WIND to 0.20f
            ),
            iconEmoji = "🛌"
        ),
        SoundPreset(
            id = "preset_study_mode",
            name = "Study Mode",
            nameRes = R.string.preset_study_mode,
            volumes = mapOf(
                SoundId.RAINY_CAFE to 0.60f,
                SoundId.RAIN_WINDOW to 0.40f,
                SoundId.WIND to 0.20f
            ),
            iconEmoji = "📚"
        ),
        SoundPreset(
            id = "preset_rainy_night",
            name = "Rainy Night",
            nameRes = R.string.preset_rainy_night,
            volumes = mapOf(
                SoundId.HEAVY_RAIN to 0.70f,
                SoundId.FIREPLACE to 0.40f,
                SoundId.RAIN_WINDOW to 0.35f
            ),
            iconEmoji = "⛈️"
        ),
        SoundPreset(
            id = "preset_cozy_evening",
            name = "Cozy Evening",
            nameRes = R.string.preset_cozy_evening,
            volumes = mapOf(
                SoundId.FIREPLACE to 0.75f,
                SoundId.RAIN_WINDOW to 0.35f,
                SoundId.GENTLE_RAIN to 0.25f
            ),
            iconEmoji = "🔥"
        ),
        SoundPreset(
            id = "preset_forest_morning",
            name = "Forest Morning",
            nameRes = R.string.preset_forest_morning,
            volumes = mapOf(
                SoundId.FOREST to 0.70f,
                SoundId.BIRDS to 0.60f,
                SoundId.RIVER to 0.45f
            ),
            iconEmoji = "🌲"
        ),
        SoundPreset(
            id = "preset_peaceful_meditation",
            name = "Peaceful Meditation",
            nameRes = R.string.preset_peaceful_meditation,
            volumes = mapOf(
                SoundId.OCEAN to 0.65f,
                SoundId.WIND to 0.25f,
                SoundId.PEACEFUL_GARDEN to 0.45f
            ),
            iconEmoji = "🧘"
        )
    )

    fun getRecommendedSoundForHour(hour: Int): Soundscape {
        return when (hour) {
            in 5..11 -> getSound(SoundId.FOREST)
            in 12..16 -> getSound(SoundId.GENTLE_RAIN)
            in 17..21 -> getSound(SoundId.FIREPLACE)
            else -> getSound(SoundId.NIGHT_NATURE)
        }
    }
}
