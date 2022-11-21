package com.wutsi.application.marketplace.settings.catalog.add.page

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.AbstractSecuredEndpointTest
import com.wutsi.application.Page
import com.wutsi.application.marketplace.settings.catalog.add.entity.PictureEntity
import com.wutsi.platform.core.storage.StorageService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.net.URL
import kotlin.test.assertTrue

internal class AppProduct00PicturePageTest : AbstractSecuredEndpointTest() {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var storageService: StorageService

    private fun url(action: String = "") =
        "http://localhost:$port${Page.getSettingsCatalogUrl()}/add/pages/picture$action"

    @Test
    fun index() = assertEndpointEquals("/marketplace/settings/catalog/home/pages/picture.json", url())

    @Test
    fun upload() {
        // GIVEN
        val filename = "toto.png"
        val fileUrl = URL("http://www.wutsi.com/asset/1/$filename")
        doReturn(fileUrl).whenever(storageService).store(any(), any(), anyOrNull(), anyOrNull(), anyOrNull())

        // WHEN
        uploadFile(url("/upload"), "toto.png")

        // THEN
        val path = argumentCaptor<String>()
        verify(storageService).store(path.capture(), any(), eq("image/png"), anyOrNull(), anyOrNull())
        assertTrue(path.firstValue.startsWith("product/pictures/"))
        assertTrue(path.firstValue.endsWith(filename))

        verify(cache).put(
            DEVICE_ID,
            PictureEntity(fileUrl.toString())
        )
    }
}
