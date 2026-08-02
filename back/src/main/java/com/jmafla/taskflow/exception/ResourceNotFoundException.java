package com.jmafla.taskflow.exception;

/**
 * Se lanza cuando un recurso solicitado no existe. El manejador global la
 * traduce a un HTTP 404 con cuerpo estandarizado.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Fabrica un mensaje homogeneo para todos los recursos del sistema.
     *
     * @param resource nombre del recurso, por ejemplo {@code "Proyecto"}
     * @param field    campo por el que se busco
     * @param value    valor buscado
     */
    public static ResourceNotFoundException of(String resource, String field, Object value) {
        return new ResourceNotFoundException(
                "%s no encontrado con %s = '%s'".formatted(resource, field, value));
    }
}
