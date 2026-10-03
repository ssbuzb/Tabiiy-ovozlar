package com.example.audio

import com.example.data.model.SoundId
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh

class ProceduralSoundSynthesizer(private val sampleRate: Int = 44100) {

    private val random = Random()

    // Layer state instances
    private val gentleRain = RainGenerator(sampleRate, isHeavy = false)
    private val heavyRain = RainGenerator(sampleRate, isHeavy = true)
    private val forest = ForestGenerator(sampleRate)
    private val ocean = OceanGenerator(sampleRate)
    private val river = RiverGenerator(sampleRate)
    private val wind = WindGenerator(sampleRate)
    private val birds = BirdsGenerator(sampleRate)
    private val fireplace = FireplaceGenerator(sampleRate)
    private val nightNature = NightNatureGenerator(sampleRate)
    private val rainyCafe = RainyCafeGenerator(sampleRate)
    private val rainWindow = RainWindowGenerator(sampleRate)
    private val peacefulGarden = GardenGenerator(sampleRate)

    fun fillBuffer(
        buffer: ShortArray,
        activeVolumes: Map<SoundId, Float>,
        masterVolume: Float,
        fadeMultiplier: Float
    ) {
        if (activeVolumes.isEmpty() || masterVolume <= 0.001f || fadeMultiplier <= 0.001f) {
            buffer.fill(0)
            return
        }

        val effectiveMultiplier = masterVolume * fadeMultiplier

        for (i in buffer.indices) {
            var mixedSample = 0.0f

            for ((soundId, volume) in activeVolumes) {
                if (volume > 0.01f) {
                    val s = when (soundId) {
                        SoundId.GENTLE_RAIN -> gentleRain.nextSample()
                        SoundId.HEAVY_RAIN -> heavyRain.nextSample()
                        SoundId.FOREST -> forest.nextSample()
                        SoundId.OCEAN -> ocean.nextSample()
                        SoundId.RIVER -> river.nextSample()
                        SoundId.WIND -> wind.nextSample()
                        SoundId.BIRDS -> birds.nextSample()
                        SoundId.FIREPLACE -> fireplace.nextSample()
                        SoundId.NIGHT_NATURE -> nightNature.nextSample()
                        SoundId.RAINY_CAFE -> rainyCafe.nextSample()
                        SoundId.RAIN_WINDOW -> rainWindow.nextSample()
                        SoundId.PEACEFUL_GARDEN -> peacefulGarden.nextSample()
                    }
                    mixedSample += s * volume
                }
            }

            // Soft-knee limiter / saturation
            val limited = tanh((mixedSample * effectiveMultiplier).toDouble()).toFloat()
            val pcm = (limited * 32767.0f).toInt().coerceIn(-32768, 32767)
            buffer[i] = pcm.toShort()
        }
    }

    // --- Sound Layer Generators ---

    private class RainGenerator(private val sampleRate: Int, private val isHeavy: Boolean) {
        private var b0 = 0.0; private var b1 = 0.0; private var b2 = 0.0
        private var b3 = 0.0; private var b4 = 0.0; private var b5 = 0.0; private var b6 = 0.0
        private val rand = Random()
        private var thunderTimer = 0
        private var thunderPhase = 0.0
        private var thunderAmp = 0.0

        fun nextSample(): Float {
            val white = (rand.nextDouble() * 2.0 - 1.0)
            // Pink noise filter (Paul Kellet's algorithm)
            b0 = 0.99886 * b0 + white * 0.0555179
            b1 = 0.99332 * b1 + white * 0.0750759
            b2 = 0.96900 * b2 + white * 0.1538520
            b3 = 0.86650 * b3 + white * 0.3104856
            b4 = 0.55000 * b4 + white * 0.5329522
            b5 = -0.7616 * b5 - white * 0.0168980
            val pink = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362) * 0.11
            b6 = white * 0.115926

            // Droplet impulse
            var drop = 0.0
            val dropChance = if (isHeavy) 0.015 else 0.008
            if (rand.nextDouble() < dropChance) {
                drop = (rand.nextDouble() - 0.5) * 0.4
            }

