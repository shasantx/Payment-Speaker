package com.paisabol.paymentspeaker.domain

import com.paisabol.paymentspeaker.model.PaymentSource
import com.paisabol.paymentspeaker.model.SpeechLanguage
import java.util.Locale
import kotlin.math.roundToLong

object NepaliSpeechFormatter {

    fun buildAnnouncement(
        amount: Double,
        source: PaymentSource,
        sourceLabel: String,
        language: SpeechLanguage,
        includeSourceApp: Boolean
    ): String {
        val totalPaisa = (amount * 100.0).roundToLong()
        val rupees = totalPaisa / 100L
        val paisa = (totalPaisa % 100L).toInt()

        return when (language) {
            SpeechLanguage.NEPALI -> buildNepaliDevanagari(rupees, paisa, source, includeSourceApp)
            SpeechLanguage.ROMAN_NEPALI -> buildRomanNepali(rupees, paisa, sourceLabel, includeSourceApp)
            SpeechLanguage.ENGLISH -> buildEnglish(rupees, paisa, sourceLabel, includeSourceApp)
        }
    }

    private fun buildNepaliDevanagari(
        rupees: Long,
        paisa: Int,
        source: PaymentSource,
        includeSourceApp: Boolean
    ): String {
        val rupeesNp = toDevanagariDigits(rupees.toString())
        val amountPhrase = if (paisa > 0) {
            val paisaNp = toDevanagariDigits(paisa.toString())
            "$rupeesNp रुपैयाँ $paisaNp पैसा प्राप्त भयो"
        } else {
            "$rupeesNp रुपैयाँ प्राप्त भयो"
        }

        return if (includeSourceApp && source != PaymentSource.OTHER) {
            "${source.nepaliName} मार्फत $amountPhrase"
        } else {
            amountPhrase
        }
    }

    private fun buildRomanNepali(
        rupees: Long,
        paisa: Int,
        sourceLabel: String,
        includeSourceApp: Boolean
    ): String {
        val amountPhrase = if (paisa > 0) {
            "$rupees rupaiya $paisa paisa prapta bhayo"
        } else {
            "$rupees rupaiya prapta bhayo"
        }
        return if (includeSourceApp && sourceLabel.isNotBlank() && sourceLabel != "Payment") {
            "$sourceLabel bata $amountPhrase"
        } else {
            amountPhrase
        }
    }

    private fun buildEnglish(
        rupees: Long,
        paisa: Int,
        sourceLabel: String,
        includeSourceApp: Boolean
    ): String {
        val rupeeWord = if (rupees == 1L) "rupee" else "rupees"
        val amountPhrase = if (paisa > 0) {
            "$rupees $rupeeWord and $paisa paisa received"
        } else {
            "$rupees $rupeeWord received"
        }
        return if (includeSourceApp && sourceLabel.isNotBlank() && sourceLabel != "Payment") {
            "$amountPhrase via $sourceLabel"
        } else {
            amountPhrase
        }
    }

    fun toDevanagariDigits(ascii: String): String {
        val sb = StringBuilder(ascii.length)
        for (ch in ascii) {
            if (ch in '0'..'9') {
                sb.append(('\u0966'.code + (ch.code - '0'.code)).toChar())
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Formats amount in Nepali/South-Asian numbering style:
     * e.g., 500.0 -> "Rs. 500", 12500.5 -> "Rs. 12,500.50", 150000.0 -> "Rs. 1,50,000"
     */
    fun formatNepaliCurrency(amount: Double): String {
        val negative = amount < 0
        val absVal = kotlin.math.abs(amount)
        val totalPaisa = (absVal * 100.0).roundToLong()
        val whole = totalPaisa / 100L
        val frac = (totalPaisa % 100L).toInt()

        val formattedWhole = formatSouthAsianGrouping(whole)
        val prefix = if (negative) "-Rs. " else "Rs. "
        return if (frac > 0) {
            val fracStr = String.format(Locale.US, "%02d", frac)
            "$prefix$formattedWhole.$fracStr"
        } else {
            "$prefix$formattedWhole"
        }
    }

    private fun formatSouthAsianGrouping(number: Long): String {
        val str = number.toString()
        if (str.length <= 3) return str
        val lastThree = str.substring(str.length - 3)
        val rest = str.substring(0, str.length - 3)
        val sb = StringBuilder()
        var count = 0
        for (i in rest.length - 1 downTo 0) {
            sb.append(rest[i])
            count++
            if (count == 2 && i != 0) {
                sb.append(',')
                count = 0
            }
        }
        return sb.reverse().toString() + "," + lastThree
    }
}
