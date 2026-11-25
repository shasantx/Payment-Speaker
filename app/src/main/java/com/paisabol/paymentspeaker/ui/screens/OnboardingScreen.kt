package com.paisabol.paymentspeaker.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paisabol.paymentspeaker.ui.components.PaisaBolMark
import com.paisabol.paymentspeaker.ui.components.PrimaryActionButton
import com.paisabol.paymentspeaker.ui.components.StandardEaseOut
import com.paisabol.paymentspeaker.ui.components.TopIconButton
import com.paisabol.paymentspeaker.ui.theme.BgCanvas
import com.paisabol.paymentspeaker.ui.theme.BrassAccent
import com.paisabol.paymentspeaker.ui.theme.Hairline
import com.paisabol.paymentspeaker.ui.theme.HairlineStrong
import com.paisabol.paymentspeaker.ui.theme.InkMuted
import com.paisabol.paymentspeaker.ui.theme.InkPrimary
import com.paisabol.paymentspeaker.ui.theme.InkSecondary
import com.paisabol.paymentspeaker.ui.theme.PositiveMint
import com.paisabol.paymentspeaker.ui.theme.PositiveTint
import com.paisabol.paymentspeaker.ui.theme.SurfaceCard
import com.paisabol.paymentspeaker.ui.theme.SurfaceRaised
import com.paisabol.paymentspeaker.util.PermissionHelper
import com.paisabol.paymentspeaker.util.PermissionStatus

@Composable
fun OnboardingScreen(
    permissionStatus: PermissionStatus,
    onRefreshPermissions: () -> Unit,
    onTestVoice: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    var step by rememberSaveable { mutableIntStateOf(0) }

    val postNotifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onRefreshPermissions()
    }

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            fadeIn(tween(220, easing = StandardEaseOut)) togetherWith
                fadeOut(tween(160, easing = StandardEaseOut))
        },
        label = "onboarding_page"
    ) { currentStep ->
        when (currentStep) {
            0 -> WelcomePage(onContinue = { step = 1 })

            1 -> HowItWorksPage(
                onBack = { step = 0 },
                onTestVoice = onTestVoice,
                onContinue = { step = 2 }
            )

            else -> PermissionsSetupPage(
                status = permissionStatus,
                onBack = { step = 1 },
                onGrantNotificationAccess = {
                    PermissionHelper.openNotificationListenerSettings(context)
                },
                onGrantPostNotifications = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onRefreshPermissions()
                    }
                },
                onGrantBattery = {
                    PermissionHelper.openBatteryOptimizationSettings(context)
                },
                onFinish = onFinish
            )
        }
    }
}

/**
 * Step 1 — Matte hardware welcome screen with an acoustic soundbox grille centerpiece.
 */
@Composable
private fun WelcomePage(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaisaBolMark(sizeDp = 20)
            Text(
                text = "PaisaBol",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Hardware acoustic soundbox visual moment
        SoundboxHardwareVisual(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Payment speaker\nfor Nepal",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Speaks verified eSewa, Khalti and Nepali bank alerts aloud the moment they arrive",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryActionButton(
            text = "Continue",
            onClick = onContinue
        )
    }
}

/**
 * Tactile matte counter soundbox graphic: perforated speaker grille, brass status LED,
 * and sample Nepali readout card. Grounded directly in the product's physical world.
 */
@Composable
private fun SoundboxHardwareVisual(modifier: Modifier = Modifier) {
    val outerShape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(outerShape)
            .background(SurfaceCard)
            .border(width = 1.dp, color = HairlineStrong, shape = outerShape)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
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
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PositiveMint)
                    )
                    Text(
                        text = "Offline soundbox",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(
                    text = "ne-NP",
                    style = MaterialTheme.typography.labelMedium.copy(color = InkMuted)
                )
            }

            // Perforated speaker grille
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 20.dp)
            ) {
                val cols = 15
                val rows = 7
                val padX = 8.dp.toPx()
                val padY = 8.dp.toPx()
                val usableW = (size.width - padX * 2).coerceAtLeast(1f)
                val usableH = (size.height - padY * 2).coerceAtLeast(1f)
                val stepX = usableW / (cols - 1)
                val stepY = usableH / (rows - 1)
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val maxDist = kotlin.math.hypot(usableW / 2f, usableH / 2f).coerceAtLeast(1f)

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val x = padX + c * stepX
                        val y = padY + r * stepY
                        val dist = kotlin.math.hypot(x - centerX, y - centerY)
                        val norm = 1f - (dist / maxDist).coerceIn(0f, 1f)
                        val isCenterHighlight = r in 2..4 && c in 6..8
                        val radius = (1.4f + norm * 1.4f).dp.toPx()
                        val color = if (isCenterHighlight && c == 7 && r == 3) {
                            BrassAccent
                        } else {
                            InkPrimary.copy(alpha = 0.08f + norm * 0.14f)
                        }
                        drawCircle(
                            color = color,
                            radius = radius,
                            center = Offset(x, y)
                        )
                    }
                }
            }

            // Quiet sample readout strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceRaised)
                    .border(width = 1.dp, color = Hairline, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "eSewa",
                        style = MaterialTheme.typography.labelMedium.copy(color = InkSecondary)
                    )
                    Text(
                        text = "+ Rs. 500",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = PositiveMint,
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
                Text(
                    text = "ई-सेवा मार्फत ५०० रुपैयाँ प्राप्त भयो",
                    style = MaterialTheme.typography.bodyMedium.copy(color = InkPrimary)
                )
            }
        }
    }
}

