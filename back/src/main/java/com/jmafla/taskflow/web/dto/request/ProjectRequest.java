package com.jmafla.taskflow.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para crear o actualizar un proyecto.
 *
 * <p>Se usa un {@code record} en lugar de una clase con setters: el DTO es
 * inmutable por construccion y no hay forma de modificarlo despues de que el
 * controlador lo recibe.</p>
 *
 * @param code        codigo unico, formato {@code XX-XXXX} en mayusculas
 * @param name        nombre visible del proyecto
 * @param description resumen opcional
 */
public record ProjectRequest(

        @NotBlank(message = "El codigo es obligatorio")
        @Pattern(
                regexp = "^[A-Z][A-Z0-9]{1,5}(-[A-Z0-9]{1,8})?$",
                message = "El codigo debe ir en mayusculas, por ejemplo TF-CORE"
        )
        String code,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String name,

        @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
        String description
) {
}
