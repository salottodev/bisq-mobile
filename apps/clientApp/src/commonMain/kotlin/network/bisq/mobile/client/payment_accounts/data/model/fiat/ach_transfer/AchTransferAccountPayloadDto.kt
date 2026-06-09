package network.bisq.mobile.client.payment_accounts.data.model.fiat.ach_transfer

import kotlinx.serialization.Serializable
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.BankAccountTypeDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.CountryDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.FiatCurrencyDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.common.FiatPaymentAccountPayloadDto
import network.bisq.mobile.client.payment_accounts.data.model.fiat.payment_method.FiatPaymentMethodChargebackRiskDto
import network.bisq.mobile.domain.utils.maskTail

@Serializable
data class AchTransferAccountPayloadDto(
    override val chargebackRisk: FiatPaymentMethodChargebackRiskDto? = null,
    override val paymentMethodName: String,
    val currency: FiatCurrencyDto,
    val country: CountryDto,
    val holderName: String,
    val holderAddress: String,
    val bankName: String,
    val routingNr: String,
    val accountNr: String,
    val bankAccountType: BankAccountTypeDto,
) : FiatPaymentAccountPayloadDto {
    override fun toString(): String =
        "AchTransferAccountPayloadDto(paymentMethodName=$paymentMethodName, " +
            "currency=$currency, " +
            "country=$country, " +
            "holderName=***, " +
            "holderAddress=***," +
            "bankName=$bankName," +
            "routingNr=${routingNr.maskTail(2)}," +
            "accountNr=${accountNr.maskTail(4)}," +
            "bankAccountType=$bankAccountType, " +
            "chargebackRisk=$chargebackRisk)"
}
