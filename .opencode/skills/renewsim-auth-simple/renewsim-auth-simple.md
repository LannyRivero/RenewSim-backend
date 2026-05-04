---
description: "Implementa y audita el flujo de autenticación simple de RenewSim: Email verification + Login directo (email+password) + JWT + refresh token rotation."
version: 2
scope: renewsim-auth-simple
auto_invoke: true
Usar cuando:
  - Se implemente login
  - Se modifique OTP
  - Se cambie JWT o refresh tokens
  - Se audite seguridad de auth
---

# Flow Definition (OBLIGATORIO)
### Register → Verify Email → Login

## Step 1 — Register
Input:
- email 
- password
- fullName
**Proceso:** 
1. Validar rate limit 
2. Verificar email no existe 
3. Hashear password (BCrypt)
4. Crear usuario (status=INACTIVE, email_verified=false)
5. Generar verification token (32 bytes, base64) 
6. Guardar EmailVerificationToken (expires en 48h) 
7. Enviar email con link de verificación
**Output:** 
- mensaje genérico: "Check your email to verify your account" - userId
---
## Step 2 — Verify Email

Input:

- verificationToken (desde URL: `/verify-email?token=...`)

**Proceso:** 
1. Buscar EmailVerificationToken activo 
2. Verificar no expirado 
3. Verificar no usado 
4. Marcar token como usado 
5. Actualizar usuario: email_verified=true, status=ACTIVE

**Output:** - mensaje: "Email verified successfully. You can now login."

---
#### Step 3 — Login 
**Input:** 
- email - password 

**Proceso:** 
1. Validar rate limit 
2. Buscar usuario por email
3. Verificar password (BCrypt) 
4. **Verificar email_verified=true** ← CRÍTICO
5. Verificar account enabled 
6. Generar tokens: - accessToken (JWT, 1h) - refreshToken (JWT, 7 días) 
7. Guardar RefreshToken en BD
8. Enviar refreshToken en cookie HttpOnly

**Output:** 
- accessToken (header `Authorization: Bearer <token>`) 
- refreshToken (cookie HttpOnly, Secure, SameSite=Strict)
- userId
- username 
- roles
- 
---
## Tokens

### Access Token
 - **Duración**: 1h - 
 **Contiene**: 
 - userId
 - username 
 - roles
 - jti (JWT ID)
 **Enviado en**: 
 - Header `Authorization: Bearer <token>` 
 
 ### Refresh Token
  - **Duración**: 7 días 
  - **Almacenado**: Hash en BD (`refresh_tokens` table)
  - **Enviado en**: Cookie `HttpOnly`, `Secure`, `SameSite=Strict` 
  - **Rotación**: Obligatoria en cada uso

---

# Security Rules

✅ **MUST**
- Password siempre BCrypt 
- Email verification obligatoria antes de login 
- Tokens nunca logueados 
- Rate limiting obligatorio en `/login` y `/register` 
- Refresh token rotation en cada uso 
- Access token en header (NO en cookies) 
- Refresh token en cookie HttpOnly (NO en localStorage)

❌ **NEVER**
 - Login sin email verificado
 - Password en texto plano
 - Tokens en logs 
 - Refresh token en localStorage 
 - Access token en cookies 
 - Respuestas que revelen si usuario existe
---

# Domain Model

Debe existir:
### EmailVerificationToken 
```
java class EmailVerificationToken { Long userId; String token; // 32 bytes base64 LocalDateTime expiresAt; // 48h desde creación boolean used; } 
``` 
### RefreshToken 
```
java class RefreshToken { Long userId; String tokenHash; // hash del JWT LocalDateTime expiresAt; // 7 días boolean revoked; }
```

---

# Anti-patterns
### 🚨 CRÍTICOS (P0): 
- Login permitido sin email verificado
- Token sin rotación 
- Login sin rate limiting 
- Controller con lógica de negocio 
- Password sin hash
###  ⚠️ IMPORTANTES (P1):
- Tokens logueados
- Respuestas que filtran información 
- Falta de tests de seguridad
---

# Required Tests

### Unit - 
- ✅ Login exitoso con email verificado
- ✅ Login bloqueado si email NO verificado 
- ✅ Password incorrecto → 401 
- ✅ Usuario no existe → 401 (genérico) 
- ✅ Email verification token expirado 
### Integration 
- ✅ Flujo completo: Register → VerifyEmail → Login 
- ✅ Refresh token rotation - 
- ✅ Logout invalidation

### Security 
- ✅ Sin token → 401 
- ✅ Token inválido → 401
- ✅ Acceso sin permisos → 403 
- ✅ Rate limit en login → 429

---

## API Contracts

POST /api/v1/auth/register 
POST /api/v1/auth/email-verification/verify 
POST /api/v1/auth/email-verification/resend 
POST /api/v1/auth/login ← NUEVO (simple)
POST /api/v1/auth/refresh 
POST /api/v1/auth/logout

---
# Expected Result

Sistema:
- ✅ Resistente a ataques (brute force, credential stuffing)
- ✅ Desacoplado (hexagonal)
- ✅ Testeado (≥70% coverage)
- ✅ Alineado con API REST
- ✅ Email verification obligatoria
- ✅ Login simple y seguro

--- 
## Migration Notes **Cambios desde v1.0 (2FA con OTP):**
- ❌ REMOVIDO: LoginStep1/Step2 (flujo OTP)
- ❌ REMOVIDO: OtpCode domain model
- ❌ REMOVIDO: ActivationToken 
- ✅ AGREGADO: Email verification obligatoria 
- ✅ AGREGADO: Login simple (email+password) 
- ✅ SIMPLIFICADO: Un solo endpoint `/login` **Decisión documentada en**: ADR-011