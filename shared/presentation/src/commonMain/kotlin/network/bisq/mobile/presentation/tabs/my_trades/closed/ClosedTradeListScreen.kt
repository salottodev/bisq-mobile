package network.bisq.mobile.presentation.tabs.my_trades.closed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.i18n.i18n
import network.bisq.mobile.presentation.common.ui.components.ListStateSection
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqButtonType
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.layout.BisqGap
import network.bisq.mobile.presentation.common.ui.components.atoms.rememberStarPainters
import network.bisq.mobile.presentation.common.ui.components.layout.BisqStaticLayout
import network.bisq.mobile.presentation.common.ui.components.molecules.inputfield.SearchWithFilterField
import network.bisq.mobile.presentation.common.ui.theme.BisqTheme
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants
import network.bisq.mobile.presentation.common.ui.utils.ExcludeFromCoverage
import network.bisq.mobile.presentation.common.ui.utils.RememberPresenterLifecycle
import network.bisq.mobile.presentation.tabs.my_trades.closed.components.ClosedTradeListCard
import network.bisq.mobile.presentation.tabs.my_trades.closed.components.ClosedTradeListFilterSheet
import network.bisq.mobile.presentation.tabs.my_trades.closed.components.TradeDetailsDialog
import network.bisq.mobile.presentation.tabs.my_trades.shared.TradeResultBar
import org.koin.compose.koinInject

@Composable
fun ClosedTradeListScreen() {
    val presenter: ClosedTradeListPresenter = koinInject()
    RememberPresenterLifecycle(presenter)

    val uiState by presenter.uiState.collectAsState()
    val totalCount by presenter.totalCount.collectAsState()
    val lazyItems = presenter.pagingData.collectAsLazyPagingItems()
    val starPainters = rememberStarPainters()
    val isEmpty = lazyItems.itemCount == 0
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.sortBy, uiState.outcomeFilter, uiState.roleFilter, uiState.searchQuery) {
        snapshotFlow { lazyItems.loadState.refresh }
            .dropWhile { it !is LoadState.Loading }
            .first { it is LoadState.NotLoading }
        if (lazyItems.itemCount > 0) {
            listState.scrollToItem(0)
        }
    }

    uiState.selectedTradeForDetails?.let { item ->
        TradeDetailsDialog(
            item = item,
            onDismiss = { presenter.onAction(ClosedTradeListUiAction.OnDismissDetails) },
        )
    }

    if (uiState.showFilterSheet) {
        ClosedTradeListFilterSheet(
            sort = uiState.sortBy,
            outcome = uiState.outcomeFilter,
            role = uiState.roleFilter,
            onSortChange = { presenter.onAction(ClosedTradeListUiAction.OnSortChange(it)) },
            onOutcomeChange = { presenter.onAction(ClosedTradeListUiAction.OnOutcomeFilterChange(it)) },
            onRoleChange = { presenter.onAction(ClosedTradeListUiAction.OnRoleFilterChange(it)) },
            onReset = { presenter.onAction(ClosedTradeListUiAction.OnResetFilters) },
            onDismiss = { presenter.onAction(ClosedTradeListUiAction.OnDismissFilterSheet) },
        )
    }

    BisqStaticLayout(
        contentPadding = PaddingValues(all = BisqUIConstants.Zero),
        verticalArrangement = Arrangement.Top,
    ) {
        BisqGap.V1()

        SearchWithFilterField(
            value = uiState.searchQuery,
            onValueChange = { presenter.onAction(ClosedTradeListUiAction.OnSearchQueryChange(it)) },
            placeholder = "mobile.tradeHistory.search.placeholder".i18n(),
            isFilterActive = uiState.isFilterActive,
            onFilterClick = { presenter.onAction(ClosedTradeListUiAction.OnShowFilterSheet) },
        )

        TradeResultBar(
            sort = uiState.sortBy,
            outcome = uiState.outcomeFilter,
            role = uiState.roleFilter,
            loadedCount = lazyItems.itemCount,
            totalCount = totalCount ?: 0,
            onClearAll = { presenter.onAction(ClosedTradeListUiAction.OnResetFilters) },
            modifier = Modifier.padding(vertical = BisqUIConstants.ScreenPaddingHalf),
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = BisqUIConstants.ScreenPadding,
                    bottom = BisqUIConstants.ScreenPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPadding),
        ) {
            when {
                lazyItems.loadState.refresh is LoadState.Loading -> items(3, key = { "shimmer_$it" }) { ShimmerCard() }

                lazyItems.loadState.refresh is LoadState.Error ->
                    item(key = "error") {
                        ClosedTradeListErrorState(
                            modifier = Modifier.fillParentMaxSize(),
                            onRetry = { lazyItems.retry() },
                        )
                    }

                isEmpty && uiState.searchQuery.isBlank() && !uiState.isFilterActive ->
                    item(key = "empty") {
                        ClosedTradeListEmptyState(
                            modifier = Modifier.fillParentMaxSize(),
                            onBrowseOffers = { presenter.onAction(ClosedTradeListUiAction.OnBrowseOffers) },
                        )
                    }

                isEmpty ->
                    item(key = "no_results") {
                        ClosedTradeListNoResultsState(
                            modifier = Modifier.fillParentMaxSize(),
                            onClearSearch = { presenter.onAction(ClosedTradeListUiAction.OnClearSearch) },
                        )
                    }

                else -> {
                    items(
                        count = lazyItems.itemCount,
                        key = lazyItems.itemKey { it.tradeId },
                    ) { index ->
                        val item = lazyItems[index] ?: return@items
                        ClosedTradeListCard(
                            item = item,
                            userProfileIconProvider = presenter.userProfileIconProvider,
                            starPainters = starPainters,
                            onClick = { presenter.onAction(ClosedTradeListUiAction.OnSelectTrade(item)) },
                            onPeerProfileClick = {
                                presenter.onAction(ClosedTradeListUiAction.OnPeerProfileClick(item.peersUserProfile.id))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShimmerCard() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(BisqUIConstants.BorderRadius))
                .background(BisqTheme.colors.dark_grey40)
                .padding(BisqUIConstants.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPaddingHalf),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .clip(RoundedCornerShape(BisqUIConstants.BorderRadiusSmall))
                    .background(BisqTheme.colors.dark_grey50),
        )
        BisqGap.VQuarter()
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.45f)
                    .height(15.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BisqTheme.colors.dark_grey50),
        )
        Box(
            modifier =
                Modifier
                    .padding(start = BisqUIConstants.ScreenPadding6X)
                    .fillMaxWidth(0.40f)
                    .height(15.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BisqTheme.colors.dark_grey50),
        )
        Box(
            modifier =
                Modifier
                    .padding(start = BisqUIConstants.ScreenPadding6X)
                    .fillMaxWidth(0.25f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BisqTheme.colors.dark_grey50),
        )
        BisqGap.V1()
        Box(
            modifier =
                Modifier
                    .align(Alignment.End)
                    .fillMaxWidth(0.40f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BisqTheme.colors.dark_grey50),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.30f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BisqTheme.colors.dark_grey50),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.55f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(BisqTheme.colors.dark_grey50),
        )
    }
}

