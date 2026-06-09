package network.bisq.mobile.client.payment_accounts.data.mapping.fiat

import network.bisq.mobile.client.payment_accounts.data.model.fiat.ach_transfer.AchTransferAccountDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.ach_transfer.AchTransferAccountPayloadDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.ach_transfer.CreateAchTransferAccountDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.BankAccountTypeDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.CountryDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.FiatCurrencyDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.FiatPaymentRailDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.payment_method.FiatPaymentMethodChargebackRiskDto
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.ach_transfer.CreateAchTransferAccount
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.ach_transfer.CreateAchTransferAccountPayload
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.bank.BankAccountType
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.country.Country
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.currency.FiatCurrency
import network.bisq.mobile.domain.model.account.fiat.FiatPaymentMethodChargebackRisk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AchTransferAccountMappingTest {
    @Test
    fun `toDomain maps all AchTransferAccountDto fields correctly`() {
        val dto =
            AchTransferAccountDto(
                accountName = "ACH Main",
                accountPayload =
                    AchTransferAccountPayloadDto(
                        chargebackRisk = FiatPaymentMethodChargebackRiskDto.MODERATE,
                        paymentMethodName = "ACH",
                        currency = FiatCurrencyDto(code = "USD", name = "US Dollar"),
                        country = CountryDto(code = "US", name = "United States"),
                        holderName = "Alice Doe",
                        holderAddress = "123 Main St",
                        bankName = "Bisq Bank",
                        routingNr = "123456789",
                        accountNr = "000123456789",
                        bankAccountType = BankAccountTypeDto.CHECKING,
                    ),
                creationDate = "2026-05-10",
                tradeLimitInfo = "5000.00 USD",
                tradeDuration = "5 days",
            )

        val domain = dto.toDomain()

        assertEquals("ACH Main", domain.accountName)
        assertEquals(FiatPaymentMethodChargebackRisk.MODERATE, domain.accountPayload.chargebackRisk)
        assertEquals("ACH", domain.accountPayload.paymentMethodName)
        assertEquals(FiatCurrency(code = "USD", name = "US Dollar"), domain.accountPayload.currency)
        assertEquals(Country(code = "US", name = "United States"), domain.accountPayload.country)
        assertEquals("Alice Doe", domain.accountPayload.holderName)
        assertEquals("123 Main St", domain.accountPayload.holderAddress)
        assertEquals("Bisq Bank", domain.accountPayload.bankName)
        assertEquals("123456789", domain.accountPayload.routingNr)
        assertEquals("000123456789", domain.accountPayload.accountNr)
        assertEquals(BankAccountType.CHECKING, domain.accountPayload.bankAccountType)
        assertEquals("2026-05-10", domain.creationDate)
        assertEquals("5000.00 USD", domain.tradeLimitInfo)
        assertEquals("5 days", domain.tradeDuration)
    }

    @Test
    fun `toDto maps create AchTransferAccount fields correctly`() {
        val domain =
            CreateAchTransferAccount(
                accountName = "ACH Main",
                accountPayload =
                    CreateAchTransferAccountPayload(
                        holderName = "Alice Doe",
                        holderAddress = "123 Main St",
                        bankName = "Bisq Bank",
                        routingNr = "123456789",
                        accountNr = "000123456789",
                        bankAccountType = BankAccountType.SAVINGS,
                    ),
            )

        val dto = domain.toDto()

        assertIs<CreateAchTransferAccountDto>(dto)
        assertEquals(FiatPaymentRailDto.ACH_TRANSFER, dto.paymentRail)
        assertEquals("ACH Main", dto.accountName)
        assertEquals("Alice Doe", dto.accountPayload.holderName)
        assertEquals("123 Main St", dto.accountPayload.holderAddress)
        assertEquals("Bisq Bank", dto.accountPayload.bankName)
        assertEquals("123456789", dto.accountPayload.routingNr)
        assertEquals("000123456789", dto.accountPayload.accountNr)
        assertEquals(BankAccountTypeDto.SAVINGS, dto.accountPayload.bankAccountType)
    }
}
