package network.bisq.mobile.node.common.domain.service.chat.private_chat

import bisq.chat.ChatChannelDomain
import bisq.chat.ChatMessageType
import bisq.chat.ChatService
import bisq.chat.notifications.ChatNotificationService
import bisq.chat.priv.SendOutcome
import bisq.chat.priv.SendRejection
import bisq.chat.two_party.TwoPartyPrivateChatChannelService
import bisq.common.observable.collection.ObservableSet
import bisq.network.SendMessageResult
import bisq.user.UserService
import bisq.user.banned.BannedUserService
import bisq.user.identity.UserIdentity
import bisq.user.identity.UserIdentityService
import bisq.user.profile.UserProfile
import bisq.user.profile.UserProfileService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.reactions.ReactionEnum
import network.bisq.mobile.data.replicated.chat.two_party.TwoPartyPrivateChatMessageReaction
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.createMockUserProfile
import network.bisq.mobile.data.service.chat.private_chat.PrivateChatSendRefusedException
import network.bisq.mobile.data.service.chat.private_chat.PrivateChatSendRejection
import network.bisq.mobile.domain.utils.CoroutineJobsManager
import network.bisq.mobile.node.common.domain.service.AndroidApplicationService
import network.bisq.mobile.node.common.test_utils.NodeKoinIntegrationTestBase
import network.bisq.mobile.test.coroutines.TestCoroutineJobsManager
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.Optional
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import bisq.chat.reactions.TwoPartyPrivateChatMessageReaction as Bisq2TwoPartyPrivateChatMessageReaction
import bisq.chat.two_party.TwoPartyPrivateChatChannel as Bisq2TwoPartyPrivateChatChannel
import bisq.chat.two_party.TwoPartyPrivateChatMessage as Bisq2TwoPartyPrivateChatMessage

