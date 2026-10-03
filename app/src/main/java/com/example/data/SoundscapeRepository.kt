package com.example.data

import com.example.data.local.CustomPresetEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.NatureCalmDatabase
import com.example.data.local.UserSessionEntity
import com.example.data.model.SoundId
import com.example.data.model.SoundPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

class SoundscapeRepository(private val database: NatureCalmDatabase) {

    private val dao = database.soundscapeDao()

    val customPresets: Flow<List<SoundPreset>> = dao.getAllCustomPresets().map { entities ->
        entities.map { entity ->
            val volumes = parseVolumesJson(entity.soundVolumesJson)
            SoundPreset(
                id = "custom_${entity.id}",
                name = entity.name,
                volumes = volumes,
                isCustom = true,
                iconEmoji = entity.iconEmoji
            )
        }
    }

    val favorites: Flow<Set<String>> = dao.getAllFavorites().map { list ->
        list.map { it.id }.toSet()
    }

    suspend fun saveCustomPreset(name: String, volumes: Map<SoundId, Float>, iconEmoji: String = "✨"): Long {
        val json = JSONObject()
        volumes.forEach { (soundId, volume) ->
            json.put(soundId.key, volume.toDouble())
        }
        val entity = CustomPresetEntity(
            name = name,
            soundVolumesJson = json.toString(),
            iconEmoji = iconEmoji
        )
        return dao.insertCustomPreset(entity)
    }

    suspend fun deleteCustomPreset(presetId: String) {
        val id = presetId.removePrefix("custom_").toLongOrNull() ?: return
        dao.deleteCustomPresetById(id)
    }

    suspend fun toggleFavorite(itemId: String, itemType: String) {
        if (dao.isFavorite(itemId)) {
            dao.deleteFavorite(itemId)
        } else {
            dao.insertFavorite(FavoriteEntity(id = itemId, itemType = itemType))
        }
    }

    suspend fun isFavorite(itemId: String): Boolean = dao.isFavorite(itemId)

    suspend fun recordSession(presetOrSoundName: String, durationMinutes: Int) {
        if (durationMinutes > 0) {
            dao.insertSession(
                UserSessionEntity(
                    presetOrSoundName = presetOrSoundName,
                    durationMinutes = durationMinutes
                )
            )
        }
    }

    private fun parseVolumesJson(jsonStr: String): Map<SoundId, Float> {
        val result = mutableMapOf<SoundId, Float>()
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val soundId = SoundId.fromKey(key)
                val volume = json.optDouble(key, 0.5).toFloat()
                result[soundId] = volume
            }
        } catch (e: Exception) {
            // fallback
        }
        return result
    }
}
