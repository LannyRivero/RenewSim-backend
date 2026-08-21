# Documento de Requerimientos — Plataforma RenewSim

## Introducción

RenewSim es una plataforma profesional de simulación de energías renovables que permite a los usuarios modelar, analizar y comparar instalaciones de energía renovable (solar, eólica, hidroeléctrica) mediante simulaciones personalizadas y escenarios predefinidos. La plataforma calcula la producción energética, métricas financieras (ROI, VAN, TIR, período de recuperación) e impacto ambiental (reducción de CO₂), y presenta los resultados a través de dashboards interactivos e informes exportables.

El sistema está construido sobre un backend Java 21 / Spring Boot siguiendo Arquitectura Hexagonal (Puertos y Adaptadores) con DDD Táctico. La visión completa del producto contempla frontend, capacidades avanzadas y despliegues evolutivos, pero este repositorio concentra el backend real actualmente implementado y endurecido. Sirve a estudiantes, hogares, pequeñas empresas, gobiernos y empresas energéticas como plataforma de simulación y análisis de proyectos renovables.

---

## Glosario

- **Sistema**: La plataforma RenewSim en su conjunto (backend + frontend)
- **Auth_Service**: El bounded context responsable de autenticación, emisión de JWT y gestión de sesiones
- **User_Service**: El bounded context responsable del registro, activación y gestión de perfil de usuarios
- **Role_Service**: El bounded context responsable de la gestión de roles y permisos
- **Technology_Service**: El bounded context responsable del catálogo de tecnologías de energía renovable
- **Simulation_Service**: El bounded context principal responsable de la creación, cálculo, ciclo de vida e informes de simulaciones
- **Simulation_Engine**: El componente de dominio dentro de Simulation_Service que ejecuta los cálculos energéticos, financieros y ambientales
- **AI_Service**: Capacidad de producto planificada para sugerencias, predicciones y asistencia conversacional impulsadas por IA. No forma parte del baseline implementado actual de este repositorio.
- **Usuario**: Un actor humano autenticado que interactúa con la plataforma
- **Admin**: Un Usuario con el rol ADMIN, autorizado para gestionar datos del catálogo y roles
- **Analista**: Un Usuario con el rol ANALYST, autorizado para acceder a analíticas avanzadas
- **Simulación**: Un aggregate root que representa el análisis de un proyecto de energía renovable con entradas, resultados calculados y un estado de ciclo de vida
- **Escenario**: Una plantilla de simulación predefinida con parámetros pre-rellenados para casos de uso comunes
- **Tecnología**: Un tipo de fuente de energía renovable (SOLAR, WIND, HYDRO, BIOMASS, GEOTHERMAL) con parámetros técnicos asociados
- **Ubicación**: Un value object que representa coordenadas geográficas (latitud, longitud)
- **Dinero**: Un value object que representa un monto monetario con moneda (por defecto: USD)
- **DatosEnergia**: Un value object que representa la generación anual de energía en kWh/año
- **DatosClimaticos**: Un value object que encapsula datos de irradiación solar, velocidad del viento y temperatura
- **SimulationStatus**: Un enum con estados DRAFT → COMPLETED → DELETED
- **EnergyType**: Un enum con valores SOLAR, WIND, HYDRO, BIOMASS, GEOTHERMAL
- **JWT**: JSON Web Token utilizado para autenticación sin estado
- **ROI**: Retorno sobre la Inversión — ganancia porcentual relativa a la inversión inicial
- **VAN**: Valor Actual Neto — valor presente de flujos de caja futuros menos la inversión inicial
- **TIR**: Tasa Interna de Retorno — tasa de descuento que hace el VAN igual a cero
- **Payback**: El número de años necesarios para recuperar la inversión inicial con los ahorros anuales
- **Reducción CO₂**: Reducción anual de emisiones de dióxido de carbono en toneladas métricas
- **WCAG**: Pautas de Accesibilidad para el Contenido Web versión 2.1 Nivel AA
- **ADR**: Architecture Decision Record (Registro de Decisión Arquitectónica)
- **PII**: Información de Identificación Personal

---

## Requerimientos

### Requerimiento 1: Registro de Usuario y Verificación de Cuenta

**Historia de Usuario:** Como nuevo visitante, quiero registrar una cuenta con mi email y contraseña, para poder acceder a la plataforma de simulación.

#### Criterios de Aceptación

1. CUANDO se envía una solicitud de registro con un email único y una contraseña válida, EL User_Service DEBERÁ crear una nueva cuenta de usuario en estado INACTIVE con el rol USER asignado por defecto.
2. CUANDO se envía una solicitud de registro, EL User_Service DEBERÁ enviar un email de activación que contenga un token de activación con tiempo limitado (válido por 24 horas).
3. SI una solicitud de registro contiene un email que ya existe en el sistema, ENTONCES EL User_Service DEBERÁ retornar una respuesta de error HTTP 409 con un mensaje descriptivo.
4. SI una solicitud de registro contiene una contraseña de menos de 8 caracteres o sin letra mayúscula, dígito o símbolo especial, ENTONCES EL User_Service DEBERÁ retornar un error HTTP 400 con un mensaje de validación a nivel de campo.
5. CUANDO se envía un token de verificación mediante POST /api/v1/auth/email-verification/verify, EL sistema DEBERÁ validar el token y permitir que la cuenta continúe el flujo de acceso previsto.
6. SI un token de activación está expirado o no se encuentra, ENTONCES EL User_Service DEBERÁ retornar un error HTTP 400 con un mensaje descriptivo.
7. EL User_Service DEBERÁ almacenar todas las contraseñas como hashes BCrypt con factor de fuerza 12 y NUNCA persistirá ni registrará contraseñas en texto plano.

---

### Requerimiento 2: Autenticación con JWT y Refresh Token

**Historia de Usuario:** Como usuario registrado, quiero iniciar sesión con mis credenciales y obtener un access token más un refresh token seguro, para acceder a la plataforma sin depender de estado de sesión en servidor.

#### Flujo Actual

```
LOGIN:
  Usuario → POST /api/v1/auth/login (email + password)
  Sistema valida credenciales y estado de cuenta
  Respuesta: { accessToken, tokenType, expiresIn, user... } + refresh token en cookie HttpOnly

REFRESH:
  Usuario → POST /api/v1/auth/refresh
  Sistema rota refresh token y emite nuevo access token

LOGOUT:
  Usuario → POST /api/v1/auth/logout
  Sistema revoca refresh tokens e invalida el access token
```

#### Criterios de Aceptación

1. CUANDO se envía POST /api/v1/auth/login con credenciales válidas de una cuenta habilitada, EL Auth_Service DEBERÁ emitir un access token JWT y un refresh token, retornando HTTP 200.
2. SI las credenciales son inválidas, ENTONCES EL Auth_Service DEBERÁ retornar HTTP 401 sin revelar cuál campo es incorrecto.
3. MIENTRAS una cuenta o combinación IP/usuario supere el umbral configurado de intentos fallidos, EL sistema DEBERÁ retornar HTTP 429.
4. CUANDO se invoca POST /api/v1/auth/refresh con un refresh token válido, EL Auth_Service DEBERÁ emitir un nuevo access token y rotar el refresh token.
5. SI el refresh token está expirado o invalidado, ENTONCES EL Auth_Service DEBERÁ retornar HTTP 401.
6. CUANDO se invoca POST /api/v1/auth/logout con un access token válido, EL Auth_Service DEBERÁ invalidar el access token y revocar los refresh tokens asociados.
7. EL Auth_Service DEBERÁ registrar en el log de auditoría los eventos relevantes de login, refresh y logout sin incluir contraseñas ni tokens completos.
8. EL Auth_Service DEBERÁ validar el secreto JWT al arranque y rechazar el inicio si el secreto está ausente o no cumple el mínimo de seguridad configurado.

#### Reglas de Negocio

- JWT: firmado con HS512, access token 1h, refresh token 7 días
- El refresh token se rota mediante cookie HttpOnly
- El rate limiting de login se aplica en el flujo actual sin dependencias de OTP

