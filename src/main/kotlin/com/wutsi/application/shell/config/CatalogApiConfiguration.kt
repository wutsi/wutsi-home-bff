package com.wutsi.application.shell.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.platform.catalog.Environment.PRODUCTION
import com.wutsi.platform.catalog.Environment.SANDBOX
import com.wutsi.platform.catalog.WutsiCatalogApi
import com.wutsi.platform.catalog.WutsiCatalogApiBuilder
import com.wutsi.platform.core.security.feign.FeignAuthorizationRequestInterceptor
import com.wutsi.platform.core.tracing.feign.FeignTracingRequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

@Configuration
class CatalogApiConfiguration(
    private val authorizationRequestInterceptor: FeignAuthorizationRequestInterceptor,
    private val tracingRequestInterceptor: FeignTracingRequestInterceptor,
    private val mapper: ObjectMapper,
    private val env: Environment
) {
    @Bean
    fun catalogApi(): WutsiCatalogApi =
        WutsiCatalogApiBuilder().build(
            env = environment(),
            mapper = mapper,
            interceptors = listOf(
                tracingRequestInterceptor,
                authorizationRequestInterceptor
            )
        )

    private fun environment(): com.wutsi.platform.catalog.Environment =
        if (env.acceptsProfiles(Profiles.of("prod")))
            PRODUCTION
        else
            SANDBOX
}