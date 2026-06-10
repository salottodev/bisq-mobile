package network.bisq.mobile.client.payment_accounts.presentation.create_payment_account.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme

private val CountryPickerLabels =
    PickerBottomSheetLabels(
        titleKey = "paymentAccounts.createAccount.accountData.sepa.acceptCountries",
        summaryKey = "mobile.paymentAccounts.countryPicker.summary",
        allSelectedKey = "mobile.paymentAccounts.countryPicker.allSelected",
        searchHintKey = "mobile.paymentAccounts.countryPicker.searchHint",
        noResultsKey = "mobile.paymentAccounts.countryPicker.noResults",
    )

@Composable
fun CountrySummaryRow(
    selectedCount: Int,
    totalCount: Int,
    isError: Boolean,
    onClick: () -> Unit,
) {
    PickerSummaryRow(
        selectedCount = selectedCount,
        totalCount = totalCount,
        isError = isError,
        labels = CountryPickerLabels,
        onClick = onClick,
    )
}

@Composable
fun CountryPickerBottomSheet(
    selectedCountryCodes: Set<String>,
    countries: List<PickerItem>,
    searchQuery: String,
    selectedCount: Int,
    totalCount: Int,
    onSearchChange: (String) -> Unit,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    PickerBottomSheet(
        selectedItemIds = selectedCountryCodes,
        items = countries,
        searchQuery = searchQuery,
        selectedCount = selectedCount,
        totalCount = totalCount,
        labels = CountryPickerLabels,
        onSearchChange = onSearchChange,
        onToggle = onToggle,
        onSelectAll = onSelectAll,
        onClearAll = onClearAll,
        onDismiss = onDismiss,
    )
}

@Preview
@Composable
private fun CountrySummaryRowPreview() {
    BisqTheme.Preview {
        CountrySummaryRow(
            selectedCount = 2,
            totalCount = 4,
            isError = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun CountryPickerBottomSheetPreview() {
    BisqTheme.Preview {
        CountryPickerBottomSheet(
            selectedCountryCodes = setOf("DE", "FR"),
            countries =
                listOf(
                    PickerItem("DE", "Germany"),
                    PickerItem("FR", "France"),
                    PickerItem("ES", "Spain"),
                    PickerItem("NL", "Netherlands"),
                ),
            searchQuery = "",
            selectedCount = 2,
            totalCount = 4,
            onSearchChange = {},
            onToggle = {},
            onSelectAll = {},
            onClearAll = {},
            onDismiss = {},
        )
    }
}
