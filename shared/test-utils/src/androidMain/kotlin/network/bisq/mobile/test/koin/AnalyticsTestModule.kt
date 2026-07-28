package network.bisq.mobile.test.koin

import network.bisq.mobile.domain.analytics.AnalyticsService
import network.bisq.mobile.domain.analytics.NoOpAnalyticsService
import org.koin.dsl.module

/**
 * Binds an inert [AnalyticsService] so presenter tests don't have to care about analytics.
 *
 * `BasePresenter` resolves [AnalyticsService] as a non-null Koin dependency, so any test that
 * attaches a presenter which opts into screen-view tracking needs the binding present. Loading
 * this module first (see [KoinIntegrationTestBase.baseSetup]) makes that the default; tests that
 * DO assert on analytics load their own mock afterwards, which wins because Koin allows overrides
 * and the last definition applies.
 */
val analyticsTestModule =
    module {
        single<AnalyticsService> { NoOpAnalyticsService }
    }
