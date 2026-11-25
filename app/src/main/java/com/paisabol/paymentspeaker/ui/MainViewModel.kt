package com.paisabol.paymentspeaker.ui

import android.app.Application
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paisabol.paymentspeaker.PaisaBolApp
import com.paisabol.paymentspeaker.domain.NepaliSpeechFormatter
import com.paisabol.paymentspeaker.model.AppSettings
import com.paisabol.paymentspeaker.model.PaymentRecord
import com.paisabol.paymentspeaker.model.PaymentSource
import com.paisabol.paymentspeaker.model.SpeechLanguage
import com.paisabol.paymentspeaker.service.PaymentNotificationListener
import com.paisabol.paymentspeaker.service.PaymentSpeakerForegroundService
import com.paisabol.paymentspeaker.util.PermissionHelper
import com.paisabol.paymentspeaker.util.PermissionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class TodaySummary(
    val totalAmount: Double = 0.0,
    val count: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PaisaBolApp
    private val settingsStore = app.settingsStore
    private val database = app.paymentDatabase
    private val speechEngine = app.speechEngine

    val settings: StateFlow<AppSettings> = settingsStore.settings
    val payments: StateFlow<List<PaymentRecord>> = database.payments

    private val _permissionStatus = MutableStateFlow(PermissionHelper.checkStatus(application))
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    private val dayRefreshTrigger = MutableStateFlow(startOfTodayMillis())

    val todaySummary: StateFlow<TodaySummary> = combine(payments, dayRefreshTrigger) { list, startOfDay ->
        val todays = list.filter { it.timestampMillis >= startOfDay }
        TodaySummary(
            totalAmount = todays.sumOf { it.amount },
            count = todays.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodaySummary()
    )

    fun refreshPermissions() {
        dayRefreshTrigger.value = startOfTodayMillis()
        val status = PermissionHelper.checkStatus(app)
        _permissionStatus.value = status
        syncForegroundService(settings.value.speakerEnabled, status)
    }

    fun completeOnboarding() {
        settingsStore.setOnboardingCompleted(true)
        dayRefreshTrigger.value = startOfTodayMillis()
        val status = PermissionHelper.checkStatus(app)
        _permissionStatus.value = status
        syncForegroundService(settings.value.speakerEnabled, status)
    }

    fun toggleSpeaker() {
        setSpeakerEnabled(!settings.value.speakerEnabled)
    }

    fun setSpeakerEnabled(enabled: Boolean) {
        settingsStore.setSpeakerEnabled(enabled)
        if (!enabled) {
            speechEngine.stopSpeaking()
        }
        val status = PermissionHelper.checkStatus(app)
        _permissionStatus.value = status
        syncForegroundService(enabled, status)
    }

    fun cycleLanguage() {
        val all = SpeechLanguage.entries
        val nextIndex = (all.indexOf(settings.value.speechLanguage) + 1) % all.size
        settingsStore.setSpeechLanguage(all[nextIndex])
    }

    fun setSpeechLanguage(language: SpeechLanguage) {
        settingsStore.setSpeechLanguage(language)
    }

    fun setAnnounceSourceApp(announce: Boolean) {
        settingsStore.setAnnounceSourceApp(announce)
    }

    fun toggleAutoBoostVolume() {
        settingsStore.setAutoBoostVolume(!settings.value.autoBoostVolume)
    }

    fun setSpeechRate(rate: Float) {
        settingsStore.setSpeechRate(rate)
    }

    /**
     * Audio-only voice check so users can verify speaker volume and Nepali TTS clarity.
     * Never writes to the verified payment ledger.
     */
    fun testVoiceOnly() {
        val current = settings.value
        val sampleText = NepaliSpeechFormatter.buildAnnouncement(
            amount = 500.0,
            source = PaymentSource.ESEWA,
            sourceLabel = "eSewa",
            language = current.speechLanguage,
            includeSourceApp = current.announceSourceApp
        )
        speechEngine.speak(
            text = sampleText,
            language = current.speechLanguage,
            speechRate = current.speechRate,
            boostVolume = current.autoBoostVolume,
            flush = true
        )
    }

    fun replayPayment(record: PaymentRecord) {
        val current = settings.value
        val announcement = NepaliSpeechFormatter.buildAnnouncement(
            amount = record.amount,
            source = record.source,
            sourceLabel = record.sourceLabel,
            language = current.speechLanguage,
            includeSourceApp = current.announceSourceApp
        )
        speechEngine.speak(
            text = announcement,
            language = current.speechLanguage,
            speechRate = current.speechRate,
            boostVolume = current.autoBoostVolume,
            flush = true
        )
    }

    fun deletePayment(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.deleteById(id)
        }
    }

    fun clearAllPayments() {
        viewModelScope.launch(Dispatchers.IO) {
            database.clearAll()
        }
    }

    private fun syncForegroundService(enabled: Boolean, status: PermissionStatus) {
        if (enabled && status.notificationAccessGranted) {
            PaymentSpeakerForegroundService.start(app)
            runCatching {
                NotificationListenerService.requestRebind(
                    ComponentName(app, PaymentNotificationListener::class.java)
                )
            }
        } else {
            PaymentSpeakerForegroundService.stop(app)
        }
    }

    private fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}
