package com.wutsi.application.servlet

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.platform.core.security.TokenBlacklistService
import com.wutsi.platform.core.security.TokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class TokenVerifierFilterTest {
    private lateinit var blacklist: TokenBlacklistService
    private lateinit var tokenProvider: TokenProvider
    private lateinit var requestMatcher: RequestMatcher
    private lateinit var filter: TokenVerifierFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var chain: FilterChain

    private val token: String = "430430940394"

    @BeforeEach
    fun setUp() {
        blacklist = mock()
        tokenProvider = mock()
        requestMatcher = mock()
        request = mock()
        response = mock()
        chain = mock()
        filter = TokenVerifierFilter(blacklist, tokenProvider, requestMatcher, mock())

        doReturn(token).whenever(tokenProvider).getToken()
        doReturn(true).whenever(requestMatcher).matches(any())
    }

    @Test
    fun `do not verify when request doesnt match`() {
        doReturn(false).whenever(requestMatcher).matches(any())

        filter.doFilter(request, response, chain)

        verify(blacklist, never()).contains(any())
        verify(chain).doFilter(request, response)
    }

    @Test
    fun `do not verify when no token available`() {
        doReturn(null).whenever(tokenProvider).getToken()

        filter.doFilter(request, response, chain)

        verify(blacklist, never()).contains(any())
        verify(chain).doFilter(request, response)
    }

    @Test
    fun `verify matching request`() {
        doReturn(false).whenever(blacklist).contains(any())

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
    }

    @Test
    fun `return 401 when token blacklisted`() {
        doReturn(true).whenever(blacklist).contains(any())

        filter.doFilter(request, response, chain)

        verify(response).sendError(401, "Logged out")
        verify(chain, never()).doFilter(any(), any())
    }
}