#### Endpoints

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| POST | /api/v1/auth/login | Público | Valida credenciales y retorna JWT + refresh token |
| POST | /api/v1/auth/refresh | Refresh token | Renueva access token |
| POST | /api/v1/auth/logout | JWT | Invalida tokens |

#### Roadmap Futuro (Fases)

- Fase futura: MFA/TOTP si el producto evoluciona hacia requisitos regulatorios o mayor sensibilidad de credenciales

---

### Requerimiento 3: Gestión del Perfil de Usuario

**Historia de Usuario:** Como usuario autenticado, quiero ver y actualizar mi información de perfil y cambiar mi contraseña, para mantener mis datos de cuenta actualizados.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía GET /api/v1/users/me, EL User_Service DEBERÁ retornar los datos del perfil del usuario (id, email, nombre, roles, estado de cuenta, fecha de creación).
2. CUANDO un usuario autenticado envía PUT /api/v1/users/me con campos de perfil válidos, EL User_Service DEBERÁ actualizar el nombre del usuario y otros campos no sensibles y retornar el perfil actualizado.
3. SI una solicitud de actualización de perfil intenta cambiar el email a uno ya utilizado por otra cuenta, ENTONCES EL User_Service DEBERÁ retornar un error HTTP 409.
4. CUANDO un usuario autenticado envía PUT /api/v1/users/me/password con la contraseña actual y una nueva contraseña válida, EL User_Service DEBERÁ actualizar el hash de la contraseña e invalidar todos los refresh tokens existentes para ese usuario.
5. SI la contraseña actual proporcionada en una solicitud de cambio de contraseña no coincide con el hash almacenado, ENTONCES EL User_Service DEBERÁ retornar un error HTTP 400.

---

### Requerimiento 4: Gestión de Roles y Permisos

**Historia de Usuario:** Como Admin, quiero gestionar roles y asignarlos a usuarios, para controlar el acceso a las funcionalidades de la plataforma.

#### Criterios de Aceptación

1. CUANDO un Admin envía GET /api/v1/roles, EL Role_Service DEBERÁ retornar la lista de todos los roles definidos con sus permisos asociados.
2. CUANDO un Admin envía POST /api/v1/roles con un nombre de rol único y conjunto de permisos, EL Role_Service DEBERÁ crear el nuevo rol y retornarlo con HTTP 201.
3. SI una solicitud de creación de rol contiene un nombre de rol que ya existe, ENTONCES EL Role_Service DEBERÁ retornar un error HTTP 409.
4. CUANDO un Admin envía POST /api/v1/users/{userId}/roles/{roleId}, EL Role_Service DEBERÁ asignar el rol especificado al usuario especificado.
5. SI una solicitud de asignación de rol referencia un usuario o rol inexistente, ENTONCES EL Role_Service DEBERÁ retornar un error HTTP 404.
6. EL Sistema DEBERÁ aplicar control de acceso basado en roles en todos los endpoints protegidos, retornando HTTP 403 para solicitudes que carezcan del rol requerido.

---

### Requerimiento 5: Catálogo de Tecnologías de Energía Renovable

**Historia de Usuario:** Como usuario, quiero explorar el catálogo de tecnologías de energía renovable, para seleccionar la tecnología adecuada para mi simulación.

#### Criterios de Aceptación

1. CUANDO cualquier usuario autenticado envía GET /api/v1/technologies, EL Technology_Service DEBERÁ retornar la lista paginada de todas las tecnologías activas con sus parámetros técnicos.
2. CUANDO cualquier usuario autenticado envía GET /api/v1/technologies/{id}, EL Technology_Service DEBERÁ retornar el detalle completo de la tecnología solicitada.
3. SI una solicitud de detalle de tecnología referencia un ID inexistente, ENTONCES EL Technology_Service DEBERÁ retornar un error HTTP 404.
4. CUANDO un Admin envía POST /api/v1/technologies con datos de tecnología válidos, EL Technology_Service DEBERÁ crear la entrada de tecnología y retornarla con HTTP 201.
5. CUANDO un Admin envía PUT /api/v1/technologies/{id} con parámetros actualizados, EL Technology_Service DEBERÁ actualizar la tecnología y retornar el registro actualizado.
6. EL Technology_Service DEBERÁ cachear la lista de tecnologías con un TTL de 10 minutos usando Caffeine e invalidar el caché en cualquier operación de creación o actualización.

---

### Requerimiento 6: Creación de Simulación Personalizada

**Historia de Usuario:** Como usuario, quiero crear una simulación personalizada ingresando mi ubicación, tamaño del proyecto, presupuesto y consumo energético, para obtener proyecciones precisas de producción energética y financieras.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía POST /api/v1/simulations con parámetros de entrada válidos, EL Simulation_Service DEBERÁ crear un nuevo agregado Simulación en estado DRAFT y retornar el ID de simulación con HTTP 201.
2. EL Simulation_Engine DEBERÁ calcular la energía generada anual usando la fórmula: energíaGenerada (kWh/año) = capacidad (kW) × eficiencia × horasPorAño × factorClimático.
3. EL Simulation_Engine DEBERÁ calcular el ROI usando la fórmula: ROI (%) = [(ingresoTotal − inversiónInicial) / inversiónInicial] × 100.
4. EL Simulation_Engine DEBERÁ calcular el período de recuperación usando la fórmula: Payback (años) = inversiónInicial / ahorrosAnuales.
5. EL Simulation_Engine DEBERÁ calcular la reducción anual de CO₂ usando la fórmula: ReducciónCO₂ (ton/año) = energíaGenerada × factorEmisión / 1000, donde el factor de emisión por defecto es 0,5 kg CO₂/kWh.
6. EL Simulation_Engine DEBERÁ calcular el VAN usando la fórmula: VAN = Σ [flujoNeto_t / (1 + tasaDescuento)^t] − inversiónInicial durante la vida útil del proyecto.
7. EL Simulation_Engine DEBERÁ calcular la TIR resolviendo iterativamente: 0 = Σ [flujoNeto_t / (1 + TIR)^t] − inversiónInicial.
8. SI una solicitud de creación de simulación especifica una capacidad fuera del rango [1 kW, 10.000 kW], ENTONCES EL Simulation_Service DEBERÁ retornar un error HTTP 400 con un mensaje de validación descriptivo.
9. SI una solicitud especifica una inversión inicial ≤ 0, ENTONCES EL Simulation_Service DEBERÁ retornar un error HTTP 400.
10. SI una solicitud especifica latitud fuera de [−90, 90] o longitud fuera de [−180, 180], ENTONCES EL Simulation_Service DEBERÁ retornar un error HTTP 400.
11. EL Simulation_Service DEBERÁ completar los cálculos de simulación en menos de 3 segundos para el 95% de las solicitudes bajo carga normal.

---

### Requerimiento 7: Gestión del Ciclo de Vida de Simulaciones

**Historia de Usuario:** Como usuario, quiero gestionar mis simulaciones a través de su ciclo de vida (borrador, completada, archivada), para organizar y hacer seguimiento de mis proyectos energéticos.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía GET /api/v1/simulations/{id} para una simulación que le pertenece, EL Simulation_Service DEBERÁ retornar el detalle completo de la simulación incluyendo todos los resultados calculados.
2. SI una solicitud de consulta referencia una simulación que no pertenece al usuario solicitante, ENTONCES EL Simulation_Service DEBERÁ retornar un error HTTP 403.
3. CUANDO un usuario autenticado envía GET /api/v1/simulations/my-simulations, EL Simulation_Service DEBERÁ retornar la lista paginada de todas las simulaciones no archivadas pertenecientes a ese usuario.
4. CUANDO un usuario autenticado envía PUT /api/v1/simulations/{id} para una simulación en estado DRAFT, EL Simulation_Service DEBERÁ actualizar los parámetros, disparar el recálculo y retornar la simulación actualizada.
5. SI una solicitud de actualización apunta a una simulación en estado DELETED, ENTONCES EL Simulation_Service DEBERÁ retornar un error HTTP 409 indicando que la simulación no puede modificarse en su estado actual.
6. CUANDO un usuario autenticado envía DELETE /api/v1/simulations/{id}, EL Simulation_Service DEBERÁ realizar un soft delete transicionando la simulación a estado DELETED sin eliminar físicamente el registro.
7. EL Simulation_Service DEBERÁ aplicar la máquina de estados actual del baseline: DRAFT → COMPLETED → DELETED, rechazando cualquier transición inválida con un error HTTP 409.
8. CUANDO un usuario autenticado envía POST /api/v1/simulations/{id}/clone, EL Simulation_Service DEBERÁ crear una nueva Simulación en estado DRAFT con los mismos parámetros que la simulación origen y retornar el nuevo ID.

