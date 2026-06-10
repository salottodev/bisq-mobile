package network.bisq.mobile.client.payment_accounts.presentation.create_payment_account.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme

private val CurrencyPickerLabels =
    PickerBottomSheetLabels(
        titleKey = "mobile.paymentAccounts.currencyPicker.title",
        summaryKey = "mobile.paymentAccounts.currencyPicker.summary",
        allSelectedKey = "mobile.paymentAccounts.currencyPicker.allSelected",
        searchHintKey = "mobile.paymentAccounts.currencyPicker.searchHint",
        noResultsKey = "mobile.paymentAccounts.currencyPicker.noResults",
    )

@Composable
fun CurrencySummaryRow(
    selectedCount: Int,
    totalCount: Int,
    isError: Boolean,
    onClick: () -> Unit,
) {
    PickerSummaryRow(
        selectedCount = selectedCount,
        totalCount = totalCount,
        isError = isError,
        labels = CurrencyPickerLabels,
        onClick = onClick,
    )
}

@Composable
fun CurrencyPickerBottomSheet(
    selectedCurrencyCodes: Set<String>,
    currencies: List<PickerItem>,
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
        selectedItemIds = selectedCurrencyCodes,
        items = currencies,
        searchQuery = searchQuery,
        selectedCount = selectedCount,
        totalCount = totalCount,
        labels = CurrencyPickerLabels,
        onSearchChange = onSearchChange,
        onToggle = onToggle,
        onSelectAll = onSelectAll,
        onClearAll = onClearAll,
        onDismiss = onDismiss,
    )
}

@Preview
@Composable
private fun CurrencySummaryRowPreview() {
    BisqTheme.Preview {
        CurrencySummaryRow(
            selectedCount = 2,
            totalCount = 4,
            isError = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun CurrencySummaryRowErrorPreview() {
    BisqTheme.Preview {
        CurrencySummaryRow(
            selectedCount = 0,
            totalCount = 4,
            isError = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun CurrencyPickerBottomSheetPreview() {
    BisqTheme.Preview {
        CurrencyPickerBottomSheet(
            selectedCurrencyCodes = setOf("USD", "EUR"),
            currencies =
                listOf(
                    PickerItem("USD", "USD (US Dollar)"),
                    PickerItem("EUR", "EUR (Euro)"),
                    PickerItem("GBP", "GBP (British Pound)"),
                    PickerItem("CAD", "CAD (Canadian Dollar)"),
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
