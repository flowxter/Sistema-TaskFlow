package com.jmafla.taskflow.repository;

import com.jmafla.taskflow.domain.model.Task;
import com.jmafla.taskflow.domain.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Acceso a datos de {@link Task}. */
@Repository
public interface TaskRepository
        extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    /**
     * Conteo de tareas por estado en una sola consulta agregada.
     *
     * <p>Alternativa a traer todas las tareas y contarlas en memoria, que no
     * escala cuando el proyecto crece.</p>
     *
     * @return lista de pares {@code [TaskStatus, Long]}
     */
    @Query("""
            SELECT t.status, COUNT(t)
            FROM Task t
            WHERE t.project.id = :projectId
            GROUP BY t.status
            """)
    List<Object[]> countGroupedByStatus(@Param("projectId") Long projectId);

    /** Tareas vencidas y aun abiertas de un proyecto. */
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.project.id = :projectId
              AND t.status <> com.jmafla.taskflow.domain.model.TaskStatus.DONE
              AND t.dueDate < CURRENT_DATE
            """)
    long countOverdue(@Param("projectId") Long projectId);
}
