package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppLanguage
import com.example.data.model.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class NaturePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nature_calm_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isBatterySaver = MutableStateFlow(prefs.getBoolean(KEY_BATTERY_SAVER, false))
    val isBatterySaver: StateFlow<Boolean> = _isBatterySaver.asStateFlow()

    private val _isAnimationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_ANIMATIONS, true))
    val isAnimationsEnabled: StateFlow<Boolean> = _isAnimationsEnabled.asStateFlow()

    private val _defaultVolume = MutableStateFlow(prefs.getFloat(KEY_DEFAULT_VOLUME, 0.75f))
    val defaultVolume: StateFlow<Float> = _defaultVolume.asStateFlow()

    private val _defaultTimerMinutes = MutableStateFlow(prefs.getInt(KEY_DEFAULT_TIMER, 30))
    val defaultTimerMinutes: StateFlow<Int> = _defaultTimerMinutes.asStateFlow()

    private val _isFadeOutEnabled = MutableStateFlow(prefs.getBoolean(KEY_FADE_OUT, true))
    val isFadeOutEnabled: StateFlow<Boolean> = _isFadeOutEnabled.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private fun loadLanguage(): AppLanguage {
        val saved = prefs.getString(KEY_LANGUAGE, null)
        if (saved != null) {
            return AppLanguage.values().find { it.code == saved } ?: AppLanguage.SYSTEM
        }
        // First launch detection: if device language is Uzbek, default to Uzbek, else System
        val deviceLang = Locale.getDefault().language
        return if (deviceLang.startsWith("uz")) {
            AppLanguage.UZBEK
        } else if (deviceLang.startsWith("ru")) {
            AppLanguage.RUSSIAN
        } else {
            AppLanguage.ENGLISH
        }
    }

    private fun loadThemeMode(): AppThemeMode {
        val saved = prefs.getString(KEY_THEME, AppThemeMode.SYSTEM.name)
        return try {
            AppThemeMode.valueOf(saved ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun setLanguage(lang: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, lang.code).apply()
        _language.value = lang
    }

    fun setThemeMode(theme: AppThemeMode) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _themeMode.value = theme
    }

    fun setBatterySaver(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BATTERY_SAVER, enabled).apply()
        _isBatterySaver.value = enabled
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _isAnimationsEnabled.value = enabled
    }

    fun setDefaultVolume(volume: Float) {
        prefs.edit().putFloat(KEY_DEFAULT_VOLUME, volume).apply()
        _defaultVolume.value = volume
    }

    fun setDefaultTimerMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_DEFAULT_TIMER, minutes).apply()
        _defaultTimerMinutes.value = minutes
    }

    fun setFadeOutEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FADE_OUT, enabled).apply()
        _isFadeOutEnabled.value = enabled
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, completed).apply()
        _isOnboardingCompleted.value = completed
    }

    companion object {
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_THEME = "app_theme"
        private const val KEY_BATTERY_SAVER = "battery_saver"
        private const val KEY_ANIMATIONS = "animations_enabled"
        private const val KEY_DEFAULT_VOLUME = "default_volume"
        private const val KEY_DEFAULT_TIMER = "default_timer"
        private const val KEY_FADE_OUT = "fade_out_enabled"
        private const val KEY_ONBOARDING = "onboarding_completed"
    }
}
