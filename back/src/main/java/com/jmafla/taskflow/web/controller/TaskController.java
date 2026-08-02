package com.jmafla.taskflow.web.controller;

import com.jmafla.taskflow.domain.model.TaskPriority;
import com.jmafla.taskflow.domain.model.TaskStatus;
import com.jmafla.taskflow.service.TaskService;
import com.jmafla.taskflow.web.dto.request.TaskRequest;
import com.jmafla.taskflow.web.dto.request.TaskStatusRequest;
import com.jmafla.taskflow.web.dto.response.PageResponse;
import com.jmafla.taskflow.web.dto.response.TaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Endpoints de tareas.
 *
 * <p>Las tareas se crean y listan bajo la ruta del proyecto, porque no existen
 * fuera de el. Las operaciones sobre una tarea concreta cuelgan de
 * {@code /api/v1/tasks/{id}}, ya que su identificador es global.</p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Tareas", description = "Gestion de tareas y transiciones de estado")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Lista las tareas de un proyecto con filtros combinables")
    public ResponseEntity<PageResponse<TaskResponse>> listProjectTasks(
            @PathVariable Long projectId,

            @Parameter(description = "Filtra por estado")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "Filtra por prioridad")
            @RequestParam(required = false) TaskPriority priority,

            @Parameter(description = "Filtra por responsable, sin distinguir mayusculas")
            @RequestParam(required = false) String assignee,

            @Parameter(description = "Busqueda parcial sobre titulo y descripcion")
            @RequestParam(required = false) String search,

            @Parameter(description = "Si es true devuelve solo tareas vencidas y abiertas")
            @RequestParam(required = false) Boolean overdue,

            @PageableDefault(size = 20, sort = "dueDate", direction = Sort.Direction.ASC)
            Pageable pageable) {

        PageResponse<TaskResponse> tasks = taskService.findByProject(
                projectId, status, priority, assignee, search, overdue, pageable);

        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Crea una tarea dentro de un proyecto activo")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequest request,
            UriComponentsBuilder uriBuilder) {

        TaskResponse created = taskService.create(projectId, request);

        URI location = uriBuilder.path("/api/v1/tasks/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Obtiene una tarea por su identificador")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "Actualiza los datos de una tarea, sin incluir su estado")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {

        return ResponseEntity.ok(taskService.update(id, request));
    }

    /**
     * Cambio de estado como operacion dedicada.
     *
     * <p>Se usa {@code PATCH} y no {@code PUT} porque modifica un solo atributo,
     * y se separa del endpoint de actualizacion para que la maquina de estados
     * sea la unica puerta de entrada a ese campo.</p>
     */
    @PatchMapping("/tasks/{id}/status")
    @Operation(summary = "Aplica una transicion de estado validada")
    public ResponseEntity<TaskResponse> changeTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusRequest request) {

        return ResponseEntity.ok(taskService.changeStatus(id, request.status()));
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una tarea")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
