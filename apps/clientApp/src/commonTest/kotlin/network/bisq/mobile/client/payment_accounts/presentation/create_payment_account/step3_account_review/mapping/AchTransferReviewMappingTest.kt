package network.bisq.mobile.client.payment_accounts.presentation.create_payment_account.step3_account_review.mapping

import network.bisq.mobile.client.payment_accounts.domain.model.fiat.FiatPaymentMethod
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.ach_transfer.CreateAchTransferAccount
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.ach_transfer.CreateAchTransferAccountPayload
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.bank.BankAccountType
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.country.Country
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.currency.FiatCurrency
import network.bisq.mobile.data.replicated.account.payment_method.FiatPaymentRail
import network.bisq.mobile.domain.model.account.fiat.FiatPaymentMethodChargebackRisk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AchTransferReviewMappingTest {
    @Test
    fun `toReviewPaymentAccount maps create account and payment method fields correctly`() {
        val createAccount =
            CreateAchTransferAccount(
                accountName = "ACH Main",
                accountPayload =
                    CreateAchTransferAccountPayload(
                        holderName = "Alice Doe",
                        holderAddress = "123 Main St",
                        bankName = "Bisq Bank",
                        routingNr = "123456789",
                        accountNr = "000123456789",
                        bankAccountType = BankAccountType.CHECKING,
                    ),
            )
        val paymentMethod = samplePaymentMethod()

        val account = createAccount.toReviewPaymentAccount(paymentMethod)

        assertEquals("ACH Main", account.accountName)
        assertEquals(FiatPaymentMethodChargebackRisk.MODERATE, account.accountPayload.chargebackRisk)
        assertEquals("ACH", account.accountPayload.paymentMethodName)
        assertEquals(FiatCurrency(code = "USD", name = "US Dollar"), account.accountPayload.currency)
        assertEquals(Country(code = "US", name = "United States"), account.accountPayload.country)
        assertEquals("Alice Doe", account.accountPayload.holderName)
        assertEquals("123 Main St", account.accountPayload.holderAddress)
        assertEquals("Bisq Bank", account.accountPayload.bankName)
        assertEquals("123456789", account.accountPayload.routingNr)
        assertEquals("000123456789", account.accountPayload.accountNr)
        assertEquals(BankAccountType.CHECKING, account.accountPayload.bankAccountType)
        assertNull(account.creationDate)
        assertEquals("5000.00 USD", account.tradeLimitInfo)
        assertEquals("5 days", account.tradeDuration)
    }

    private fun samplePaymentMethod(): FiatPaymentMethod =
        FiatPaymentMethod(
            paymentRail = FiatPaymentRail.ACH_TRANSFER,
            name = "ACH",
            supportedCurrencies = listOf(FiatCurrency(code = "USD", name = "US Dollar")),
            supportedCountries = listOf(Country(code = "US", name = "United States")),
            matchesAllCountries = false,
            chargebackRisk = FiatPaymentMethodChargebackRisk.MODERATE,
            tradeLimitInfo = "5000.00 USD",
            tradeDuration = "5 days",
        )
}
