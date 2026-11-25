package com.paisabol.paymentspeaker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.paisabol.paymentspeaker.PaisaBolApp
import com.paisabol.paymentspeaker.util.PermissionHelper

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as? PaisaBolApp ?: return
            if (app.settingsStore.current.speakerEnabled &&
                PermissionHelper.isNotificationListenerEnabled(context)
            ) {
                PaymentSpeakerForegroundService.start(context)
            }
        }
    }
}
