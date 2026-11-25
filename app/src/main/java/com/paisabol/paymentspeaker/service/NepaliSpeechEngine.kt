package com.paisabol.paymentspeaker.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.paisabol.paymentspeaker.model.SpeechLanguage
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

class NepaliSpeechEngine(context: Context) : TextToSpeech.OnInitListener {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var tts: TextToSpeech? = null
    @Volatile
    private var initialized = false

    private data class QueuedSpeech(
        val text: String,
        val language: SpeechLanguage,
        val speechRate: Float,
        val boostVolume: Boolean,
        val flush: Boolean
    )

    private val pendingQueue = ConcurrentLinkedQueue<QueuedSpeech>()
    private val activeUtteranceIds = mutableSetOf<String>()
    private var previousVolume: Int? = null
    private var activeFocusRequest: AudioFocusRequest? = null
    private var activeWakeLock: PowerManager.WakeLock? = null

    init {
        tts = TextToSpeech(appContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    onUtteranceFinished(utteranceId)
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    onUtteranceFinished(utteranceId)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onUtteranceFinished(utteranceId)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    onUtteranceFinished(utteranceId)
                }
            })
            initialized = true
            drainQueue()
        } else {
            initialized = false
            tts = null
            pendingQueue.clear()
        }
    }

    fun speak(
        text: String,
        language: SpeechLanguage,
        speechRate: Float = 0.95f,
        boostVolume: Boolean = true,
        flush: Boolean = false
    ) {
        if (text.isBlank()) return
        val item = QueuedSpeech(text, language, speechRate, boostVolume, flush)
        if (!initialized) {
            if (flush) pendingQueue.clear()
            while (pendingQueue.size >= MAX_PENDING_ITEMS) {
                pendingQueue.poll()
            }
            pendingQueue.add(item)
            if (tts == null) {
                tts = TextToSpeech(appContext, this)
            }
            return
        }
        speakInternal(item)
    }

    private fun drainQueue() {
        while (true) {
            val next = pendingQueue.poll() ?: break
            speakInternal(next)
        }
    }

    @Synchronized
    private fun speakInternal(item: QueuedSpeech) {
        val engine = tts ?: return

        val effectiveText = configureLocaleAndGetText(engine, item.text, item.language)
        engine.setSpeechRate(item.speechRate.coerceIn(0.65f, 1.35f))
        engine.setPitch(1.0f)

        val utteranceId = "pb_${UUID.randomUUID()}"
        if (item.flush) {
            activeUtteranceIds.clear()
        }
        activeUtteranceIds.add(utteranceId)
        acquireAudioAndWakeLock(item.boostVolume)

        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val queueMode = if (item.flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val result = engine.speak(effectiveText, queueMode, params, utteranceId)
        if (result == TextToSpeech.ERROR) {
            onUtteranceFinished(utteranceId)
        }
    }

    @Synchronized
    private fun onUtteranceFinished(utteranceId: String?) {
        if (utteranceId != null) {
            activeUtteranceIds.remove(utteranceId)
        } else {
            activeUtteranceIds.clear()
        }
        if (activeUtteranceIds.isEmpty()) {
            releaseAudioAndWakeLock()
        }
    }

    private fun configureLocaleAndGetText(
        engine: TextToSpeech,
        text: String,
        language: SpeechLanguage
    ): String {
        val nepaliLocale = Locale("ne", "NP")
        val hindiLocale = Locale("hi", "IN")
        val indianEnglishLocale = Locale("en", "IN")

        return when (language) {
            SpeechLanguage.NEPALI -> {
                when {
                    isLocaleUsable(engine, nepaliLocale) -> {
                        engine.language = nepaliLocale
                        text
                    }
                    isLocaleUsable(engine, hindiLocale) -> {
                        engine.language = hindiLocale
                        text
                    }
                    else -> {
                        engine.language = if (isLocaleUsable(engine, indianEnglishLocale)) {
                            indianEnglishLocale
                        } else {
                            Locale.US
                        }
                        transliterateBasicNepaliFallback(text)
                    }
                }
            }
            SpeechLanguage.ROMAN_NEPALI -> {
                engine.language = when {
                    isLocaleUsable(engine, indianEnglishLocale) -> indianEnglishLocale
                    else -> Locale.US
                }
                text
            }
            SpeechLanguage.ENGLISH -> {
                engine.language = when {
                    isLocaleUsable(engine, indianEnglishLocale) -> indianEnglishLocale
                    else -> Locale.US
                }
                text
            }
        }
    }

    private fun isLocaleUsable(engine: TextToSpeech, locale: Locale): Boolean {
        val result = engine.isLanguageAvailable(locale)
        return result == TextToSpeech.LANG_AVAILABLE ||
            result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
            result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
    }

    private fun transliterateBasicNepaliFallback(devanagari: String): String {
        val asciiDigits = com.paisabol.paymentspeaker.domain.PaymentParser.convertDevanagariDigits(devanagari)
        return asciiDigits
            .replace("ई-सेवा मार्फत", "eSewa bata")
            .replace("खल्ती मार्फत", "Khalti bata")
            .replace("आइएमई पे मार्फत", "IME Pay bata")
            .replace("फोनपे मार्फत", "Fonepay bata")
            .replace("कनेक्ट आइपिएस मार्फत", "ConnectIPS bata")
            .replace("बैंक मार्फत", "Bank bata")
            .replace("रुपैयाँ", "rupaiya")
            .replace("पैसा", "paisa")
            .replace("प्राप्त भयो", "prapta bhayo")
    }

    @Synchronized
    private fun acquireAudioAndWakeLock(boostVolume: Boolean) {
        if (activeWakeLock == null || activeWakeLock?.isHeld == false) {
            activeWakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "PaisaBol::PaymentSpeakerWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(15_000L)
            }
        } else {
            // Extend timeout when consecutive payments queue back-to-back
            runCatching { activeWakeLock?.acquire(15_000L) }
        }

        if (activeFocusRequest == null) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attrs)
                .setWillPauseWhenDucked(false)
                .build()
            activeFocusRequest = request
            runCatching { audioManager.requestAudioFocus(request) }
        }

        if (boostVolume && previousVolume == null) {
            runCatching {
                val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val target = (max * 0.90f).toInt().coerceAtLeast(current)
                if (target > current) {
                    previousVolume = current
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
                }
            }
        }
    }

    @Synchronized
    private fun releaseAudioAndWakeLock() {
        previousVolume?.let { prev ->
            runCatching {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, prev, 0)
            }
            previousVolume = null
        }
        activeFocusRequest?.let { req ->
            runCatching { audioManager.abandonAudioFocusRequest(req) }
            activeFocusRequest = null
        }
        activeWakeLock?.let { wl ->
            if (wl.isHeld) {
                runCatching { wl.release() }
            }
            activeWakeLock = null
        }
    }

    fun shutdown() {
        activeUtteranceIds.clear()
        releaseAudioAndWakeLock()
        tts?.stop()
        tts?.shutdown()
        tts = null
        initialized = false
    }

    companion object {
        private const val MAX_PENDING_ITEMS = 10
    }
}
