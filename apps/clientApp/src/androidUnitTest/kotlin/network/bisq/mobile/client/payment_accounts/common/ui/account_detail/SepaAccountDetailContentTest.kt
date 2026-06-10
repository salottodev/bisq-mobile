package network.bisq.mobile.client.payment_accounts.common.ui.account_detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import network.bisq.mobile.client.common.test_utils.TestApplication
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.country.Country
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.currency.FiatCurrency
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.sepa.SepaAccount
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.sepa.SepaAccountPayload
import network.bisq.mobile.client.payment_accounts.presentation.common.ui.account_detail.sepa.SepaAccountDetailContent
import network.bisq.mobile.domain.model.account.fiat.FiatPaymentMethodChargebackRisk
import network.bisq.mobile.i18n.I18nSupport
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.utils.LocalIsTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(application = TestApplication::class)
@RunWith(AndroidJUnit4::class)
class SepaAccountDetailContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        I18nSupport.setLanguage()
    }

    private fun setTestContent(account: SepaAccount) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalIsTest provides true) {
                BisqTheme {
                    SepaAccountDetailContent(account = account)
                }
            }
        }
    }

    @Test
    fun `when sepa review renders then shows base account details`() {
        setTestContent(sampleAccount())

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("SEPA").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.createAccount.accountData.country".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Germany").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.currency".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("EUR (Euro)").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.holderName".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Alice Doe").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.sepa.iban".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("DE89370400440532013000").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.sepa.bic".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("DEUTDEFF").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.createAccount.accountData.sepa.acceptCountries".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("France, Germany, Spain").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("5000.00 EUR").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("5 days").assertCountEquals(1)
    }

    @Test
    fun `when chargeback risk is present then badge is displayed`() {
        setTestContent(sampleAccount(chargebackRisk = FiatPaymentMethodChargebackRisk.VERY_LOW))

        composeTestRule.waitForIdle()
        composeTestRule
            .onAllNodesWithText(
                "paymentAccounts.createAccount.paymentMethod.risk.veryLow".i18n(),
                substring = true,
            ).assertCountEquals(1)
    }

    @Test
    fun `when chargeback risk is absent then badge is hidden`() {
        setTestContent(sampleAccount(chargebackRisk = null))

        composeTestRule.waitForIdle()
        composeTestRule
            .onAllNodesWithText(
                "paymentAccounts.createAccount.paymentMethod.risk.veryLow".i18n(),
                substring = true,
            ).assertCountEquals(0)
    }

    private fun sampleAccount(
        chargebackRisk: FiatPaymentMethodChargebackRisk? = null,
    ): SepaAccount =
        SepaAccount(
            accountName = "SEPA Main",
            accountPayload =
                SepaAccountPayload(
                    chargebackRisk = chargebackRisk,
                    paymentMethodName = "SEPA",
                    currency = FiatCurrency("EUR", "Euro"),
                    country = Country("DE", "Germany"),
                    acceptedCountries = listOf(Country("DE", "Germany"), Country("ES", "Spain"), Country("FR", "France")),
                    holderName = "Alice Doe",
                    iban = "DE89370400440532013000",
                    bic = "DEUTDEFF",
                ),
            creationDate = "Apr 3, 2026",
            tradeLimitInfo = "5000.00 EUR",
            tradeDuration = "5 days",
        )
}
