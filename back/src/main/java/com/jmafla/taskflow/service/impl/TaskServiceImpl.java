package com.jmafla.taskflow.service.impl;

import com.jmafla.taskflow.domain.model.Project;
import com.jmafla.taskflow.domain.model.Task;
import com.jmafla.taskflow.domain.model.TaskPriority;
import com.jmafla.taskflow.domain.model.TaskStatus;
import com.jmafla.taskflow.exception.BusinessRuleException;
import com.jmafla.taskflow.exception.ResourceNotFoundException;
import com.jmafla.taskflow.repository.ProjectRepository;
import com.jmafla.taskflow.repository.TaskRepository;
import com.jmafla.taskflow.repository.specification.TaskSpecifications;
import com.jmafla.taskflow.service.TaskService;
import com.jmafla.taskflow.web.dto.request.TaskRequest;
import com.jmafla.taskflow.web.dto.response.PageResponse;
import com.jmafla.taskflow.web.dto.response.TaskResponse;
import com.jmafla.taskflow.web.mapper.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/** Implementacion de las operaciones de tarea. */
@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public PageResponse<TaskResponse> findByProject(Long projectId,
                                                    TaskStatus status,
                                                    TaskPriority priority,
                                                    String assignee,
                                                    String search,
                                                    Boolean overdue,
                                                    Pageable pageable) {
        requireProjectExists(projectId);

        // Los criterios se encadenan sin condicionales: cada filtro nulo aporta
        // una especificacion neutra.
        Specification<Task> filters = TaskSpecifications.belongsToProject(projectId)
                .and(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.hasPriority(priority))
                .and(TaskSpecifications.assignedTo(assignee))
                .and(TaskSpecifications.matchesText(search))
                .and(TaskSpecifications.isOverdue(overdue));

        Page<Task> tasks = taskRepository.findAll(filters, pageable);
        return PageResponse.from(tasks, taskMapper::toResponse);
    }

    @Override
    public TaskResponse findById(Long id) {
        return taskMapper.toResponse(requireTask(id));
    }

    @Override
    @Transactional
    public TaskResponse create(Long projectId, TaskRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> ResourceNotFoundException.of("Proyecto", "id", projectId));

        requireEditableProject(project);

        Task task = taskMapper.toEntity(request);

        // Se agrega a traves del agregado para que ambos lados de la relacion
        // queden sincronizados.
        project.addTask(task);
        Task saved = taskRepository.save(task);

        log.info("Tarea creada en el proyecto {}: {}", project.getCode(), saved.getTitle());
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = requireTask(id);
        requireEditableProject(task.getProject());

        task.updateDetails(
                request.title(),
                request.description(),
                request.priorityOrDefault(),
                request.assignee(),
                request.dueDate());

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse changeStatus(Long id, TaskStatus targetStatus) {
        Task task = requireTask(id);
        requireEditableProject(task.getProject());

        TaskStatus currentStatus = task.getStatus();

        // La decision de si la transicion es valida pertenece al dominio.
        // El servicio solo traduce el rechazo a una excepcion de negocio.
        if (!task.transitionTo(targetStatus)) {
            throw new BusinessRuleException(
                    "Transicion invalida de %s a %s. Estados permitidos desde %s: %s"
                            .formatted(
                                    currentStatus,
                                    targetStatus,
                                    currentStatus,
                                    formatAllowedTransitions(currentStatus)));
        }

        log.info("Tarea {} paso de {} a {}", id, currentStatus, targetStatus);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Task task = requireTask(id);
        requireEditableProject(task.getProject());

        taskRepository.delete(task);
        log.info("Tarea eliminada: {}", id);
    }

    // ------------------------------------------------------------------
    // Metodos de apoyo
    // ------------------------------------------------------------------

    private Task requireTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Tarea", "id", id));
    }

    private void requireProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw ResourceNotFoundException.of("Proyecto", "id", projectId);
        }
    }

    /** Un proyecto archivado es historico: no admite cambios en sus tareas. */
    private void requireEditableProject(Project project) {
        if (!project.isEditable()) {
            throw new BusinessRuleException(
                    "El proyecto %s esta archivado y no admite modificaciones"
                            .formatted(project.getCode()));
        }
    }

    private String formatAllowedTransitions(TaskStatus status) {
        if (status.getAllowedTransitions().isEmpty()) {
            return "ninguno, es un estado final";
        }
        return status.getAllowedTransitions().stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
