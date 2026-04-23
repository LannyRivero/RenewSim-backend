# ADR-008: Uso de enums del dominio en entidades JPA

## Status: Accepted
## Date: 2026-04-23
## Deciders: Development Team

## Contexto

Durante la auditoría arquitectónica del módulo `auth_service` se identificó que las entidades JPA en `infrastructure/persistence/entity` usan directamente enums definidos en el dominio:
- `OtpCode.Purpose` (LOGIN, PASSWORD_RESET, EMAIL_VERIFICATION)
- Potencialmente otros enums de dominio en el futuro

Esto genera una decisión arquitectónica: ¿deben las entidades JPA usar directamente los enums del dominio, o deben tener sus propios enums y mapear entre ellos?

### Opciones consideradas

**Opción A — Enums separados (purista DDD):**
```java
// domain/model/OtpCode.java
public enum Purpose { LOGIN, PASSWORD_RESET, EMAIL_VERIFICATION }

// infrastructure/persistence/entity/OtpCodeEntity.java
public enum JpaPurpose { LOGIN, PASSWORD_RESET, EMAIL_VERIFICATION }

// Mapeo en adapter
private OtpCode.Purpose toDomain(JpaPurpose jpa) { ... }
```

**Opción B — Compartir enums (pragmática):**
```java
// domain/model/OtpCode.java
public enum Purpose { LOGIN, PASSWORD_RESET, EMAIL_VERIFICATION }

// infrastructure/persistence/entity/OtpCodeEntity.java
@Enumerated(EnumType.STRING)
private OtpCode.Purpose purpose; // Usa directamente el enum del dominio
```

## Decisión

**Adoptamos Opción B** con restricciones:

Permitir que entidades JPA usen enums del dominio **SOLO si se cumplen todas estas condiciones:**

1. El enum NO contiene lógica de negocio
2. El enum es un Value Object inmutable (solo constantes)
3. El mapeo JPA es trivial (`EnumType.STRING` o `EnumType.ORDINAL`)
4. El enum representa un concepto del dominio, no un detalle técnico

## Justificación

### Ventajas de compartir enums:
- Menos código de mapeo (DRY)
- Consistencia garantizada entre dominio y persistencia
- Refactors más simples (renombrar un valor se propaga automáticamente)
- Claridad semántica (el mismo concepto usa el mismo tipo)

### Desventajas de compartir enums:
- Acoplamiento técnico: el dominio "sabe" que será persistido con JPA
- Menor flexibilidad: cambiar la representación en BD requiere tocar el dominio
- Posible contaminación si el enum crece y añade lógica

### Por qué aceptamos el trade-off:
Los enums en `auth_service` (`OtpCode.Purpose`, etc.) son **catálogos de valores** sin lógica. Son inmutables, estables y representan conceptos del dominio puro.

El costo de duplicarlos (enums JPA + mapeo bidireccional) no aporta valor arquitectónico real en este caso.

## Consecuencias

### Positivas:
- ✅ Código más conciso y mantenible
- ✅ Un solo punto de verdad para los valores del enum
- ✅ Tests más simples (menos mocks de mappers)

### Negativas:
- ⚠️ El dominio tiene una dependencia conceptual con persistencia (aunque no técnica)
- ⚠️ Si un enum crece en complejidad, deberá refactorizarse a Opción A

### Reglas de aplicación:
- ✅ Permitido: Enums simples como `Purpose`, `UserStatus`, `SimulationState`
- ❌ Prohibido: Enums con métodos de negocio, cálculos o dependencias

### Deuda técnica:
- **Marcado como P2** para revisión futura
- Si algún enum compartido añade lógica de negocio → refactorizar a enums separados
- Revisar en cada code review si nuevos enums cumplen las condiciones

## Monitoreo

En code reviews, validar que los enums compartidos:
- No tengan métodos más allá de getters triviales
- No dependan de otros objetos del dominio
- Sean inmutables y sin estado

Si se detecta un enum que viola estas reglas → crear issue P1 para separarlo.

## Referencias
- [Implementing Domain-Driven Design, Vaughn Vernon]
- [Clean Architecture, Robert C. Martin]
- Discusión en PR #xxx (si aplica)