---

### Requerimiento 8: Simulación desde Escenario Predefinido

**Historia de Usuario:** Como usuario sin conocimientos técnicos avanzados, quiero iniciar una simulación desde un escenario predefinido, para evaluar rápidamente casos de uso comunes sin ingresar todos los parámetros manualmente.

#### Criterios de Aceptación

1. CUANDO cualquier usuario autenticado envía GET /api/v1/scenarios, EL Scenario_Service DEBERÁ retornar la lista de escenarios predefinidos disponibles con sus descripciones y parámetros pre-rellenados.
2. CUANDO cualquier usuario autenticado envía GET /api/v1/scenarios/{id}, EL Scenario_Service DEBERÁ retornar el detalle completo del escenario solicitado.
3. SI una solicitud de detalle de escenario referencia un ID inexistente, ENTONCES EL Scenario_Service DEBERÁ retornar un error HTTP 404.
4. CUANDO un usuario autenticado envía POST /api/v1/simulations/from-scenario con un `scenarioId` válido en el cuerpo de la solicitud, EL Simulation_Service DEBERÁ crear una nueva Simulación pre-poblada con los parámetros del escenario y retornar el resultado con HTTP 201.
5. CUANDO un Admin envía POST /api/v1/scenarios con datos de escenario válidos, EL Scenario_Service DEBERÁ crear el escenario y retornarlo con HTTP 201.

---

### Requerimiento 9: Generación de Reporte de Simulación

**Historia de Usuario:** Como usuario que completó una simulación, quiero generar un reporte detallado en PDF, para presentarlo a inversores o tomar decisiones de negocio.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía GET /api/v1/simulations/{id}/report para una simulación en estado COMPLETED, EL Simulation_Service DEBERÁ retornar un reporte estructurado con todos los parámetros de entrada, resultados calculados (energía, ROI, payback, VAN, TIR, CO₂) y desglose mensual de producción energética.
2. SI una solicitud de reporte apunta a una simulación en estado DRAFT o DELETED, ENTONCES EL Simulation_Service DEBERÁ retornar un error HTTP 409 indicando que el reporte solo está disponible para simulaciones COMPLETED.
3. CUANDO se solicita exportación PDF mediante el header Accept: application/pdf, EL Simulation_Service DEBERÁ retornar un reporte PDF con formato profesional.
4. EL Simulation_Service DEBERÁ generar el reporte PDF en menos de 5 segundos para el 95% de las solicitudes.

---

### Requerimiento 10: Comparación de Simulaciones

**Historia de Usuario:** Como usuario evaluando múltiples opciones, quiero comparar diferentes simulaciones lado a lado, para identificar la mejor alternativa de inversión.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía POST /api/v1/simulations/compare con una lista de 2 a 5 IDs de simulación, EL Simulation_Service DEBERÁ retornar un objeto de comparación estructurado con las métricas clave (producción energética, ROI, payback, VAN, TIR, CO₂) para cada simulación en paralelo.
2. SI una solicitud de comparación incluye un ID de simulación que no pertenece al usuario solicitante, ENTONCES EL Simulation_Service DEBERÁ retornar un error HTTP 403.
3. SI una solicitud de comparación incluye menos de 2 o más de 5 IDs, ENTONCES EL Simulation_Service DEBERÁ retornar un error HTTP 400.

---

### Requerimiento 11: Compartir Simulación

**Historia de Usuario:** Como usuario propietario de una simulación, quiero compartirla con otros mediante un enlace público, para colaborar con colegas o presentar resultados a clientes.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía POST /api/v1/simulations/{id}/share, EL Simulation_Service DEBERÁ generar un token de compartición único con tiempo limitado y retornar una URL compartible.
2. CUANDO se realiza una solicitud a la URL compartible con un token válido, EL Sistema DEBERÁ retornar los resultados de la simulación en modo solo lectura sin requerir autenticación.
3. SI un token de compartición está expirado o es inválido, ENTONCES EL Sistema DEBERÁ retornar un error HTTP 404.
4. EL Simulation_Service DEBERÁ establecer la expiración por defecto del token de compartición en 30 días.

---

### Requerimiento 12: Dashboard Interactivo

**Historia de Usuario:** Como usuario, quiero ver un dashboard interactivo que resuma mis simulaciones y métricas clave, para monitorear mis proyectos energéticos de un vistazo.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía GET /api/v1/simulations/dashboard, EL Simulation_Service DEBERÁ retornar estadísticas agregadas incluyendo total de simulaciones, producción energética anual estimada total, reducción de CO₂ total estimada y ahorros anuales totales estimados.
2. CUANDO un usuario autenticado envía GET /api/v1/simulations/{id}/energy-chart, EL Sistema DEBERÁ retornar datos de producción energética mensual de la simulación formateados para renderizado de gráficos.
3. CUANDO un usuario autenticado envía GET /api/v1/simulations/map-data, EL Sistema DEBERÁ retornar las coordenadas geográficas y datos de resumen de todas las simulaciones no archivadas del usuario para renderizado de mapa.
4. EL Frontend DEBERÁ renderizar el dashboard con estados de carga para cada sección de datos y DEBERÁ mostrar estados vacíos significativos cuando no existan simulaciones.

---

### Requerimiento 13: Sugerencias de Configuración con IA (Roadmap de producto)

**Historia de Usuario:** Como usuario sin conocimiento técnico profundo, quiero que la plataforma sugiera configuraciones óptimas de simulación basadas en mi ubicación y perfil, para tomar decisiones mejor informadas.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía POST /api/v1/ai/suggest-configuration con datos de ubicación y consumo, EL AI_Service DEBERÁ retornar una lista ordenada de al menos 3 configuraciones de tecnología recomendadas con justificaciones.
2. EL AI_Service DEBERÁ basar las sugerencias en datos geográficos, patrones climáticos históricos y el consumo energético y presupuesto declarados por el usuario.
3. SI el proveedor de IA no está disponible, ENTONCES EL AI_Service DEBERÁ retornar una respuesta de degradación elegante con HTTP 503 y un mensaje indicando que el servicio no está disponible temporalmente, sin propagar el error como excepción no controlada.

---

### Requerimiento 14: Análisis Predictivo de Rendimiento con IA (Roadmap de producto)

**Historia de Usuario:** Como usuario, quiero que la plataforma prediga el rendimiento futuro de mi simulación en un horizonte temporal configurable, para planificar inversiones energéticas a largo plazo.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía POST /api/v1/ai/predict-performance/{simulationId} con un horizonte temporal en años, EL AI_Service DEBERÁ retornar proyecciones año a año de producción energética, ahorros acumulados y reducción acumulada de CO₂.
2. EL AI_Service DEBERÁ aplicar factores de degradación apropiados al tipo de energía de la simulación (ej: 0,5% de degradación anual de paneles para SOLAR).
3. SI la simulación referenciada no existe o no pertenece al usuario solicitante, ENTONCES EL AI_Service DEBERÁ retornar un error HTTP 404 o HTTP 403 respectivamente.

---

### Requerimiento 15: Asistente Conversacional con IA (Roadmap de producto)

