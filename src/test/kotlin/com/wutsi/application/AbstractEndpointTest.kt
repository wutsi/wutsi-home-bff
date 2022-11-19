package com.wutsi.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.platform.core.tracing.TracingContext
import com.wutsi.platform.core.tracing.spring.SpringTracingRequestInterceptor
import com.wutsi.platform.payment.core.ErrorCode
import com.wutsi.security.manager.SecurityManagerApi
import feign.FeignException
import feign.Request
import feign.RequestTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.Cache
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractEndpointTest {
    companion object {
        val DEVICE_ID = UUID.randomUUID().toString()
        val TRACE_ID = UUID.randomUUID().toString()
        const val PHONE_NUMBER = "+237670000010"
    }

    @Autowired
    private lateinit var mapper: ObjectMapper

    @MockBean
    protected lateinit var tracingContext: TracingContext

    @MockBean
    protected lateinit var cache: Cache

    @MockBean
    protected lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    protected lateinit var securityManagerApi: SecurityManagerApi

    @Autowired
    private lateinit var messages: MessageSource

    protected val rest = RestTemplate()

    @BeforeTest
    fun setUp() {
        doReturn(DEVICE_ID).whenever(tracingContext).deviceId()
        doReturn(TRACE_ID).whenever(tracingContext).traceId()

        rest.interceptors.add(SpringTracingRequestInterceptor(tracingContext))
    }

    protected fun assertEndpointEquals(expectedPath: String, url: String) {
        val request = emptyMap<String, String>()
        val response = rest.postForEntity(url, request, Map::class.java)

        assertJsonEquals(expectedPath, response.body)
    }

    private fun assertJsonEquals(expectedPath: String, value: Any?) {
        val input = AbstractEndpointTest::class.java.getResourceAsStream(expectedPath)
        val expected = mapper.readValue(input, Any::class.java)

        val writer = mapper.writerWithDefaultPrettyPrinter()
        assertEquals(writer.writeValueAsString(expected), writer.writeValueAsString(value))
    }

    protected fun getText(key: String, args: Array<Any?> = emptyArray()) =
        messages.getMessage(key, args, LocaleContextHolder.getLocale()) ?: key

    protected fun createFeignException(
        errorCode: String,
        downstreamError: ErrorCode? = null,
        data: Map<String, Any> = emptyMap()
    ) = FeignException.Conflict(
        "",
        Request.create(
            Request.HttpMethod.POST,
            "https://www.google.ca",
            emptyMap(),
            "".toByteArray(),
            Charset.defaultCharset(),
            RequestTemplate()
        ),
        """
            {
                "error":{
                    "code": "$errorCode",
                    "downstreamCode": "$downstreamError",
                    "data": ${toJsonString(data)}
                }
            }
        """.trimIndent().toByteArray(),
        emptyMap()
    )

    private fun toJsonString(data: Map<String, Any>): String =
        ObjectMapper().writeValueAsString(data)
}