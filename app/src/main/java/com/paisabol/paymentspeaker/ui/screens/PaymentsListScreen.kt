package com.paisabol.paymentspeaker.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.paisabol.paymentspeaker.domain.NepaliSpeechFormatter
import com.paisabol.paymentspeaker.domain.PaymentParser
import com.paisabol.paymentspeaker.model.PaymentRecord
import com.paisabol.paymentspeaker.model.SourceCategory
import com.paisabol.paymentspeaker.ui.components.PaymentListRow
import com.paisabol.paymentspeaker.ui.components.PaymentSourceAvatar
import com.paisabol.paymentspeaker.ui.components.PrimaryActionButton
import com.paisabol.paymentspeaker.ui.components.TopIconButton
import com.paisabol.paymentspeaker.ui.theme.BgCanvas
import com.paisabol.paymentspeaker.ui.theme.BrassAccent
import com.paisabol.paymentspeaker.ui.theme.DangerRed
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsListScreen(
    payments: List<PaymentRecord>,
    onReplayPayment: (PaymentRecord) -> Unit,
    onDeletePayment: (Long) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryName by rememberSaveable { mutableStateOf(SourceCategory.ALL.name) }
    val selectedCategory = remember(selectedCategoryName) {
        SourceCategory.entries.firstOrNull { it.name == selectedCategoryName } ?: SourceCategory.ALL
    }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    var inspectedPayment by remember { mutableStateOf<PaymentRecord?>(null) }

    val filtered = remember(payments, selectedCategory, searchQuery) {
        val rawQuery = searchQuery.trim()
        val normalizedNumericQuery = PaymentParser.convertDevanagariDigits(rawQuery)
            .replace(",", "")
            .replace("rs.", "", ignoreCase = true)
            .replace("rs", "", ignoreCase = true)
            .trim()

        payments.filter { item ->
            val matchesCat = selectedCategory == SourceCategory.ALL ||
                item.source.category == selectedCategory
            val matchesSearch = rawQuery.isEmpty() ||
                item.sourceLabel.contains(rawQuery, ignoreCase = true) ||
                item.senderName.orEmpty().contains(rawQuery, ignoreCase = true) ||
                item.rawText.contains(rawQuery, ignoreCase = true) ||
                NepaliSpeechFormatter.formatNepaliCurrency(item.amount)
                    .contains(rawQuery, ignoreCase = true) ||
                (normalizedNumericQuery.isNotEmpty() &&
                    item.amount.toString().contains(normalizedNumericQuery))
            matchesCat && matchesSearch
        }
    }

    val grouped = remember(filtered) {
        filtered.groupBy { formatDateHeader(it.timestampMillis) }
    }

    val filteredTotal = remember(filtered) { filtered.sumOf { it.amount } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCanvas)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Payments",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = if (filtered.isEmpty()) {
                        "Verified incoming payments"
                    } else {
                        "${NepaliSpeechFormatter.formatNepaliCurrency(filteredTotal)} received"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (filtered.isEmpty()) InkSecondary else PositiveMint,
                        fontWeight = FontWeight.Medium,
                        fontFeatureSettings = "tnum"
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TopIconButton(
                    icon = if (searchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
                    contentDescription = "Search payments",
                    active = searchVisible,
                    onClick = {
                        searchVisible = !searchVisible
                        if (!searchVisible) searchQuery = ""
                    }
                )
                if (payments.isNotEmpty()) {
                    TopIconButton(
                        icon = Icons.Rounded.DeleteOutline,
                        contentDescription = "Clear history",
                        onClick = { showClearDialog = true }
                    )
                }
            }
        }

        AnimatedVisibility(visible = searchVisible) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search", color = InkMuted) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrassAccent,
                    unfocusedBorderColor = Hairline,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedTextColor = InkPrimary,
                    unfocusedTextColor = InkPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp)
            )
        }

        // Control-radius filter tabs (consistent with 8-10dp control language)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            items(SourceCategory.entries) { category ->
                val selected = category == selectedCategory
                val chipShape = RoundedCornerShape(8.dp)
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(chipShape)
                        .background(if (selected) InkPrimary else SurfaceCard)
                        .then(
                            if (!selected) {
                                Modifier.border(width = 1.dp, color = Hairline, shape = chipShape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { selectedCategoryName = category.name }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = if (selected) BgCanvas else InkSecondary
                        )
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (payments.isEmpty()) "No verified payments yet" else "No matching payments",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (payments.isEmpty()) {
                            "Incoming alerts from eSewa, Khalti, IME Pay, Fonepay and Nepali banks will appear here automatically"
                        } else {
                            "Try a different filter or search term"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp)
            ) {
                grouped.forEach { (dateHeader, dayItems) ->
                    item(key = "header_$dateHeader") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = NepaliSpeechFormatter.formatNepaliCurrency(
                                    dayItems.sumOf { it.amount }
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFeatureSettings = "tnum"
                                )
                            )
                        }
                    }

                    items(dayItems, key = { it.id }) { payment ->
                        Column {
                            PaymentListRow(
                                payment = payment,
                                showRelativeDate = false,
                                onClick = { inspectedPayment = payment }
                            )
                            HorizontalDivider(color = Hairline, thickness = 1.dp)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    inspectedPayment?.let { record ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { inspectedPayment = null },
            sheetState = sheetState,
            containerColor = SurfaceCard,
            contentColor = InkPrimary
        ) {
            PaymentDetailSheet(
                payment = record,
                onReplay = { onReplayPayment(record) },
                onDelete = {
                    onDeletePayment(record.id)
                    inspectedPayment = null
                }
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = SurfaceCard,
            titleContentColor = InkPrimary,
            textContentColor = InkSecondary,
            title = { Text("Clear payment history?") },
            text = { Text("This deletes all saved payment records stored on this phone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = DangerRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = InkSecondary)
                }
            }
        )
    }
}

@Composable
private fun PaymentDetailSheet(
    payment: PaymentRecord,
    onReplay: () -> Unit,
    onDelete: () -> Unit
) {
    val fullDate = remember(payment.timestampMillis) {
        SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.US).format(Date(payment.timestampMillis))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PaymentSourceAvatar(source = payment.source, modifier = Modifier.size(48.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "+ ${NepaliSpeechFormatter.formatNepaliCurrency(payment.amount)}",
            style = MaterialTheme.typography.displayLarge.copy(color = PositiveMint)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = null,
                tint = BrassAccent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Verified ${payment.sourceLabel} alert",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val cardShape = RoundedCornerShape(12.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(SurfaceRaised)
                .border(width = 1.dp, color = Hairline, shape = cardShape)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!payment.senderName.isNullOrBlank()) {
                DetailRow("Sender", payment.senderName)
            }
            DetailRow("Source", payment.sourceLabel)
            DetailRow("Time", fullDate)
            DetailRow("Announcement", payment.spokenText)
        }

        Spacer(modifier = Modifier.height(20.dp))

        PrimaryActionButton(
            text = "Replay announcement",
            leadingIcon = Icons.AutoMirrored.Rounded.VolumeUp,
            onClick = onReplay
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onDelete) {
            Text(
                text = "Remove record",
                style = MaterialTheme.typography.labelLarge.copy(color = DangerRed)
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = InkPrimary,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatDateHeader(timestampMillis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"
        cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(timestampMillis))
    }
}
