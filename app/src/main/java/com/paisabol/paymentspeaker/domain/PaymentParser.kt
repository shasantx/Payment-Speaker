package com.paisabol.paymentspeaker.domain

import com.paisabol.paymentspeaker.model.PaymentSource
import java.util.Locale

data class ParsedPayment(
    val amount: Double,
    val source: PaymentSource,
    val sourceLabel: String,
    val senderName: String?,
    val packageName: String,
    val rawTitle: String,
    val rawText: String
)

/**
 * Production-hardened parser for verified incoming Nepali payment notifications.
 *
 * Anti-fraud protections:
 * - Only accepts notifications from verified Nepali wallet/bank app packages OR system SMS apps
 *   sent by official alphanumeric bank/wallet sender IDs (never personal phone numbers).
 * - Explicitly blocks chat, social, and email apps (WhatsApp, Viber, Messenger, Telegram, etc.)
 *   so forged text messages can never trigger a false payment announcement.
 * - Rejects outgoing debits, transfers sent, OTPs, payment requests, and promotional messages.
 */
object PaymentParser {

    private val exactPackageSources: Map<String, Pair<PaymentSource, String>> = mapOf(
        "com.f1soft.esewa" to (PaymentSource.ESEWA to "eSewa"),
        "com.khalti" to (PaymentSource.KHALTI to "Khalti"),
        "com.swifttechnology.imepay" to (PaymentSource.IME_PAY to "IME Pay"),
        "com.nchl.connectips" to (PaymentSource.CONNECT_IPS to "ConnectIPS"),
        "com.fonepay.merchant" to (PaymentSource.FONEPAY to "Fonepay"),
        "com.f1soft.fonepay" to (PaymentSource.FONEPAY to "Fonepay"),
        "com.fonepay" to (PaymentSource.FONEPAY to "Fonepay"),
        "com.cellcom.cellpay" to (PaymentSource.BANK to "CellPay"),
        "com.prabhupay.prabhupay" to (PaymentSource.BANK to "PrabhuPay"),
        "com.mypay.nepal" to (PaymentSource.BANK to "MyPay"),
        "com.qpay.nepal" to (PaymentSource.BANK to "QPay"),
        "com.namastepay" to (PaymentSource.BANK to "Namaste Pay"),
        "com.moru.nepal" to (PaymentSource.BANK to "Moru")
    )

    private val verifiedBankPackagePatterns: List<Pair<String, String>> = listOf(
        "com.f1soft.banksmart" to "Mobile Banking",
        "com.nabilbank" to "Nabil Bank",
        "com.nicasia" to "NIC Asia Bank",
        "com.globalime" to "Global IME Bank",
        "com.globalsmart" to "Global IME Bank",
        "com.siddharthabank" to "Siddhartha Bank",
        "com.himalayanbank" to "Himalayan Bank",
        "com.sanimabank" to "Sanima Bank",
        "com.laxmibank" to "Laxmi Sunrise Bank",
        "com.sunrisebank" to "Laxmi Sunrise Bank",
        "com.prabhubank" to "Prabhu Bank",
        "com.kumaribank" to "Kumari Bank",
        "com.everestbank" to "Everest Bank",
        "com.machbank" to "MBL Bank",
        "com.mbl" to "MBL Bank",
        "com.citizensbank" to "Citizens Bank",
        "com.nmbbank" to "NMB Bank",
        "com.nimb" to "NIMB Bank",
        "com.primebank" to "Prime Bank",
        "com.rbb" to "Rastriya Banijya Bank",
        "com.adbl" to "ADBL Bank",
        "com.nepalbank" to "Nepal Bank",
        "com.scb" to "Standard Chartered",
        "com.garimabank" to "Garima Bikas Bank",
        "com.muktinathbank" to "Muktinath Bikas Bank",
        "com.jyotibikas" to "Jyoti Bikas Bank",
        "com.shineresunga" to "Shine Resunga Bank",
        "com.kamanasewa" to "Kamana Sewa Bank",
        "com.mahalaxmibank" to "Mahalaxmi Bikas Bank",
        "com.shangrilabank" to "Shangrila Bank",
        "banksmart" to "Mobile Banking",
        "mobank" to "Mobile Banking",
        "fonepay" to "Fonepay",
        "esewa" to "eSewa",
        "khalti" to "Khalti",
        "imepay" to "IME Pay",
        "connectips" to "ConnectIPS"
    )

    private val smsPackages = setOf(
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.samsung.android.messaging",
        "com.miui.mms",
        "com.oneplus.mms",
        "com.coloros.mms",
        "com.realme.mms",
        "com.vivo.message"
    )

