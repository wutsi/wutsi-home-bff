package com.wutsi.application.shell.endpoint.profile.strength

import com.google.i18n.phonenumbers.PhoneNumberUtil
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
class ProfileStrengthWhatsapp(
    private val phoneNumberUtil: PhoneNumberUtil
) : AbstractProfileStrengthWidget() {
    override fun shouldShow(account: Account): Boolean =
        account.business && account.whatsapp.isNullOrEmpty()

    override fun getIcon(account: Account, size: Double) =
        Icon(code = Theme.ICON_WHATSAPP, size = size, color = Theme.COLOR_WHATSAPP)

    override fun getContent(account: Account): WidgetAware =
        Column(
            mainAxisAlignment = MainAxisAlignment.start,
            crossAxisAlignment = CrossAxisAlignment.start,
            children = listOf(
                Text(
                    caption = getText("profile-strength.whatsapp.title"),
                    bold = true
                ),
                Text(
                    caption = getText(
                        key = "profile-strength.whatsapp.description",
                        args = arrayOf(
                            formattedPhoneNumber(account.phone?.number, account.country)
                        ),
                    ),
                    maxLines = 5
                ),
                Button(
                    padding = 10.0,
                    stretched = false,
                    caption = getText("profile-strength.whatsapp.button"),
                    action = Action(
                        type = ActionType.Route,
                        url = urlBuilder.build("/settings/profile/whatsapp")
                    )
                )
            ),
        )

    private fun formattedPhoneNumber(number: String?, country: String): String {
        if (number == null)
            return ""

        val phoneNumber = phoneNumberUtil.parse(number, country)
        return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
    }
}