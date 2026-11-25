package com.paisabol.paymentspeaker.service

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.paisabol.paymentspeaker.PaisaBolApp
import com.paisabol.paymentspeaker.domain.NepaliSpeechFormatter
import com.paisabol.paymentspeaker.domain.PaymentDeduplicator
import com.paisabol.paymentspeaker.domain.PaymentParser
import com.paisabol.paymentspeaker.model.PaymentRecord
import java.util.concurrent.Executors

class PaymentNotificationListener : NotificationListenerService() {

    private val deduplicator = PaymentDeduplicator(windowMillis = 10_000L)
    private val dbExecutor = Executors.newSingleThreadExecutor()

    override fun onListenerConnected() {
        super.onListenerConnected()
        val app = applicationContext as? PaisaBolApp ?: return
        if (app.settingsStore.current.speakerEnabled) {
            PaymentSpeakerForegroundService.start(app)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        runCatching {
            requestRebind(ComponentName(this, PaymentNotificationListener::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dbExecutor.shutdown()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val packageName = sbn.packageName ?: return

        // Skip ongoing or group summary notifications to avoid duplicates or persistent status alerts
        if (sbn.isOngoing) return
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

        val extras = notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val shortText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.mapNotNull { it?.toString()?.trim()?.takeIf { s -> s.isNotEmpty() } }
            .orEmpty()

        val candidates = buildList {
            if (!bigText.isNullOrBlank()) add(bigText)
            if (!shortText.isNullOrBlank() && shortText != bigText) add(shortText)
            if (textLines.isNotEmpty()) add(textLines.last())
        }

        val parsed = candidates.firstNotNullOfOrNull { body ->
            PaymentParser.parse(packageName, title, body)
        } ?: return

        if (deduplicator.isDuplicate(parsed)) return

        val app = applicationContext as? PaisaBolApp ?: return
        val settings = app.settingsStore.current

        val announcement = NepaliSpeechFormatter.buildAnnouncement(
            amount = parsed.amount,
            source = parsed.source,
            sourceLabel = parsed.sourceLabel,
            language = settings.speechLanguage,
            includeSourceApp = settings.announceSourceApp
        )

        val record = PaymentRecord(
            amount = parsed.amount,
            source = parsed.source,
            sourceLabel = parsed.sourceLabel,
            senderName = parsed.senderName,
            packageName = parsed.packageName,
            rawTitle = parsed.rawTitle,
            rawText = parsed.rawText,
            spokenText = announcement,
            timestampMillis = System.currentTimeMillis()
        )

        // Speak immediately so the shopkeeper hears the payment without waiting on SQLite I/O
        if (settings.speakerEnabled) {
            app.speechEngine.speak(
                text = announcement,
                language = settings.speechLanguage,
                speechRate = settings.speechRate,
                boostVolume = settings.autoBoostVolume,
                flush = false
            )
        }

        dbExecutor.execute {
            runCatching { app.paymentDatabase.insert(record) }
        }
    }
}