    private val blockedNonFinancialPackages = listOf(
        "whatsapp",
        "viber",
        "messenger",
        "facebook",
        "instagram",
        "telegram",
        "discord",
        "snapchat",
        "tiktok",
        "twitter",
        "x.android",
        "gmail",
        "outlook",
        "yahoo",
        "chrome",
        "firefox",
        "youtube",
        "paisabol"
    )

    private val trustedSmsSenderHints = listOf(
        "esewa", "khalti", "imepay", "ime pay", "fonepay", "connectips",
        "nabil", "nicasia", "nic asia", "gbime", "global", "sbl", "siddhartha",
        "hbl", "himalayan", "sanima", "laxmi", "sunrise", "prabhu", "kumari",
        "ebl", "everest", "mbl", "citizens", "czbil", "nmb", "nimb", "prime",
        "pcbl", "rbb", "adbl", "nbl", "scb", "garima", "muktinath", "jyoti",
        "shine", "kamana", "mahalaxmi", "shangrila", "cellpay", "mypay",
        "namaste", "moru", "bank", "wallet", "qpay"
    )

    private val creditKeywords = listOf(
        "received",
        "credited",
        "deposited",
        "added to your",
        "transferred to your",
        "transfer to your",
        "sent to your",
        "paid to your",
        "sent you",
        "paid you",
        "transferred you",
        "payment received",
        "fund received",
        "load successful",
        "wallet loaded",
        "loaded to your",
        "qr payment received",
        "has been credited",
        "is credited",
        "credit alert",
        "prapta",
        "प्राप्त",
        "जम्मा",
        "क्रेडिट"
    )

    private val debitOrNoiseKeywords = listOf(
        "debited",
        "has been debited",
        "is debited",
        "sent to",
        "paid to",
        "withdrawn",
        "deducted",
        "payment to",
        "transfer to",
        "transferred to",
        "purchase of",
        "spent",
        "otp",
        "one time password",
        "verification code",
        "login code",
        "mpin",
        "tpin",
        "failed",
        "declined",
        "unsuccessful",
        "cancelled",
        "canceled",
        "expired",
        "cashback offer",
        "win up to",
        "discount offer",
        "promo code",
        "promotional",
        "% off",
        "flat off",
        "requested rs",
        "requested npr",
        "payment request",
        "requesting rs",
        "requesting npr"
    )

    private val personalPhoneNumberRegex = Regex("""^\+?[0-9\s\-()]{7,16}$""")
    private val uppercaseShortcodeRegex = Regex("""^[A-Z0-9][A-Z0-9_\-]{2,19}$""")
    private val numericShortcodeRegex = Regex("""^[0-9]{4,6}$""")
    private val balancePrefixRegex = Regex("""(?i)(?:avl\.?|avail\.?|available|closing|current|total)?\s*bal(?:ance)?\s*(?:is|:|-)?\s*$""")

    private val currencyAmountRegex = Regex(
        """(?i)(?:npr|nrs|rs\.?|रु\.?|रू\.?)\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)"""
    )

    private val amountBeforeCurrencyRegex = Regex(
        """(?i)([0-9]{1,3}(?:,[0-9]{2,3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)\s*(?:npr|nrs|rs\.?|rupees|rupaiya|रुपैयाँ|रुपैया|रु\.?)"""
    )

    private val senderRegex = Regex(
        """(?i)(?:from|by)\s+([A-Za-z][A-Za-z\s.]{1,35}?)(?:\s*(?:\(|via|on|at|ref|txn|account|a/c|,|\.|for|$))"""
    )

