# Diagramas de Secuencia — RenewSim

## 1. Flujo Completo de Login 2FA

```mermaid
sequenceDiagram
    actor U as Usuario
    participant FE as Frontend React
    participant AC as AuthController
    participant AS as AuthServiceImpl
    participant RL as LoginRateLimiter
    participant DB as MySQL
    participant MAIL as SMTP/SendGrid
    participant JWT as JwtTokenProvider

    U->>FE: Ingresa email + password
    FE->>AC: POST /api/v1/auth/login/step1
    AC->>RL: checkRateLimit(ip, email)
    alt Rate limit superado (≥5 intentos)
        RL-->>AC: RateLimitExceededException
        AC-->>FE: HTTP 429 (bloqueado 15 min)
    else Rate limit OK
        AC->>AS: step1(email, password)
        AS->>DB: findUserByEmail(email)
        DB-->>AS: User | null
        AS->>AS: BCrypt.verify(password, hash)
        alt Credenciales inválidas
            AS->>DB: incrementFailedAttempts(userId)
            AS-->>AC: InvalidCredentialsException
            AC-->>FE: HTTP 401
        else Credenciales válidas
            AS->>AS: generateOtp() — SecureRandom 6 dígitos
            AS->>AS: BCrypt.hash(otp)
            AS->>DB: INSERT otp_codes (userId, codeHash, expiresAt=now+5min)
            AS->>MAIL: sendOtpEmail(email, otpCode)
            AS-->>AC: OtpSentResult
            AC-->>FE: HTTP 200 { message, expiresInSeconds: 300 }
        end
    end

    U->>FE: Ingresa código OTP de 6 dígitos
    FE->>AC: POST /api/v1/auth/login/step2
    AC->>AS: step2(email, otpCode)
    AS->>DB: findActiveOtp(userId)
    DB-->>AS: OtpCode
    alt OTP expirado o ya usado
        AS-->>AC: OtpExpiredException
        AC-->>FE: HTTP 401
    else OTP incorrecto
        AS->>DB: incrementOtpFailedAttempts(userId)
        alt ≥3 intentos OTP fallidos
            AS->>DB: markOtpInvalid(otpId)
            AS-->>AC: OtpBlockedException
            AC-->>FE: HTTP 429 (bloqueado 15 min)
        else
            AS-->>AC: InvalidOtpException
            AC-->>FE: HTTP 401 { remainingAttempts }
        end
    else OTP correcto y vigente
        AS->>DB: markOtpUsed(otpId)
        AS->>JWT: generateAccessToken(user, jti=UUID)
        AS->>JWT: generateRefreshToken(user)
        AS->>DB: INSERT refresh_tokens (userId, tokenHash, expiresAt=now+7d)
        AS->>DB: resetFailedAttempts(userId)
        AS-->>AC: AuthResponse(accessToken, refreshToken, user)
        AC-->>FE: HTTP 200 { accessToken, user } + Set-Cookie refreshToken HttpOnly
        FE->>FE: Almacena accessToken en memoria (Zustand)
    end
```

---

## 2. Crear Simulación Personalizada

```mermaid
sequenceDiagram
    actor U as Usuario
    participant FE as Frontend React
    participant SC as SimulationController
    participant SV as SimulationValidator
    participant SE as SimulationEngine
    participant ROI as ROICalculator
    participant CO2 as CO2Calculator
    participant NPV as NPVCalculator
    participant IRR as IRRCalculator
    participant DB as MySQL

    U->>FE: Completa formulario de simulación
    FE->>FE: Validación Zod (client-side)
    FE->>SC: POST /api/v1/simulations (JWT en header)
    SC->>SV: validate(SimulationRequestDTO)
    alt Parámetros inválidos
        SV-->>SC: ValidationException(fieldErrors)
        SC-->>FE: HTTP 400 { fieldErrors }
    else Parámetros válidos
        SC->>DB: findTechnology(technologyId)
        DB-->>SC: Technology
        SC->>SE: calculate(SimulationInput)
        SE->>SE: climateFactor = strategy.calculate(climateData)
        SE->>SE: energyGenerated = capacity × efficiency × 8760 × climateFactor
        SE->>ROI: calculate(totalIncome, initialInvestment)
        ROI-->>SE: roiPercentage
        SE->>SE: payback = initialInvestment / annualSavings
        SE->>CO2: calculate(energyGenerated, emissionFactor=0.5)
        CO2-->>SE: co2ReductionAnnual
        SE->>NPV: calculate(cashFlows, discountRate, lifespanYears)
        NPV-->>SE: npvValue
        SE->>IRR: calculate(cashFlows, initialInvestment)
        IRR-->>SE: irrPercentage (Newton-Raphson)
        SE-->>SC: SimulationResults
        SC->>DB: INSERT simulations (status=COMPLETED, results...)
        DB-->>SC: simulationId
        SC-->>FE: HTTP 201 { id, status: "COMPLETED", results }
        FE->>FE: Redirige a /simulations/{id}
    end
```

---

## 3. Generar Reporte con IA

