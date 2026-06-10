package network.bisq.mobile.client.payment_accounts.presentation.create_payment_account.step2_payment_account_form.form.sepa

import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.FiatPaymentMethod
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.country.Country
import network.bisq.mobile.client.payment_accounts.domain.model.fiat.common.currency.FiatCurrency
import network.bisq.mobile.client.payment_accounts.presentation.create_payment_account.step2_payment_account_form.form.AccountFormUiAction
import network.bisq.mobile.client.test_utils.TestCoroutineJobsManager
import network.bisq.mobile.data.replicated.account.payment_method.FiatPaymentRail
import network.bisq.mobile.domain.model.account.fiat.FiatPaymentMethodChargebackRisk
import network.bisq.mobile.domain.utils.CoroutineJobsManager
import network.bisq.mobile.presentation.common.ui.base.GlobalUiManager
import network.bisq.mobile.presentation.common.ui.navigation.manager.NavigationManager
import network.bisq.mobile.presentation.main.MainPresenter
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SepaFormPresenterTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mainPresenter: MainPresenter
    private lateinit var presenter: SepaFormPresenter

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mainPresenter = mockk(relaxed = true)

        runCatching { stopKoin() }
        startKoin {
            modules(
                module {
                    single<NavigationManager> { mockk(relaxed = true) }
                    factory<CoroutineJobsManager> { TestCoroutineJobsManager(testDispatcher) }
                    single<GlobalUiManager> { mockk(relaxed = true) }
                },
            )
        }

        presenter = SepaFormPresenter(mainPresenter = mainPresenter)
        presenter.initialize(samplePaymentMethod())
    }

    @AfterTest
    fun tearDown() {
        presenter.onDestroy()
        runCatching { stopKoin() }
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize sorts countries and selects all accepted countries by default`() {
        val state = presenter.uiState.value

        assertEquals(listOf("FR", "DE", "ES"), state.countries.map { country -> country.code })
        assertEquals(listOf("FR", "DE", "ES"), state.availableAcceptedCountries.map { country -> country.id })
        assertEquals(setOf("FR", "DE", "ES"), state.selectedAcceptedCountryCodes)
    }

    @Test
    fun `when country selected then updates selected country and clears country error`() =
        runTest(testDispatcher) {
            presenter.onCommonAction(AccountFormUiAction.OnNextClick)
            assertNotNull(presenter.uiState.value.countryErrorMessage)

            presenter.onAction(SepaFormUiAction.OnCountrySelect(1))

            assertEquals(1, presenter.uiState.value.selectedCountryIndex)
            assertEquals(Country(code = "DE", name = "Germany"), presenter.uiState.value.selectedCountry)
            assertNull(presenter.uiState.value.countryErrorMessage)
        }

    @Test
    fun `when holder name changes then updates holderNameEntry`() =
        runTest(testDispatcher) {
            presenter.onAction(SepaFormUiAction.OnHolderNameChange("John Doe"))
            assertEquals("John Doe", presenter.uiState.value.holderNameEntry.value)
        }

    @Test
    fun `when iban changes then updates ibanEntry`() =
        runTest(testDispatcher) {
            presenter.onAction(SepaFormUiAction.OnIbanChange("DE89370400440532013000"))
            assertEquals("DE89370400440532013000", presenter.uiState.value.ibanEntry.value)
        }

    @Test
    fun `when bic changes then updates bicEntry`() =
        runTest(testDispatcher) {
            presenter.onAction(SepaFormUiAction.OnBicChange("DEUTDEFF"))
            assertEquals("DEUTDEFF", presenter.uiState.value.bicEntry.value)
        }

    @Test
    fun `when accepted countries picker opens and closes then state updates and search clears`() =
        runTest(testDispatcher) {
            presenter.onAction(SepaFormUiAction.OnOpenAcceptedCountriesPicker)
            presenter.onAction(SepaFormUiAction.OnAcceptedCountrySearchChange("ger"))

            assertTrue(presenter.uiState.value.isAcceptedCountriesPickerOpen)
            assertEquals("ger", presenter.uiState.value.acceptedCountrySearchQuery)

            presenter.onAction(SepaFormUiAction.OnCloseAcceptedCountriesPicker)

            assertFalse(presenter.uiState.value.isAcceptedCountriesPickerOpen)
            assertEquals("", presenter.uiState.value.acceptedCountrySearchQuery)
        }

    @Test
    fun `when accepted country search changes then updates query`() =
        runTest(testDispatcher) {
            presenter.onAction(SepaFormUiAction.OnAcceptedCountrySearchChange("fra"))
            assertEquals("fra", presenter.uiState.value.acceptedCountrySearchQuery)
        }

    @Test
    fun `when accepted country toggled then updates selected set`() =
        runTest(testDispatcher) {
            presenter.onAction(SepaFormUiAction.OnAcceptedCountryToggle("DE"))

            assertEquals(setOf("FR", "ES"), presenter.uiState.value.selectedAcceptedCountryCodes)

            presenter.onAction(SepaFormUiAction.OnAcceptedCountryToggle("DE"))

            assertEquals(setOf("FR", "ES", "DE"), presenter.uiState.value.selectedAcceptedCountryCodes)
        }

    @Test
    fun `when clear all accepted countries then selected countries become empty`() =
        runTest(testDispatcher) {
            presenter.onAction(SepaFormUiAction.OnClearAllAcceptedCountries)

            assertTrue(
                presenter.uiState.value.selectedAcceptedCountryCodes
                    .isEmpty(),
            )
        }

    @Test
    fun `when select all accepted countries then selected countries include all available`() =
        runTest(testDispatcher) {
            presenter.onAction(SepaFormUiAction.OnClearAllAcceptedCountries)
            presenter.onAction(SepaFormUiAction.OnSelectAllAcceptedCountries)

            assertEquals(setOf("FR", "DE", "ES"), presenter.uiState.value.selectedAcceptedCountryCodes)
        }

    @Test
    fun `when next clicked with invalid fields then no effect and errors are set`() =
        runTest(testDispatcher) {
            presenter.onCommonAction(AccountFormUiAction.OnUniqueAccountNameChange("a"))
            presenter.onAction(SepaFormUiAction.OnHolderNameChange("a"))
            presenter.onAction(SepaFormUiAction.OnIbanChange("bad"))
            presenter.onAction(SepaFormUiAction.OnBicChange("bad"))
            presenter.onAction(SepaFormUiAction.OnClearAllAcceptedCountries)

            val effectDeferred = async { presenter.effect.first() }
            presenter.onCommonAction(AccountFormUiAction.OnNextClick)
            advanceUntilIdle()

            assertFalse(effectDeferred.isCompleted)
            effectDeferred.cancel()
            assertNotNull(presenter.uniqueAccountNameEntry.value.errorMessage)
            assertNotNull(presenter.uiState.value.countryErrorMessage)
            assertNotNull(presenter.uiState.value.holderNameEntry.errorMessage)
            assertNotNull(presenter.uiState.value.ibanEntry.errorMessage)
            assertNotNull(presenter.uiState.value.bicEntry.errorMessage)
            assertNotNull(presenter.uiState.value.acceptedCountriesErrorMessage)
        }

    @Test
    fun `when next clicked with only unsupported accepted country codes then no effect and accepted countries error is set`() =
        runTest(testDispatcher) {
            fillValidFields()
            presenter.onAction(SepaFormUiAction.OnClearAllAcceptedCountries)
            presenter.onAction(SepaFormUiAction.OnAcceptedCountryToggle("UNSUPPORTED"))

            val effectDeferred = async { presenter.effect.first() }
            presenter.onCommonAction(AccountFormUiAction.OnNextClick)
            advanceUntilIdle()

            assertFalse(effectDeferred.isCompleted)
            effectDeferred.cancel()
            assertNotNull(presenter.uiState.value.acceptedCountriesErrorMessage)
        }

    @Test
    fun `when next clicked with iban from different country then no effect and iban error is set`() =
        runTest(testDispatcher) {
            fillValidFields()
            presenter.onAction(SepaFormUiAction.OnCountrySelect(1))
            presenter.onAction(SepaFormUiAction.OnIbanChange("FR1420041010050500013M02606"))

            val effectDeferred = async { presenter.effect.first() }
            presenter.onCommonAction(AccountFormUiAction.OnNextClick)
            advanceUntilIdle()

            assertFalse(effectDeferred.isCompleted)
            effectDeferred.cancel()
            assertNotNull(presenter.uiState.value.ibanEntry.errorMessage)
        }

    @Test
    fun `when next clicked with valid fields then emits SEPA account payload`() =
        runTest(testDispatcher) {
            presenter.onCommonAction(AccountFormUiAction.OnUniqueAccountNameChange("SEPA Main"))
            presenter.onAction(SepaFormUiAction.OnCountrySelect(1))
            presenter.onAction(SepaFormUiAction.OnHolderNameChange(" John Doe "))
            presenter.onAction(SepaFormUiAction.OnIbanChange("DE89 3704 0044 0532 0130 00"))
            presenter.onAction(SepaFormUiAction.OnBicChange(" DEUTDEFF "))
            presenter.onAction(SepaFormUiAction.OnClearAllAcceptedCountries)
            presenter.onAction(SepaFormUiAction.OnAcceptedCountryToggle("ES"))
            presenter.onAction(SepaFormUiAction.OnAcceptedCountryToggle("DE"))

            val effectDeferred = async { presenter.effect.first() }
            presenter.onCommonAction(AccountFormUiAction.OnNextClick)
            advanceUntilIdle()

            val effect = effectDeferred.await()
            assertTrue(effect is SepaFormEffect.NavigateToNextScreen)
            val account = effect.account
            assertEquals("SEPA Main", account.accountName)
            assertEquals("DE", account.accountPayload.selectedCountryCode)
            assertEquals(listOf("DE", "ES"), account.accountPayload.acceptedCountryCodes)
            assertEquals("John Doe", account.accountPayload.holderName)
            assertEquals("DE89370400440532013000", account.accountPayload.iban)
            assertEquals("DEUTDEFF", account.accountPayload.bic)
        }

    @Test
    fun `validate holder name accepts trimmed valid value`() {
        assertNull(validateHolderName("  John Doe  "))
    }

    @Test
    fun `validate holder name rejects too short value`() {
        assertNotNull(validateHolderName("a"))
    }

    @Test
    fun `validate bic accepts valid trimmed value`() {
        assertNull(validateBic("  DEUTDEFF  "))
    }

    @Test
    fun `validate bic rejects invalid value`() {
        assertNotNull(validateBic("bad"))
    }

    private fun fillValidFields() {
        presenter.onCommonAction(AccountFormUiAction.OnUniqueAccountNameChange("SEPA Main"))
        presenter.onAction(SepaFormUiAction.OnCountrySelect(1))
        presenter.onAction(SepaFormUiAction.OnHolderNameChange("John Doe"))
        presenter.onAction(SepaFormUiAction.OnIbanChange("DE89370400440532013000"))
        presenter.onAction(SepaFormUiAction.OnBicChange("DEUTDEFF"))
    }

    private fun samplePaymentMethod(): FiatPaymentMethod =
        FiatPaymentMethod(
            paymentRail = FiatPaymentRail.SEPA,
            name = "SEPA",
            supportedCurrencies = listOf(FiatCurrency(code = "EUR", name = "Euro")),
            supportedCountries =
                listOf(
                    Country(code = "DE", name = "Germany"),
                    Country(code = "ES", name = "Spain"),
                    Country(code = "FR", name = "France"),
                ),
            matchesAllCountries = false,
            chargebackRisk = FiatPaymentMethodChargebackRisk.VERY_LOW,
            tradeLimitInfo = "5000.00",
            tradeDuration = "5 days",
        )
}
