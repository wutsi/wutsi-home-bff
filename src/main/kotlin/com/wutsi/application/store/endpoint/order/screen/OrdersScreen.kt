package com.wutsi.application.store.endpoint.order.screen

import com.wutsi.application.shared.Theme
import com.wutsi.application.shared.service.TenantProvider
import com.wutsi.application.shared.ui.Avatar
import com.wutsi.application.store.endpoint.AbstractQuery
import com.wutsi.application.store.endpoint.Page
import com.wutsi.ecommerce.order.WutsiOrderApi
import com.wutsi.ecommerce.order.dto.OrderSummary
import com.wutsi.ecommerce.order.dto.SearchOrderRequest
import com.wutsi.ecommerce.order.entity.OrderStatus
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.DefaultTabController
import com.wutsi.flutter.sdui.ListItem
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.TabBar
import com.wutsi.flutter.sdui.TabBarView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.WidgetAware
import com.wutsi.platform.account.WutsiAccountApi
import com.wutsi.platform.account.dto.AccountSummary
import com.wutsi.platform.account.dto.SearchAccountRequest
import com.wutsi.platform.tenant.dto.Tenant
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/orders")
class OrdersScreen(
    private val accountApi: WutsiAccountApi,
    private val tenantProvider: TenantProvider,
    private val orderApi: WutsiOrderApi
) : AbstractQuery() {
    companion object {
        const val MAX_ORDERS = 100
    }

    @PostMapping
    fun index(): Widget {
        val tenant = tenantProvider.get()
        val user = securityContext.currentAccount()

        val tabs = TabBar(
            tabs = listOfNotNull(
                if (user.business)
                    Text(getText("page.orders.customer-orders").uppercase(), bold = true)
                else
                    null,

                Text(getText("page.orders.my-orders").uppercase(), bold = true)
            )
        )
        val tabViews = TabBarView(
            children = listOfNotNull(
                if (user.business)
                    customerOrders(tenant)
                else
                    null,

                myOrders(tenant)
            )
        )

        return if (tabViews.children.size == 1)
            Screen(
                id = Page.ORDERS,
                appBar = AppBar(
                    elevation = 0.0,
                    backgroundColor = Theme.COLOR_PRIMARY,
                    foregroundColor = Theme.COLOR_WHITE,
                    title = getText("page.orders.app-bar.title")
                ),
                child = tabViews.children[0],
                bottomNavigationBar = bottomNavigationBar()
            ).toWidget()
        else
            DefaultTabController(
                id = Page.ORDERS,
                length = tabs.tabs.size,
                initialIndex = 0,
                child = Screen(
                    backgroundColor = Theme.COLOR_WHITE,
                    appBar = AppBar(
                        elevation = 0.0,
                        backgroundColor = Theme.COLOR_PRIMARY,
                        foregroundColor = Theme.COLOR_WHITE,
                        title = getText("page.orders.app-bar.title"),
                        bottom = tabs
                    ),
                    child = tabViews,
                    bottomNavigationBar = bottomNavigationBar()
                )
            ).toWidget()
    }

    private fun myOrders(tenant: Tenant): WidgetAware {
        val orders = orderApi.searchOrders(
            request = SearchOrderRequest(
                accountId = securityContext.currentAccountId(),
                status = getOrderStatusList().map { it.name },
                limit = MAX_ORDERS
            )
        ).orders
        return toListView(orders, tenant, true)
    }

    private fun customerOrders(tenant: Tenant): WidgetAware {
        val orders = orderApi.searchOrders(
            request = SearchOrderRequest(
                merchantId = securityContext.currentAccountId(),
                status = getOrderStatusList().map { it.name },
                limit = MAX_ORDERS
            )
        ).orders
        return toListView(orders, tenant, false)
    }

    private fun toListView(orders: List<OrderSummary>, tenant: Tenant, showMerchantIcon: Boolean): WidgetAware {
        val accountIds = orders.map { it.merchantId }.toSet()
        val merchants = accountApi.searchAccount(
            SearchAccountRequest(
                ids = accountIds.toList(),
                limit = accountIds.size
            )
        ).accounts.associateBy { it.id }

        return ListView(
            separatorColor = Theme.COLOR_DIVIDER,
            separator = true,
            children = orders.map {
                toOrderWidget(it, merchants, tenant, showMerchantIcon)
            }
        )
    }

    private fun toOrderWidget(
        order: OrderSummary,
        merchants: Map<Long, AccountSummary>,
        tenant: Tenant,
        showMerchantIcon: Boolean
    ): WidgetAware {
        val moneyFormat = DecimalFormat(tenant.monetaryFormat)
        val dateFormat = DateTimeFormatter.ofPattern(tenant.dateFormat, LocaleContextHolder.getLocale())
        val merchant = merchants[order.merchantId]
        return ListItem(
            leading = if (showMerchantIcon)
                merchant?.let {
                    Avatar(
                        model = sharedUIMapper.toAccountModel(it),
                        radius = 24.0
                    )
                }
            else
                null,
            trailing = Text(
                caption = moneyFormat.format(order.totalPrice),
                bold = true,
                color = Theme.COLOR_PRIMARY
            ),
            caption = getOrderCaption(order, tenant),
            subCaption = if (order.itemCount <= 1)
                getText("page.order.1_item")
            else
                getText("page.order.n_items", arrayOf(order.itemCount.toString())),
            action = gotoUrl(
                url = urlBuilder.build("/order?id=${order.id}")
            )
        )
    }

    private fun getOrderCaption(order: OrderSummary, tenant: Tenant): String {
        val fmt = DateTimeFormatter.ofPattern(tenant.dateFormat)
        return when (order.status) {
            OrderStatus.CANCELLED.name -> getText("page.orders.order-cancelled")
            OrderStatus.OPENED.name -> getText("page.orders.order-opened", arrayOf(order.created.format(fmt)))
            OrderStatus.DONE.name -> getText("page.orders.order-done")
            else -> ""
        }
    }

    private fun getOrderStatusList(): List<OrderStatus> =
        listOf(
            OrderStatus.OPENED,
            OrderStatus.DONE,
            OrderStatus.CANCELLED
        )
}