**Historia de Usuario:** Como usuario sin experiencia técnica, quiero interactuar con un asistente de IA sobre temas de energía renovable y mis simulaciones, para recibir orientación guiada sin salir de la plataforma.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía POST /api/v1/ai/chat con un mensaje, EL AI_Service DEBERÁ retornar una respuesta contextualmente relevante en menos de 10 segundos.
2. EL AI_Service DEBERÁ mantener el contexto de la conversación a través de múltiples turnos dentro de una sesión usando un identificador de sesión.
3. SI un mensaje de chat contiene contenido que viola las políticas de uso, ENTONCES EL AI_Service DEBERÁ retornar una respuesta de rechazo segura sin exponer los prompts internos del sistema.
4. EL AI_Service DEBERÁ ser capaz de responder preguntas sobre las simulaciones propias del usuario cuando se proporcione el ID de simulación en el contexto de la solicitud.

---

### Requerimiento 16: Generación Automática de Reportes con IA (Roadmap de producto)

**Historia de Usuario:** Como usuario que completó una simulación, quiero que la IA genere un resumen narrativo de mis resultados, para incluir análisis escrito profesional en mis reportes.

#### Criterios de Aceptación

1. CUANDO un usuario autenticado envía POST /api/v1/ai/generate-report/{simulationId}, EL AI_Service DEBERÁ retornar un reporte narrativo estructurado que resuma la producción energética, viabilidad financiera e impacto ambiental de la simulación en el idioma preferido del usuario.
2. EL AI_Service DEBERÁ incluir valores numéricos específicos de los resultados de la simulación en la narrativa generada.
3. SI la simulación está en estado DRAFT, ENTONCES EL AI_Service DEBERÁ retornar un error HTTP 409 indicando que la simulación debe completarse antes de generar el reporte.

---

### Requerimiento 17: No Funcional — Rendimiento

**Historia de Usuario:** Como usuario, quiero que la plataforma responda rápidamente bajo carga normal, para que mi flujo de trabajo no se vea interrumpido por respuestas lentas.

#### Criterios de Aceptación

1. EL Sistema DEBERÁ responder a solicitudes API estándar en menos de 200ms en el percentil 95 bajo una carga de 100 solicitudes concurrentes por segundo.
2. EL Simulation_Engine DEBERÁ completar todos los cálculos de una simulación en menos de 3 segundos en el percentil 95.
3. EL Frontend DEBERÁ alcanzar un Largest Contentful Paint (LCP) de 3 segundos o menos en una conexión de banda ancha estándar.

---

### Requerimiento 18: No Funcional — Seguridad

**Historia de Usuario:** Como operador de la plataforma, quiero que todos los endpoints estén asegurados y los datos sensibles protegidos, para mantener la integridad del sistema y los datos de los usuarios.

#### Criterios de Aceptación

1. EL Sistema DEBERÁ requerir un JWT válido en todos los endpoints excepto los endpoints públicos de registro/login/verificación/refresh definidos por el baseline actual y cualquier URL pública de compartición o health que se exponga explícitamente.
2. EL Auth_Service DEBERÁ firmar los JWTs usando el algoritmo HS512 con un secreto de al menos 32 caracteres.
3. EL Sistema DEBERÁ aplicar rate limiting de 100 solicitudes por minuto por dirección IP en todos los endpoints API y DEBERÁ retornar HTTP 429 cuando se supere el límite.
4. EL Sistema NUNCA DEBERÁ incluir PII, contraseñas ni valores completos de JWT en los logs de aplicación.
5. EL Sistema DEBERÁ aplicar headers de seguridad (HSTS, X-Content-Type-Options, X-Frame-Options, CSP) en todas las respuestas HTTP.

---

### Requerimiento 19: No Funcional — Observabilidad

**Historia de Usuario:** Como operador de la plataforma, quiero logs estructurados, métricas y health checks, para monitorear y diagnosticar el sistema en producción.

#### Criterios de Aceptación

1. EL Sistema DEBERÁ emitir logs JSON estructurados para todas las solicitudes, incluyendo correlation ID, método HTTP, ruta, código de estado y tiempo de respuesta, sin incluir PII ni secretos.
2. EL Sistema DEBERÁ exponer un endpoint de health check en GET /actuator/health retornando el estado de la aplicación, base de datos y caché.
3. EL Sistema DEBERÁ exponer métricas Micrometer incluyendo conteo de solicitudes, percentiles de tiempo de respuesta, tasa de creación de simulaciones y conteo de usuarios activos.
4. EL Sistema DEBERÁ incluir un correlation ID en cada entrada de log y propagarlo a través de todas las llamadas de servicio dentro de una misma solicitud.

---

### Requerimiento 20: No Funcional — Escalabilidad y Disponibilidad

**Historia de Usuario:** Como operador de la plataforma, quiero que el sistema sea stateless y escalable horizontalmente, para manejar la demanda creciente de usuarios.

#### Criterios de Aceptación

1. EL Sistema DEBERÁ mantener cero estado de sesión en el servidor, apoyándose exclusivamente en JWT para el contexto de autenticación, habilitando escalado horizontal sin afinidad de sesión.
2. EL Sistema DEBERÁ configurar el pool de conexiones de base de datos con un mínimo de 5 y un máximo de 20 conexiones.
3. EL Sistema DEBERÁ implementar lógica de reintento con backoff exponencial para todas las llamadas a servicios externos (proveedor de IA, servicio de email).
4. CUANDO una dependencia externa no crítica no esté disponible, EL Sistema DEBERÁ degradar elegantemente retornando una respuesta parcial o un error descriptivo en lugar de fallar toda la solicitud.
5. EL Sistema DEBERÁ apuntar a un uptime del 99,5% medido en una ventana móvil de 30 días.

---

### Requerimiento 21: No Funcional — Mantenibilidad y Calidad

**Historia de Usuario:** Como desarrollador, quiero que el código base siga los principios de Clean Architecture con alta cobertura de tests, para que el sistema sea fácil de evolucionar y mantener.

#### Criterios de Aceptación

1. EL Sistema DEBERÁ mantener un mínimo del 70% de cobertura de líneas en todos los bounded contexts medido por JaCoCo, con tests significativos que validen comportamiento de negocio.
2. LA Capa de Dominio NO DEBERÁ contener anotaciones de Spring Framework ni dependencias de infraestructura.
3. EL Sistema DEBERÁ documentar todas las decisiones arquitectónicas clave en archivos ADR almacenados en el repositorio.
4. EL Sistema DEBERÁ usar el formato Conventional Commits para todos los mensajes de commit.
5. EL Sistema DEBERÁ proveer documentación OpenAPI completa para todos los endpoints incluyendo esquemas de request/response, códigos de error y requisitos de seguridad.
6. EL Simulation_Engine DEBERÁ estar extraído a la Capa de Dominio, libre de anotaciones Spring, con todos los cálculos financieros y energéticos encapsulados en servicios de dominio (SimulationEngine, ROICalculator).

---

### Requerimiento 22: No Funcional — Internacionalización y Accesibilidad

**Historia de Usuario:** Como usuario de cualquier región, quiero que la plataforma soporte múltiples idiomas y sea accesible para usuarios con discapacidades, para que sea inclusiva y usable globalmente.

#### Criterios de Aceptación

1. EL Frontend DEBERÁ soportar español (es) e inglés (en) con la capacidad de cambiar idioma en tiempo de ejecución sin recargar la página.
2. EL Frontend DEBERÁ formatear fechas, números y valores de moneda según el locale activo.
3. EL Frontend DEBERÁ cumplir las pautas WCAG 2.1 Nivel AA para todos los componentes interactivos, incluyendo navegación por teclado, contraste de color suficiente y etiquetas ARIA en controles de formulario.
4. EL Frontend DEBERÁ ser responsivo y usable en móvil (≥320px), tablet (≥768px) y escritorio (≥1280px).

---

### Requerimiento 23: Motor de Simulación — Propiedades de Invariantes y Round-Trip

**Historia de Usuario:** Como desarrollador, quiero que los cálculos del motor de simulación sean verificables mediante tests basados en propiedades, para garantizar la corrección en todos los rangos de entrada válidos.

#### Criterios de Aceptación

