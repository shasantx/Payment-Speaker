package com.paisabol.paymentspeaker.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SettingsVoice
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paisabol.paymentspeaker.domain.NepaliSpeechFormatter
import com.paisabol.paymentspeaker.model.AppSettings
import com.paisabol.paymentspeaker.model.PaymentRecord
import com.paisabol.paymentspeaker.model.SpeechLanguage
import com.paisabol.paymentspeaker.ui.TodaySummary
import com.paisabol.paymentspeaker.ui.components.HeroSpeakerCard
import com.paisabol.paymentspeaker.ui.components.PaisaBolMark
import com.paisabol.paymentspeaker.ui.components.PaymentListRow
import com.paisabol.paymentspeaker.ui.components.PrimaryActionButton
import com.paisabol.paymentspeaker.ui.components.QuickActionTile
import com.paisabol.paymentspeaker.ui.components.TopIconButton
import com.paisabol.paymentspeaker.ui.theme.BgCanvas
import com.paisabol.paymentspeaker.ui.theme.BrassAccent
import com.paisabol.paymentspeaker.ui.theme.BrassTint
import com.paisabol.paymentspeaker.ui.theme.Hairline
import com.paisabol.paymentspeaker.ui.theme.InkMuted
import com.paisabol.paymentspeaker.ui.theme.InkPrimary
import com.paisabol.paymentspeaker.ui.theme.InkSecondary
import com.paisabol.paymentspeaker.ui.theme.PositiveMint
import com.paisabol.paymentspeaker.ui.theme.SurfaceCard
import com.paisabol.paymentspeaker.ui.theme.SurfaceRaised
import com.paisabol.paymentspeaker.util.PermissionHelper
import com.paisabol.paymentspeaker.util.PermissionStatus
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerHomeScreen(
    settings: AppSettings,
    todaySummary: TodaySummary,
    recentPayments: List<PaymentRecord>,
    permissionStatus: PermissionStatus,
    onSetSpeakerEnabled: (Boolean) -> Unit,
    onTestVoice: () -> Unit,
    onToggleVolumeBoost: () -> Unit,
    onCycleLanguage: () -> Unit,
    onSelectLanguage: (SpeechLanguage) -> Unit,
    onToggleAnnounceSource: (Boolean) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onReplayPayment: (PaymentRecord) -> Unit,
    onSeeAllPayments: () -> Unit,
    onRefreshPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showVoiceSheet by remember { mutableStateOf(false) }

    val postNotifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { onRefreshPermissions() }

    val isLive = settings.speakerEnabled && permissionStatus.notificationAccessGranted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speaker",
                    style = MaterialTheme.typography.headlineLarge
                )
                TopIconButton(
                    icon = Icons.Rounded.Tune,
                    contentDescription = "Voice and permissions",
                    active = showVoiceSheet,
                    onClick = { showVoiceSheet = true }
                )
            }
        }

        // Inline permission prompt only when notification access is missing
        if (!permissionStatus.notificationAccessGranted) {
            item {
                val shape = RoundedCornerShape(12.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(SurfaceCard)
                        .border(width = 1.dp, color = BrassAccent.copy(alpha = 0.35f), shape = shape)
                        .clickable { PermissionHelper.openNotificationListenerSettings(context) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrassAccent)
                        )
                        Text(
                            text = "Notification access is off",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Text(
                        text = "Enable",
                        style = MaterialTheme.typography.labelLarge.copy(color = BrassAccent)
                    )
                }
            }
        }

        // Tactile Soundbox Status Deck
        item {
            HeroSpeakerCard(active = isLive) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaisaBolMark(sizeDp = 18, accentActive = isLive)
                            Text(
                                text = "PaisaBol",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isLive -> PositiveMint
                                            settings.speakerEnabled -> BrassAccent
                                            else -> InkMuted
                                        }
                                    )
                            )
                            Text(
                                text = when {
                                    isLive -> "Listening"
                                    settings.speakerEnabled -> "Needs access"
                                    else -> "Paused"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (isLive) InkPrimary else InkSecondary
                                )
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Today's received",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = NepaliSpeechFormatter.formatNepaliCurrency(todaySummary.totalAmount),
                            style = MaterialTheme.typography.displayLarge
                        )
                    }

                    HorizontalDivider(color = Hairline, thickness = 1.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (todaySummary.count) {
                                0 -> "No payments today"
                                1 -> "1 payment today"
                                else -> "${todaySummary.count} payments today"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFeatureSettings = "tnum"
                            )
                        )
                        Text(
                            text = when (settings.speechLanguage) {
                                SpeechLanguage.NEPALI -> "नेपाली"
                                SpeechLanguage.ROMAN_NEPALI -> "Roman"
                                SpeechLanguage.ENGLISH -> "English"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(color = InkPrimary)
                        )
                    }
                }
            }
        }

        // Explicit Turn on / Turn off two-button speaker control
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryActionButton(
                    text = "Turn on",
                    leadingIcon = Icons.AutoMirrored.Rounded.VolumeUp,
                    primaryStyle = settings.speakerEnabled,
                    onClick = {
                        onSetSpeakerEnabled(true)
                        if (!permissionStatus.notificationAccessGranted) {
                            PermissionHelper.openNotificationListenerSettings(context)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                PrimaryActionButton(
                    text = "Turn off",
                    leadingIcon = Icons.AutoMirrored.Rounded.VolumeOff,
                    primaryStyle = !settings.speakerEnabled,
                    onClick = { onSetSpeakerEnabled(false) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4 equal-width quick controls on strict 4px grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionTile(
                    icon = Icons.Rounded.Campaign,
                    label = "Test voice",
                    onClick = onTestVoice,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Rounded.GraphicEq,
                    label = "Boost",
                    active = settings.autoBoostVolume,
                    onClick = onToggleVolumeBoost,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Rounded.Translate,
                    label = when (settings.speechLanguage) {
                        SpeechLanguage.NEPALI -> "नेपाली"
                        SpeechLanguage.ROMAN_NEPALI -> "Roman"
                        SpeechLanguage.ENGLISH -> "English"
                    },
                    onClick = onCycleLanguage,
                    modifier = Modifier.weight(1f)
                )
                QuickActionTile(
                    icon = Icons.Rounded.Shield,
                    label = "Access",
                    active = permissionStatus.notificationAccessGranted,
                    onClick = { showVoiceSheet = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent payments header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent",
                    style = MaterialTheme.typography.titleLarge
                )
                if (recentPayments.isNotEmpty()) {
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.labelLarge.copy(color = BrassAccent),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onSeeAllPayments)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (recentPayments.isEmpty()) {
            item {
                val shape = RoundedCornerShape(12.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(SurfaceCard)
                        .border(width = 1.dp, color = Hairline, shape = shape)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Waiting for payments",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Verified eSewa, Khalti and Nepali bank alerts will be spoken aloud and logged here",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(recentPayments.take(4), key = { it.id }) { payment ->
                Column {
                    PaymentListRow(
                        payment = payment,
                        showRelativeDate = true,
                        onClick = { onReplayPayment(payment) }
                    )
                    HorizontalDivider(color = Hairline, thickness = 1.dp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    if (showVoiceSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showVoiceSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceCard,
            contentColor = InkPrimary
        ) {
            VoiceAndPermissionSheet(
                settings = settings,
                permissionStatus = permissionStatus,
                onSelectLanguage = onSelectLanguage,
                onToggleAnnounceSource = onToggleAnnounceSource,
                onSpeechRateChange = onSpeechRateChange,
                onTestVoice = onTestVoice,
                onOpenNotifAccess = { PermissionHelper.openNotificationListenerSettings(context) },
                onRequestPostNotif = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onRefreshPermissions()
                    }
                },
                onOpenBattery = { PermissionHelper.openBatteryOptimizationSettings(context) },
                onOpenTtsSettings = { PermissionHelper.openTtsSettings(context) }
            )
        }
    }
}

@Composable
private fun VoiceAndPermissionSheet(
    settings: AppSettings,
    permissionStatus: PermissionStatus,
    onSelectLanguage: (SpeechLanguage) -> Unit,
    onToggleAnnounceSource: (Boolean) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onTestVoice: () -> Unit,
    onOpenNotifAccess: () -> Unit,
    onRequestPostNotif: () -> Unit,
    onOpenBattery: () -> Unit,
    onOpenTtsSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Voice and permissions",
            style = MaterialTheme.typography.headlineMedium
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Announcement language",
                style = MaterialTheme.typography.labelMedium
            )
            SpeechLanguage.entries.forEach { lang ->
                val selected = settings.speechLanguage == lang
                val rowShape = RoundedCornerShape(10.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(rowShape)
                        .background(if (selected) BrassTint else SurfaceRaised)
                        .border(
                            width = 1.dp,
                            color = if (selected) BrassAccent.copy(alpha = 0.4f) else Hairline,
                            shape = rowShape
                        )
                        .clickable { onSelectLanguage(lang) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = lang.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = lang.samplePhrase,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (selected) BrassAccent else InkSecondary
                            )
                        )
                    }
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BrassAccent)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Speak app name",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Include eSewa, Khalti or bank before the amount",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = settings.announceSourceApp,
                onCheckedChange = onToggleAnnounceSource,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BgCanvas,
                    checkedTrackColor = BrassAccent,
                    uncheckedThumbColor = InkSecondary,
                    uncheckedTrackColor = SurfaceRaised,
                    uncheckedBorderColor = Hairline
                )
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speech speed",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = String.format(Locale.US, "%.2fx", settings.speechRate),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFeatureSettings = "tnum",
                        color = BrassAccent
                    )
                )
            }
            Slider(
                value = settings.speechRate,
                onValueChange = onSpeechRateChange,
                valueRange = 0.65f..1.35f,
                colors = SliderDefaults.colors(
                    thumbColor = InkPrimary,
                    activeTrackColor = BrassAccent,
                    inactiveTrackColor = SurfaceRaised
                )
            )
        }

        HorizontalDivider(color = Hairline, thickness = 1.dp)

        Column {
            PermissionListRow(
                icon = Icons.Rounded.RecordVoiceOver,
                title = "Notification access",
                subtitle = "Reads verified payment alerts",
                granted = permissionStatus.notificationAccessGranted,
                onClick = onOpenNotifAccess
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionListRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Background speaker",
                    subtitle = "Keeps listening when screen is locked",
                    granted = permissionStatus.postNotificationGranted,
                    onClick = onRequestPostNotif
                )
            }
            PermissionListRow(
                icon = Icons.Rounded.BatteryChargingFull,
                title = "Unrestricted battery",
                subtitle = "Prevents OS from pausing background voice",
                granted = permissionStatus.batteryOptimizationIgnored,
                onClick = onOpenBattery
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryActionButton(
                text = "TTS voices",
                leadingIcon = Icons.Rounded.SettingsVoice,
                primaryStyle = false,
                onClick = onOpenTtsSettings,
                modifier = Modifier.weight(1f)
            )
            PrimaryActionButton(
                text = "Test voice",
                leadingIcon = Icons.AutoMirrored.Rounded.VolumeUp,
                primaryStyle = true,
                onClick = onTestVoice,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
