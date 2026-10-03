package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiRelaxationService
import com.example.audio.NatureAudioEngine
import com.example.data.SoundCatalog
import com.example.data.SoundscapeRepository
import com.example.data.local.NatureCalmDatabase
import com.example.data.local.NaturePreferences
import com.example.data.model.AiSoundscapeResponse
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import com.example.data.model.SleepTimerState
import com.example.data.model.SoundId
import com.example.data.model.SoundPreset
import com.example.service.SoundscapePlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface AiAlchemistUiState {
    object Idle : AiAlchemistUiState
    object Generating : AiAlchemistUiState
    data class Success(val response: AiSoundscapeResponse) : AiAlchemistUiState
    data class Error(val message: String) : AiAlchemistUiState
}

class NatureCalmViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = NatureCalmDatabase.getInstance(context)
    val repository = SoundscapeRepository(database)
    val preferences = NaturePreferences(context)
    val audioEngine = NatureAudioEngine(context)
    private val geminiService = GeminiRelaxationService()

    // Preferences & Settings
    val appLanguage: StateFlow<AppLanguage> = preferences.language
    val appThemeMode: StateFlow<AppThemeMode> = preferences.themeMode
    val isBatterySaver: StateFlow<Boolean> = preferences.isBatterySaver
    val isAnimationsEnabled: StateFlow<Boolean> = preferences.isAnimationsEnabled
    val isOnboardingCompleted: StateFlow<Boolean> = preferences.isOnboardingCompleted

    // Audio State
    val isPlaying: StateFlow<Boolean> = audioEngine.isPlaying
    val activeSoundVolumes: StateFlow<Map<SoundId, Float>> = audioEngine.activeSoundVolumes
    val masterVolume: StateFlow<Float> = audioEngine.masterVolume
    val currentSoundOrPresetName: StateFlow<String> = audioEngine.currentSoundOrPresetName
    val timerState: StateFlow<SleepTimerState> = audioEngine.timerState
    val visualizerAmp: StateFlow<Float> = audioEngine.audioVisualizerAmp

    // Room Data
    val customPresets: StateFlow<List<SoundPreset>> = repository.customPresets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favorites: StateFlow<Set<String>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    // Primary Sound for dynamic screen theme/background
    private val _primarySoundId = MutableStateFlow(SoundId.GENTLE_RAIN)
    val primarySoundId: StateFlow<SoundId> = _primarySoundId.asStateFlow()

    // AI State
    private val _aiState = MutableStateFlow<AiAlchemistUiState>(AiAlchemistUiState.Idle)
    val aiState: StateFlow<AiAlchemistUiState> = _aiState.asStateFlow()

    // Greeting based on time of day
    val greetingRes: Int
        get() {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 5..11 -> com.example.R.string.greeting_morning
                in 12..16 -> com.example.R.string.greeting_afternoon
                in 17..21 -> com.example.R.string.greeting_evening
                else -> com.example.R.string.greeting_night
            }
        }

    val recommendedSound = SoundCatalog.getRecommendedSoundForHour(
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    )

    init {
        // Sync foreground service state with audio playback
        viewModelScope.launch {
            isPlaying.collect { playing ->
                if (playing) {
                    SoundscapePlaybackService.start(
                        context,
                        currentSoundOrPresetName.value,
                        true
                    )
                } else {
                    SoundscapePlaybackService.update(
                        context,
                        currentSoundOrPresetName.value,
                        false
                    )
                }
            }
        }

        viewModelScope.launch {
            currentSoundOrPresetName.collect { name ->
                if (isPlaying.value) {
                    SoundscapePlaybackService.update(context, name, true)
                }
            }
        }

        viewModelScope.launch {
            activeSoundVolumes.collect { vols ->
                val highest = vols.maxByOrNull { it.value }
                if (highest != null) {
                    _primarySoundId.value = highest.key
                }
            }
        }
    }

    fun togglePlayPause() {
        audioEngine.togglePlayPause()
    }

    fun playSingleSound(soundId: SoundId, volume: Float = 0.75f) {
        _primarySoundId.value = soundId
        audioEngine.playSingleSound(soundId, volume)
    }

    fun applyPreset(preset: SoundPreset) {
        val prominent = preset.volumes.maxByOrNull { it.value }?.key ?: SoundId.GENTLE_RAIN
        _primarySoundId.value = prominent
        audioEngine.applyPreset(preset)
    }

    fun setLayerVolume(soundId: SoundId, volume: Float) {
        audioEngine.setLayerVolume(soundId, volume)
    }

    fun setMasterVolume(volume: Float) {
        audioEngine.setMasterVolume(volume)
        preferences.setDefaultVolume(volume)
    }

    fun toggleFavorite(itemId: String, itemType: String = "SOUND") {
        viewModelScope.launch {
            repository.toggleFavorite(itemId, itemType)
        }
    }

    fun saveCurrentMixAsPreset(name: String, iconEmoji: String = "✨") {
        viewModelScope.launch {
            val volumes = activeSoundVolumes.value
            if (volumes.isNotEmpty()) {
                repository.saveCustomPreset(name, volumes, iconEmoji)
            }
        }
    }

    fun deleteCustomPreset(presetId: String) {
        viewModelScope.launch {
            repository.deleteCustomPreset(presetId)
        }
    }

    fun setSleepTimer(minutes: Int, fadeOut: Boolean = true) {
        audioEngine.setSleepTimer(minutes, fadeOut)
        preferences.setDefaultTimerMinutes(minutes)
        preferences.setFadeOutEnabled(fadeOut)
    }

    fun cancelSleepTimer() {
        audioEngine.cancelTimer()
    }

    fun askAiSoundscape(userPrompt: String) {
        _aiState.value = AiAlchemistUiState.Generating
        viewModelScope.launch {
            val result = geminiService.generateSoundscape(userPrompt, appLanguage.value)
            result.fold(
                onSuccess = { response ->
                    _aiState.value = AiAlchemistUiState.Success(response)
                },
                onFailure = { error ->
                    _aiState.value = AiAlchemistUiState.Error(error.localizedMessage ?: "Unknown error")
                }
            )
        }
    }

    fun resetAiState() {
        _aiState.value = AiAlchemistUiState.Idle
    }

    fun applyAiSoundscape(response: AiSoundscapeResponse) {
        val preset = SoundPreset(
            id = "ai_${System.currentTimeMillis()}",
            name = response.title,
            volumes = response.volumes,
            isCustom = true,
            iconEmoji = "✨"
        )
        applyPreset(preset)
        if (response.recommendedTimerMinutes > 0) {
            setSleepTimer(response.recommendedTimerMinutes, fadeOut = true)
        }
    }

    fun setLanguage(language: AppLanguage) {
        preferences.setLanguage(language)
    }

    fun setThemeMode(themeMode: AppThemeMode) {
        preferences.setThemeMode(themeMode)
    }

    fun setBatterySaver(enabled: Boolean) {
        preferences.setBatterySaver(enabled)
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        preferences.setAnimationsEnabled(enabled)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        preferences.setOnboardingCompleted(completed)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
        SoundscapePlaybackService.stop(context)
    }
}
