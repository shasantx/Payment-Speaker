package com.paisabol.paymentspeaker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.paisabol.paymentspeaker.domain.NepaliSpeechFormatter
import com.paisabol.paymentspeaker.model.PaymentRecord
import com.paisabol.paymentspeaker.model.PaymentSource
import com.paisabol.paymentspeaker.ui.theme.BgDock
import com.paisabol.paymentspeaker.ui.theme.BrassAccent
import com.paisabol.paymentspeaker.ui.theme.DotBank
import com.paisabol.paymentspeaker.ui.theme.DotConnectIps
import com.paisabol.paymentspeaker.ui.theme.DotEsewa
import com.paisabol.paymentspeaker.ui.theme.DotFonepay
import com.paisabol.paymentspeaker.ui.theme.DotImePay
import com.paisabol.paymentspeaker.ui.theme.DotKhalti
import com.paisabol.paymentspeaker.ui.theme.Hairline
import com.paisabol.paymentspeaker.ui.theme.InkMuted
import com.paisabol.paymentspeaker.ui.theme.InkPrimary
import com.paisabol.paymentspeaker.ui.theme.InkSecondary
import com.paisabol.paymentspeaker.ui.theme.PositiveMint
import com.paisabol.paymentspeaker.ui.theme.SurfaceCard
import com.paisabol.paymentspeaker.ui.theme.SurfaceRaised
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun sourceDotColor(source: PaymentSource): Color = when (source) {
    PaymentSource.ESEWA -> DotEsewa
    PaymentSource.KHALTI -> DotKhalti
    PaymentSource.IME_PAY -> DotImePay
    PaymentSource.FONEPAY -> DotFonepay
    PaymentSource.CONNECT_IPS -> DotConnectIps
    PaymentSource.BANK -> DotBank
    PaymentSource.OTHER -> InkSecondary
}

/**
 * Quiet neutral monogram badge with a 6dp toned source marker.
 */
@Composable
fun PaymentSourceAvatar(
    source: PaymentSource,
    modifier: Modifier = Modifier
) {
    val dotColor = sourceDotColor(source)
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(SurfaceRaised)
            .border(width = 1.dp, color = Hairline, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = source.shortBadge,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = InkPrimary
            )
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

/**
 * Ledger payment row on a strict 4px rhythm.
 * Uses smart relative time when shown outside a date-grouped section.
 */
@Composable
fun PaymentListRow(
    payment: PaymentRecord,
    onClick: () -> Unit,
    showRelativeDate: Boolean = false,
    modifier: Modifier = Modifier
) {
    val timeText = remember(payment.timestampMillis, showRelativeDate) {
        formatRowTimestamp(payment.timestampMillis, showRelativeDate)
    }
    val primaryTitle = payment.senderName?.takeIf { it.isNotBlank() } ?: payment.sourceLabel
    val subtitle = if (!payment.senderName.isNullOrBlank()) {
        "${payment.sourceLabel}, $timeText"
    } else {
        "Verified, $timeText"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaymentSourceAvatar(source = payment.source)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primaryTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "+ ${NepaliSpeechFormatter.formatNepaliCurrency(payment.amount)}",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                fontFeatureSettings = "tnum",
                color = PositiveMint
            )
        )
    }
}

private fun formatRowTimestamp(timestampMillis: Long, showRelativeDate: Boolean): String {
    val timeOnly = SimpleDateFormat("h:mm a", Locale.US).format(Date(timestampMillis))
    if (!showRelativeDate) return timeOnly

    val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> timeOnly
        cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday, $timeOnly"
        else -> SimpleDateFormat("dd MMM, h:mm a", Locale.US).format(Date(timestampMillis))
    }
}

enum class MainTab(val label: String, val icon: ImageVector) {
    SPEAKER("Speaker", Icons.Rounded.GraphicEq),
    PAYMENTS("Payments", Icons.AutoMirrored.Rounded.ReceiptLong)
}

/**
 * Quiet hairline-separated bottom navigation dock with clear labels and a brass active indicator.
 */
@Composable
fun MinimalBottomDock(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BgDock)
    ) {
        HorizontalDivider(color = Hairline, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val interactionSource = remember { MutableInteractionSource() }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelectTab(tab) }
                        )
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) BrassAccent else Color.Transparent)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) InkPrimary else InkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isSelected) InkPrimary else InkMuted
                        )
                    )
                }
            }
        }
    }
}