            // Occasional thunder for Heavy Rain
            var thunder = 0.0
            if (isHeavy) {
                if (thunderTimer <= 0) {
                    if (rand.nextDouble() < 0.00003) { // every ~15-25 seconds
                        thunderTimer = (sampleRate * (3.0 + rand.nextDouble() * 4.0)).toInt()
                        thunderAmp = 0.6
                        thunderPhase = 0.0
                    }
                } else {
                    thunderTimer--
                    thunderPhase += (2.0 * PI * (45.0 + sin(thunderPhase * 0.1) * 20.0)) / sampleRate
                    thunderAmp *= 0.99992
                    thunder = sin(thunderPhase) * thunderAmp * (0.6 + rand.nextDouble() * 0.4)
                }
            }

            return ((pink * (if (isHeavy) 1.2 else 0.85) + drop + thunder) * 0.65).toFloat()
        }
    }

    private class OceanGenerator(private val sampleRate: Int) {
        private var wavePhase = 0.0
        private val rand = Random()
        private var lp = 0.0

        fun nextSample(): Float {
            // Wave swell LFO (~0.08 Hz)
            wavePhase += (2.0 * PI * 0.08) / sampleRate
            if (wavePhase > 2.0 * PI) wavePhase -= 2.0 * PI
            val swell = (sin(wavePhase) * 0.5 + 0.5) // 0.0 to 1.0

            val white = rand.nextDouble() * 2.0 - 1.0
            // Dynamic low-pass filter modulated by swell
            val alpha = 0.02 + swell * 0.08
            lp += alpha * (white - lp)

            return (lp * (0.3 + swell * 0.7) * 1.5).toFloat()
        }
    }

    private class ForestGenerator(private val sampleRate: Int) {
        private var lp = 0.0
        private var lfo = 0.0
        private val rand = Random()

        fun nextSample(): Float {
            lfo += (2.0 * PI * 0.18) / sampleRate
            val mod = (sin(lfo) * 0.3 + 0.7)
            val white = rand.nextDouble() * 2.0 - 1.0
            lp += 0.03 * (white - lp)

            var rustle = 0.0
            if (rand.nextDouble() < 0.002) {
                rustle = (rand.nextDouble() - 0.5) * 0.25
            }
            return ((lp * mod + rustle) * 0.8).toFloat()
        }
    }

    private class RiverGenerator(private val sampleRate: Int) {
        private var bp1 = 0.0
        private var bp2 = 0.0
        private val rand = Random()

        fun nextSample(): Float {
            val white = rand.nextDouble() * 2.0 - 1.0
            bp1 += 0.08 * (white - bp1)
            bp2 += 0.04 * (white - bp2)
            val water = (bp1 * 0.6 + bp2 * 0.4)
            return (water * 0.9).toFloat()
        }
    }

    private class WindGenerator(private val sampleRate: Int) {
        private var phase = 0.0
        private var centerFreq = 350.0
        private var targetFreq = 350.0
        private var lp = 0.0
        private val rand = Random()
        private var step = 0

        fun nextSample(): Float {
            step++
            if (step % 4410 == 0) { // update target frequency every 100ms
                targetFreq = 180.0 + rand.nextDouble() * 450.0
            }
            centerFreq += (targetFreq - centerFreq) * 0.0005
            val white = rand.nextDouble() * 2.0 - 1.0
            val alpha = (2.0 * PI * centerFreq) / sampleRate
            lp += alpha.coerceIn(0.01, 0.4) * (white - lp)
            return (lp * 0.95).toFloat()
        }
    }

    private class BirdsGenerator(private val sampleRate: Int) {
        private val rand = Random()
        private var chirpTimer = 0
        private var chirpPhase = 0.0
        private var chirpFreq = 3200.0
        private var chirpAmp = 0.0

        fun nextSample(): Float {
            if (chirpTimer <= 0) {
                if (rand.nextDouble() < 0.0002) { // trigger chirp burst
                    chirpTimer = (sampleRate * (0.08 + rand.nextDouble() * 0.15)).toInt()
                    chirpFreq = 2600.0 + rand.nextDouble() * 1400.0
                    chirpAmp = 0.35
                }
            } else {
                chirpTimer--
                chirpFreq += 15.0 * (rand.nextDouble() - 0.4)
                chirpPhase += (2.0 * PI * chirpFreq) / sampleRate
                chirpAmp *= 0.9996
                return (sin(chirpPhase) * chirpAmp).toFloat()
            }
            return 0.0f
        }
    }

    private class FireplaceGenerator(private val sampleRate: Int) {
        private val rand = Random()
        private var rumble = 0.0

        fun nextSample(): Float {
            val white = rand.nextDouble() * 2.0 - 1.0
            rumble += 0.015 * (white - rumble)

            var pop = 0.0
            if (rand.nextDouble() < 0.0007) {
                pop = (rand.nextDouble() * 2.0 - 1.0) * 0.7
            }
            return ((rumble * 0.5 + pop) * 0.85).toFloat()
        }
    }

    private class NightNatureGenerator(private val sampleRate: Int) {
        private var cricketPhase = 0.0
        private var burstPhase = 0.0
        private val rand = Random()
        private var windLp = 0.0

        fun nextSample(): Float {
            cricketPhase += (2.0 * PI * 4600.0) / sampleRate
            burstPhase += (2.0 * PI * 16.0) / sampleRate
            val burst = (sin(burstPhase) * 0.5 + 0.5)
            val cricket = if (burst > 0.4) sin(cricketPhase) * (burst - 0.4) * 0.35 else 0.0

            val white = rand.nextDouble() * 2.0 - 1.0
            windLp += 0.01 * (white - windLp)

            return (cricket * 0.5 + windLp * 0.3).toFloat()
        }
    }

    private class RainyCafeGenerator(private val sampleRate: Int) {
        private var rainLp = 0.0
        private var roomLp = 0.0
        private val rand = Random()

        fun nextSample(): Float {
            val white = rand.nextDouble() * 2.0 - 1.0
            rainLp += 0.04 * (white - rainLp)
            roomLp += 0.01 * (white - roomLp)
            return ((rainLp * 0.6 + roomLp * 0.4) * 0.75).toFloat()
        }
    }

    private class RainWindowGenerator(private val sampleRate: Int) {
        private var backLp = 0.0
        private val rand = Random()

        fun nextSample(): Float {
            val white = rand.nextDouble() * 2.0 - 1.0
            backLp += 0.03 * (white - backLp)

            var tap = 0.0
            if (rand.nextDouble() < 0.003) {
                tap = (rand.nextDouble() - 0.5) * 0.6
            }
            return ((backLp * 0.4 + tap) * 0.8).toFloat()
        }
    }

    private class GardenGenerator(private val sampleRate: Int) {
        private val rand = Random()
        private var chimeTimer = 0
        private var chimePhase = 0.0
        private var chimeFreq = 587.0
        private var chimeAmp = 0.0
        private var breezeLp = 0.0

        private val pentatonic = doubleArrayOf(523.25, 587.33, 659.25, 783.99, 880.00, 1046.50)

        fun nextSample(): Float {
            val white = rand.nextDouble() * 2.0 - 1.0
            breezeLp += 0.02 * (white - breezeLp)

            var chime = 0.0
            if (chimeTimer <= 0) {
                if (rand.nextDouble() < 0.00015) { // gentle chime
                    chimeTimer = (sampleRate * 2.5).toInt()
                    chimeFreq = pentatonic[rand.nextInt(pentatonic.size)]
                    chimeAmp = 0.25
                    chimePhase = 0.0
                }
            } else {
                chimeTimer--
                chimePhase += (2.0 * PI * chimeFreq) / sampleRate
                chimeAmp *= 0.99993
                chime = sin(chimePhase) * chimeAmp
            }

            return ((breezeLp * 0.5 + chime) * 0.8).toFloat()
        }
    }
}
