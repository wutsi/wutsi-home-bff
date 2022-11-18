package com.wutsi.application.common.page

import com.wutsi.application.AbstractEndpoint
import com.wutsi.application.shared.Theme
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Icon
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Row
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.Alignment
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextAlignment
import org.springframework.web.bind.annotation.PostMapping

abstract class AbstractPageEndpoint : AbstractEndpoint() {
    protected abstract fun getPageIndex(): Int
    protected abstract fun getBody(): WidgetAware?
    protected abstract fun getTitle(): String?
    protected open fun getSubTitle(): String? = null
    protected open fun getIcon(): Icon? = null

    @PostMapping
    fun index(): Widget =
        Column(
            children = listOfNotNull(
                if (showHeader()) {
                    Row(
                        mainAxisAlignment = if (getPageIndex() > 0) MainAxisAlignment.spaceBetween else MainAxisAlignment.end,
                        crossAxisAlignment = CrossAxisAlignment.start,
                        children = listOfNotNull(
                            if (getPageIndex() > 0) {
                                IconButton(
                                    icon = Theme.ICON_ARROW_BACK,
                                    color = Theme.COLOR_BLACK,
                                    action = gotoPreviousPage()
                                )
                            } else {
                                null
                            },
                            IconButton(
                                icon = Theme.ICON_CANCEL,
                                color = Theme.COLOR_BLACK,
                                action = gotoPreviousScreen()
                            )
                        )
                    )
                } else {
                    Container(padding = 20.0)
                },
                getIcon()?.let {
                    Container(
                        alignment = Alignment.Center,
                        padding = 20.0,
                        child = it
                    )
                },
                getTitle()?.let {
                    Container(
                        alignment = Alignment.Center,
                        padding = 10.0,
                        child = Text(
                            caption = it,
                            alignment = TextAlignment.Center,
                            size = Theme.TEXT_SIZE_LARGE,
                            color = Theme.COLOR_PRIMARY,
                            bold = true
                        )
                    )
                },
                getSubTitle()?.let {
                    Container(
                        alignment = Alignment.Center,
                        padding = 10.0,
                        child = Text(
                            caption = it,
                            alignment = TextAlignment.Center,
                        )
                    )
                },
                Container(padding = 20.0),
                getBody()
            )
        ).toWidget()

    protected open fun showHeader(): Boolean = true

    private fun gotoPreviousPage() = gotoPage(getPageIndex() - 1)

    protected fun gotoNextPage() = gotoPage(getPageIndex() + 1)
}
