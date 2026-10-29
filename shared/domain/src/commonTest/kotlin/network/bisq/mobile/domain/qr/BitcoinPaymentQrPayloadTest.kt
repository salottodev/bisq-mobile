package network.bisq.mobile.domain.qr

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitcoinPaymentQrPayloadTest {
    private val address = "bc1qw508d6qejxtdg4y5r3zarvary0c5xw7kv8f3t4"
    private val invoice = "lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdq5xysxxatsyp3k7enxv4js"

    @Test
    fun `on-chain payload is a bip21 uri with the amount`() {
        assertEquals("bitcoin:$address?amount=0.0025", bitcoinPaymentQrPayload(address, isLightning = false, amountSats = 250_000))
    }

    @Test
    fun `lightning payload is the raw invoice`() {
        assertEquals(invoice, bitcoinPaymentQrPayload(invoice, isLightning = true, amountSats = 250_000))
        assertEquals(invoice, bitcoinPaymentQrPayload(invoice, isLightning = true, amountSats = 0))
    }

    @Test
    fun `sats to btc trims trailing zeros and never uses scientific notation`() {
        assertEquals("0.00000001", satsToBtcString(1))
        assertEquals("0.0025", satsToBtcString(250_000))
        assertEquals("1", satsToBtcString(100_000_000))
        assertEquals("1.5", satsToBtcString(150_000_000))
        assertEquals("1.23456789", satsToBtcString(123_456_789))
    }

    @Test
    fun `wallet uri prefixes lightning scheme only`() {
        assertEquals("lightning:$invoice", bitcoinPaymentWalletUri(invoice, isLightning = true))
        assertEquals("bitcoin:$address?amount=0.0025", bitcoinPaymentWalletUri("bitcoin:$address?amount=0.0025", isLightning = false))
    }

    @Test
    fun `payment data with a scheme or query is not double-wrapped`() {
        assertEquals("bitcoin:$address?amount=0.0025", bitcoinPaymentQrPayload("BITCOIN:$address?label=x", isLightning = false, amountSats = 250_000))
        assertEquals(invoice, bitcoinPaymentQrPayload("lightning:$invoice", isLightning = true, amountSats = 0))
        assertEquals("bitcoin:$address?amount=0.00000001", bitcoinPaymentQrPayload("bitcoin://$address", isLightning = false, amountSats = 1))
    }

    @Test
    fun `lightning invoice amount detection follows the bolt11 prefix`() {
        assertTrue(lightningInvoiceHasAmount("lnbc2500u1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypq"))
        assertTrue(lightningInvoiceHasAmount("LNBC20M1PVJLUEZPP5QQQSYQCYQ5RQWZQFQQQSYQCYQ5RQWZQFQQQSYQCYQ5RQWZQFQYPQ"))
        assertFalse(lightningInvoiceHasAmount("lnbc1pvjluezpp5qqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqqqsyqcyq5rqwzqfqypqdpl2pkx2ctnv5sxxmmwwd5kgetjypeh2ursdae8g6twvus8g6rfwvs8qun0dfjkxaq8rkx3yf5tcsyz3d73gafnh3cax9rn449d9p5uxz9ezhhypd0elx87sjle52x86fux2ypatgddc6k63n7erqz25le42c4u4ecky03ylcqca784w"))
        assertFalse(lightningInvoiceHasAmount(""))
    }

    @Test
    fun `a lightning invoice is encoded in upper case for the compact qr mode`() {
        assertEquals(invoice.uppercase(), bitcoinPaymentQrCodeText(invoice, isLightning = true))
        assertEquals(invoice.uppercase(), bitcoinPaymentQrCodeText(invoice.uppercase(), isLightning = true))
    }

    @Test
    fun `qr code text keeps on-chain uris and non-invoice data as they are`() {
        val bip21 = "bitcoin:$address?amount=0.0025"
        assertEquals(bip21, bitcoinPaymentQrCodeText(bip21, isLightning = false))
        // Mixed case is not valid bech32, and anything else may not survive a case change.
        assertEquals("lnBC2500u1pvjluez", bitcoinPaymentQrCodeText("lnBC2500u1pvjluez", isLightning = true))
        assertEquals("lnurl1dp68gurn8ghj7?x=1", bitcoinPaymentQrCodeText("lnurl1dp68gurn8ghj7?x=1", isLightning = true))
    }
}
