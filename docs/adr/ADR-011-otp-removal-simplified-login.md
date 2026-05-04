# ADR-011: Eliminación del flujo 2FA con OTP y simplificación del login

## Status: Accepted
## Date: 2026-05-04
## Deciders: Development Team

## Contexto

El diseño original de `auth_service` contemplaba un flujo de autenticación en dos pasos (2FA):
- **Paso 1** (`POST /api/v1/auth/login/step1`): validar credenciales → generar OTP → enviar por email
- **Paso 2** (`POST /api/v1/auth/login/step2`): validar OTP → emitir tokens JWT

Este flujo fue reemplazado por un login directo de un solo paso (`POST /api/v1/auth/login`) que valida credenciales y emite tokens inmediatamente.

La decisión se tomó tras evaluar el coste de implementación del flujo OTP (EmailPort no implementado, tabla `otp_codes`, limpieza programada, rate limiting por OTP) frente al valor que aporta en el contexto actual del proyecto.

## Decisión

**Eliminar el flujo 2FA con OTP** y adoptar autenticación directa de un paso con JWT.

El flujo simplificado es:
1. Validar formato de credenciales
2. Buscar usuario por email
3. Verificar hash de contraseña
4. Verificar cuenta activa (`enabled = true`)
5. Emitir access token (TTL: 3600s) + refresh token (TTL: 604800s)

## Justificación

### Por qué se elimina el OTP:
- `EmailPort` no tiene implementación real — el OTP nunca llegaría al usuario
- La tabla `otp_codes` y su limpieza programada añaden complejidad operacional sin valor demostrable en v1
- El rate limiting por OTP duplica lógica ya cubierta por el rate limiter de login
- El proyecto es una plataforma de simulación energética, no un sistema bancario — el nivel de seguridad 2FA no es requisito del dominio en esta fase

### Trade-off aceptado conscientemente:
- **Se sacrifica**: seguridad adicional frente a robo de credenciales
- **Se gana**: flujo implementable, testeable y funcional en producción real
- **Condición**: la decisión es revisable si el proyecto evoluciona hacia usuarios con datos sensibles o requisitos regulatorios

## Decisión sobre el flag `enabled`

El campo `enabled` en `UserSnapshot` unifica dos conceptos:
- Email verificado (`emailVerified = true`)
- Cuenta activa (`status = ACTIVE`)

Ambas condiciones se comprueban mediante un único flag booleano. Como consecuencia, un usuario con email no verificado y un usuario desactivado por un administrador reciben el mismo error (`AUTH_USER_DISABLED`).

**Esta unificación es una decisión consciente para v1.2** bajo las siguientes condiciones:
- El mensaje de error al cliente es intencionalmente genérico (no revelar motivo exacto)
- Los logs internos distinguen el caso con `userId` y `email` enmascarado
- Si en el futuro se necesita distinguir ambos casos (ej: UI diferente, soporte al usuario), se separarán en dos flags con error codes distintos

## Consecuencias

### Positivas:
- ✅ Login funcional y testeable en producción (`POST /api/v1/auth/login` → HTTP 200)
- ✅ `LoginServiceTest` cubre happy path, usuario no encontrado, contraseña incorrecta y cuenta desactivada
- ✅ Refresh token con TTL correcto (7 días) via `TokenProvider.generate(user, ttl)`
- ✅ Deuda técnica eliminada: tabla `otp_codes`, `OtpGeneratorPort`, `OtpRepositoryPort`, limpieza programada

### Negativas:
- ⚠️ Sin 2FA, el sistema es vulnerable a ataques de credenciales robadas
- ⚠️ El flag `enabled` unificado impide distinguir el motivo de bloqueo desde el error HTTP
- ⚠️ Los endpoints `/login/step1`, `/login/step2` y `/auth/resend-otp` referenciados en tests E2E del `IMPLEMENTATION_PLAN.md` son obsoletos y deben actualizarse

## Deuda técnica registrada

| ID | Descripción | Prioridad |
|---|---|---|
| D3-01 | Actualizar tests E2E en `IMPLEMENTATION_PLAN.md` para reflejar flujo de un paso | P2 |
| D3-02 | Si se requiere 2FA en el futuro, implementar via TOTP (RFC 6238) en lugar de OTP por email | P3 |
| D3-03 | Separar `enabled` en `emailVerified` + `accountActive` si la UI necesita distinguir casos | P2 |

## Referencias
- ADR-007: Auth service rewrite
- `LoginService.java` — implementación del flujo simplificado
- `LoginServiceTest.java` — cobertura del flujo actual
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)