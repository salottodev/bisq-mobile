package network.bisq.mobile.presentation.main

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import network.bisq.mobile.data.model.TradeReadStateMap
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeChannel
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.BisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades.createMockBisqEasyOpenTradeMessage
import network.bisq.mobile.data.replicated.presentation.open_trades.TradeItemPresentationModel
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.replicated.user.profile.userProfileDemoObj
import network.bisq.mobile.data.service.message_delivery.MessageDeliveryServiceFacade
import network.bisq.mobile.data.service.settings.SettingsServiceFacade
import network.bisq.mobile.data.service.trades.TradesServiceFacade
import network.bisq.mobile.data.service.user_profile.UserProfileServiceFacade
import network.bisq.mobile.data.utils.UrlLauncher
import network.bisq.mobile.domain.repository.TradeReadStateRepository
import network.bisq.mobile.presentation.common.service.OpenTradesNotificationService
import network.bisq.mobile.presentation.common.test_utils.TestApplicationLifecycleService
import network.bisq.mobile.test.presentation.coroutines.PlatformPresentationKoinTestBase
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for the unread badge logic in MainPresenter.
 * Eventually we should refactor this responsibility to a separate service that
 * the presenter can interact with
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainPresenterUnreadBadgeTest : PlatformPresentationKoinTestBase() {
    override val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `unread badge count reflects the total unread chat messages exactly`() =
        runTest {
            // Mock dependencies
            val tradesServiceFacade = mockk<TradesServiceFacade>()
            val tradeReadStateRepository = mockk<TradeReadStateRepository>()
            val userProfileServiceFacade = mockk<UserProfileServiceFacade>()
            val openTradesNotificationService = mockk<OpenTradesNotificationService>()
            val settingsService = mockk<SettingsServiceFacade>()
            every { settingsService.languageCode } returns MutableStateFlow("en")
            every { settingsService.useAnimations } returns MutableStateFlow(false)
            val urlLauncher = mockk<UrlLauncher>(relaxed = true)

            // Mock ignored user IDs
            val ignoredUserIdsFlow = MutableStateFlow(setOf("ignoredUser1"))
            every { userProfileServiceFacade.ignoredProfileIds } returns ignoredUserIdsFlow

            // Mock read states
            val readStatesFlow =
                MutableStateFlow(TradeReadStateMap(mapOf("trade1" to 1, "trade2" to 0)))
            every { tradeReadStateRepository.data } returns readStatesFlow

            // Mock myUserProfile for models
            val myUserProfile = createMockUserProfile("myUser")
            val messageDeliveryServiceFacade: MessageDeliveryServiceFacade =
                mockk<MessageDeliveryServiceFacade>(relaxed = true)
            // Messages for trade1
            val model1 =
                createMockBisqEasyOpenTradeMessage(
                    id = "msg1",
                    text = null,
                    date = 0L,
                    senderUserProfile = createMockUserProfile("User1"),
                    myUserProfile = myUserProfile,
                    tradeId = "trade1",
                )

            val model2 =
                createMockBisqEasyOpenTradeMessage(
                    id = "msg2",
                    text = null,
                    date = 0L,
                    senderUserProfile = createMockUserProfile("User2"),
                    myUserProfile = myUserProfile,
                    tradeId = "trade1",
                )

            val model3 =
                createMockBisqEasyOpenTradeMessage(
                    id = "msg3",
                    text = null,
                    date = 0L,
                    senderUserProfile = createMockUserProfile("ignoredUser1"),
                    myUserProfile = myUserProfile,
                    tradeId = "trade1",
                )

            val trade1MessagesFlow: StateFlow<Set<BisqEasyOpenTradeMessage>> =
                MutableStateFlow(setOf(model1, model2, model3))

            // Message for trade2
            val model4 =
                createMockBisqEasyOpenTradeMessage(
                    id = "msg4",
                    text = null,
                    date = 0L,
                    senderUserProfile =
                        userProfileDemoObj.copy(
                            userName = "User3",
                            nym = "User3",
                        ),
                    myUserProfile = myUserProfile,
                    tradeId = "trade2",
                )

            val trade2MessagesFlow = MutableStateFlow(setOf(model4))

            val channelModel1 = mockk<BisqEasyOpenTradeChannel>()
            every { channelModel1.chatMessages } answers { trade1MessagesFlow }

            val channelModel2 = mockk<BisqEasyOpenTradeChannel>()
            every { channelModel2.chatMessages } answers { trade2MessagesFlow }

            val trade1 = mockk<TradeItemPresentationModel>()
            every { trade1.tradeId } returns "trade1"
            every { trade1.bisqEasyOpenTradeChannelModel } returns channelModel1

            val trade2 = mockk<TradeItemPresentationModel>()
            every { trade2.tradeId } returns "trade2"
            every { trade2.bisqEasyOpenTradeChannelModel } returns channelModel2

            val openTradeItemsFlow = MutableStateFlow(listOf(trade1, trade2))
            every { tradesServiceFacade.openTradeItems } returns openTradeItemsFlow

            // Create presenter
            val presenter =
                MainPresenter(
                    tradesServiceFacade,
                    userProfileServiceFacade,
                    openTradesNotificationService,
                    settingsService,
                    tradeReadStateRepository,
                    urlLauncher,
                    TestApplicationLifecycleService(),
                )

            advanceUntilIdle()

            // Collect the unread messages map
            val unreadMap =
                presenter.tradesWithUnreadMessages.first { unreadMap ->
                    unreadMap.isNotEmpty()
                }

            // Assertions
            // Trade1: 3 messages - 1 ignored = 2 visible, read 1, unread 1
            // Trade2: 1 message, read 0, unread 1
            assertEquals(mapOf("trade1" to 1, "trade2" to 1), unreadMap)
        }
}
