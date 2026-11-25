package com.paisabol.paymentspeaker

import com.paisabol.paymentspeaker.domain.PaymentDeduplicator
import com.paisabol.paymentspeaker.domain.PaymentParser
import com.paisabol.paymentspeaker.model.PaymentSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaymentParserTest {

    @Test
    fun parsesEsewaIncomingPayment() {
        val parsed = PaymentParser.parse(
            packageName = "com.f1soft.esewa",
            title = "Payment Received",
            text = "You have received NPR 1,250.00 from RAM SHARMA (9841234567)."
        )
        assertNotNull(parsed)
        assertEquals(1250.0, parsed!!.amount, 0.001)
        assertEquals(PaymentSource.ESEWA, parsed.source)
        assertEquals("eSewa", parsed.sourceLabel)
        assertEquals("Ram Sharma", parsed.senderName)
    }

    @Test
    fun parsesKhaltiIncomingPayment() {
        val parsed = PaymentParser.parse(
            packageName = "com.khalti",
            title = "Khalti",
            text = "Your Khalti account has been credited with Rs. 500.50 from Sita Gurung."
        )
        assertNotNull(parsed)
        assertEquals(500.50, parsed!!.amount, 0.001)
        assertEquals(PaymentSource.KHALTI, parsed.source)
        assertEquals("Sita Gurung", parsed.senderName)
    }

    @Test
    fun parsesVerifiedNepaliBankSmsAlert() {
        val parsed = PaymentParser.parse(
            packageName = "com.google.android.apps.messaging",
            title = "NABIL_ALERT",
            text = "Dear Customer, Your A/C XX1234 is credited by NPR 3,500.00 via Fonepay QR."
        )
        assertNotNull(parsed)
        assertEquals(3500.0, parsed!!.amount, 0.001)
        assertEquals(PaymentSource.FONEPAY, parsed.source)
    }

    @Test
    fun rejectsSpoofedPersonalPhoneNumberSms() {
        val parsed = PaymentParser.parse(
            packageName = "com.google.android.apps.messaging",
            title = "+9779841000000",
            text = "You have received NPR 5,000.00 from eSewa."
        )
        assertNull(parsed)
    }

    @Test
    fun rejectsSpoofedWhatsAppChatMessage() {
        val parsed = PaymentParser.parse(
            packageName = "com.whatsapp",
            title = "Customer",
            text = "You have received NPR 2,000.00 in eSewa from Ram"
        )
        assertNull(parsed)
    }

    @Test
    fun parsesDevanagariNepaliNotification() {
        val parsed = PaymentParser.parse(
            packageName = "com.f1soft.esewa",
            title = "ई-सेवा",
            text = "तपाईंको खातामा रु. ७५० प्राप्त भयो।"
        )
        assertNotNull(parsed)
        assertEquals(750.0, parsed!!.amount, 0.001)
        assertEquals(PaymentSource.ESEWA, parsed.source)
    }

    @Test
    fun rejectsOutgoingDebitNotification() {
        val parsed = PaymentParser.parse(
            packageName = "com.f1soft.esewa",
            title = "Payment Sent",
            text = "NPR 500.00 has been debited from your account and sent to Hari Thapa."
        )
        assertNull(parsed)
    }

    @Test
    fun rejectsOtpNotification() {
        val parsed = PaymentParser.parse(
            packageName = "com.f1soft.esewa",
            title = "Verification Code",
            text = "Your OTP is 458921 for NPR 500 payment."
        )
        assertNull(parsed)
    }

    @Test
    fun deduplicatesRepeatedAlertsWithinWindow() {
        val dedup = PaymentDeduplicator(windowMillis = 10_000L)
        val parsed = PaymentParser.parse(
            packageName = "com.khalti",
            title = "Khalti",
            text = "You have received Rs. 200 from Bikash Rai"
        )!!
        assertFalse(dedup.isDuplicate(parsed, nowMillis = 1_000L))
        assertTrue(dedup.isDuplicate(parsed, nowMillis = 5_000L))
        assertFalse(dedup.isDuplicate(parsed, nowMillis = 15_000L))
    }

    @Test
    fun rejectsSpoofedSavedContactNameSms() {
        val parsed = PaymentParser.parse(
            packageName = "com.google.android.apps.messaging",
            title = "Bikash",
            text = "You have received Rs. 1,500.00 in your eSewa account."
        )
        assertNull(parsed)
    }

    @Test
    fun ignoresCurrencyTokenAsSenderName() {
        val parsed = PaymentParser.parse(
            packageName = "com.google.android.apps.messaging",
            title = "NABIL_ALERT",
            text = "Your A/C XX1234 is credited by Rs. 3,500.00 via Fonepay QR."
        )
        assertNotNull(parsed)
        assertEquals(3500.0, parsed!!.amount, 0.001)
        assertNull(parsed.senderName)
    }

    @Test
    fun parsesSentYouAndSentToYourIncomingPayments() {
        val parsed1 = PaymentParser.parse(
            packageName = "com.f1soft.esewa",
            title = "eSewa",
            text = "Suman Adhikari sent you Rs. 450.00."
        )
        assertNotNull(parsed1)
        assertEquals(450.0, parsed1!!.amount, 0.001)

        val parsed2 = PaymentParser.parse(
            packageName = "com.khalti",
            title = "Khalti",
            text = "Rs. 900 has been sent to your Khalti wallet from Gita KC."
        )
        assertNotNull(parsed2)
        assertEquals(900.0, parsed2!!.amount, 0.001)
        assertEquals("Gita Kc", parsed2.senderName)
    }

    @Test
    fun skipsBalanceAmountWhenCreditedAmountFollows() {
        val parsed = PaymentParser.parse(
            packageName = "com.nabilbank.mobile",
            title = "Credit Alert",
            text = "Avl Bal: NPR 25,000.00. Credited NPR 650.00 via Fonepay."
        )
        assertNotNull(parsed)
        assertEquals(650.0, parsed!!.amount, 0.001)
    }
}
