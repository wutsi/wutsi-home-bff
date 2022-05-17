package com.wutsi.application.shell.endpoint.profile.strength

import com.wutsi.application.shared.Theme
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.platform.account.dto.Account
import org.springframework.stereotype.Service

@Service
class ProfileStrengthLocationWidget : AbstractProfileStrengthWidget() {
    override fun shouldShow(account: Account): Boolean =
        account.cityId == null

    override fun getIcon(account: Account, size: Double) =
        Icon(code = Theme.ICON_PLACE, size = size, color = Theme.COLOR_PRIMARY)

    override fun getContent(account: Account): WidgetAware =
        Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = listOf(
                Text(
                    caption = getText("profile-strength.location.title"),
                    bold = true
                ),
                Text(
                    caption = getText(
                        "profile-strength.location.description"
                    ),
                    maxLines = 5
                ),
                Button(
                    padding = 10.0,
                    stretched = false,
                    caption = getText("profile-strength.location.button"),
                    action = Action(
                        type = ActionType.Route,
                        url = urlBuilder.build("/settings/profile/location")
                    )
                )
            ),
        )
}