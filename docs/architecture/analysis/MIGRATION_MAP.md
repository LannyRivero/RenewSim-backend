# MAPA DE MIGRACIÓN - TASK 1.2

## SHARED KERNEL (Debe moverse a shared/)

### shared/domain/vo/
- `role_service/domain/model/RoleName.java` (15 usos)

### shared/domain/exception/
- `role_service/domain/exception/LastAdminRemovalException.java` (revisar si es realmente shared)

### shared/application/dto/ (ANTI-PATTERN - DEPRECAR)
- `user_service/dto/UpdateUserRolesRequestDTO` → Mover a command interno
- `role_service/dto/RoleDTO` → Eliminar o internalizar

## VIOLACIONES A CORREGIR

### P0 - Infraestructura expuesta
- ❌ `user_service.infraestructure.client.FeignRoleConfig` importa `JwtTokenProvider`
  - Solución: Usar interceptor genérico de auth

### P1 - DTOs compartidos
- ❌ `user_service.dto.*` usado cross-BC
  - Solución: Crear contracts/ports en shared/ o usar eventos

## BOUNDED CONTEXTS INDEPENDIENTES (pueden migrarse solos)

1. **technology_service** - Sin dependencias detectadas ✅
2. **simulation_service** - Revisar dependencias

## ORDEN DE MIGRACIÓN PROPUESTO

1. Extraer RoleName a shared/domain/vo/
2. Migrar technology_service (BC aislado)
3. Limpiar violaciones de infraestructura
4. Migrar user_service + role_service juntos (alta cohesión)
5. Migrar auth_service
6. Migrar simulation_service
