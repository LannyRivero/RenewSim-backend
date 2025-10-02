package com.renewsim.backend.shared.common.application.port.in;
/**
 * Caso de uso genérico para operaciones de borrado.
 * @param <ID> tipo del identificador de la entidad
 * @param <RESULT> tipo del resultado devuelto tras la operación
 */
public interface DeleteUseCase<ID, RESULT> {
    RESULT delete(ID id);
}
