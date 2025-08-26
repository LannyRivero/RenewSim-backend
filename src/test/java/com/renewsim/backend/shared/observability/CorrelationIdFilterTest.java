package com.renewsim.backend.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    @DisplayName("Propagates incoming header as X-Correlation-Id and clears MDC after chain")
    void shouldPropagateIncomingHeader() throws Exception {
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        req.addHeader(CorrelationIdFilter.HEADER, "abc-123");

        filter.doFilter(req, res, new MockFilterChain());

        assertThat(res.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("abc-123");
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Generates a correlation id if header is missing")
    void shouldGenerateWhenMissing() throws Exception {
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        String echoed = res.getHeader(CorrelationIdFilter.HEADER);
        assertThat(echoed).isNotBlank();
        assertThat(echoed).matches("^[0-9a-f\\-]{36}$");
    }

    @Test
    @DisplayName("Accepts safe incoming correlation id and clears MDC at the end")
    void usesIncomingSafeHeader() throws Exception {
        var req = mock(HttpServletRequest.class);
        var res = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);

        when(req.getHeader(CorrelationIdFilter.HEADER)).thenReturn("abc-123_ZZ");

        filter.doFilter(req, res, chain);

        verify(res).setHeader(eq(CorrelationIdFilter.HEADER), eq("abc-123_ZZ"));
        verify(chain).doFilter(req, res);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Generates UUID when incoming header is invalid and echoes it")
    void generatesWhenInvalid() throws Exception {
        var req = mock(HttpServletRequest.class);
        var res = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);

        when(req.getHeader(CorrelationIdFilter.HEADER)).thenReturn("   ../\\bad   ");

        var echoedRef = new AtomicReference<String>();
        doAnswer(inv -> {
            echoedRef.set(inv.getArgument(1, String.class));
            return null;
        }).when(res).setHeader(eq(CorrelationIdFilter.HEADER), any(String.class));

        filter.doFilter(req, res, chain);

        String echoed = echoedRef.get();
        assertThat(echoed).isNotBlank();
        assertThat(echoed).matches("^[0-9a-f\\-]{36}$");
        verify(chain).doFilter(req, res);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Puts correlation id into MDC during chain and clears it afterwards")
    void putsMdcDuringChainAndClearsAfter() throws Exception {
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();

        FilterChain assertingChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
                assertThat(cid).isNotBlank();
            }
        };

        filter.doFilter(req, res, assertingChain);

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
