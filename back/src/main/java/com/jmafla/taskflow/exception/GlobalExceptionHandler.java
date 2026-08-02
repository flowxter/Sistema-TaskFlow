package com.jmafla.taskflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce excepciones del dominio a respuestas HTTP coherentes.
 *
 * <p>Centralizar el manejo aqui mantiene los controladores limpios: ninguno
 * necesita bloques {@code try/catch} y todos devuelven el mismo contrato de
 * error. Los detalles internos nunca se filtran al cliente.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception,
                                                   HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException exception,
                                                       HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException exception,
                                                    HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    /** Errores de {@code @Valid}: se devuelve el detalle campo por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
                                                     HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ApiError body = ApiError.withFieldErrors(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "La solicitud contiene campos invalidos",
                request.getRequestURI(),
                fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /** JSON malformado o valor de enum inexistente. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException exception,
                                                     HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no se pudo interpretar. Revise el formato y los valores permitidos.",
                request);
    }

    /**
     * Red de seguridad. Se registra la traza completa en el log pero al cliente
     * solo se le devuelve un mensaje generico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception,
                                                     HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado. Contacte al administrador del sistema.",
                request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status,
                                           String message,
                                           HttpServletRequest request) {
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
