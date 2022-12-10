package com.wutsi.application

import com.wutsi.application.common.endpoint.AbstractSecuredEndpoint
import com.wutsi.application.membership.onboard.screen.OnboardV2Screen
import com.wutsi.application.widget.OrderWidget
import com.wutsi.checkout.manager.CheckoutManagerApi
import com.wutsi.checkout.manager.dto.SearchOrderRequest
import com.wutsi.enums.OrderStatus
import com.wutsi.flutter.sdui.Action
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.IconButton
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.flutter.sdui.enums.ActionType
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.flutter.sdui.enums.TextDecoration
import com.wutsi.membership.manager.dto.Member
import com.wutsi.platform.core.image.ImageService
import com.wutsi.regulation.RegulationEngine
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/2")
class HomeV2Screen(
    private val onboard: OnboardV2Screen,
    private val checkoutManagerApi: CheckoutManagerApi,
    private val regulationEngine: RegulationEngine,
    private val imageService: ImageService
) : AbstractSecuredEndpoint() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(HomeV2Screen::class.java)
    }

    @PostMapping
    fun index(): Widget {
        try {
            val member = getCurrentMember()
            return Screen(
                id = Page.HOME,
                appBar = AppBar(
                    elevation = 0.0,
                    backgroundColor = Theme.COLOR_WHITE,
                    foregroundColor = Theme.COLOR_BLACK,
                    actions = listOf(
                        IconButton(
                            icon = Theme.ICON_SETTINGS,
                            action = Action(
                                type = ActionType.Route,
                                url = urlBuilder.build(Page.getSettingsUrl())
                            )
                        )
                    ),
                    automaticallyImplyLeading = false,
                    title = member.displayName
                ),
                bottomNavigationBar = createBottomNavigationBarWidget(member),
                backgroundColor = Theme.COLOR_GRAY_LIGHT,
                child = SingleChildScrollView(
                    child = Column(
                        mainAxisAlignment = MainAxisAlignment.start,
                        crossAxisAlignment = CrossAxisAlignment.start,
                        children = listOfNotNull(
                            getRecentOrdersWidget(member)
                        )
                    )
                )
            ).toWidget()
        } catch (ex: FeignException.NotFound) {
            LOGGER.warn("Unable to resolve current member", ex)

            logger.add("member_not_found", true)
            return onboard.index()
        }
    }

    private fun getRecentOrdersWidget(member: Member): WidgetAware? {
        if (!member.business || member.businessId == null) {
            return null
        }

        try {
            val orders = checkoutManagerApi.searchOrder(
                request = SearchOrderRequest(
                    businessId = member.businessId,
                    status = listOf(
                        OrderStatus.OPENED.name
                    ),
                    limit = 3
                )
            ).orders
            if (orders.isEmpty()) {
                return null
            }

            val children = mutableListOf<WidgetAware>()
            children.add(
                Container(
                    padding = 10.0,
                    child = Text(
                        caption = getText("page.home.recent-orders"),
                        size = Theme.TEXT_SIZE_LARGE,
                        bold = true
                    )
                )
            )
            children.add(Divider(height = 1.0, color = Theme.COLOR_DIVIDER))

            children.addAll(
                orders.flatMap {
                    listOf(
                        OrderWidget.of(
                            order = it,
                            country = regulationEngine.country(member.country),
                            imageService = imageService,
                            action = gotoUrl(
                                url = urlBuilder.build(Page.getOrderUrl()),
                                parameters = mapOf(
                                    "id" to it.id
                                )
                            )
                        ),
                        Divider(height = 1.0, color = Theme.COLOR_DIVIDER)
                    )
                }
            )
            children.add(
                Container(
                    padding = 10.0,
                    child = Text(
                        caption = getText("page.home.recent-orders.more"),
                        color = Theme.COLOR_PRIMARY,
                        decoration = TextDecoration.Underline
                    ),
                    action = gotoUrl(urlBuilder.build(Page.getOrderListUrl()))
                )
            )

            return Container(
                margin = 10.0,
                border = 1.0,
                borderColor = Theme.COLOR_DIVIDER,
                borderRadius = 5.0,
                background = Theme.COLOR_WHITE,
                child = Column(
                    mainAxisAlignment = MainAxisAlignment.start,
                    crossAxisAlignment = CrossAxisAlignment.start,
                    children = children
                )
            )
        } catch (ex: Exception) {
            LOGGER.warn("Error while building the recent orders", ex)
            return null
        }
    }
}