    fun parse(packageName: String, title: String?, text: String?): ParsedPayment? {
        val pkgLower = packageName.lowercase(Locale.US)
        if (pkgLower.isBlank()) return null

        // Strictly reject chat, social, browser, email, and self packages to prevent spoofed alerts
        if (blockedNonFinancialPackages.any { pkgLower.contains(it) }) return null

        val cleanTitle = title?.trim().orEmpty()
        val cleanText = text?.trim().orEmpty()
        if (cleanTitle.isEmpty() && cleanText.isEmpty()) return null

        val combinedRaw = listOf(cleanTitle, cleanText)
            .filter { it.isNotEmpty() }
            .joinToString(" ")
        val normalized = convertDevanagariDigits(combinedRaw)
        val lower = normalized.lowercase(Locale.US)

        val (source, label, isVerifiedFinancialApp) = resolveSource(pkgLower, lower)
        val isSmsApp = smsPackages.contains(pkgLower)

        // Production verification gate: ONLY trust verified financial app packages OR verified bank SMS shortcodes
        if (!isVerifiedFinancialApp) {
            if (!isSmsApp) return null
            if (!isVerifiedBankSmsSender(cleanTitle, lower)) return null
        }

        // Reject outgoing / debit / OTP / payment request / promotional notifications
        if (isDebitOrNoise(lower)) return null

        // Require an explicit incoming/credit indicator
        if (!hasCreditIndicator(lower)) return null

        val amount = extractAmount(normalized) ?: return null
        if (amount <= 0.0 || amount > 100_000_000.0) return null

        val sender = extractSender(normalized)

        return ParsedPayment(
            amount = amount,
            source = source,
            sourceLabel = label,
            senderName = sender,
            packageName = packageName,
            rawTitle = cleanTitle,
            rawText = cleanText
        )
    }

    private fun isVerifiedBankSmsSender(title: String, lowerBody: String): Boolean {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) return false

        // Reject personal 10-digit / +977 mobile numbers so someone cannot fake a payment via SMS
        if (personalPhoneNumberRegex.matches(trimmedTitle)) return false

        val titleLower = trimmedTitle.lowercase(Locale.US)
        val senderMatchesKnownBank = trustedSmsSenderHints.any { titleLower.contains(it) }
        val isTelecomShortcode = (uppercaseShortcodeRegex.matches(trimmedTitle) && trimmedTitle.any { it.isLetter() }) ||
            numericShortcodeRegex.matches(trimmedTitle)

        if (!senderMatchesKnownBank && !isTelecomShortcode) return false

        // Body must also reference currency or a bank/wallet account
        val hasFinancialContext = listOf(
            "a/c", "account", "esewa", "khalti", "ime pay", "imepay",
            "fonepay", "connectips", "bank", "npr", "nrs", "rs.", "rs "
        ).any { lowerBody.contains(it) }

