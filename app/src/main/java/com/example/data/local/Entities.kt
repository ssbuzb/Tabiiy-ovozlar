package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_presets")
data class CustomPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val soundVolumesJson: String,
    val iconEmoji: String = "✨",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, // e.g. sound:gentle_rain or preset:preset_deep_sleep
    val itemType: String,       // "SOUND" or "PRESET"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_sessions")
data class UserSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val presetOrSoundName: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
