package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.ProceduralSoundSynthesizer
import com.example.data.SoundCatalog
import com.example.data.model.SoundId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NatureCalmAppTest {

    @Test
    fun appName_isNatureCalm() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Nature Calm", appName)
    }

    @Test
    fun soundCatalog_containsAll12Sounds() {
        assertEquals(12, SoundCatalog.allSounds.size)
        val soundIds = SoundCatalog.allSounds.map { it.id }.toSet()
        assertTrue(soundIds.contains(SoundId.GENTLE_RAIN))
        assertTrue(soundIds.contains(SoundId.HEAVY_RAIN))
        assertTrue(soundIds.contains(SoundId.FOREST))
        assertTrue(soundIds.contains(SoundId.OCEAN))
        assertTrue(soundIds.contains(SoundId.RIVER))
        assertTrue(soundIds.contains(SoundId.WIND))
        assertTrue(soundIds.contains(SoundId.BIRDS))
        assertTrue(soundIds.contains(SoundId.FIREPLACE))
        assertTrue(soundIds.contains(SoundId.NIGHT_NATURE))
        assertTrue(soundIds.contains(SoundId.RAINY_CAFE))
        assertTrue(soundIds.contains(SoundId.RAIN_WINDOW))
        assertTrue(soundIds.contains(SoundId.PEACEFUL_GARDEN))
    }

    @Test
    fun soundCatalog_defaultPresetsAreValid() {
        assertTrue(SoundCatalog.defaultPresets.isNotEmpty())
        SoundCatalog.defaultPresets.forEach { preset ->
            assertTrue(preset.volumes.isNotEmpty())
            preset.volumes.values.forEach { vol ->
                assertTrue(vol in 0.0f..1.0f)
            }
        }
    }

    @Test
    fun proceduralSynthesizer_fillsAudioBuffer() {
        val synth = ProceduralSoundSynthesizer(44100)
        val buffer = ShortArray(1024)
        synth.fillBuffer(
            buffer = buffer,
            activeVolumes = mapOf(SoundId.GENTLE_RAIN to 0.75f, SoundId.OCEAN to 0.5f),
            masterVolume = 0.8f,
            fadeMultiplier = 1.0f
        )
        // Verify buffer has audio samples generated
        val hasNonZero = buffer.any { it.toInt() != 0 }
        assertTrue(hasNonZero)
    }
}
