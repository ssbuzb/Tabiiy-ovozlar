package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.example.data.SoundCatalog
import com.example.data.model.SleepTimerState
import com.example.data.model.SoundId
import com.example.data.model.SoundPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class NatureAudioEngine(private val context: Context) {

    private val sampleRate = 44100
    private val bufferSize = 2048
    private val synthesizer = ProceduralSoundSynthesizer(sampleRate)

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private var timerJob: Job? = null
    private val engineScope = CoroutineScope(Dispatchers.Default)

    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _activeSoundVolumes = MutableStateFlow<Map<SoundId, Float>>(
        mapOf(SoundId.GENTLE_RAIN to 0.75f)
    )
    val activeSoundVolumes: StateFlow<Map<SoundId, Float>> = _activeSoundVolumes.asStateFlow()

    private val _masterVolume = MutableStateFlow(0.8f)
    val masterVolume: StateFlow<Float> = _masterVolume.asStateFlow()

    private val _currentSoundOrPresetName = MutableStateFlow("Gentle Rain")
    val currentSoundOrPresetName: StateFlow<String> = _currentSoundOrPresetName.asStateFlow()

    private val _timerState = MutableStateFlow(SleepTimerState())
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    // Real-time amplitude for ambient visualizer (0.0 to 1.0)
    private val _audioVisualizerAmp = MutableStateFlow(0.0f)
    val audioVisualizerAmp: StateFlow<Float> = _audioVisualizerAmp.asStateFlow()

    private var fadeMultiplier = 1.0f
    private var audioFocusDuckingMultiplier = 1.0f

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                audioFocusDuckingMultiplier = 0.3f
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                audioFocusDuckingMultiplier = 1.0f
                if (!isPlaying.value) {
                    resume()
                }
            }
        }
    }

    fun playSingleSound(soundId: SoundId, volume: Float = 0.75f) {
        _activeSoundVolumes.value = mapOf(soundId to volume)
        val sound = SoundCatalog.getSound(soundId)
        _currentSoundOrPresetName.value = sound.iconEmoji + " " + sound.id.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        startPlayback()
    }

    fun applyPreset(preset: SoundPreset) {
        _activeSoundVolumes.value = preset.volumes
        _currentSoundOrPresetName.value = preset.name
        startPlayback()
    }

    fun setLayerVolume(soundId: SoundId, volume: Float) {
        val current = _activeSoundVolumes.value.toMutableMap()
        if (volume <= 0.001f) {
            current.remove(soundId)
        } else {
            current[soundId] = volume.coerceIn(0.0f, 1.0f)
        }
        _activeSoundVolumes.value = current

        if (current.isEmpty()) {
            pause()
        } else if (!_isPlaying.value) {
            startPlayback()
        }
    }

    fun setMasterVolume(volume: Float) {
        _masterVolume.value = volume.coerceIn(0.0f, 1.0f)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            resume()
        }
    }

    fun startPlayback() {
        requestAudioFocus()
        _isPlaying.value = true
        fadeMultiplier = 1.0f

        if (playbackJob?.isActive != true) {
            startAudioTrackThread()
        }
    }

    fun pause() {
        _isPlaying.value = false
        _audioVisualizerAmp.value = 0.0f
        audioTrack?.pause()
    }

    fun resume() {
        if (_activeSoundVolumes.value.isEmpty()) {
            _activeSoundVolumes.value = mapOf(SoundId.GENTLE_RAIN to 0.75f)
        }
        startPlayback()
    }

    fun stop() {
        _isPlaying.value = false
        _audioVisualizerAmp.value = 0.0f
        cancelTimer()
        abandonAudioFocus()
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioTrack = null
    }

    // --- Sleep Timer & Fade Out ---

    fun setSleepTimer(minutes: Int, fadeOut: Boolean = true) {
        timerJob?.cancel()
        val totalSecs = minutes * 60
        _timerState.value = SleepTimerState(
            isActive = true,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            isFadeOutEnabled = fadeOut
        )

        timerJob = engineScope.launch {
            var remaining = totalSecs
            // Fade out during last 5 minutes (or 25% of total time if < 5m)
            val fadeThresholdSecs = minOf(300, totalSecs / 2)

            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _timerState.value = _timerState.value.copy(remainingSeconds = remaining)

                if (fadeOut && remaining <= fadeThresholdSecs) {
                    fadeMultiplier = (remaining.toFloat() / fadeThresholdSecs.toFloat()).coerceIn(0.0f, 1.0f)
                } else {
                    fadeMultiplier = 1.0f
                }
            }

            if (remaining <= 0) {
                stop()
                _timerState.value = SleepTimerState(isActive = false)
            }
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        fadeMultiplier = 1.0f
        _timerState.value = SleepTimerState(isActive = false)
    }

    // --- Audio Track Core Loop ---

    private fun startAudioTrackThread() {
        val minBufSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackBufSize = maxOf(minBufSize, bufferSize * 2)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackBufSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        playbackJob = engineScope.launch {
            val buffer = ShortArray(bufferSize)

            while (isActive) {
                if (_isPlaying.value) {
                    val activeMap = _activeSoundVolumes.value
                    val master = _masterVolume.value * audioFocusDuckingMultiplier

                    synthesizer.fillBuffer(buffer, activeMap, master, fadeMultiplier)

                    audioTrack?.write(buffer, 0, buffer.size)

                    // Compute RMS amplitude for ambient visualizer
                    var sum = 0.0
                    for (i in 0 until buffer.size step 16) {
                        val s = buffer[i] / 32768.0
                        sum += s * s
                    }
                    val rms = kotlin.math.sqrt(sum / (buffer.size / 16)).toFloat()
                    _audioVisualizerAmp.value = (_audioVisualizerAmp.value * 0.7f + rms * 2.5f * 0.3f).coerceIn(0.05f, 1.0f)
                } else {
                    _audioVisualizerAmp.value = 0.0f
                    delay(50)
                }
            }
        }
    }

    private fun requestAudioFocus() {
        try {
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun abandonAudioFocus() {
        try {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        } catch (e: Exception) {
            // ignore
        }
    }
}
