package com.wutsi.application.shell.endpoint.settings.about.screen

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

internal class AppInfoRestInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        request.headers["X-Client-Version"] = "1.1.100.20"
        request.headers["X-OS"] = "Android"
        request.headers["X-OS-Version"] = "10.02302930"

        return execution.execute(request, body)
    }
}
