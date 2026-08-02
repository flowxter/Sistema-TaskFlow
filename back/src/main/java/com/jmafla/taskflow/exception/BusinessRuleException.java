package com.jmafla.taskflow.exception;

/**
 * Se lanza cuando una operacion es sintacticamente valida pero viola una regla
 * de negocio, por ejemplo una transicion de estado no permitida. Se traduce a
 * HTTP 409 (Conflict), que describe mejor la situacion que un 400 generico.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
