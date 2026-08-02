package com.jmafla.taskflow.exception;

/**
 * Se lanza al intentar crear un recurso cuyo identificador de negocio ya existe.
 * Se traduce a HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException of(String resource, String field, Object value) {
        return new DuplicateResourceException(
                "Ya existe un %s con %s = '%s'".formatted(resource, field, value));
    }
}