1. PARA TODAS las entradas de Simulación válidas, EL Simulation_Engine DEBERÁ producir un valor de energíaGenerada estrictamente mayor que 0 kWh/año.
2. PARA TODAS las entradas de Simulación válidas donde ahorrosAnuales > 0, EL Simulation_Engine DEBERÁ producir un período de payback estrictamente mayor que 0 años.
3. PARA TODAS las entradas de Simulación válidas, EL Simulation_Engine DEBERÁ producir un valor de reducción de CO₂ estrictamente mayor que 0 ton/año.
4. PARA TODAS las entradas de Simulación válidas, incrementar la capacidad manteniendo constantes todos los demás parámetros DEBERÁ resultar en un valor de energíaGenerada proporcionalmente mayor (invariante de monotonicidad).
5. PARA TODAS las entradas de Simulación válidas, EL Simulation_Engine DEBERÁ producir un VAN positivo para proyectos donde ahorrosAnuales > inversiónInicial / añosVidaÚtilProyecto.
6. PARA TODAS las entradas de Simulación válidas, EL Simulation_Engine DEBERÁ producir un valor de TIR tal que al sustituirlo como tasa de descuento en la fórmula del VAN se obtenga un resultado dentro de ±0,01 de cero (propiedad round-trip).

---

### Requerimiento 24: Serialización de Datos y Contrato de API

**Historia de Usuario:** Como desarrollador frontend, quiero que la API serialice y deserialice los datos de simulación de forma consistente, para poder confiar en un contrato estable entre frontend y backend.

#### Criterios de Aceptación

1. EL Sistema DEBERÁ serializar todas las respuestas API como JSON codificado en UTF-8 con nomenclatura de campos consistente (camelCase).
2. PARA TODAS las entradas SimulationRequestDTO válidas, serializar a JSON y deserializar de vuelta DEBERÁ producir un objeto equivalente sin pérdida de datos (propiedad round-trip).
3. EL Sistema DEBERÁ retornar timestamps en formato ISO 8601 en todos los campos de fecha/hora.
4. SI un cuerpo de solicitud contiene campos desconocidos, EL Sistema DEBERÁ ignorarlos sin retornar un error (compatibilidad hacia adelante).
5. EL Sistema DEBERÁ retornar un envelope de respuesta de error consistente para todos los casos de error que contenga: timestamp, status, errorCode, message y path.

---

## Anexo A — Requerimientos de Frontend (RF-FE)

### RF-FE-001: Configuración del Proyecto Frontend

**Historia de Usuario:** Como desarrollador, quiero un proyecto React + TypeScript correctamente configurado con todas las herramientas del stack, para tener una base sólida y consistente desde el inicio.

#### Criterios de Aceptación

1. EL Frontend DEBERÁ estar configurado con React 18+ y TypeScript en modo estricto (`strict: true` en tsconfig).
2. EL Frontend DEBERÁ usar Vite como bundler con soporte para path aliases (`@/` apuntando a `src/`).
3. EL Frontend DEBERÁ incluir ESLint + Prettier configurados con reglas para React y TypeScript.
4. EL Frontend DEBERÁ usar Tailwind CSS con shadcn/ui como sistema de componentes base.
5. EL Frontend DEBERÁ tener TanStack Query (React Query) configurado como cliente de estado del servidor con un `QueryClient` global.
6. EL Frontend DEBERÁ tener Zustand configurado para estado global del cliente (auth, preferencias de usuario).
7. EL Frontend DEBERÁ tener React Hook Form + Zod configurados para validación de formularios con inferencia de tipos TypeScript.
8. EL Frontend DEBERÁ tener un cliente HTTP centralizado (Axios o fetch wrapper) con interceptores para inyección automática del JWT y manejo de refresh token.

---

### RF-FE-002: Sistema de Autenticación Frontend

**Historia de Usuario:** Como usuario, quiero un flujo de autenticación claro en el frontend, para iniciar sesión de forma segura con feedback directo sobre credenciales, verificación de cuenta y refresh de sesión.

#### Criterios de Aceptación

1. EL Frontend DEBERÁ implementar una pantalla de login basada en email y password contra `POST /api/v1/auth/login`.
2. EL Frontend DEBERÁ almacenar el access token en memoria (no en localStorage) y el refresh token en una cookie HttpOnly mediante el backend.
3. EL Frontend DEBERÁ implementar un interceptor que detecte respuestas HTTP 401 y ejecute automáticamente el flujo de refresh token antes de reintentar la solicitud original.
4. EL Frontend DEBERÁ redirigir al usuario a `/login` al detectar que el refresh token ha expirado o ha sido invalidado, limpiando el estado de autenticación.
5. EL Frontend DEBERÁ implementar rutas protegidas (`ProtectedRoute`) que redirijan a `/login` si el usuario no está autenticado.
6. EL Frontend DEBERÁ mostrar mensajes de error claros para credenciales inválidas, cuenta no verificada/deshabilitada y sesión expirada.

---

### RF-FE-003: Gestión de Simulaciones — Formulario de Creación

**Historia de Usuario:** Como usuario, quiero un formulario de creación de simulación intuitivo con validación en tiempo real, para ingresar mis datos correctamente sin errores.

#### Criterios de Aceptación

1. EL Frontend DEBERÁ implementar el formulario de creación de simulación con validación Zod que refleje exactamente las reglas de negocio del backend (capacidad 1-10.000 kW, inversión > 0, coordenadas válidas).
2. EL Frontend DEBERÁ mostrar un selector de tecnología con tarjetas visuales que incluyan el tipo de energía, eficiencia y costo base de cada tecnología del catálogo.
3. EL Frontend DEBERÁ incluir un selector de ubicación mediante mapa interactivo (Leaflet) que auto-complete los campos de latitud y longitud al hacer clic en el mapa.
4. EL Frontend DEBERÁ mostrar errores de validación inline bajo cada campo afectado, en tiempo real al perder el foco (`onBlur`).
5. EL Frontend DEBERÁ mostrar un estado de carga durante el cálculo de la simulación con un indicador de progreso y el mensaje "Calculando simulación...".
6. EL Frontend DEBERÁ redirigir automáticamente a la página de detalle de la simulación tras una creación exitosa.
7. EL Frontend DEBERÁ implementar la opción "Crear desde escenario" que pre-rellene el formulario con los parámetros del escenario seleccionado, permitiendo ajustes antes de enviar.

---

### RF-FE-004: Dashboard Principal

**Historia de Usuario:** Como usuario, quiero un dashboard que muestre mis métricas agregadas y simulaciones recientes de forma visual, para tener una visión general de mis proyectos energéticos.

#### Criterios de Aceptación

1. EL Frontend DEBERÁ renderizar tarjetas de métricas agregadas (total simulaciones, energía total estimada en kWh/año, CO₂ total evitado en ton/año, ahorro total estimado en USD/año) con skeleton loaders durante la carga.
2. EL Frontend DEBERÁ mostrar un gráfico de barras (Recharts) con las últimas 5 simulaciones comparando su producción energética anual.
3. EL Frontend DEBERÁ mostrar un mapa interactivo (Leaflet) con marcadores en las ubicaciones de todas las simulaciones del usuario, con popups que muestren nombre, tecnología y ROI.
4. EL Frontend DEBERÁ mostrar una tabla de simulaciones recientes con columnas: nombre, tecnología, estado (badge de color), ROI, fecha de creación y acciones (ver, clonar, archivar).
5. EL Frontend DEBERÁ mostrar un estado vacío con ilustración y CTA "Crear tu primera simulación" cuando el usuario no tenga simulaciones.
6. EL Frontend DEBERÁ actualizar los datos del dashboard automáticamente al volver a la pestaña (window focus refetch con TanStack Query).

---

### RF-FE-005: Visualización de Resultados de Simulación

**Historia de Usuario:** Como usuario, quiero ver los resultados de mi simulación en gráficos interactivos y métricas claras, para entender fácilmente la viabilidad del proyecto.

#### Criterios de Aceptación

