package com.wutsi.application.marketplace.catalog.home.fragment

import com.wutsi.application.AbstractEndpoint
import com.wutsi.application.Page
import com.wutsi.application.shared.Theme
import com.wutsi.application.widget.GridWidget
import com.wutsi.application.widget.OfferWidget
import com.wutsi.flutter.sdui.Column
import com.wutsi.flutter.sdui.Container
import com.wutsi.flutter.sdui.Divider
import com.wutsi.flutter.sdui.SingleChildScrollView
import com.wutsi.flutter.sdui.Text
import com.wutsi.flutter.sdui.Widget
import com.wutsi.flutter.sdui.enums.CrossAxisAlignment
import com.wutsi.flutter.sdui.enums.MainAxisAlignment
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.SearchProductRequest
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.platform.core.image.ImageService
import com.wutsi.regulation.RegulationEngine
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/catalog/2/fragment")
class CatalogV2Fragment(
    private val membershipManagerApi: MembershipManagerApi,
    private val marketplaceManagerApi: MarketplaceManagerApi,
    private val regulationEngine: RegulationEngine,
    private val imageService: ImageService
) : AbstractEndpoint() {
    @PostMapping
    fun widget(@RequestParam(required = false) id: Long? = null): Widget {
        val member = membershipManagerApi.getMember(id).member
        val country = regulationEngine.country(member.country)
        if (!member.business || member.storeId == null || !country.supportsStore) {
            return Container().toWidget()
        }

        val products = marketplaceManagerApi.searchProduct(
            request = SearchProductRequest(
                storeId = member.storeId,
                status = "PUBLISHED",
                limit = regulationEngine.maxProducts()
            )
        ).products

        return SingleChildScrollView(
            child = Column(
                mainAxisAlignment = MainAxisAlignment.start,
                crossAxisAlignment = CrossAxisAlignment.start,
                children = listOf(
                    Container(
                        padding = 10.0,
                        child = Text(
                            caption = if (products.isEmpty()) {
                                getText("page.catalog.0_product")
                            } else if (products.size == 1) {
                                getText("page.catalog.1_product")
                            } else {
                                getText("page.catalog.n_products", arrayOf(products.size))
                            }
                        )
                    ),
                    Divider(color = Theme.COLOR_DIVIDER),
                    GridWidget(
                        columns = 2,
                        children = products.map {
                            OfferWidget.of(
                                product = it,
                                country = country,
                                action = gotoUrl(urlBuilder.build("${Page.getProductUrl()}?id=${it.id}")),
                                imageService = imageService
                            )
                        }
                    )
                )
            )
        ).toWidget()
    }
}