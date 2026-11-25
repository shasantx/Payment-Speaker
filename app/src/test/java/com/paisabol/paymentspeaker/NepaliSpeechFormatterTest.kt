package com.paisabol.paymentspeaker

import com.paisabol.paymentspeaker.domain.NepaliSpeechFormatter
import com.paisabol.paymentspeaker.model.PaymentSource
import com.paisabol.paymentspeaker.model.SpeechLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class NepaliSpeechFormatterTest {

    @Test
    fun formatsNepaliWholeRupeesWithSource() {
        val text = NepaliSpeechFormatter.buildAnnouncement(
            amount = 500.0,
            source = PaymentSource.ESEWA,
            sourceLabel = "eSewa",
            language = SpeechLanguage.NEPALI,
            includeSourceApp = true
        )
        assertEquals("ई-सेवा मार्फत ५०० रुपैयाँ प्राप्त भयो", text)
    }

    @Test
    fun formatsNepaliRupeesAndPaisaWithoutSource() {
        val text = NepaliSpeechFormatter.buildAnnouncement(
            amount = 125.50,
            source = PaymentSource.KHALTI,
            sourceLabel = "Khalti",
            language = SpeechLanguage.NEPALI,
            includeSourceApp = false
        )
        assertEquals("१२५ रुपैयाँ ५० पैसा प्राप्त भयो", text)
    }

    @Test
    fun formatsRomanNepaliPhrase() {
        val text = NepaliSpeechFormatter.buildAnnouncement(
            amount = 1000.0,
            source = PaymentSource.KHALTI,
            sourceLabel = "Khalti",
            language = SpeechLanguage.ROMAN_NEPALI,
            includeSourceApp = true
        )
        assertEquals("Khalti bata 1000 rupaiya prapta bhayo", text)
    }

    @Test
    fun formatsSouthAsianCurrencyCommas() {
        assertEquals("Rs. 500", NepaliSpeechFormatter.formatNepaliCurrency(500.0))
        assertEquals("Rs. 12,450", NepaliSpeechFormatter.formatNepaliCurrency(12450.0))
        assertEquals("Rs. 1,50,000", NepaliSpeechFormatter.formatNepaliCurrency(150000.0))
        assertEquals("Rs. 1,250.50", NepaliSpeechFormatter.formatNepaliCurrency(1250.5))
        assertEquals("Rs. 100", NepaliSpeechFormatter.formatNepaliCurrency(99.996))
    }

    @Test
    fun formatsEnglishSingularAndPluralRupees() {
        val singular = NepaliSpeechFormatter.buildAnnouncement(
            amount = 1.0,
            source = PaymentSource.ESEWA,
            sourceLabel = "eSewa",
            language = SpeechLanguage.ENGLISH,
            includeSourceApp = true
        )
        assertEquals("1 rupee received via eSewa", singular)

        val plural = NepaliSpeechFormatter.buildAnnouncement(
            amount = 250.25,
            source = PaymentSource.BANK,
            sourceLabel = "Nabil Bank",
            language = SpeechLanguage.ENGLISH,
            includeSourceApp = true
        )
        assertEquals("250 rupees and 25 paisa received via Nabil Bank", plural)
    }
}