@Composable
private fun ClosedTradeListEmptyState(
    onBrowseOffers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListStateSection(
        title = "mobile.tradeHistory.empty.noTrades".i18n(),
        subtitle = "mobile.tradeHistory.empty.noTrades.sub".i18n(),
        icon = {
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(BisqUIConstants.BorderRadius))
                        .background(BisqTheme.colors.dark_grey40),
                contentAlignment = Alignment.Center,
            ) { BisqText.H4LightGrey("?") }
        },
        buttonText = "action.browseOffers".i18n(),
        onButtonClick = onBrowseOffers,
        verticalArrangement = Arrangement.Center,
        verticalPadding = BisqUIConstants.ScreenPadding4X,
        modifier = modifier,
    )
}

@Composable
private fun ClosedTradeListNoResultsState(
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListStateSection(
        title = "mobile.tradeHistory.empty.noResults".i18n(),
        useHeadlineStyle = false,
        buttonText = "action.clearSearch".i18n(),
        buttonType = BisqButtonType.Grey,
        onButtonClick = onClearSearch,
        modifier = modifier,
    )
}

@Composable
private fun ClosedTradeListErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListStateSection(
        title = "mobile.tradeHistory.error.title".i18n(),
        subtitle = "mobile.tradeHistory.error.sub".i18n(),
        buttonText = "mobile.tradeHistory.error.retry".i18n(),
        buttonType = BisqButtonType.Grey,
        onButtonClick = onRetry,
        modifier = modifier,
    )
}

// -------------------------------------------------------------------------------------
// Previews
// -------------------------------------------------------------------------------------

@ExcludeFromCoverage
@Preview(showBackground = true, heightDp = 500)
@Composable
private fun Shimmer_Preview() {
    BisqTheme.Preview {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(BisqTheme.colors.backgroundColor)
                    .padding(BisqUIConstants.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPadding),
        ) {
            repeat(3) { ShimmerCard() }
        }
    }
}

@ExcludeFromCoverage
@Preview(showBackground = true, heightDp = 700)
@Composable
private fun EmptyState_Preview() {
    BisqTheme.Preview {
        Column(modifier = Modifier.fillMaxSize().background(BisqTheme.colors.backgroundColor)) {
            ClosedTradeListEmptyState(onBrowseOffers = {}, modifier = Modifier.fillMaxSize())
        }
    }
}

@ExcludeFromCoverage
@Preview(showBackground = true, heightDp = 400)
@Composable
private fun NoResultsState_Preview() {
    BisqTheme.Preview {
        Column(modifier = Modifier.fillMaxSize().background(BisqTheme.colors.backgroundColor)) {
            ClosedTradeListNoResultsState(onClearSearch = {}, modifier = Modifier.fillMaxSize())
        }
    }
}

@ExcludeFromCoverage
@Preview(showBackground = true, heightDp = 500)
@Composable
private fun ErrorState_Preview() {
    BisqTheme.Preview {
        Column(modifier = Modifier.fillMaxSize().background(BisqTheme.colors.backgroundColor)) {
            ClosedTradeListErrorState(onRetry = {}, modifier = Modifier.fillMaxSize())
        }
    }
}
