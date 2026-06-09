package network.bisq.mobile.client.payment_accounts.common.ui.account_detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import network.bisq.mobile.client.common.test_utils.TestApplication
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.ach_transfer.AchTransferAccount
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.ach_transfer.AchTransferAccountPayload
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.bank.BankAccountType
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.country.Country
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.currency.FiatCurrency
import network.bisq.mobile.client.payment_accounts.presentation.common.ui.account_detail.ach_transfer.AchTransferAccountDetailContent
import network.bisq.mobile.client.payment_accounts.presentation.common.util.toDisplayString
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
class AchTransferAccountDetailContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        I18nSupport.setLanguage()
    }

    private fun setTestContent(account: AchTransferAccount) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalIsTest provides true) {
                BisqTheme {
                    AchTransferAccountDetailContent(account = account)
                }
            }
        }
    }

    @Test
    fun `when ach transfer review renders then shows base account details`() {
        setTestContent(sampleAccount())

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("ACH").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.country".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("United States").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.currency".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("USD (US Dollar)").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.holderName".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Alice Doe").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.holderAddress".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("123 Main St").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.bank.bankName".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Bisq Bank").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.achTransfer.routingNr".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("123456789").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.accountNr".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("000123456789").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("paymentAccounts.bank.bankAccountType".i18n()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText(BankAccountType.CHECKING.toDisplayString()).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("5000.00 USD").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("5 days").assertCountEquals(1)
    }

    @Test
    fun `when chargeback risk is present then badge is displayed`() {
        setTestContent(sampleAccount(chargebackRisk = FiatPaymentMethodChargebackRisk.MODERATE))

        composeTestRule.waitForIdle()
        composeTestRule
            .onAllNodesWithText(
                "paymentAccounts.createAccount.paymentMethod.risk.moderate".i18n(),
                substring = true,
            ).assertCountEquals(1)
    }

    @Test
    fun `when chargeback risk is absent then badge is hidden`() {
        setTestContent(sampleAccount(chargebackRisk = null))

        composeTestRule.waitForIdle()
        composeTestRule
            .onAllNodesWithText(
                "paymentAccounts.createAccount.paymentMethod.risk.moderate".i18n(),
                substring = true,
            ).assertCountEquals(0)
    }

    private fun sampleAccount(
        chargebackRisk: FiatPaymentMethodChargebackRisk? = null,
    ): AchTransferAccount =
        AchTransferAccount(
            accountName = "ACH Main",
            accountPayload =
                AchTransferAccountPayload(
                    chargebackRisk = chargebackRisk,
                    paymentMethodName = "ACH",
                    currency = FiatCurrency("USD", "US Dollar"),
                    country = Country("US", "United States"),
                    holderName = "Alice Doe",
                    holderAddress = "123 Main St",
                    bankName = "Bisq Bank",
                    routingNr = "123456789",
                    accountNr = "000123456789",
                    bankAccountType = BankAccountType.CHECKING,
                ),
            creationDate = "Apr 3, 2026",
            tradeLimitInfo = "5000.00 USD",
            tradeDuration = "5 days",
        )
}
