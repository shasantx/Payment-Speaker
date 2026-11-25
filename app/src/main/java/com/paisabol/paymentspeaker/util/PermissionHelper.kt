package com.paisabol.paymentspeaker.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.paisabol.paymentspeaker.service.PaymentNotificationListener

data class PermissionStatus(
    val notificationAccessGranted: Boolean,
    val postNotificationGranted: Boolean,
    val batteryOptimizationIgnored: Boolean
) {
    val coreReady: Boolean
        get() = notificationAccessGranted
}

object PermissionHelper {

    fun checkStatus(context: Context): PermissionStatus = PermissionStatus(
        notificationAccessGranted = isNotificationListenerEnabled(context),
        postNotificationGranted = isPostNotificationsGranted(context),
        batteryOptimizationIgnored = isIgnoringBatteryOptimizations(context)
    )

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            ?: return false
        val expected = ComponentName(context, PaymentNotificationListener::class.java).flattenToString()
        val expectedShort = ComponentName(context, PaymentNotificationListener::class.java).flattenToShortString()
        return flat.split(":").any { entry ->
            entry.equals(expected, ignoreCase = true) ||
                entry.equals(expectedShort, ignoreCase = true) ||
                entry.contains(context.packageName, ignoreCase = true)
        }
    }

    fun isPostNotificationsGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun openNotificationListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(fallback) }
        }
    }

    @SuppressLint("BatteryLife")
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(fallback) }
        }
    }

    fun openTtsSettings(context: Context) {
        val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }.onFailure {
            val fallback = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            runCatching { context.startActivity(fallback) }.onFailure {
                val generic = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(generic) }
            }
        }
    }
}
