package network.bisq.mobile.data.replicated.chat.bisq_easy.open_trades

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import network.bisq.mobile.data.replicated.chat.ChatChannel
import network.bisq.mobile.data.replicated.chat.ChatChannelDomainEnum
import network.bisq.mobile.data.replicated.chat.notifications.ChatChannelNotificationTypeEnum
import network.bisq.mobile.data.replicated.offer.bisq_easy.BisqEasyOfferVO
import network.bisq.mobile.data.replicated.user.identity.UserIdentityVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.profile.UserProfileVOExtension.id
import network.bisq.mobile.domain.utils.Logging
import network.bisq.mobile.i18n.i18n

// todo will get completed with work on chat
class BisqEasyOpenTradeChannel(
    id: String,
    val tradeId: String,
    val bisqEasyOffer: BisqEasyOfferVO,
    val myUserIdentity: UserIdentityVO,
    val traders: Set<UserProfileVO>,
    val mediator: UserProfileVO?,
) : ChatChannel<BisqEasyOpenTradeMessage>(id, ChatChannelDomainEnum.BISQ_EASY_OPEN_TRADES),
    Logging {
    // Mutable properties
    private val _isInMediation: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInMediation: StateFlow<Boolean> = _isInMediation.asStateFlow()
    val chatChannelNotificationType: MutableStateFlow<ChatChannelNotificationTypeEnum> =
        MutableStateFlow(ChatChannelNotificationTypeEnum.ALL)
    val userProfileIdsOfActiveParticipants: MutableSet<String> = mutableSetOf()
    val numMessagesByAuthorId: MutableMap<String, Int> = mutableMapOf()
    val userProfileIdsOfSendingLeaveMessage: MutableSet<String> = mutableSetOf()

    // Utils
    fun getDisplayString(): String {
        val shortOfferId = bisqEasyOffer.id.substring(0, 4)
        if (isMediator()) {
            require(traders.size == 2) { "traders.size() need to be 2 but is ${traders.size}" }
            val tradersAsList: List<UserProfileVO> = traders.toList()
            return (shortOfferId + ": " + tradersAsList[0].userName) + " - " + tradersAsList[1].userName
        } else {
            val peer: String = getPeer().userName
            val optionalMediatorPostfix: String
            if (mediator != null && isInMediation.value) {
                optionalMediatorPostfix = ", " + mediator.userName + " (" + "bisqEasy.mediator".i18n() + ")"
            } else {
                optionalMediatorPostfix = ""
            }
            return "$shortOfferId: $peer$optionalMediatorPostfix"
        }
    }

    fun setIsMediator(value: Boolean) {
        _isInMediation.value = value
    }

    fun isMediator(): Boolean = mediator != null && mediator.id == myUserIdentity.userProfile.id

    // For the trade peer use case we have only one UserProfileVO in traders.
    // Only for the mediator use case there are the 2 traders, but then getPeer() is not called
    fun getPeer(): UserProfileVO {
        require(traders.size == 1) { "traders is expected to has size 1 at getPeer()" }
        return traders.iterator().next()
    }
}
