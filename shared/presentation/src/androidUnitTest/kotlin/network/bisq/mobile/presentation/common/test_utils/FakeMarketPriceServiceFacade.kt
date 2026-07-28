package network.bisq.mobile.presentation.common.test_utils

import network.bisq.mobile.data.model.market.MarketPriceItem
import network.bisq.mobile.data.model.offerbook.MarketListItem
import network.bisq.mobile.data.replicated.common.currency.MarketVO
import network.bisq.mobile.data.service.market_price.MarketPriceServiceFacade
import network.bisq.mobile.domain.repository.SettingsRepository

/**
 * Test fake for [MarketPriceServiceFacade] backed by a fixed price table.
 *
 * Lookups match on currency codes only, so a [MarketVO] built without display names still resolves.
 * Both [findMarketPriceItem] and [findUSDMarketPriceItem] must return non-null for the amount-limit
 * math in [network.bisq.mobile.domain.utils.BisqEasyTradeAmountLimits] to work — that is the reason
 * this fake exists instead of a relaxed mockk, which returns chained mocks that blow up inside
 * `toBaseSideMonetary`.
 */
class FakeMarketPriceServiceFacade(
    settingsRepository: SettingsRepository,
    private val prices: Map<MarketVO, MarketPriceItem> = OfferTestFactory.usdPrices(),
) : MarketPriceServiceFacade(settingsRepository) {
    override fun findMarketPriceItem(marketVO: MarketVO): MarketPriceItem? =
        prices.entries
            .firstOrNull { (market, _) ->
                market.baseCurrencyCode == marketVO.baseCurrencyCode &&
                    market.quoteCurrencyCode == marketVO.quoteCurrencyCode
            }?.value

    override fun findUSDMarketPriceItem(): MarketPriceItem? = findMarketPriceItem(MarketVO("BTC", "USD"))

    override fun refreshSelectedFormattedMarketPrice() {}

    override fun selectMarket(marketListItem: MarketListItem): Result<Unit> = Result.success(Unit)
}
