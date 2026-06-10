package network.bisq.mobile.client.payment_accounts.data.mapping.fiat

import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.CountryDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.FiatCurrencyDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.FiatPaymentRailDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.payment_method.FiatPaymentMethodChargebackRiskDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.sepa.CreateSepaAccountDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.sepa.SepaAccountDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.sepa.SepaAccountPayloadDto
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.country.Country
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.currency.FiatCurrency
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.sepa.CreateSepaAccount
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.sepa.CreateSepaAccountPayload
import network.bisq.mobile.domain.model.account.fiat.FiatPaymentMethodChargebackRisk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SepaAccountMappingTest {
    @Test
    fun `toDomain maps all SepaAccountDto fields correctly`() {
        val dto =
            SepaAccountDto(
                accountName = "SEPA Main",
                accountPayload =
                    SepaAccountPayloadDto(
                        chargebackRisk = FiatPaymentMethodChargebackRiskDto.VERY_LOW,
                        paymentMethodName = "SEPA",
                        currency = FiatCurrencyDto(code = "EUR", name = "Euro"),
                        country = CountryDto(code = "DE", name = "Germany"),
                        acceptedCountries = listOf(CountryDto(code = "DE", name = "Germany"), CountryDto(code = "FR", name = "France"), CountryDto(code = "ES", name = "Spain")),
                        holderName = "Alice Doe",
                        iban = "DE89370400440532013000",
                        bic = "DEUTDEFF",
                    ),
                creationDate = "2026-05-10",
                tradeLimitInfo = "5000.00 EUR",
                tradeDuration = "5 days",
            )

        val domain = dto.toDomain()

        assertEquals("SEPA Main", domain.accountName)
        assertEquals(FiatPaymentMethodChargebackRisk.VERY_LOW, domain.accountPayload.chargebackRisk)
        assertEquals("SEPA", domain.accountPayload.paymentMethodName)
        assertEquals(FiatCurrency(code = "EUR", name = "Euro"), domain.accountPayload.currency)
        assertEquals(Country(code = "DE", name = "Germany"), domain.accountPayload.country)
        assertEquals(listOf(Country(code = "DE", name = "Germany"), Country(code = "FR", name = "France"), Country(code = "ES", name = "Spain")), domain.accountPayload.acceptedCountries)
        assertEquals("Alice Doe", domain.accountPayload.holderName)
        assertEquals("DE89370400440532013000", domain.accountPayload.iban)
        assertEquals("DEUTDEFF", domain.accountPayload.bic)
        assertEquals("2026-05-10", domain.creationDate)
        assertEquals("5000.00 EUR", domain.tradeLimitInfo)
        assertEquals("5 days", domain.tradeDuration)
    }

    @Test
    fun `toDto maps create SepaAccount fields correctly`() {
        val domain =
            CreateSepaAccount(
                accountName = "SEPA Main",
                accountPayload =
                    CreateSepaAccountPayload(
                        selectedCountryCode = "DE",
                        acceptedCountryCodes = listOf("DE", "FR", "ES"),
                        holderName = "Alice Doe",
                        iban = "DE89370400440532013000",
                        bic = "DEUTDEFF",
                    ),
            )

        val dto = domain.toDto()

        assertIs<CreateSepaAccountDto>(dto)
        assertEquals(FiatPaymentRailDto.SEPA, dto.paymentRail)
        assertEquals("SEPA Main", dto.accountName)
        assertEquals("DE", dto.accountPayload.selectedCountryCode)
        assertEquals(listOf("DE", "FR", "ES"), dto.accountPayload.acceptedCountryCodes)
        assertEquals("Alice Doe", dto.accountPayload.holderName)
        assertEquals("DE89370400440532013000", dto.accountPayload.iban)
        assertEquals("DEUTDEFF", dto.accountPayload.bic)
    }
}
