package com.paisabol.paymentspeaker.model

enum class PaymentSource(
    val displayName: String,
    val nepaliName: String,
    val shortBadge: String,
    val category: SourceCategory
) {
    ESEWA("eSewa", "ई-सेवा", "eS", SourceCategory.ESEWA),
    KHALTI("Khalti", "खल्ती", "Kh", SourceCategory.KHALTI),
    IME_PAY("IME Pay", "आइएमई पे", "IM", SourceCategory.IME_PAY),
    FONEPAY("Fonepay", "फोनपे", "FP", SourceCategory.BANK),
    CONNECT_IPS("ConnectIPS", "कनेक्ट आइपिएस", "CI", SourceCategory.BANK),
    BANK("Bank", "बैंक", "BK", SourceCategory.BANK),
    OTHER("Payment", "भुक्तानी", "Rs", SourceCategory.BANK);

    companion object {
        fun fromName(raw: String): PaymentSource =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: OTHER
    }
}

enum class SourceCategory(val label: String) {
    ALL("All"),
    ESEWA("eSewa"),
    KHALTI("Khalti"),
    IME_PAY("IME Pay"),
    BANK("Banks")
}

enum class SpeechLanguage(
    val label: String,
    val subtitle: String,
    val samplePhrase: String
) {
    NEPALI(
        label = "नेपाली (Nepali)",
        subtitle = "५०० रुपैयाँ प्राप्त भयो",
        samplePhrase = "ई-सेवा मार्फत ५०० रुपैयाँ प्राप्त भयो"
    ),
    ROMAN_NEPALI(
        label = "Nepali (Roman)",
        subtitle = "500 rupaiya prapta bhayo",
        samplePhrase = "500 rupaiya prapta bhayo"
    ),
    ENGLISH(
        label = "English",
        subtitle = "500 rupees received",
        samplePhrase = "500 rupees received via eSewa"
    );

    companion object {
        fun fromName(raw: String?): SpeechLanguage =
            entries.firstOrNull { it.name == raw } ?: NEPALI
    }
}

data class PaymentRecord(
    val id: Long = 0L,
    val amount: Double,
    val source: PaymentSource,
    val sourceLabel: String,
    val senderName: String?,
    val packageName: String,
    val rawTitle: String,
    val rawText: String,
    val spokenText: String,
    val timestampMillis: Long
)

data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val speakerEnabled: Boolean = true,
    val speechLanguage: SpeechLanguage = SpeechLanguage.NEPALI,
    val announceSourceApp: Boolean = true,
    val autoBoostVolume: Boolean = true,
    val speechRate: Float = 0.95f
)
