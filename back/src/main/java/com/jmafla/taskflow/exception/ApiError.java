package com.jmafla.taskflow.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo unico de error para toda la API.
 *
 * <p>Un formato consistente evita que cada cliente tenga que interpretar una
 * estructura distinta segun el endpoint. Los campos nulos se omiten para no
 * ensuciar la respuesta.</p>
 *
 * @param timestamp     momento en que se genero el error
 * @param status        codigo HTTP
 * @param error         nombre corto del error
 * @param message       descripcion legible para el desarrollador cliente
 * @param path          ruta que origino el error
 * @param fieldErrors   detalle por campo cuando falla la validacion
 * @param allowedValues valores admitidos, util en errores de transicion de estado
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors,
        List<String> allowedValues
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null, null);
    }

    public static ApiError withFieldErrors(int status,
                                           String error,
                                           String message,
                                           String path,
                                           Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, path, fieldErrors, null);
    }
}