1. EL Frontend DEBERÁ mostrar las métricas principales en tarjetas destacadas: energía generada anual (kWh), ROI (%), payback (años), VAN (USD), TIR (%) y CO₂ evitado (ton/año), con iconos representativos y formato de número localizado.
2. EL Frontend DEBERÁ renderizar un gráfico de área (Recharts) con la producción energética mensual estimada a lo largo de 12 meses.
3. EL Frontend DEBERÁ renderizar un gráfico de líneas con la proyección de flujo de caja acumulado a lo largo de la vida útil del proyecto, marcando el punto de payback.
4. EL Frontend DEBERÁ mostrar una sección de análisis ambiental con la equivalencia de CO₂ evitado en términos comprensibles (ej: "equivalente a plantar X árboles").
5. EL Frontend DEBERÁ permitir exportar el reporte como PDF mediante un botón que llame al endpoint `/report` con el header `Accept: application/pdf`.
6. EL Frontend DEBERÁ mostrar el estado de la simulación (DRAFT / COMPLETED / DELETED) con un badge de color y las acciones disponibles según el estado.

---

### RF-FE-006: Comparación de Simulaciones

**Historia de Usuario:** Como usuario, quiero seleccionar múltiples simulaciones y compararlas en una tabla lado a lado, para identificar la mejor opción de inversión.

#### Criterios de Aceptación

1. EL Frontend DEBERÁ permitir seleccionar entre 2 y 5 simulaciones mediante checkboxes en la lista de simulaciones, con un botón "Comparar seleccionadas" que se habilite al seleccionar al menos 2.
2. EL Frontend DEBERÁ renderizar una tabla de comparación con filas de métricas (energía, ROI, payback, VAN, TIR, CO₂, inversión inicial, tecnología) y columnas por simulación.
3. EL Frontend DEBERÁ resaltar visualmente el mejor valor de cada métrica (ej: mayor ROI, menor payback) con un indicador de color verde.
4. EL Frontend DEBERÁ incluir un gráfico de radar (Recharts) que visualice las 5 métricas principales normalizadas para todas las simulaciones comparadas.
5. EL Frontend DEBERÁ permitir exportar la comparación como PDF o imagen PNG.

---

### RF-FE-007: Asistente de IA Conversacional

**Historia de Usuario:** Como usuario, quiero un chat integrado en la plataforma que me guíe en la creación de simulaciones y responda mis preguntas sobre energía renovable, para obtener ayuda contextual sin salir de la aplicación.

#### Criterios de Aceptación

1. EL Frontend DEBERÁ implementar un widget de chat flotante accesible desde cualquier página mediante un botón fijo en la esquina inferior derecha.
2. EL Frontend DEBERÁ renderizar los mensajes del chat con soporte para Markdown (negrita, listas, código) usando una librería como `react-markdown`.
3. EL Frontend DEBERÁ mostrar un indicador de escritura animado ("...") mientras espera la respuesta del AI_Service.
4. EL Frontend DEBERÁ mantener el historial de la conversación en el estado local de la sesión (Zustand), limpiándolo al cerrar sesión.
5. EL Frontend DEBERÁ incluir sugerencias de preguntas rápidas predefinidas al abrir el chat por primera vez (ej: "¿Qué tecnología es mejor para mi zona?", "¿Cómo interpreto el VAN?").
6. EL Frontend DEBERÁ manejar errores del AI_Service mostrando un mensaje amigable ("El asistente no está disponible en este momento") sin romper la UI.

---

## Anexo B — Datos Seed: Escenarios Predefinidos

Los siguientes 3 escenarios deben insertarse en la tabla `scenarios` mediante una migración Flyway al inicializar el sistema.

```sql
-- Migración: V5__seed_scenarios.sql

INSERT INTO scenarios (
    name, description, technology_id,
    default_capacity_kw, default_investment, default_tariff,
    default_consumption, climate_profile, is_active
) VALUES

-- Escenario 1: Hogar solar en clima soleado
(
    'Hogar con paneles solares — clima soleado',
    'Instalación residencial de paneles fotovoltaicos para un hogar de 4 personas en zona de alta irradiación solar (sur de España, norte de África, Latinoamérica tropical). Ideal para evaluar autoconsumo y reducción de factura eléctrica.',
    (SELECT id FROM technologies WHERE energy_type = 'SOLAR' LIMIT 1),
    5.00,        -- 5 kW de capacidad instalada
    7500.00,     -- USD inversión inicial (paneles + inversor + instalación)
    0.1500,      -- USD/kWh tarifa eléctrica media
    6000.00,     -- kWh/año consumo anual hogar medio
    '{
        "avg_solar_irradiation": 5.5,
        "avg_wind_speed": 3.2,
        "avg_temperature": 22.0,
        "climate_zone": "Mediterranean",
        "peak_sun_hours": 5.5
    }',
    TRUE
),

-- Escenario 2: Pequeña empresa con turbinas eólicas
(
    'Pequeña empresa — turbinas eólicas en zona costera',
    'Instalación de micro-turbinas eólicas para una pequeña empresa o nave industrial ubicada en zona costera o de montaña con vientos constantes superiores a 6 m/s. Orientado a reducir costes energéticos industriales.',
    (SELECT id FROM technologies WHERE energy_type = 'WIND' LIMIT 1),
    50.00,       -- 50 kW de capacidad instalada
    75000.00,    -- USD inversión inicial
    0.1200,      -- USD/kWh tarifa industrial
    80000.00,    -- kWh/año consumo anual empresa media
    '{
        "avg_solar_irradiation": 3.8,
        "avg_wind_speed": 7.5,
        "avg_temperature": 15.0,
        "climate_zone": "Coastal",
        "wind_class": "IEC_II"
    }',
    TRUE
),

-- Escenario 3: Microcentral hidroeléctrica rural
(
    'Microcentral hidroeléctrica — zona rural con río',
    'Microcentral de paso fluyente para comunidad rural o municipio pequeño con acceso a caudal de río constante. Genera energía base 24/7 con mínima variabilidad estacional. Ideal para zonas sin acceso a red eléctrica.',
    (SELECT id FROM technologies WHERE energy_type = 'HYDRO' LIMIT 1),
    100.00,      -- 100 kW de capacidad instalada
    200000.00,   -- USD inversión inicial (obra civil + turbina + generador)
    0.0800,      -- USD/kWh tarifa rural subsidiada
    150000.00,   -- kWh/año consumo comunidad rural
    '{
        "avg_solar_irradiation": 4.2,
        "avg_wind_speed": 2.5,
        "avg_temperature": 12.0,
        "climate_zone": "Continental",
        "avg_flow_rate_m3s": 0.8,
        "hydraulic_head_m": 15.0
    }',
    TRUE
);
```

---

## Anexo C — Datos Seed: Tecnologías Iniciales

Las siguientes 3 tecnologías deben insertarse en la tabla `technologies` mediante una migración Flyway previa a los escenarios.

