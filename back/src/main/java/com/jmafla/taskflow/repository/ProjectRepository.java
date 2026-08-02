package com.jmafla.taskflow.repository;

import com.jmafla.taskflow.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Acceso a datos de {@link Project}.
 *
 * <p>Extiende {@code JpaSpecificationExecutor} para soportar filtros dinamicos
 * sin escribir un metodo por combinacion posible de parametros.</p>
 */
@Repository
public interface ProjectRepository
        extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    boolean existsByCode(String code);

    Optional<Project> findByCode(String code);

    /**
     * Carga el proyecto junto con sus tareas en una sola consulta.
     * Evita el problema N+1 cuando se necesita el agregado completo.
     */
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.id = :id")
    Optional<Project> findByIdWithTasks(@Param("id") Long id);
}
