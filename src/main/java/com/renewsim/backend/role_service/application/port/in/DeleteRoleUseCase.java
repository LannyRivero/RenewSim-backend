package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.role_service.application.result.RoleDeletionResultDTO;
import com.renewsim.backend.shared.common.application.port.in.DeleteUseCase;

/**
 * Caso de uso para eliminar roles, devuelve un resultado detallado.
 */
public interface DeleteRoleUseCase extends DeleteUseCase<Long, RoleDeletionResultDTO> {
}