```sql
-- Migración: V4__seed_technologies.sql

INSERT INTO technologies (
    name, energy_type, efficiency,
    base_cost_per_kw, lifespan_years,
    maintenance_cost_annual_percentage, description
) VALUES

-- Tecnología 1: Panel Solar Fotovoltaico
(
    'Panel Solar Fotovoltaico',
    'SOLAR',
    0.1850,      -- 18.5% eficiencia (monocristalino estándar)
    1200.00,     -- USD/kW instalado (panel + inversor + instalación)
    25,          -- 25 años de vida útil
    1.50,        -- 1.5% del coste inicial por año en mantenimiento
    'Tecnología fotovoltaica monocristalina de alta eficiencia para generación de electricidad a partir de radiación solar. Incluye paneles, inversor string, estructura de montaje y monitorización. Factor de degradación anual: 0.5%. Horas equivalentes de sol pico (HSP) variables según latitud y orientación. Óptimo para tejados residenciales, cubiertas industriales y plantas solares en suelo.'
),

-- Tecnología 2: Turbina Eólica Terrestre
(
    'Turbina Eólica Terrestre',
    'WIND',
    0.3500,      -- 35% eficiencia (factor de capacidad medio onshore)
    1500.00,     -- USD/kW instalado
    20,          -- 20 años de vida útil
    2.50,        -- 2.5% del coste inicial por año
    'Aerogenerador de eje horizontal para instalación terrestre (onshore). Potencia nominal variable según modelo (desde 10 kW micro-eólica hasta 3 MW utility-scale). Factor de capacidad típico entre 25-40% dependiendo del régimen de vientos. Velocidad de arranque: 3 m/s, velocidad nominal: 12 m/s, velocidad de corte: 25 m/s. Requiere velocidad media de viento mínima de 5 m/s para viabilidad económica.'
),

-- Tecnología 3: Microcentral Hidroeléctrica
(
    'Microcentral Hidroeléctrica de Paso',
    'HYDRO',
    0.8500,      -- 85% eficiencia (la más alta entre renovables)
    2500.00,     -- USD/kW instalado (mayor coste por obra civil)
    40,          -- 40 años de vida útil (la más duradera)
    1.00,        -- 1.0% del coste inicial por año (bajo mantenimiento)
    'Central hidroeléctrica de pequeña potencia (< 10 MW) de tipo run-of-river (paso fluyente), sin embalse. Aprovecha el caudal natural del río mediante una pequeña derivación. Genera energía base con alta predictibilidad y factor de capacidad del 50-80%. Requiere estudio hidrológico previo, concesión de aguas y evaluación de impacto ambiental. Tecnología más madura y con mayor vida útil entre las renovables.'
);
```

---

## Anexo D — DDL Completo de Todas las Tablas

Script completo de creación de base de datos para MySQL 8. Debe ejecutarse en orden mediante migraciones Flyway versionadas.

```sql
-- ============================================================
-- V1__create_users_and_roles.sql
-- ============================================================

CREATE TABLE users (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(255)    NOT NULL,
    phone           VARCHAR(50)     NULL,
    status          ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'INACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activated_at    TIMESTAMP       NULL,
    INDEX idx_users_email  (email),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roles (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(50)     NOT NULL UNIQUE,
    description VARCHAR(255)    NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id     BIGINT      NOT NULL,
    role_id     BIGINT      NOT NULL,
    assigned_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- V2__create_auth_tables.sql
-- ============================================================

CREATE TABLE refresh_tokens (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    token_hash  VARCHAR(255)    NOT NULL UNIQUE,
    expires_at  TIMESTAMP       NOT NULL,
    revoked     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_token_hash    (token_hash),
    INDEX idx_refresh_token_user    (user_id),
    INDEX idx_refresh_token_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE token_blacklist (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT,
    jti         VARCHAR(255)    NOT NULL UNIQUE,  -- JWT ID claim
    expires_at  TIMESTAMP       NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_blacklist_jti     (jti),
    INDEX idx_blacklist_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Legacy residual table from the removed OTP flow.
-- It may still exist in historical migrations, but it is not part of the
-- current authentication baseline described in Requerimiento 2.
CREATE TABLE otp_codes (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    code_hash   VARCHAR(255)    NOT NULL,
    expires_at  TIMESTAMP       NOT NULL,
    used        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_otp_codes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_otp_user_expires (user_id, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE activation_tokens (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL UNIQUE,
    token_hash  VARCHAR(255)    NOT NULL UNIQUE,
    expires_at  TIMESTAMP       NOT NULL,
    used        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activation_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_activation_token_hash    (token_hash),
    INDEX idx_activation_token_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- V3__create_technologies_and_scenarios.sql
-- ============================================================

CREATE TABLE technologies (
    id                                  BIGINT          PRIMARY KEY AUTO_INCREMENT,
    name                                VARCHAR(100)    NOT NULL,
    energy_type                         ENUM('SOLAR','WIND','HYDRO','BIOMASS','GEOTHERMAL') NOT NULL,
    efficiency                          DECIMAL(5,4)    NOT NULL CHECK (efficiency > 0 AND efficiency <= 1),
    base_cost_per_kw                    DECIMAL(12,2)   NOT NULL CHECK (base_cost_per_kw > 0),
    lifespan_years                      INT             NOT NULL CHECK (lifespan_years > 0),
    maintenance_cost_annual_percentage  DECIMAL(5,2)    NULL,
    description                         TEXT            NULL,
    is_active                           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at                          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_technologies_energy_type (energy_type),
    INDEX idx_technologies_active      (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE scenarios (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT,
    name                VARCHAR(255)    NOT NULL,
    description         TEXT            NULL,
    technology_id       BIGINT          NOT NULL,
    default_capacity_kw DECIMAL(10,2)   NULL,
    default_investment  DECIMAL(15,2)   NULL,
    default_tariff      DECIMAL(8,4)    NULL,
    default_consumption DECIMAL(15,2)   NULL,
    climate_profile     JSON            NULL,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_scenarios_technology FOREIGN KEY (technology_id) REFERENCES technologies(id),
    INDEX idx_scenarios_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- V4__create_simulations.sql
-- ============================================================

CREATE TABLE simulations (
    id                          BIGINT          PRIMARY KEY AUTO_INCREMENT,
    user_id                     BIGINT          NOT NULL,
    technology_id               BIGINT          NOT NULL,
    name                        VARCHAR(255)    NOT NULL,
    status                      ENUM('DRAFT','COMPLETED','DELETED') NOT NULL DEFAULT 'DRAFT',

    -- Parámetros de entrada
    latitude                    DECIMAL(10,8)   NOT NULL,
    longitude                   DECIMAL(11,8)   NOT NULL,
    capacity_kw                 DECIMAL(10,2)   NOT NULL CHECK (capacity_kw >= 1 AND capacity_kw <= 10000),
    initial_investment          DECIMAL(15,2)   NOT NULL CHECK (initial_investment > 0),
    electricity_tariff          DECIMAL(8,4)    NOT NULL CHECK (electricity_tariff > 0),
    current_consumption_kwh_year DECIMAL(15,2)  NULL,

    -- Datos climáticos
    avg_solar_irradiation       DECIMAL(8,2)    NULL,  -- kWh/m²/día
    avg_wind_speed              DECIMAL(5,2)    NULL,  -- m/s
    avg_temperature             DECIMAL(5,2)    NULL,  -- °C

    -- Resultados calculados
    energy_generated_annual     DECIMAL(15,2)   NULL,  -- kWh/año
    roi_percentage              DECIMAL(8,4)    NULL,
    payback_years               DECIMAL(6,2)    NULL,
    co2_reduction_annual        DECIMAL(12,2)   NULL,  -- toneladas/año
    npv_value                   DECIMAL(15,2)   NULL,  -- VAN en USD
    irr_percentage              DECIMAL(8,4)    NULL,  -- TIR en %

    version                     INT             NOT NULL DEFAULT 1,
    created_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_simulations_user       FOREIGN KEY (user_id)       REFERENCES users(id)        ON DELETE CASCADE,
    CONSTRAINT fk_simulations_technology FOREIGN KEY (technology_id) REFERENCES technologies(id),
    INDEX idx_simulations_user_status  (user_id, status),
    INDEX idx_simulations_technology   (technology_id),
    INDEX idx_simulations_created_at   (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE simulation_share_tokens (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    simulation_id   BIGINT          NOT NULL,
    token           VARCHAR(255)    NOT NULL UNIQUE,
    expires_at      TIMESTAMP       NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_share_tokens_simulation FOREIGN KEY (simulation_id) REFERENCES simulations(id) ON DELETE CASCADE,
    INDEX idx_share_token       (token),
    INDEX idx_share_expires     (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- V5__seed_roles.sql
-- ============================================================

INSERT INTO roles (name, description) VALUES
    ('USER',     'Usuario estándar con acceso a simulaciones propias'),
    ('ADMIN',    'Administrador con acceso completo a la plataforma'),
    ('ANALYST',  'Analista con acceso a reportes y métricas avanzadas');
```

---

## Anexo E — Especificación Técnica de Integración con LLMs (Roadmap de producto)

### E.1 Arquitectura de Integración

El siguiente diseño describe una capacidad de producto planificada. No forma parte del baseline implementado actual del backend, pero documenta una dirección técnica coherente para una futura integración con LLMs.

