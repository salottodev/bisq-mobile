package network.bisq.mobile.presentation.common.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.BadgedBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import network.bisq.mobile.data.replicated.user.profile.UserProfileVO
import network.bisq.mobile.data.replicated.user.reputation.ReputationScoreVO
import network.bisq.mobile.data.utils.PlatformImage
import network.bisq.mobile.presentation.common.ui.components.atoms.BisqText
import network.bisq.mobile.presentation.common.ui.components.atoms.StarPainters
import network.bisq.mobile.presentation.common.ui.components.atoms.StarRating
import network.bisq.mobile.presentation.common.ui.components.atoms.animations.AnimatedBadge
import network.bisq.mobile.presentation.common.ui.components.atoms.debouncedClickable
import network.bisq.mobile.presentation.common.ui.components.atoms.rememberStarPainters
import network.bisq.mobile.presentation.common.ui.theme.BisqUIConstants

@Composable
fun UserProfileRow(
    userProfile: UserProfileVO,
    reputation: ReputationScoreVO,
    userProfileIconProvider: suspend (UserProfileVO) -> PlatformImage,
    modifier: Modifier = Modifier,
    showUserName: Boolean = true,
    badgeCount: Int = 0,
    starPainters: StarPainters = rememberStarPainters(),
    onIconClick: (() -> Unit)? = null,
) {
    val fiveSystemScore = reputation.fiveSystemScore

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(BisqUIConstants.ScreenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BadgedBox(
            modifier = Modifier.graphicsLayer(clip = false),
            badge = {
                if (badgeCount > 0) {
                    AnimatedBadge(
                        text = badgeCount.toString(),
                        xOffset = 3.dp,
                        yOffset = 35.dp,
                    )
                }
            },
        ) {
            // The click sits on the icon rather than the BadgedBox, whose bounds extend past the
            // avatar to host the unread badge, and never on the enclosing row: hosts like
            // `OpenTradeListItem` are themselves clickable, and a row-wide link would eat their taps.
            val iconModifier =
                if (onIconClick == null) {
                    Modifier
                } else {
                    Modifier.debouncedClickable(role = Role.Button, onClick = onIconClick)
                }
            Box(modifier = iconModifier) {
                UserProfileIcon(userProfile, userProfileIconProvider)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (showUserName) {
                BisqText.BaseLight(
                    text = userProfile.userName,
                    singleLine = true,
                )
            }
            StarRating(rating = fiveSystemScore, painters = starPainters)
        }
    }
}
