package com.renewsim.backend.auth_service.infrastructure.security;

import com.renewsim.backend.auth_service.application.port.out.TokenBlacklistPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.testutil.UnitTestBase;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest extends UnitTestBase {

    @Mock
    private TokenProvider tokenProvider;
    @Mock
    private TokenBlacklistPort tokenBlacklistPort;
    @Mock
    private FilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest req;
    private MockHttpServletResponse res;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Should populate SecurityContext when Bearer token is valid")
    void testShouldPopulateContext_WhenBearerValid() throws Exception {
        req.addHeader("Authorization", "Bearer valid-token");

        var user = new AuthenticatedUser(
                "john",
                Set.of("USER"),
                Set.of("read"));

        when(tokenProvider.validate("valid-token")).thenReturn(Optional.of(user));

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(((AuthenticatedUser)auth.getPrincipal()).username()).isEqualTo("john");
        assertThat(auth.getAuthorities().stream().map(Object::toString))
                .containsExactlyInAnyOrder("ROLE_USER", "SCOPE_read");

        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should not populate SecurityContext when Authorization header is missing")
    void testShouldNotPopulateContext_WhenNoAuthorizationHeader() throws Exception {
        filter.doFilter(req, res, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should not populate SecurityContext when token is invalid")
    void testShouldNotPopulateContext_WhenTokenInvalid() throws Exception {
        req.addHeader("Authorization", "Bearer invalid");
        when(tokenProvider.validate("invalid")).thenReturn(Optional.empty());

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should skip filter when authentication already exists in SecurityContext")
    void testShouldSkipFilter_WhenAuthenticationAlreadyPresent() throws Exception {
        var preAuth = new UsernamePasswordAuthenticationToken("already", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(preAuth);

        req.addHeader("Authorization", "Bearer any-token");

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(preAuth);
        verifyNoInteractions(tokenProvider);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should not validate token when Authorization header does not start with Bearer")
    void testShouldNotValidate_WhenHeaderWithoutBearerPrefix() throws Exception {
        req.addHeader("Authorization", "Token abc");
        filter.doFilter(req, res, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should continue filter chain when TokenProvider throws an exception")
    void testShouldContinueChain_WhenTokenProviderThrows() throws Exception {
        req.addHeader("Authorization", "Bearer boom");
        when(tokenProvider.validate("boom")).thenThrow(new RuntimeException("parse error"));
        filter.doFilter(req, res, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokenProvider).validate("boom");
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should populate only role authorities when scopes are null")
    void testShouldPopulateOnlyRoleAuthorities_WhenScopesAreNull() throws Exception {
        req.addHeader("Authorization", "Bearer t");
        var user = new AuthenticatedUser("john", Set.of("USER", "ADMIN"), null);
        when(tokenProvider.validate("t")).thenReturn(Optional.of(user));
        filter.doFilter(req, res, chain);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities().stream().map(Object::toString))
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should set WebAuthenticationDetails on authentication")
    void testShouldSetWebAuthenticationDetails_OnAuthentication() throws Exception {
        req.setRemoteAddr("127.0.0.1");
        req.addHeader("Authorization", "Bearer ok");
        var user = new AuthenticatedUser("john", Set.of("USER"), Set.of("read"));
        when(tokenProvider.validate("ok")).thenReturn(Optional.of(user));
        filter.doFilter(req, res, chain);
        var details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        assertThat(details)
                .isInstanceOf(org.springframework.security.web.authentication.WebAuthenticationDetails.class);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should accept case-insensitive 'bearer' and trim token and header spaces")
    void testShouldAcceptCaseInsensitiveBearerAndTrim() throws Exception {
        req.addHeader("Authorization", "   bEaReR    ok   ");
        when(tokenProvider.validate("ok")).thenReturn(Optional.of(
                new AuthenticatedUser("john", Set.of("USER"), Set.of("read"))));

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(((AuthenticatedUser)auth.getPrincipal()).username()).isEqualTo("john");
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should NOT authenticate when provider throws on expired token")
    void testShouldNotAuthenticate_WhenProviderThrowsExpired() throws Exception {
        req.addHeader("Authorization", "Bearer expired");
        when(tokenProvider.validate("expired")).thenThrow(new RuntimeException("Expired"));

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should authenticate with empty authorities when token has only subject (no roles/scopes)")
    void testShouldAuthenticate_WithSubjectOnly() throws Exception {
        req.addHeader("Authorization", "Bearer subject-only");
        when(tokenProvider.validate("subject-only"))
                .thenReturn(Optional.of(new AuthenticatedUser("john", null, null)));

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(((AuthenticatedUser)auth.getPrincipal()).username()).isEqualTo("john");
        assertThat(auth.getAuthorities()).isEmpty();
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should not validate when 'Bearer' has no token or only spaces")
    void testShouldNotValidate_WhenBearerWithoutToken() throws Exception {
        req.addHeader("Authorization", "Bearer    ");

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should ignore header not matching Bearer scheme even if it contains the word 'Bearer'")
    void testShouldIgnoreHeader_ContainingBearerWordButWrongScheme() throws Exception {
        req.addHeader("Authorization", "Token Bearer abc");

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("ShouldNotFilter: skip JWT validation on public endpoints (e.g., /auth/login)")
    void shouldSkipOnPublicEndpoints() throws Exception {
        req.setRequestURI("/auth/login");
        req.addHeader("Authorization", "Bearer whatever");

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenBlacklistPort);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should not validate when 'Bearer' prefix is glued to token (missing space)")
    void shouldNotValidate_WhenBearerPrefixWithoutSpace() throws Exception {
        req.addHeader("Authorization", "BearerX abc.def.ghi");
        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenProvider);
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("Should accept odd spacing around Bearer and still extract token")
    void shouldHandleOddSpacingAroundBearer() throws Exception {
        req.addHeader("Authorization", "\tBearer\t\tok\t");
        when(tokenProvider.validate("ok"))
                .thenReturn(Optional.of(new AuthenticatedUser("john", Set.of("USER"), Set.of("read"))));

        filter.doFilter(req, res, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(((AuthenticatedUser)auth.getPrincipal()).username()).isEqualTo("john");
        assertThat(auth.getAuthorities().stream().map(Object::toString))
                .containsExactlyInAnyOrder("ROLE_USER", "SCOPE_read");
        verify(chain).doFilter(req, res);
    }

}