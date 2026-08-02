package com.jmafla.taskflow.domain.model;

/**
 * Estados posibles de un proyecto.
 *
 * <p>Un proyecto archivado es de solo lectura: no admite tareas nuevas.
 * La regla se aplica en la capa de servicio, no en el controlador, para que
 * se cumpla sin importar por que camino llegue la peticion.</p>
 */
public enum ProjectStatus {

    /** Proyecto en curso: acepta creacion y edicion de tareas. */
    ACTIVE,

    /** Proyecto cerrado: se conserva como historico y no admite cambios. */
    ARCHIVED
}
