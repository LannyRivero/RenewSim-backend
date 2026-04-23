# RenewSim — AI Agents System

## Purpose

Este sistema define cómo los agentes y skills colaboran para desarrollar, auditar y evolucionar RenewSim con estándares de producción.

El objetivo NO es generar código rápido, sino asegurar:

- Arquitectura limpia (hexagonal)
    
- Seguridad realista
    
- Testing profesional
    
- Decisiones defendibles
    

---

## Core Principles

1. **El Orchestrator nunca implementa lógica compleja**
    
    - Solo clasifica, enruta y coordina
        
2. **Los Agents son especialistas**
    
    - Cada uno domina un bounded context o área técnica
        
3. **Las Skills contienen conocimiento operativo**
    
    - Patrones reutilizables
        
    - Reglas concretas
        
    - Checklists accionables
        
4. **El dominio manda**
    
    - Ningún agent puede romper reglas del dominio
        

---

## Execution Flow

1. El Orchestrator analiza la petición
    
2. Identifica:
    
    - Tipo de tarea
        
    - Bounded context
        
3. Selecciona agent principal
    
4. Activa skills relevantes
    
5. Ejecuta validaciones cruzadas
    
6. Devuelve resultado consolidado
    

---

## Agents

|Agent|Responsabilidad|
|---|---|
|orchestrator|Routing y coordinación|
|architecture|Arquitectura, capas, DDD|
|auth|Autenticación, JWT, OTP|
|simulation|Motor de simulación|
|security|Revisión transversal de seguridad|
|testing|Estrategia y calidad de tests|
|frontend|React, estado, UX técnico|

---

## Skills

Las skills representan conocimiento especializado reutilizable.

Ejemplos:

- renewsim-architecture
    
- renewsim-auth-2fa
    
- renewsim-jwt-security
    
- renewsim-testing-backend
    

---

## Rules (Non-Negotiable)

- ❌ No mezclar capas (web → domain)
    
- ❌ No lógica de negocio en controllers
    
- ❌ No persistir datos sin validación de dominio
    
- ❌ No exponer datos sensibles
    
- ❌ No implementar sin tests en lógica crítica
    

---

## When in doubt

Siempre priorizar:

1. Correctitud
    
2. Mantenibilidad
    
3. Seguridad
    
4. Claridad
    
## Agent Activation Rules

- Si la tarea afecta estructura, capas o diseño → usar `architecture-agent`
- Si la tarea afecta login, JWT, OTP, refresh token o sesiones → usar `auth-agent`
- Si la tarea afecta permisos, cookies, headers, rate limiting o exposición de datos → usar `security-agent`
- Si la tarea afecta lógica de simulación, cálculos o lifecycle → usar `simulation-agent`
- Si la tarea afecta cobertura, tests o estrategia de validación → usar `testing-agent`
- Si la tarea afecta frontend React, estado o UX técnica → usar `frontend-agent`

Si una tarea afecta varias áreas, el orchestrator debe coordinar más de un agent.

## Orchestrator Rule

El orchestrator:
- clasifica
- enruta
- coordina

El orchestrator no debe implementar directamente lógica especializada si existe un agent específico para esa responsabilidad.

## Review Priority

Toda tarea debe validarse al menos desde:
1. arquitectura
2. seguridad
3. testing

Aunque el agent principal sea otro.

Nunca “rapidez”.