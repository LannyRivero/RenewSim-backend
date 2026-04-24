package com.renewsim.backend.auth_service.application.port.out;

import java.util.function.Supplier;

/**
 * Puerto de salida para ejecutar operaciones con semántica transaccional.
 * 
 * Permite a la capa de aplicación solicitar ejecución transaccional
 * sin acoplarse a la implementación técnica de gestión de transacciones.
 * 
 * @since 1.2.0
 */
public interface TransactionalPort {

    /**
     * Ejecuta una operación con retorno dentro de una transacción.
     *
     * @param action operación a ejecutar
     * @param <T>    tipo de retorno
     * @return resultado de la operación
     */
    <T> T execute(Supplier<T> action);

    /**
     * Ejecuta una operación sin retorno dentro de una transacción.
     *
     * @param action operación a ejecutar
     */
    void executeVoid(Runnable action);
}