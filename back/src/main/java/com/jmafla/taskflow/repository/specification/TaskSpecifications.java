package com.jmafla.taskflow.repository.specification;

import com.jmafla.taskflow.domain.model.Task;
import com.jmafla.taskflow.domain.model.TaskPriority;
import com.jmafla.taskflow.domain.model.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Filtros componibles para consultas de tareas.
 *
 * <p><b>Patron Specification:</b> cada criterio es un objeto independiente que
 * se combina con {@code and}/{@code or}. Un filtro nulo devuelve una
 * especificacion neutra, de modo que la capa de servicio encadena todos los
 * criterios sin escribir condicionales.</p>
 */
public final class TaskSpecifications {

    /** Clase de utilidades: no se instancia. */
    private TaskSpecifications() {
    }

    /** Neutro del algebra de especificaciones: no restringe nada. */
    private static Specification<Task> alwaysTrue() {
        return (root, query, builder) -> builder.conjunction();
    }

    public static Specification<Task> belongsToProject(Long projectId) {
        if (projectId == null) {
            return alwaysTrue();
        }
        return (root, query, builder) ->
                builder.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        if (status == null) {
            return alwaysTrue();
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        if (priority == null) {
            return alwaysTrue();
        }
        return (root, query, builder) -> builder.equal(root.get("priority"), priority);
    }

    public static Specification<Task> assignedTo(String assignee) {
        if (assignee == null || assignee.isBlank()) {
            return alwaysTrue();
        }
        return (root, query, builder) ->
                builder.equal(builder.lower(root.get("assignee")), assignee.toLowerCase());
    }

    /** Busqueda parcial e insensible a mayusculas sobre titulo y descripcion. */
    public static Specification<Task> matchesText(String term) {
        if (term == null || term.isBlank()) {
            return alwaysTrue();
        }
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("title")), pattern),
                builder.like(builder.lower(root.get("description")), pattern));
    }

    /** Tareas vencidas que siguen abiertas. */
    public static Specification<Task> isOverdue(Boolean overdue) {
        if (overdue == null || !overdue) {
            return alwaysTrue();
        }
        return (root, query, builder) -> builder.and(
                builder.lessThan(root.get("dueDate"), LocalDate.now()),
                builder.notEqual(root.get("status"), TaskStatus.DONE));
    }
}
