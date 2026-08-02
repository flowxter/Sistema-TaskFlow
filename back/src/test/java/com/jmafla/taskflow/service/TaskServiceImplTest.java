package com.jmafla.taskflow.service;

import com.jmafla.taskflow.domain.model.Project;
import com.jmafla.taskflow.domain.model.ProjectStatus;
import com.jmafla.taskflow.domain.model.Task;
import com.jmafla.taskflow.domain.model.TaskPriority;
import com.jmafla.taskflow.domain.model.TaskStatus;
import com.jmafla.taskflow.exception.BusinessRuleException;
import com.jmafla.taskflow.exception.ResourceNotFoundException;
import com.jmafla.taskflow.repository.ProjectRepository;
import com.jmafla.taskflow.repository.TaskRepository;
import com.jmafla.taskflow.service.impl.TaskServiceImpl;
import com.jmafla.taskflow.web.dto.request.TaskRequest;
import com.jmafla.taskflow.web.dto.response.TaskResponse;
import com.jmafla.taskflow.web.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de tareas.
 *
 * <p>Los repositorios se sustituyen por dobles de prueba: lo que se verifica es
 * la logica del servicio, no la persistencia. Por eso no se levanta el contexto
 * de Spring ni una base de datos.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Servicio de tareas")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    private TaskMapper taskMapper;
    private TaskServiceImpl taskService;

    private Project activeProject;
    private Project archivedProject;

    @BeforeEach
    void setUp() {
        // El mapper no tiene dependencias ni estado: usar el real es mas fiable
        // que simularlo y de paso se prueba la conversion.
        taskMapper = new TaskMapper();
        taskService = new TaskServiceImpl(taskRepository, projectRepository, taskMapper);

        activeProject = Project.builder()
                .code("TF-CORE")
                .name("Plataforma TaskFlow")
                .build();

        archivedProject = Project.builder()
                .code("TF-LEGACY")
                .name("Migracion sistema previo")
                .status(ProjectStatus.ARCHIVED)
                .build();
        archivedProject.archive();
    }

    @Test
    @DisplayName("Crea una tarea y la vincula al proyecto")
    void shouldCreateTaskAndLinkItToProject() {
        TaskRequest request = new TaskRequest(
                "Implementar filtros",
                "Filtros combinables por estado y prioridad",
                TaskPriority.HIGH,
                "Juan Mafla",
                LocalDate.now().plusDays(5));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(activeProject));
        when(taskRepository.save(any(Task.class))).thenAnswer(call -> call.getArgument(0));

        TaskResponse response = taskService.create(1L, request);

        assertThat(response.title()).isEqualTo("Implementar filtros");
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.status()).isEqualTo(TaskStatus.BACKLOG);
        assertThat(activeProject.getTasks()).hasSize(1);
    }

    @Test
    @DisplayName("Aplica una prioridad por defecto cuando el cliente no la envia")
    void shouldApplyDefaultPriorityWhenNotProvided() {
        TaskRequest request = new TaskRequest("Tarea sin prioridad", null, null, null, null);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(activeProject));
        when(taskRepository.save(any(Task.class))).thenAnswer(call -> call.getArgument(0));

        TaskResponse response = taskService.create(1L, request);

        assertThat(response.priority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    @DisplayName("Rechaza crear tareas en un proyecto archivado")
    void shouldRejectTaskCreationOnArchivedProject() {
        TaskRequest request = new TaskRequest("Tarea nueva", null, null, null, null);

        when(projectRepository.findById(9L)).thenReturn(Optional.of(archivedProject));

        assertThatThrownBy(() -> taskService.create(9L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("archivado");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Falla con 404 cuando el proyecto no existe")
    void shouldFailWhenProjectDoesNotExist() {
        TaskRequest request = new TaskRequest("Tarea huerfana", null, null, null, null);

        when(projectRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(404L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Proyecto");
    }

    @Test
    @DisplayName("Aplica una transicion de estado valida y sella la fecha de cierre")
    void shouldApplyValidStatusTransition() {
        Task task = buildTaskInProgress();
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.changeStatus(5L, TaskStatus.IN_REVIEW);

        assertThat(response.status()).isEqualTo(TaskStatus.IN_REVIEW);
        assertThat(response.completedAt()).isNull();
    }

    @Test
    @DisplayName("Rechaza una transicion invalida e informa los estados permitidos")
    void shouldRejectInvalidStatusTransition() {
        Task task = buildTaskInProgress();
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        // IN_PROGRESS no puede saltar directamente a DONE: falta la revision.
        assertThatThrownBy(() -> taskService.changeStatus(5L, TaskStatus.DONE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("IN_REVIEW");

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Registra la fecha de cierre al completar una tarea")
    void shouldStampCompletionDateWhenTaskIsDone() {
        Task task = buildTaskInProgress();
        task.transitionTo(TaskStatus.IN_REVIEW);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.changeStatus(5L, TaskStatus.DONE);

        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.completedAt()).isNotNull();
        assertThat(response.allowedTransitions()).isEmpty();
    }

    /** Construye una tarea ya vinculada a un proyecto activo y en curso. */
    private Task buildTaskInProgress() {
        Task task = Task.builder()
                .title("Filtros dinamicos")
                .priority(TaskPriority.HIGH)
                .build();

        activeProject.addTask(task);
        task.transitionTo(TaskStatus.IN_PROGRESS);
        return task;
    }
}