```mermaid
sequenceDiagram
    actor U as Usuario
    participant FE as Frontend React
    participant SC as SimulationController
    participant AI as AIApplicationService
    participant PB as PromptBuilderService
    participant LLM as LLMProviderPort
    participant OAI as OpenAIAdapter
    participant ANT as AnthropicAdapter
    participant PDF as PdfGeneratorService
    participant DB as MySQL

    U->>FE: Clic "Generar Reporte IA"
    FE->>SC: POST /api/v1/ai/generate-report/{simulationId}
    SC->>DB: findSimulation(simulationId, userId)
    DB-->>SC: Simulation
    alt Simulación en estado DRAFT
        SC-->>FE: HTTP 409 { message: "Simulación debe estar COMPLETED" }
    else Simulación COMPLETED
        SC->>AI: generateReport(simulation, language)
        AI->>PB: buildReportPrompt(simulationData, language)
        PB-->>AI: AIRequest(systemPrompt, userPrompt)
        AI->>LLM: complete(AIRequest)
        LLM->>OAI: POST /v1/chat/completions
        alt OpenAI disponible
            OAI-->>LLM: AIResponse(narrativeText)
        else OpenAI no disponible
            LLM->>ANT: POST /v1/messages (fallback)
            alt Anthropic disponible
                ANT-->>LLM: AIResponse(narrativeText)
            else Ambos caídos
                LLM-->>AI: LLMProviderException
                AI-->>SC: AIServiceUnavailableException
                SC-->>FE: HTTP 503 { errorCode: "AI_SERVICE_UNAVAILABLE" }
            end
        end
        LLM-->>AI: AIResponse
        AI-->>SC: ReportNarrative
        alt Accept: application/pdf
            SC->>PDF: generatePdf(simulation, narrative)
            PDF-->>SC: byte[] pdfContent
            SC-->>FE: HTTP 200 (Content-Type: application/pdf)
        else Accept: application/json
            SC-->>FE: HTTP 200 { narrative, simulationData }
        end
    end
```

---

## 4. Refresh Token Automático desde el Frontend

```mermaid
sequenceDiagram
    participant FE as Frontend React
    participant AX as Axios Interceptor
    participant AC as AuthController
    participant AS as AuthServiceImpl
    participant DB as MySQL

    FE->>AX: Request con accessToken expirado
    AX->>AX: Detecta HTTP 401 en respuesta
    AX->>AX: Pausa cola de requests pendientes
    AX->>AC: POST /api/v1/auth/refresh (refreshToken en cookie HttpOnly)
    AC->>AS: refresh(refreshTokenHash)
    AS->>DB: findRefreshToken(tokenHash)
    DB-->>AS: RefreshToken
    alt Token revocado o expirado
        AS-->>AC: InvalidRefreshTokenException
        AC-->>AX: HTTP 401
        AX->>AX: Limpia estado de auth (Zustand)
        AX->>FE: Redirige a /login
    else Token válido
        AS->>DB: markTokenRevoked(oldTokenId)
        AS->>AS: generateNewAccessToken(user)
        AS->>AS: generateNewRefreshToken(user)
        AS->>DB: INSERT refresh_tokens (nuevo token)
        AS-->>AC: NewTokens(accessToken)
        AC-->>AX: HTTP 200 { accessToken } + Set-Cookie refreshToken
        AX->>AX: Actualiza accessToken en Zustand
        AX->>AX: Reanuda cola de requests pendientes
        AX->>FE: Reintenta request original con nuevo token
    end
```

---

## 5. Sugerencias de Configuración con IA

```mermaid
sequenceDiagram
    actor U as Usuario
    participant FE as Frontend React
    participant AC as AIController
    participant AI as AIApplicationService
    participant PB as PromptBuilderService
    participant LLM as LLMProviderPort
    participant OAI as OpenAIAdapter
    participant ANT as AnthropicAdapter

    U->>FE: Completa formulario de sugerencias\n(ubicación, consumo, presupuesto)
    FE->>AC: POST /api/v1/ai/suggest-configuration
    AC->>AI: suggestConfiguration(location, consumption, budget, tariff)
    AI->>PB: buildSuggestConfigPrompt(request)
    PB-->>AI: AIRequest(systemPrompt, userPrompt con datos)

    AI->>LLM: complete(AIRequest)
    LLM->>OAI: POST /v1/chat/completions

    alt OpenAI disponible
        OAI-->>LLM: AIResponse(JSON con 3 recomendaciones)
    else OpenAI no disponible
        LLM->>ANT: POST /v1/messages (fallback)
        alt Anthropic disponible
            ANT-->>LLM: AIResponse(JSON con 3 recomendaciones)
        else Anthropic también caído
            LLM-->>AI: LLMProviderException
            AI-->>AC: AIServiceUnavailableException
            AC-->>FE: HTTP 503 { errorCode: "AI_SERVICE_UNAVAILABLE" }
        end
    end

    LLM-->>AI: AIResponse
    AI->>AI: parseJSON(response)
    AI->>AI: validateRecommendations()\n— al menos 3, rankings válidos
    AI-->>AC: List<ConfigurationRecommendation>
    AC-->>FE: HTTP 200 { recommendations: [...] }
    FE->>FE: Renderiza tarjetas de recomendaciones
    FE->>FE: Botón "Crear simulación desde esta recomendación"\npre-rellena el formulario
```
