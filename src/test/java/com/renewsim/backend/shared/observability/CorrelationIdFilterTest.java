package com.renewsim.backend.shared.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.jboss.logging.MDC;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void shouldPropagateIncomingHeader() throws Exception {
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        req.addHeader(CorrelationIdFilter.HEADER, "abc-123");

        filter.doFilter(req, res, new MockFilterChain());

        assertThat(res.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("abc-123");
        assertThat(org.slf4j.MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void shouldGenerateWhenMissing() throws Exception {
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();

        filter.doFilter(req, res, new MockFilterChain());

        assertThat(res.getHeader(CorrelationIdFilter.HEADER)).isNotBlank();
    }

    @Test
    @DisplayName("Sets safe incoming correlation id and clears MDC")
    void usesIncomingSafeHeader() throws Exception {
        var filter = new CorrelationIdFilter();
        var req = mock(HttpServletRequest.class);
        var res = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);

        when(req.getHeader(CorrelationIdFilter.HEADER)).thenReturn("abc-123_ZZ");

        filter.doFilter(req, res, chain);

        verify(res).setHeader(CorrelationIdFilter.HEADER, "abc-123_ZZ");
        verify(chain).doFilter(req, res);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Generates UUID when incoming header is invalid and echoes it")
    void generatesWhenInvalid() throws Exception {
        var filter = new CorrelationIdFilter();
        var req = mock(HttpServletRequest.class);
        var res = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);

        when(req.getHeader(CorrelationIdFilter.HEADER)).thenReturn("   ../\\bad   ");

        var headerCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(res).setHeader(eq(CorrelationIdFilter.HEADER), headerCaptor.capture());

        filter.doFilter(req, res, chain);

        String echoed = headerCaptor.getValue();
        assertThat(echoed).isNotBlank();
        assertThat(echoed).matches("^[0-9a-f\\-]{36}$");
        verify(chain).doFilter(req, res);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    @DisplayName("Puts correlation id in MDC and clears after")
    void putsAndClearsMdc() throws Exception {
        var filter = new CorrelationIdFilter();
        var req = mock(HttpServletRequest.class);
        var res = mock(HttpServletResponse.class);
        var chain = mock(FilterChain.class);

        when(req.getHeader(CorrelationIdFilter.HEADER)).thenReturn(null);

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
