package network.bisq.mobile.client.payment_accounts.presentation.create_payment_account.common.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqCheckbox
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.common.ui.components.molecules.bottom_sheet.BisqBottomSheet
import network.bisq.mobile.presentation.common.ui.components.molecules.inputfield.BisqSearchField
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants

data class PickerItem(
    val id: String,
    val displayName: String,
)

internal data class PickerBottomSheetLabels(
    val titleKey: String,
    val summaryKey: String,
    val allSelectedKey: String,
    val searchHintKey: String,
    val noResultsKey: String,
)

@Composable
internal fun PickerSummaryRow(
    selectedCount: Int,
    totalCount: Int,
    isError: Boolean,
    labels: PickerBottomSheetLabels,
    onClick: () -> Unit,
) {
    val summaryText =
        if (selectedCount == totalCount && totalCount > 0) {
            labels.allSelectedKey.i18n(totalCount)
        } else {
            labels.summaryKey.i18n(selectedCount, totalCount)
        }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BisqUIConstants.BorderRadius),
        color = BisqTheme.colors.dark_grey50,
    ) {
        Column(
            modifier = Modifier.padding(BisqUIConstants.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPaddingQuarter),
        ) {
            BisqText.SmallLight(
                text = summaryText,
                color = if (isError) BisqTheme.colors.danger else BisqTheme.colors.white,
            )
            BisqText.SmallLight(
                text = "mobile.paymentAccounts.picker.editLabel".i18n(),
                color = if (isError) BisqTheme.colors.danger else BisqTheme.colors.mid_grey20,
            )
        }
    }
}

@Composable
internal fun PickerBottomSheet(
    selectedItemIds: Set<String>,
    items: List<PickerItem>,
    searchQuery: String,
    selectedCount: Int,
    totalCount: Int,
    labels: PickerBottomSheetLabels,
    onSearchChange: (String) -> Unit,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val filteredItems =
        remember(items, searchQuery) {
            filterPickerItems(
                items = items,
                query = searchQuery,
            )
        }
    val canSelectAll = selectedItemIds.size != items.size
    val canSelectNone = selectedItemIds.isNotEmpty()

    BisqBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = BisqUIConstants.ScreenPadding),
        ) {
            BisqText.H5Regular(labels.titleKey.i18n())
            BisqGap.VHalf()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPaddingQuarter),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val selectAllAlpha = if (canSelectAll) 1f else 0.4f
                    BisqText.BaseRegular(
                        text = "mobile.paymentAccounts.picker.selectAll".i18n(),
                        underline = true,
                        modifier =
                            Modifier
                                .alpha(selectAllAlpha)
                                .clickable(enabled = canSelectAll) { onSelectAll() },
                    )
                    val selectNoneAlpha = if (canSelectNone) 1f else 0.4f
                    BisqText.BaseRegular(
                        text = "mobile.paymentAccounts.picker.clearAll".i18n(),
                        underline = true,
                        modifier =
                            Modifier
                                .alpha(selectNoneAlpha)
                                .clickable(enabled = canSelectNone) { onClearAll() },
                    )
                }
                BisqGap.VHalf()
                BisqText.SmallLight(
                    text = labels.summaryKey.i18n(selectedCount, totalCount),
                    color = BisqTheme.colors.mid_grey20,
                )
            }

            BisqGap.V1()

            BisqSearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = labels.searchHintKey.i18n(),
            )

            BisqGap.V1()

            if (filteredItems.isEmpty()) {
                BisqText.BaseLight(
                    modifier = Modifier.fillMaxSize(),
                    text = labels.noResultsKey.i18n(),
                    color = BisqTheme.colors.mid_grey20,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPaddingQuarter),
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BisqCheckbox(
                                checked = selectedItemIds.contains(item.id),
                                label = item.displayName,
                                onCheckedChange = { onToggle(item.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun filterPickerItems(
    items: List<PickerItem>,
    query: String,
): List<PickerItem> {
    if (query.isBlank()) {
        return items
    }

    return items.filter { item ->
        item.id.contains(query, ignoreCase = true) ||
            item.displayName.contains(query, ignoreCase = true)
    }
}
