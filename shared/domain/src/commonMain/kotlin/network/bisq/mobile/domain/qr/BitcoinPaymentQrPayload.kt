package network.bisq.mobile.domain.qr

private const val SATS_PER_BTC = 100_000_000L
private const val BTC_DECIMALS = 8
private val schemePrefix = Regex("^(bitcoin|lightning):(//)?", RegexOption.IGNORE_CASE)

// BOLT 11 mainnet prefix with an amount: "lnbc", digits, optional multiplier, then the "1" separator (same as bisq2).
private val invoiceAmountPrefix = Regex("^lnbc[0-9]+[munp]?1", RegexOption.IGNORE_CASE)

/**
 * What the seller's QR encodes: a BIP21 URI with the trade amount on-chain, the raw invoice for Lightning.
 * A leading `bitcoin:`/`lightning:` scheme (and, on-chain, any `?query`) in the buyer's data is dropped first,
 * since the buyer can submit such data through the "proceed anyway" path and desktop buyers are not normalized here.
 */
fun bitcoinPaymentQrPayload(
    paymentData: String,
    isLightning: Boolean,
    amountSats: Long,
): String {
    val bare = paymentData.trim().replace(schemePrefix, "")
    if (isLightning) return bare
    val address = bare.substringBefore('?')
    return "bitcoin:$address?amount=${satsToBtcString(amountSats)}"
}

/** Schemed URI for "Open in wallet": the BIP21 as is, the invoice with a `lightning:` prefix. */
fun bitcoinPaymentWalletUri(
    payload: String,
    isLightning: Boolean,
): String = if (isLightning) "lightning:$payload" else payload

/** Locale-invariant BTC decimal from sats with trailing zeros trimmed: 250000 -> "0.0025", 100000000 -> "1". */
fun satsToBtcString(sats: Long): String {
    val whole = sats / SATS_PER_BTC
    val fraction = (sats % SATS_PER_BTC).toString().padStart(BTC_DECIMALS, '0').trimEnd('0')
    return if (fraction.isEmpty()) whole.toString() else "$whole.$fraction"
}

/** Whether a BOLT 11 invoice carries an amount; an amount-less invoice lets the payer enter it, so the hint must say so. */
fun lightningInvoiceHasAmount(invoice: String): Boolean = invoiceAmountPrefix.containsMatchIn(invoice.trim())
