package network.bisq.mobile.client.payment_accounts.presentation.create_payment_account.step3_account_review.mapping

import network.bisq.mobile.client.payment_accounts.domain.model.fiat.FiatPaymentMethod
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.country.Country
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.currency.FiatCurrency
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.sepa.CreateSepaAccount
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.sepa.CreateSepaAccountPayload
import network.bisq.mobile.data.replicated.account.payment_method.FiatPaymentRail
import network.bisq.mobile.domain.model.account.fiat.FiatPaymentMethodChargebackRisk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SepaReviewMappingTest {
    @Test
    fun `toReviewPaymentAccount maps create account and payment method fields correctly`() {
        val createAccount =
            CreateSepaAccount(
                accountName = "SEPA Main",
                accountPayload =
                    CreateSepaAccountPayload(
                        selectedCountryCode = "DE",
                        acceptedCountryCodes = listOf("FR", "DE", "ES"),
                        holderName = "Alice Doe",
                        iban = "DE89370400440532013000",
                        bic = "DEUTDEFF",
                    ),
            )
        val paymentMethod = samplePaymentMethod()

        val account = createAccount.toReviewPaymentAccount(paymentMethod)

        assertEquals("SEPA Main", account.accountName)
        assertEquals(FiatPaymentMethodChargebackRisk.VERY_LOW, account.accountPayload.chargebackRisk)
        assertEquals("SEPA", account.accountPayload.paymentMethodName)
        assertEquals(FiatCurrency(code = "EUR", name = "Euro"), account.accountPayload.currency)
        assertEquals(Country(code = "DE", name = "Germany"), account.accountPayload.country)
        assertEquals(
            listOf(
                Country(code = "FR", name = "France"),
                Country(code = "DE", name = "Germany"),
                Country(code = "ES", name = "Spain"),
            ),
            account.accountPayload.acceptedCountries,
        )
        assertEquals("Alice Doe", account.accountPayload.holderName)
        assertEquals("DE89370400440532013000", account.accountPayload.iban)
        assertEquals("DEUTDEFF", account.accountPayload.bic)
        assertNull(account.creationDate)
        assertEquals("5000.00 EUR", account.tradeLimitInfo)
        assertEquals("5 days", account.tradeDuration)
    }

    @Test
    fun `toReviewPaymentAccount falls back to country code when selected or accepted country is unsupported`() {
        val createAccount =
            CreateSepaAccount(
                accountName = "SEPA Main",
                accountPayload =
                    CreateSepaAccountPayload(
                        selectedCountryCode = "XX",
                        acceptedCountryCodes = listOf("DE", "YY"),
                        holderName = "Alice Doe",
                        iban = "DE89370400440532013000",
                        bic = "DEUTDEFF",
                    ),
            )
        val paymentMethod = samplePaymentMethod()

        val account = createAccount.toReviewPaymentAccount(paymentMethod)

        assertEquals(Country(code = "XX", name = "XX"), account.accountPayload.country)
        assertEquals(
            listOf(
                Country(code = "DE", name = "Germany"),
                Country(code = "YY", name = "YY"),
            ),
            account.accountPayload.acceptedCountries,
        )
    }

    @Test
    fun `toReviewPaymentAccount uses Euro fallback when payment method has no supported currencies`() {
        val createAccount =
            CreateSepaAccount(
                accountName = "SEPA Main",
                accountPayload =
                    CreateSepaAccountPayload(
                        selectedCountryCode = "DE",
                        acceptedCountryCodes = listOf("DE"),
                        holderName = "Alice Doe",
                        iban = "DE89370400440532013000",
                        bic = "DEUTDEFF",
                    ),
            )
        val paymentMethod = samplePaymentMethod(supportedCurrencies = emptyList())

        val account = createAccount.toReviewPaymentAccount(paymentMethod)

        assertEquals(FiatCurrency(code = "EUR", name = "Euro"), account.accountPayload.currency)
    }

    private fun samplePaymentMethod(
        supportedCurrencies: List<FiatCurrency> = listOf(FiatCurrency(code = "EUR", name = "Euro")),
    ): FiatPaymentMethod =
        FiatPaymentMethod(
            paymentRail = FiatPaymentRail.SEPA,
            name = "SEPA",
            supportedCurrencies = supportedCurrencies,
            supportedCountries =
                listOf(
                    Country(code = "DE", name = "Germany"),
                    Country(code = "ES", name = "Spain"),
                    Country(code = "FR", name = "France"),
                ),
            matchesAllCountries = false,
            chargebackRisk = FiatPaymentMethodChargebackRisk.VERY_LOW,
            tradeLimitInfo = "5000.00 EUR",
            tradeDuration = "5 days",
        )
}
