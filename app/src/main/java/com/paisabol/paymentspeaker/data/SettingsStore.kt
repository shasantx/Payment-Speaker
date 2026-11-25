package com.paisabol.paymentspeaker.data

import android.content.Context
import com.paisabol.paymentspeaker.model.AppSettings
import com.paisabol.paymentspeaker.model.SpeechLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    val current: AppSettings
        get() = _settings.value

    private fun load(): AppSettings = AppSettings(
        onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING, false),
        speakerEnabled = prefs.getBoolean(KEY_SPEAKER_ENABLED, true),
        speechLanguage = SpeechLanguage.fromName(prefs.getString(KEY_LANGUAGE, SpeechLanguage.NEPALI.name)),
        announceSourceApp = prefs.getBoolean(KEY_ANNOUNCE_SOURCE, true),
        autoBoostVolume = prefs.getBoolean(KEY_AUTO_BOOST_VOLUME, true),
        speechRate = prefs.getFloat(KEY_SPEECH_RATE, 0.95f)
    )

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, completed).apply()
        _settings.value = _settings.value.copy(onboardingCompleted = completed)
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPEAKER_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(speakerEnabled = enabled)
    }

    fun setSpeechLanguage(language: SpeechLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _settings.value = _settings.value.copy(speechLanguage = language)
    }

    fun setAnnounceSourceApp(announce: Boolean) {
        prefs.edit().putBoolean(KEY_ANNOUNCE_SOURCE, announce).apply()
        _settings.value = _settings.value.copy(announceSourceApp = announce)
    }

    fun setAutoBoostVolume(boost: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_BOOST_VOLUME, boost).apply()
        _settings.value = _settings.value.copy(autoBoostVolume = boost)
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.65f, 1.35f)
        prefs.edit().putFloat(KEY_SPEECH_RATE, clamped).apply()
        _settings.value = _settings.value.copy(speechRate = clamped)
    }

    companion object {
        private const val PREFS_NAME = "paisabol_settings"
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val KEY_SPEAKER_ENABLED = "speaker_enabled"
        private const val KEY_LANGUAGE = "speech_language"
        private const val KEY_ANNOUNCE_SOURCE = "announce_source_app"
        private const val KEY_AUTO_BOOST_VOLUME = "auto_boost_volume"
        private const val KEY_SPEECH_RATE = "speech_rate"
    }
}
