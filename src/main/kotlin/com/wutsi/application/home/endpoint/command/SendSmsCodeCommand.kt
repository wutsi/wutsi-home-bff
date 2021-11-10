package com.wutsi.application.home.endpoint.command

import com.wutsi.application.home.dto.SendSmsCodeRequest
import com.wutsi.application.home.endpoint.AbstractCommand
import com.wutsi.application.home.exception.InvalidPhoneNumberException
import com.wutsi.application.home.service.AccountService
import com.wutsi.application.home.service.URLBuilder
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.enums.ActionType.Route
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/commands/send-sms-code")
class SendSmsCodeCommand(
    private val urlBuilder: URLBuilder,
    private val service: AccountService
) : AbstractCommand() {
    @PostMapping
    fun index(@Valid @RequestBody request: SendSmsCodeRequest): Action {
        service.sendVerificationCode(request)
        return Action(
            type = Route,
            url = urlBuilder.build("settings/accounts/verify/mobile")
        )
    }

    @ExceptionHandler(InvalidPhoneNumberException::class)
    fun onInvalidPhoneNumberException(ex: InvalidPhoneNumberException): Action =
        createErrorAction(ex, "page.link-account-mobile.error.invalid-phone-number")
}
