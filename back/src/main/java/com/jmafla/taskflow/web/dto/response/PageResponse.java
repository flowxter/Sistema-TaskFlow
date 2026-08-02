package com.jmafla.taskflow.web.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envoltura de paginacion propia de la API.
 *
 * <p>Devolver directamente el {@code Page} de Spring expone una estructura
 * interna del framework y ata el contrato de la API a su version. Este record
 * mantiene el contrato bajo nuestro control.</p>
 *
 * @param <T> tipo del contenido ya mapeado a DTO
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    /**
     * Convierte un {@code Page} de entidades en un {@code PageResponse} de DTOs.
     *
     * @param page   pagina devuelta por el repositorio
     * @param mapper funcion de conversion entidad a DTO
     * @param <E>    tipo de la entidad
     * @param <D>    tipo del DTO
     */
    public static <E, D> PageResponse<D> from(Page<E> page, Function<E, D> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
