package com.paisabol.paymentspeaker

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paisabol.paymentspeaker.ui.MainViewModel
import com.paisabol.paymentspeaker.ui.components.MainTab
import com.paisabol.paymentspeaker.ui.components.MinimalBottomDock
import com.paisabol.paymentspeaker.ui.components.StandardEaseOut
import com.paisabol.paymentspeaker.ui.screens.OnboardingScreen
import com.paisabol.paymentspeaker.ui.screens.PaymentsListScreen
import com.paisabol.paymentspeaker.ui.screens.SpeakerHomeScreen
import com.paisabol.paymentspeaker.ui.theme.BgCanvas
import com.paisabol.paymentspeaker.ui.theme.PaisaBolTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            PaisaBolTheme {
                PaisaBolRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun PaisaBolRoot(viewModel: MainViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val permissionStatus by viewModel.permissionStatus.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!settings.onboardingCompleted) {
        OnboardingScreen(
            permissionStatus = permissionStatus,
            onRefreshPermissions = viewModel::refreshPermissions,
            onTestVoice = viewModel::testVoiceOnly,
            onFinish = viewModel::completeOnboarding
        )
    } else {
        var selectedTab by rememberSaveable { mutableStateOf(MainTab.SPEAKER) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BgCanvas,
            bottomBar = {
                MinimalBottomDock(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it }
                )
            }
        ) { innerPadding ->
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(180, easing = StandardEaseOut),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                label = "main_tabs"
            ) { tab ->
                when (tab) {
                    MainTab.SPEAKER -> SpeakerHomeScreen(
                        settings = settings,
                        todaySummary = todaySummary,
                        recentPayments = payments,
                        permissionStatus = permissionStatus,
                        onSetSpeakerEnabled = viewModel::setSpeakerEnabled,
                        onTestVoice = viewModel::testVoiceOnly,
                        onToggleVolumeBoost = viewModel::toggleAutoBoostVolume,
                        onCycleLanguage = viewModel::cycleLanguage,
                        onSelectLanguage = viewModel::setSpeechLanguage,
                        onToggleAnnounceSource = viewModel::setAnnounceSourceApp,
                        onSpeechRateChange = viewModel::setSpeechRate,
                        onReplayPayment = viewModel::replayPayment,
                        onSeeAllPayments = { selectedTab = MainTab.PAYMENTS },
                        onRefreshPermissions = viewModel::refreshPermissions
                    )

                    MainTab.PAYMENTS -> PaymentsListScreen(
                        payments = payments,
                        onReplayPayment = viewModel::replayPayment,
                        onDeletePayment = viewModel::deletePayment,
                        onClearAll = viewModel::clearAllPayments
                    )
                }
            }
        }
    }
}