/**
 * Covers `removeChatMessageReaction`'s guards and the per-channel observer lifecycle.
 *
 * The lifecycle half drives real `ObservableSet`s — the same class Bisq 2 hands the facade — so that
 * `addObserver`'s replay and the add/remove/clear dispatch are exercised rather than stubbed. Only the
 * channels and messages hanging off them are mocks.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NodePrivateChatServiceFacadeTest : NodeKoinIntegrationTestBase() {
    private lateinit var channelService: TwoPartyPrivateChatChannelService
    private lateinit var userIdentityService: UserIdentityService
    private lateinit var userProfileService: UserProfileService
    private lateinit var bannedUserService: BannedUserService
    private lateinit var chatNotificationService: ChatNotificationService
    private lateinit var chatService: ChatService
    private lateinit var channels: ObservableSet<Bisq2TwoPartyPrivateChatChannel>
    private lateinit var facade: NodePrivateChatServiceFacade

    private val me: UserProfileVO = createMockUserProfile("me")

    /**
     * The facade hardcodes `withContext(Dispatchers.Default)`, so the base's `Dispatchers.setMain` is
     * not enough on its own — Default has to be redirected too, and before Koin resolves anything off
     * it. Kept static rather than made injectable so this migration stays a test-only change;
     * [onTearDown] unmocks it so no other test in the JVM inherits it.
     */
    override fun beforeStartKoin() {
        mockkStatic(Dispatchers::class)
        every { Dispatchers.Default } returns testDispatcher
    }

    override fun additionalModules(): List<Module> =
        listOf(
            module {
                factory<CoroutineJobsManager> { TestCoroutineJobsManager(testDispatcher) }
            },
        )

    override fun onSetup() {
        channelService = mockk(relaxed = true)
        userIdentityService = mockk(relaxed = true)
        userProfileService = mockk(relaxed = true)
        bannedUserService = mockk(relaxed = true)
        chatNotificationService = mockk(relaxed = true)
        channels = ObservableSet()

        every { channelService.channels } returns channels
        // Bisq 2 returns the network-store copy; the identity of the instance is all these tests need.
        every { userProfileService.getManagedUserProfile(any()) } answers { firstArg() }

        chatService = mockk(relaxed = true)
        every { chatService.twoPartyPrivateChatChannelService } returns channelService
        every { chatService.chatNotificationService } returns chatNotificationService

        val userService = mockk<UserService>(relaxed = true)
        every { userService.userIdentityService } returns userIdentityService
        every { userService.userProfileService } returns userProfileService
        every { userService.bannedUserService } returns bannedUserService

        val applicationService = mockk<AndroidApplicationService>(relaxed = true)
        every { applicationService.chatService } returns chatService
        every { applicationService.userService } returns userService

        val provider = AndroidApplicationService.Provider()
        provider.applicationService = applicationService

        facade = NodePrivateChatServiceFacade(provider)
    }

    override fun onTearDown() {
        unmockkStatic(Dispatchers::class)
    }

    /**
     * A reaction id Bisq 2 knows and this build does not must come back as a failure. Resolving the
     * enum as a call argument put the lookup outside the facade's `runCatching`, so it threw out of
     * the suspend function instead.
     */
    @Test
    fun `an unknown reaction id fails instead of throwing`() =
        runTest {
            every { userIdentityService.findUserIdentity(any()) } returns Optional.of(mockk<UserIdentity>(relaxed = true))

            val result = facade.removeChatMessageReaction("channel-1", "message-1", reaction(reactionId = 99))

            assertTrue(result.isFailure)
            verify(exactly = 0) { channelService.trySendTextMessageReaction(any(), any(), any(), any()) }
        }

    @Test
    fun `a reaction that is not ours reports false without calling the node`() =
        runTest {
            every { userIdentityService.findUserIdentity(any()) } returns Optional.empty()

            val result = facade.removeChatMessageReaction("channel-1", "message-1", reaction(reactionId = 0))

            assertFalse(result.getOrThrow())
            verify(exactly = 0) { channelService.trySendTextMessageReaction(any(), any(), any(), any()) }
        }

    /** The known-id path still reaches the channel lookup — here it misses, so it fails cleanly. */
    @Test
    fun `a known reaction id on a missing channel fails rather than throwing`() =
        runTest {
            every { userIdentityService.findUserIdentity(any()) } returns Optional.of(mockk<UserIdentity>(relaxed = true))
            // findChannel is overloaded (String / ChatMessage), so both sides need to be explicit.
            every { channelService.findChannel(any<String>()) } returns Optional.empty<Bisq2TwoPartyPrivateChatChannel>()

            val result = facade.removeChatMessageReaction("channel-1", "message-1", reaction(ReactionEnum.THUMBS_UP.ordinal))

            assertTrue(result.isFailure)
        }

    /**
     * `ObservableCollection.addAll` hands `onAllAdded` the whole argument, not just the elements that
     * were new, so a channel already present is handled a second time with no `onCleared` in between.
     * `addObserver` replays the collection too, which is how the first handling happens here.
     */
    @Test
    fun `a channel handled twice does not appear twice`() =
        runTest {
            val kept = channel("c1")
            channels.add(kept)
            facade.activate()

            channels.addAll(listOf(kept, channel("c2")))

            assertEquals(
                listOf("c1", "c2"),
                facade.channels.value
                    .map { it.id }
                    .sorted(),
            )
        }

    /**
     * `unbindChannelPins` removes by channel id. Unbinding everything instead would leave the surviving
     * channel with a model that silently stops receiving messages — invisible until a DM goes missing.
     */
    @Test
    fun `removing a channel leaves the other channel's message observer bound`() =
        runTest {
            val kept = channel("c1")
            val removed = channel("c2")
            channels.add(kept)
            channels.add(removed)
            facade.activate()

            channels.remove(removed)
            kept.chatMessages.add(message("m1"))

            assertEquals(listOf("c1"), facade.channels.value.map { it.id })
            assertEquals(
                listOf("m1"),
                facade.channels.value
                    .single()
                    .chatMessages.value
                    .map { it.id },
            )
        }

    /**
     * Bisq 2 rejects banned senders on the inbound path, so this only covers a peer banned *after*
     * their messages arrived — the same re-check desktop does.
     */
    @Test
    fun `a message from a banned sender does not reach the model`() =
        runTest {
            val channel = channel("c1")
            channels.add(channel)
            facade.activate()

            every { bannedUserService.isUserProfileBanned(any<UserProfile>()) } returns true
            channel.chatMessages.add(message("banned"))
            every { bannedUserService.isUserProfileBanned(any<UserProfile>()) } returns false
            channel.chatMessages.add(message("ok"))

            assertEquals(
                listOf("ok"),
                facade.channels.value
                    .single()
                    .chatMessages.value
                    .map { it.id },
            )
        }

    /**
     * Bisq 2 dispatches asynchronously, so the failure only shows up in the returned future. Without
     * awaiting it the facade returns `Result.success` for a message that never left, and
     * `PrivateChatPresenter` — which clears the quoted message on success only — would drop the
     * citation the user is still waiting to send and show no error.
     */
    @Test
    fun `a send whose dispatch fails is reported as a failure`() =
        runTest {
            val channel = channel("channel-1")
            every { channelService.findChannel(any<String>()) } returns Optional.of(channel)
            every { channelService.trySendTextMessage(any(), any(), any()) } returns
                SendOutcome.accepted(CompletableFuture.failedFuture(IllegalStateException("no peer reachable")))

            val result = facade.sendChatMessage("channel-1", "hello", citation = null)

            assertTrue(result.isFailure)
        }

    @Test
    fun `a send whose dispatch succeeds is reported as a success`() =
        runTest {
            val channel = channel("channel-1")
            every { channelService.findChannel(any<String>()) } returns Optional.of(channel)
            every { channelService.trySendTextMessage(any(), any(), any()) } returns
                SendOutcome.accepted(CompletableFuture.completedFuture(mockk<SendMessageResult>(relaxed = true)))

            val result = facade.sendChatMessage("channel-1", "hello", citation = null)

            assertTrue(result.isSuccess)
        }

    /**
     * Bisq 2 refuses a send for a banned profile before storing anything and reports it on the
     * `SendOutcome`. `sendTextMessage` folded that into a failed delivery future, indistinguishable from
     * a peer that could not be reached — and `PrivateChatPresenter` would tell the user to retry a send
     * that no retry can fix. Same thing the REST API answers 409 for, so Bisq Connect gets it too.
     */
    @Test
    fun `a send the node refuses names which profile is banned`() =
        runTest {
            val channel = channel("channel-1")
            every { channelService.findChannel(any<String>()) } returns Optional.of(channel)
            every { channelService.trySendTextMessage(any(), any(), any()) } returns
                SendOutcome.rejected(SendRejection.PEER_BANNED)

            val result = facade.sendChatMessage("channel-1", "hello", citation = null)

            val refused = result.exceptionOrNull() as PrivateChatSendRefusedException
            assertEquals(PrivateChatSendRejection.PEER_BANNED, refused.rejection)
        }

    /** Reactions used to drop the future without looking, so a refused reaction was completely silent. */
    @Test
    fun `a reaction the node refuses is reported rather than dropped`() =
        runTest {
            val channel = channel("channel-1")
            every { channel.chatMessages } returns ObservableSet(setOf(message("message-1")))
            every { channelService.findChannel(any<String>()) } returns Optional.of(channel)
            every { channelService.trySendTextMessageReaction(any(), any(), any(), any()) } returns
                SendOutcome.rejected(SendRejection.MY_PROFILE_BANNED)

            val result = facade.addChatMessageReaction("channel-1", "message-1", ReactionEnum.THUMBS_UP)

            val refused = result.exceptionOrNull() as PrivateChatSendRefusedException
            assertEquals(PrivateChatSendRejection.MY_PROFILE_BANNED, refused.rejection)
        }

    /**
     * The `ChatService.createAndSelectTwoPartyPrivateChatChannel` wrapper desktop uses also selects the
     * channel, and selecting a private channel persists a change of the globally selected user identity
     * that `NodeUserProfileServiceFacade` never sees. The REST API deliberately skips it; so does this.
     */
    @Test
    fun `opening a channel does not select it`() =
        runTest {
            val peer = mockk<UserProfile>(relaxed = true)
            val channel = channel("channel-1")
            every { userProfileService.findUserProfile("peer-1") } returns Optional.of(peer)
            every { channelService.findOrCreateChannel(ChatChannelDomain.DISCUSSION, peer) } returns Optional.of(channel)

            val result = facade.findOrCreateChannel("peer-1")

            assertEquals("channel-1", result.getOrThrow())
            verify(exactly = 0) { chatService.createAndSelectTwoPartyPrivateChatChannel(any(), any()) }
        }

    /**
     * A peer banned after the fact vanishes from the message list, but their reactions on my own
     * messages would stay. bisq2's `PrivateChatReactionsWebSocketService` and desktop both drop them,
     * so Bisq Connect never shows them; the node flavour must not either.
     */
    @Test
    fun `a reaction from a banned sender does not reach the model`() =
        runTest {
            val message = message("m1")
            every { message.chatMessageReactions } returns
                ObservableSet(setOf(reaction("banned", sender(banned = true)), reaction("ok", sender(banned = false))))
            addChannelWith(message)

            facade.activate()

            assertEquals(listOf("ok"), modelReactionIds())
        }

    /**
     * The live path: reactions arrive through the observer registered on the message's set, long
     * after the message was admitted, and that is where the filter was missing before the fix.
     */
    @Test
    fun `a reaction from a banned sender arriving later does not reach the model either`() =
        runTest {
            val reactions = ObservableSet<Bisq2TwoPartyPrivateChatMessageReaction>()
            val message = message("m1")
            every { message.chatMessageReactions } returns reactions
            addChannelWith(message)
            facade.activate()

            reactions.add(reaction("banned", sender(banned = true)))
            reactions.add(reaction("ok", sender(banned = false)))

            assertEquals(listOf("ok"), modelReactionIds())
        }

    /** bisq2's `isVisible` re-checks the message's sender too, so a ban of the author drops every reaction. */
    @Test
    fun `reactions on a message whose author was banned later do not reach the model`() =
        runTest {
            val author = sender(banned = false)
            val reactions = ObservableSet<Bisq2TwoPartyPrivateChatMessageReaction>()
            val message = message("m1")
            every { message.senderUserProfile } returns author
            every { message.chatMessageReactions } returns reactions
            addChannelWith(message)
            facade.activate()

            every { bannedUserService.isUserProfileBanned(author) } returns true
            reactions.add(reaction("mine", sender(banned = false)))

            assertEquals(emptyList(), modelReactionIds())
        }

    private fun sender(banned: Boolean): UserProfile =
        mockk<UserProfile>(relaxed = true).also {
            every { bannedUserService.isUserProfileBanned(it) } returns banned
        }

    private fun addChannelWith(message: Bisq2TwoPartyPrivateChatMessage) {
        val channel = channel("c1")
        every { channel.chatMessages } returns ObservableSet(setOf(message))
        channels.add(channel)
    }

    private fun modelReactionIds(): List<String> =
        facade.channels.value
            .single()
            .chatMessages.value
            .single()
            .chatReactions.value
            .map { it.id }

    private fun channel(id: String): Bisq2TwoPartyPrivateChatChannel =
        mockk<Bisq2TwoPartyPrivateChatChannel>(relaxed = true) {
            every { this@mockk.id } returns id
            every { chatChannelDomain } returns ChatChannelDomain.DISCUSSION
            every { chatMessages } returns ObservableSet()
        }

    private fun message(id: String): Bisq2TwoPartyPrivateChatMessage =
        mockk<Bisq2TwoPartyPrivateChatMessage>(relaxed = true) {
            every { this@mockk.id } returns id
            every { chatMessageType } returns ChatMessageType.TEXT
            every { text } returns Optional.of("text of $id")
            every { citation } returns Optional.empty()
            every { chatMessageReactions } returns ObservableSet()
        }

    private fun reaction(
        id: String,
        sender: UserProfile,
    ): Bisq2TwoPartyPrivateChatMessageReaction =
        mockk<Bisq2TwoPartyPrivateChatMessageReaction>(relaxed = true) {
            every { this@mockk.id } returns id
            every { senderUserProfile } returns sender
            every { isRemoved } returns false
            every { chatChannelDomain } returns ChatChannelDomain.DISCUSSION
        }

    private fun reaction(reactionId: Int) =
        TwoPartyPrivateChatMessageReaction(
            id = "reaction-1",
            senderUserProfile = me,
            receiverUserProfileId = "receiver-1",
            receiverNetworkId = me.networkId,
            chatChannelId = "channel-1",
            chatChannelDomain = ChatChannelDomainEnum.DISCUSSION,
            chatMessageId = "message-1",
            reactionId = reactionId,
            date = 1234L,
            isRemoved = false,
        )
}
