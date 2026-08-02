package com.jmafla.taskflow.service.impl;

import com.jmafla.taskflow.domain.model.Project;
import com.jmafla.taskflow.domain.model.ProjectStatus;
import com.jmafla.taskflow.domain.model.TaskStatus;
import com.jmafla.taskflow.exception.BusinessRuleException;
import com.jmafla.taskflow.exception.DuplicateResourceException;
import com.jmafla.taskflow.exception.ResourceNotFoundException;
import com.jmafla.taskflow.repository.ProjectRepository;
import com.jmafla.taskflow.repository.TaskRepository;
import com.jmafla.taskflow.repository.specification.ProjectSpecifications;
import com.jmafla.taskflow.service.ProjectService;
import com.jmafla.taskflow.web.dto.request.ProjectRequest;
import com.jmafla.taskflow.web.dto.response.PageResponse;
import com.jmafla.taskflow.web.dto.response.ProjectMetricsResponse;
import com.jmafla.taskflow.web.dto.response.ProjectResponse;
import com.jmafla.taskflow.web.mapper.ProjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Implementacion de las operaciones de proyecto.
 *
 * <p>Aqui viven las reglas de negocio. El controlador solo traduce HTTP y el
 * repositorio solo habla con la base de datos; ninguna regla se filtra a esas
 * capas.</p>
 *
 * <p>La clase es de solo lectura por defecto ({@code @Transactional(readOnly = true)})
 * y cada metodo que escribe lo declara explicitamente. Es mas seguro que el
 * camino inverso: olvidar la anotacion produce un error visible, no una
 * escritura silenciosa fuera de transaccion.</p>
 */
@Service
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ProjectMapper projectMapper;

    /**
     * Inyeccion por constructor: deja las dependencias como {@code final},
     * hace obligatorio proveerlas y permite instanciar la clase en una prueba
     * unitaria sin levantar el contexto de Spring.
     */
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              TaskRepository taskRepository,
                              ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public PageResponse<ProjectResponse> findAll(ProjectStatus status,
                                                 String search,
                                                 Pageable pageable) {
        Specification<Project> filters = ProjectSpecifications.hasStatus(status)
                .and(ProjectSpecifications.matchesText(search));

        Page<Project> projects = projectRepository.findAll(filters, pageable);
        return PageResponse.from(projects, projectMapper::toResponse);
    }

    @Override
    public ProjectResponse findById(Long id) {
        return projectMapper.toResponse(requireProject(id));
    }

    @Override
    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        if (projectRepository.existsByCode(request.code())) {
            throw DuplicateResourceException.of("proyecto", "codigo", request.code());
        }

        Project project = projectRepository.save(projectMapper.toEntity(request));
        log.info("Proyecto creado: {}", project);
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = requireProject(id);

        // El codigo es inmutable: identifica al proyecto en sistemas externos
        // y cambiarlo romperia referencias fuera de nuestro control.
        if (!project.getCode().equals(request.code())) {
            throw new BusinessRuleException(
                    "El codigo del proyecto no se puede modificar una vez creado");
        }

        project.updateDetails(request.name(), request.description());
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse archive(Long id) {
        Project project = requireProject(id);

        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new BusinessRuleException("El proyecto ya se encuentra archivado");
        }

        // Regla de negocio: archivar un proyecto con trabajo abierto ocultaria
        // tareas vivas del tablero del equipo.
        long pendingTasks = countPendingTasks(id);
        if (pendingTasks > 0) {
            throw new BusinessRuleException(
                    "No se puede archivar el proyecto: tiene %d tarea(s) sin finalizar"
                            .formatted(pendingTasks));
        }

        project.archive();
        log.info("Proyecto archivado: {}", project.getCode());
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse reactivate(Long id) {
        Project project = requireProject(id);

        if (project.getStatus() == ProjectStatus.ACTIVE) {
            throw new BusinessRuleException("El proyecto ya se encuentra activo");
        }

        project.reactivate();
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Project project = requireProject(id);
        projectRepository.delete(project);
        log.info("Proyecto eliminado: {}", project.getCode());
    }

    @Override
    public ProjectMetricsResponse calculateMetrics(Long id) {
        Project project = requireProject(id);

        // Se parte de un mapa con todos los estados en cero para que el cliente
        // reciba siempre las mismas claves y no tenga que manejar ausencias.
        Map<TaskStatus, Long> tasksByStatus = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status, 0L);
        }

        List<Object[]> groupedCounts = taskRepository.countGroupedByStatus(id);
        for (Object[] row : groupedCounts) {
            tasksByStatus.put((TaskStatus) row[0], (Long) row[1]);
        }

        long totalTasks = tasksByStatus.values().stream().mapToLong(Long::longValue).sum();
        long completedTasks = tasksByStatus.get(TaskStatus.DONE);
        long overdueTasks = taskRepository.countOverdue(id);

        return new ProjectMetricsResponse(
                project.getId(),
                project.getCode(),
                totalTasks,
                completedTasks,
                overdueTasks,
                calculateCompletionRate(completedTasks, totalTasks),
                tasksByStatus);
    }

    // ------------------------------------------------------------------
    // Metodos de apoyo
    // ------------------------------------------------------------------

    /**
     * Recupera el proyecto o falla con 404. Centralizar esta busqueda evita
     * repetir el mismo {@code orElseThrow} en cada metodo publico.
     */
    private Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Proyecto", "id", id));
    }

    private long countPendingTasks(Long projectId) {
        long total = taskRepository.countGroupedByStatus(projectId).stream()
                .mapToLong(row -> (Long) row[1])
                .sum();
        long completed = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.DONE);
        return total - completed;
    }

    /** Porcentaje de avance redondeado a un decimal. Evita la division por cero. */
    private double calculateCompletionRate(long completedTasks, long totalTasks) {
        if (totalTasks == 0) {
            return 0.0;
        }
        return Math.round((completedTasks * 1000.0) / totalTasks) / 10.0;
    }
}
