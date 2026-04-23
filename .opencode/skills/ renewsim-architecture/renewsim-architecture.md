---
name: renewsim-architecture  
description: Define y valida la arquitectura hexagonal, separación de capas y principios DDD en RenewSim.  
version: 1.0  
scope: global  
auto_invoke: true
---
# When to use

Usar SIEMPRE cuando:

- Se crea código nuevo
    
- Se refactoriza
    
- Se diseña una feature
    
- Se revisa código existente
    

---

# Core Rules

## 1. Separación de capas

- domain → lógica pura
    
- application → orquestación
    
- infrastructure → implementación técnica
    
- web → entrada HTTP
    

❌ Prohibido:

- Controller → Repository
    
- Entity JPA en domain
    
- DTOs en domain
    

---

## 2. Dominio limpio

El dominio:

- NO conoce Spring
    
- NO conoce BD
    
- NO conoce HTTP
    

Solo contiene:

- reglas
    
- invariantes
    
- lógica de negocio
    

---

## 3. Application Layer

Responsabilidad:

- ejecutar casos de uso
    
- coordinar servicios de dominio
    
- llamar a puertos
    

NO:

- lógica compleja
    
- cálculos pesados
    

---

## 4. Infrastructure

Aquí vive:

- JPA
    
- APIs externas
    
- SMTP
    
- JWT
    
- OpenAI
    

---

## 5. Web Layer

Solo:

- request → DTO
    
- DTO → command
    
- response
    

---

# Anti-patterns

🚨 CRÍTICOS:

- lógica en controller
    
- lógica en mapper
    
- services gigantes (God class)
    
- repositorios con lógica de negocio
    

---

# Checklist obligatorio

Antes de dar por válida una implementación:

-  Domain sin imports de Spring
    
-  Use cases definidos
    
-  Puertos claros
    
-  Infra implementa interfaces
    
-  Controller sin lógica
    
-  Tests cubren comportamiento
    

---

# Resultado esperado

Código:

- desacoplado
    
- testeable
    
- mantenible
    
- extensible