```
AI_Service
├── domain/
│   ├── model/
│   │   ├── ChatSession.java          -- Agregado: historial de conversación
│   │   └── AIRequest.java            -- Value Object: solicitud al LLM
│   ├── service/
│   │   ├── PromptBuilderService.java -- Construye prompts con contexto
│   │   └── ResponseParserService.java
│   └── repository/
│       └── ChatSessionRepository.java (puerto)
├── application/
│   ├── command/
│   │   ├── SendChatMessageCommand.java
│   │   ├── SuggestConfigurationCommand.java
│   │   ├── PredictPerformanceCommand.java
│   │   └── GenerateReportCommand.java
│   └── service/
│       └── AIApplicationService.java
└── infrastructure/
    ├── llm/
    │   ├── LLMProviderPort.java       -- Puerto de salida (interfaz)
    │   ├── OpenAIAdapter.java         -- Adaptador OpenAI GPT-4o
    │   ├── AnthropicAdapter.java      -- Adaptador Claude 3.5 Sonnet
    │   └── LLMProviderConfig.java     -- Selección de proveedor activo
    └── persistence/
        └── ChatSessionJpaRepository.java
```

### E.2 Configuración de Proveedores

```yaml
# application.yml
ai:
  provider: openai          # openai | anthropic | gemini
  fallback-provider: anthropic
  timeout-seconds: 30
  max-retries: 2

  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o
    max-tokens: 2048
    temperature: 0.3        # Baja temperatura para respuestas consistentes

  anthropic:
    api-key: ${ANTHROPIC_API_KEY}
    model: claude-3-5-sonnet-20241022
    max-tokens: 2048

  prompts:
    system-base: "Eres un experto en energías renovables y análisis de inversiones energéticas. Responde siempre en el idioma del usuario. Sé preciso, usa datos numéricos cuando estén disponibles y estructura tus respuestas de forma clara."
    simulation-context: "El usuario tiene una simulación con los siguientes datos: {simulationData}. Usa estos datos para contextualizar tus respuestas."
```

### E.3 Diseño de Prompts por Funcionalidad

#### E.3.1 Asistente Conversacional (RF-AI-001)

```
SYSTEM PROMPT:
Eres RenewBot, el asistente experto de RenewSim en energías renovables.
Tu objetivo es ayudar a los usuarios a:
1. Entender las tecnologías de energía renovable (solar, eólica, hidroeléctrica)
2. Interpretar los resultados de sus simulaciones (ROI, VAN, TIR, payback, CO₂)
3. Tomar decisiones de inversión informadas
4. Crear simulaciones correctamente

Reglas:
- Responde siempre en el idioma del usuario
- Usa los datos de simulación del contexto cuando estén disponibles
- Si no tienes datos suficientes, pide aclaraciones
- No inventes datos numéricos; usa solo los proporcionados en el contexto
- Limita respuestas a 300 palabras salvo que se pida más detalle

Contexto del usuario: {userContext}
Simulación activa: {simulationContext}

USER MESSAGE: {userMessage}
```

#### E.3.2 Sugerencias de Configuración (RF-AI-002)

```
SYSTEM PROMPT:
Eres un consultor experto en energías renovables. Analiza los datos del usuario
y recomienda las 3 mejores configuraciones de proyecto energético.

Para cada recomendación incluye:
1. Tecnología recomendada y justificación
2. Capacidad sugerida (kW)
3. Inversión estimada (USD)
4. ROI esperado (%)
5. Período de recuperación estimado (años)
6. Razón principal de la recomendación

Datos del usuario:
- Ubicación: latitud {lat}, longitud {lon}
- Consumo anual: {consumption} kWh/año
- Presupuesto disponible: {budget} USD
- Tarifa eléctrica local: {tariff} USD/kWh
- Datos climáticos de la zona: {climateData}

Responde en formato JSON estructurado con el esquema:
{
  "recommendations": [
    {
      "rank": 1,
      "technology": "SOLAR|WIND|HYDRO",
      "capacity_kw": number,
      "estimated_investment": number,
      "expected_roi_percentage": number,
      "estimated_payback_years": number,
      "justification": "string"
    }
  ]
}
```

#### E.3.3 Análisis Predictivo (RF-AI-003)

```
SYSTEM PROMPT:
Genera proyecciones de rendimiento a {years} años para la siguiente instalación
de energía renovable, aplicando factores de degradación realistas.

Datos de la simulación:
{simulationData}

Para cada año calcula:
- Producción energética (kWh) aplicando degradación anual del {degradationRate}%
- Ahorro económico acumulado (USD)
- CO₂ evitado acumulado (toneladas)
- Flujo de caja neto del año
- VAN acumulado a esa fecha

Responde en formato JSON:
{
  "projections": [
    {
      "year": number,
      "energy_kwh": number,
      "annual_savings_usd": number,
      "cumulative_savings_usd": number,
      "co2_avoided_ton": number,
      "cumulative_co2_ton": number,
      "net_cash_flow": number,
      "cumulative_npv": number
    }
  ],
  "summary": {
    "total_energy_kwh": number,
    "total_savings_usd": number,
    "total_co2_ton": number,
    "payback_year": number
  }
}
```

#### E.3.4 Generación de Reporte Narrativo (RF-AI-004)

```
SYSTEM PROMPT:
Genera un informe ejecutivo profesional en {language} para la siguiente
simulación de energía renovable. El informe será presentado a inversores
y tomadores de decisiones.

Estructura del informe:
1. Resumen Ejecutivo (2-3 párrafos)
2. Análisis Técnico (producción energética, factor de capacidad)
3. Análisis Económico (ROI, VAN, TIR, payback — con interpretación)
4. Impacto Ambiental (CO₂ evitado, equivalencias comprensibles)
5. Recomendación Final (viable/no viable con justificación)

Datos de la simulación:
{simulationData}

Tono: profesional, objetivo, basado en datos. Incluye todos los valores
numéricos de la simulación. Máximo 800 palabras.
```

### E.4 Manejo de Errores y Degradación

```java
// Estrategia de fallback entre proveedores
@Service
public class AIApplicationService {

    public AIResponse chat(SendChatMessageCommand cmd) {
        try {
            return primaryProvider.complete(buildPrompt(cmd));
        } catch (LLMProviderException e) {
            log.warn("Primary LLM provider failed, trying fallback", e);
            try {
                return fallbackProvider.complete(buildPrompt(cmd));
            } catch (LLMProviderException fallbackEx) {
                log.error("All LLM providers failed", fallbackEx);
                throw new AIServiceUnavailableException(
                    "El servicio de IA no está disponible temporalmente. " +
                    "Por favor, inténtalo de nuevo en unos minutos."
                );
            }
        }
    }
}
```

### E.5 Tabla de Base de Datos para Sesiones de Chat

```sql
-- V6__create_ai_tables.sql

CREATE TABLE chat_sessions (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    session_id  VARCHAR(36)     NOT NULL UNIQUE,  -- UUID
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_session_id   (session_id),
    INDEX idx_chat_user         (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chat_messages (
    id          BIGINT          PRIMARY KEY AUTO_INCREMENT,
    session_id  BIGINT          NOT NULL,
    role        ENUM('USER','ASSISTANT','SYSTEM') NOT NULL,
    content     TEXT            NOT NULL,
    tokens_used INT             NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    INDEX idx_chat_messages_session (session_id),
    INDEX idx_chat_messages_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### E.6 Variables de Entorno Requeridas

```bash
# .env (NUNCA commitear al repositorio)

# Proveedores LLM
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...
GOOGLE_AI_API_KEY=...

# Proveedor activo
AI_PROVIDER=openai
AI_FALLBACK_PROVIDER=anthropic

# Email (para verificación de cuenta y notificaciones)
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=${SENDGRID_API_KEY}
MAIL_FROM=noreply@renewsim.com

# JWT
JWT_SECRET=<mínimo-32-caracteres-aleatorios>
JWT_EXPIRATION_MS=3600000
JWT_REFRESH_EXPIRATION_MS=604800000
```
