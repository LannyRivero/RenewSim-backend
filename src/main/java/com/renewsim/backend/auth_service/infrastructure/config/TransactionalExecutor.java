package com.renewsim.backend.auth_service.infrastructure.config;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;

import java.util.function.Supplier;

/**
 * Infraestructura adapter que encapsula la gestión transaccional.
 * 
 * Permite ejecutar operaciones con semántica transaccional sin acoplar
 * la capa de aplicación a Spring Framework.
 * 
 * @since 1.2.0
 */
@Component
public class TransactionalExecutor implements TransactionalPort {

    /**
     * Ejecuta una operación con retorno dentro de una transacción.
     *
     * @param action operación a ejecutar
     * @param <T>    tipo de retorno
     * @return resultado de la operación
     */
    
    @Override
    @Transactional
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }

    /**
     * Ejecuta una operación sin retorno dentro de una transacción.
     *
     * @param action operación a ejecutar
     */
    @Override
    @Transactional
    public void executeVoid(Runnable action) {
        action.run();
    }
}