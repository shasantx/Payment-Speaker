package com.paisabol.paymentspeaker

import android.app.Application
import com.paisabol.paymentspeaker.data.PaymentDatabase
import com.paisabol.paymentspeaker.data.SettingsStore
import com.paisabol.paymentspeaker.service.NepaliSpeechEngine

class PaisaBolApp : Application() {

    lateinit var paymentDatabase: PaymentDatabase
        private set
    lateinit var settingsStore: SettingsStore
        private set
    lateinit var speechEngine: NepaliSpeechEngine
        private set

    override fun onCreate() {
        super.onCreate()
        paymentDatabase = PaymentDatabase(this)
        settingsStore = SettingsStore(this)
        speechEngine = NepaliSpeechEngine(this)
    }
}