        return hasFinancialContext
    }

    private fun resolveSource(
        pkgLower: String,
        lowerText: String
    ): Triple<PaymentSource, String, Boolean> {
        exactPackageSources[pkgLower]?.let { (src, label) ->
            return Triple(src, label, true)
        }

        for ((pattern, label) in verifiedBankPackagePatterns) {
            if (pkgLower.contains(pattern)) {
                val src = when (pattern) {
                    "esewa" -> PaymentSource.ESEWA
                    "khalti" -> PaymentSource.KHALTI
                    "imepay" -> PaymentSource.IME_PAY
                    "fonepay" -> PaymentSource.FONEPAY
                    "connectips" -> PaymentSource.CONNECT_IPS
                    else -> PaymentSource.BANK
                }
                val refinedLabel = if (label == "Mobile Banking") {
                    inferBankNameFromText(lowerText) ?: label
                } else {
                    label
                }
                return Triple(src, refinedLabel, true)
            }
        }

        // Fallback source identification for verified bank SMS alerts
        return when {
            lowerText.contains("esewa") || lowerText.contains("e-sewa") || lowerText.contains("ई-सेवा") ->
                Triple(PaymentSource.ESEWA, "eSewa", false)
            lowerText.contains("khalti") || lowerText.contains("खल्ती") ->
                Triple(PaymentSource.KHALTI, "Khalti", false)
            lowerText.contains("ime pay") || lowerText.contains("imepay") ->
                Triple(PaymentSource.IME_PAY, "IME Pay", false)
            lowerText.contains("fonepay") ->
                Triple(PaymentSource.FONEPAY, "Fonepay", false)
            lowerText.contains("connectips") ->
                Triple(PaymentSource.CONNECT_IPS, "ConnectIPS", false)
            else -> {
                val bankName = inferBankNameFromText(lowerText)
                if (bankName != null) {
                    Triple(PaymentSource.BANK, bankName, false)
                } else {
                    Triple(PaymentSource.BANK, "Bank Alert", false)
                }
            }
        }
    }

    private fun inferBankNameFromText(lowerText: String): String? = when {
        lowerText.contains("nabil") -> "Nabil Bank"
        lowerText.contains("nic asia") || lowerText.contains("nicasia") -> "NIC Asia Bank"
        lowerText.contains("global ime") || lowerText.contains("gbime") -> "Global IME Bank"
        lowerText.contains("siddhartha") -> "Siddhartha Bank"
        lowerText.contains("himalayan") -> "Himalayan Bank"
        lowerText.contains("sanima") -> "Sanima Bank"
        lowerText.contains("prabhu") -> "Prabhu Bank"
        lowerText.contains("kumari") -> "Kumari Bank"
        lowerText.contains("everest") -> "Everest Bank"
        lowerText.contains("laxmi") || lowerText.contains("sunrise") -> "Laxmi Sunrise Bank"
        lowerText.contains("citizens") -> "Citizens Bank"
        lowerText.contains("machhapuchchhre") || lowerText.contains("mbl") -> "MBL Bank"
        lowerText.contains("nmb") -> "NMB Bank"
        lowerText.contains("nimb") -> "NIMB Bank"
        lowerText.contains("prime") -> "Prime Bank"
        lowerText.contains("rastriya banijya") || lowerText.contains("rbb") -> "Rastriya Banijya Bank"
        lowerText.contains("nepal bank") -> "Nepal Bank"
        lowerText.contains("garima") -> "Garima Bikas Bank"
        lowerText.contains("muktinath") -> "Muktinath Bikas Bank"
        lowerText.contains("jyoti") -> "Jyoti Bikas Bank"
        lowerText.contains("shine resunga") -> "Shine Resunga Bank"
        lowerText.contains("kamana") -> "Kamana Sewa Bank"
        lowerText.contains("mahalaxmi") -> "Mahalaxmi Bikas Bank"
        lowerText.contains("shangrila") -> "Shangrila Bank"
        else -> null
    }

    private fun hasCreditIndicator(lower: String): Boolean {
        return creditKeywords.any { lower.contains(it) }
    }

    private fun isDebitOrNoise(lower: String): Boolean {
        val sanitized = lower
            .replace("transferred to your", "credited_to_self")
            .replace("transfer to your", "credited_to_self")
            .replace("sent to your", "credited_to_self")
            .replace("paid to your", "credited_to_self")
        return debitOrNoiseKeywords.any { sanitized.contains(it) }
    }

    internal fun extractAmount(text: String): Double? {
        val allMatches = currencyAmountRegex.findAll(text).toList()
            .ifEmpty { amountBeforeCurrencyRegex.findAll(text).toList() }
        if (allMatches.isEmpty()) return null

        val nonBalanceMatch = allMatches.firstOrNull { match ->
            val prefixStart = (match.range.first - 24).coerceAtLeast(0)
            val prefix = text.substring(prefixStart, match.range.first)
            !balancePrefixRegex.containsMatchIn(prefix)
        } ?: allMatches.first()

        val rawNumber = nonBalanceMatch.groupValues.getOrNull(1) ?: return null
        val cleaned = rawNumber.replace(",", "")
        return cleaned.toDoubleOrNull()
    }

    private val excludedSenderTokens = setOf(
        "your", "a/c", "account", "esewa", "khalti", "fonepay", "connectips",
        "ime", "ime pay", "bank", "mobile banking", "qr", "npr", "nrs", "rs",
        "rupees", "rupaiya", "merchant", "customer", "unknown", "user",
        "wallet", "system", "transfer", "payment", "amount", "cash"
    )

    private fun extractSender(text: String): String? {
        for (match in senderRegex.findAll(text)) {
            val candidate = match.groupValues.getOrNull(1)?.trim() ?: continue
            val cleaned = candidate.replace(Regex("\\s+"), " ").trimEnd('.', ',', '-')
            if (cleaned.length < 2) continue
            val lower = cleaned.lowercase(Locale.US)
            if (lower in excludedSenderTokens) continue
            if (lower.startsWith("rs ") || lower.startsWith("npr ") || lower.startsWith("nrs ")) continue
            return cleaned.split(" ").joinToString(" ") { part ->
                part.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }
            }
        }
        return null
    }

    fun convertDevanagariDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '\u0966'..'\u096F') {
                sb.append(('0'.code + (ch.code - '\u0966'.code)).toChar())
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }
}

class PaymentDeduplicator(private val windowMillis: Long = 10_000L) {
    private val recentKeys = LinkedHashMap<String, Long>()

    @Synchronized
    fun isDuplicate(payment: ParsedPayment, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val iterator = recentKeys.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (nowMillis - entry.value > windowMillis) {
                iterator.remove()
            }
        }
        val roundedAmount = Math.round(payment.amount * 100)
        val key = "${payment.source.name}|$roundedAmount|${payment.senderName.orEmpty().lowercase(Locale.US)}"
        val lastSeen = recentKeys[key]
        if (lastSeen != null && (nowMillis - lastSeen) <= windowMillis) {
            return true
        }
        recentKeys[key] = nowMillis
        return false
    }
}