/**
 * Step 2 — Clear, scrollable overview of how PaisaBol operates.
 */
@Composable
private fun HowItWorksPage(
    onBack: () -> Unit,
    onTestVoice: () -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TopIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            StepIndicators(current = 1, total = 3)
            Spacer(modifier = Modifier.size(40.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "How it works",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Turns your Android phone into an offline payment speaker",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            TutorialRow(
                icon = Icons.Rounded.VerifiedUser,
                title = "Verified Nepali apps only",
                description = "Reads alerts from eSewa, Khalti, IME Pay, Fonepay, ConnectIPS and Nepali banks. Ignores chats, OTPs and outgoing debits."
            )
            HorizontalDivider(color = Hairline, thickness = 1.dp)

            TutorialRow(
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                title = "Speaks clearly in Nepali",
                description = "Announces the received amount aloud even when your screen is locked."
            )
            HorizontalDivider(color = Hairline, thickness = 1.dp)

            TutorialRow(
                icon = Icons.Rounded.Lock,
                title = "Private and offline",
                description = "No internet permission, no bank login and no cloud sync. Every record stays on your phone."
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .border(width = 1.dp, color = Hairline, shape = RoundedCornerShape(10.dp))
                    .clickable(onClick = onTestVoice)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = BrassAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Play sample Nepali voice",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        PrimaryActionButton(
            text = "Continue",
            onClick = onContinue
        )
    }
}

@Composable
private fun TutorialRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = InkSecondary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Step 3 — Scrollable permission setup page with live status checks.
 */
@Composable
private fun PermissionsSetupPage(
    status: PermissionStatus,
    onBack: () -> Unit,
    onGrantNotificationAccess: () -> Unit,
    onGrantPostNotifications: () -> Unit,
    onGrantBattery: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TopIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            StepIndicators(current = 2, total = 3)
            Spacer(modifier = Modifier.size(40.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Allow access",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Grant notification access so PaisaBol can announce verified payments automatically",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            PermissionListRow(
                icon = Icons.Rounded.NotificationsActive,
                title = "Notification access",
                subtitle = "Required to read incoming payment alerts",
                granted = status.notificationAccessGranted,
                onClick = onGrantNotificationAccess
            )
            HorizontalDivider(color = Hairline, thickness = 1.dp)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionListRow(
                    icon = Icons.AutoMirrored.Rounded.VolumeUp,
                    title = "Background speaker",
                    subtitle = "Keeps voice alerts ready while screen is off",
                    granted = status.postNotificationGranted,
                    onClick = onGrantPostNotifications
                )
                HorizontalDivider(color = Hairline, thickness = 1.dp)
            }

            PermissionListRow(
                icon = Icons.Rounded.BatteryChargingFull,
                title = "Unrestricted battery",
                subtitle = "Recommended for Xiaomi, Samsung and Vivo devices",
                granted = status.batteryOptimizationIgnored,
                onClick = onGrantBattery
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        PrimaryActionButton(
            text = if (status.notificationAccessGranted) "Get started" else "Open notification settings",
            onClick = {
                if (status.notificationAccessGranted) onFinish()
                else onGrantNotificationAccess()
            }
        )

        if (!status.notificationAccessGranted) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Set up later",
                    style = MaterialTheme.typography.bodyMedium.copy(color = InkMuted),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onFinish)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionListRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (granted) PositiveTint else SurfaceCard)
                .border(
                    width = 1.dp,
                    color = if (granted) PositiveMint.copy(alpha = 0.25f) else Hairline,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (granted) Icons.Rounded.Check else icon,
                contentDescription = null,
                tint = if (granted) PositiveMint else InkSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = if (granted) "Allowed" else "Enable",
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (granted) PositiveMint else BrassAccent
            )
        )
    }
}

@Composable
private fun StepIndicators(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(total) { index ->
            val active = index == current
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(if (active) 20.dp else 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (active) BrassAccent else InkMuted.copy(alpha = 0.4f))
            )
        }
    }
}
