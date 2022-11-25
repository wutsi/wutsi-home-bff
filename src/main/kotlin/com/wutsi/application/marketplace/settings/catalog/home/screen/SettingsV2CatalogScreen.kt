package com.wutsi.application.marketplace.settings.catalog.home.screen

import com.wutsi.application.common.endpoint.AbstractSecuredEndpoint
import com.wutsi.application.Page
import com.wutsi.application.shared.Theme
import com.wutsi.application.widget.ProductWidget
import com.wutsi.flutter.sdui.AppBar
import com.wutsi.flutter.sdui.Button
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.Flexible
import com.wutsi.flutter.sdui.ListView
import com.wutsi.flutter.sdui.Screen
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.ButtonType
import com.wutsi.flutter.sdui.enums.TextAlignment
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.SearchProductRequest
import com.wutsi.platform.core.image.ImageService
import com.wutsi.regulation.RegulationEngine
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/settings/2/catalog")
class SettingsV2CatalogScreen(
    private val marketplaceManagerApi: MarketplaceManagerApi,
    private val regulationEngine: RegulationEngine,
    private val imageService: ImageService
) : AbstractSecuredEndpoint() {
    @PostMapping
    fun index(): Widget {
        val member = getCurrentMember()
        val products = marketplaceManagerApi.searchProduct(
            request = SearchProductRequest(
                storeId = member.storeId,
                limit = regulationEngine.maxProducts()
            )
        ).products

        return Screen(
            id = Page.SETTINGS_CATALOG,
            appBar = AppBar(
                elevation = 0.0,
                backgroundColor = Theme.COLOR_WHITE,
                foregroundColor = Theme.COLOR_BLACK,
                title = getText("page.settings.catalog.app-bar.title")
            ),
            floatingActionButton = if (products.size < regulationEngine.maxProducts()) {
                Button(
                    type = ButtonType.Floatable,
                    icon = Theme.ICON_ADD,
                    stretched = false,
                    iconColor = Theme.COLOR_WHITE,
                    action = gotoUrl(
                        url = urlBuilder.build("settings/2/catalog/add")
                    )
                )
            } else {
                null
            },
            child = Column(
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = getText("page.settings.catalog.count", arrayOf(products.size)),
                            alignment = TextAlignment.Center
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER),
                    Flexible(
                        child = ListView(
                            children = products.map {
                                ProductWidget.of(
                                    product = if (it.thumbnailUrl == null) {
                                        it.copy(thumbnailUrl = getNoPictureUrl())
                                    } else {
                                        it
                                    },
                                    country = regulationEngine.country(member.country),
                                    action = gotoUrl(
                                        url = urlBuilder.build("/settings/2/catalog/product?id=${it.id}")
                                    ),
                                    imageService = imageService
                                )
                            }
                        )
                    )
                )
            )
        ).toWidget()
    }
}
