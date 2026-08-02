package com.jmafla.taskflow.web.controller;

import com.jmafla.taskflow.domain.model.ProjectStatus;
import com.jmafla.taskflow.service.ProjectService;
import com.jmafla.taskflow.web.dto.request.ProjectRequest;
import com.jmafla.taskflow.web.dto.response.PageResponse;
import com.jmafla.taskflow.web.dto.response.ProjectMetricsResponse;
import com.jmafla.taskflow.web.dto.response.ProjectResponse;
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
 * Endpoints de proyectos.
 *
 * <p>El controlador es deliberadamente delgado: valida la entrada, delega en el
 * servicio y elige el codigo HTTP. No contiene ninguna regla de negocio.</p>
 *
 * <p>La ruta incluye la version ({@code /api/v1}) desde el primer dia: agregarla
 * despues obliga a romper a todos los clientes existentes.</p>
 */
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Proyectos", description = "Gestion de proyectos y sus indicadores de avance")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "Lista proyectos con filtros opcionales y paginacion")
    public ResponseEntity<PageResponse<ProjectResponse>> listProjects(
            @Parameter(description = "Filtra por estado del proyecto")
            @RequestParam(required = false) ProjectStatus status,

            @Parameter(description = "Busqueda parcial sobre codigo y nombre")
            @RequestParam(required = false) String search,

            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(projectService.findAll(status, search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un proyecto por su identificador")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.findById(id));
    }

    @GetMapping("/{id}/metrics")
    @Operation(summary = "Calcula los indicadores de avance del proyecto")
    public ResponseEntity<ProjectMetricsResponse> getProjectMetrics(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.calculateMetrics(id));
    }

    /**
     * Devuelve 201 con la cabecera {@code Location} apuntando al recurso creado,
     * como indica la especificacion HTTP para creaciones exitosas.
     */
    @PostMapping
    @Operation(summary = "Crea un proyecto")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            UriComponentsBuilder uriBuilder) {

        ProjectResponse created = projectService.create(request);

        URI location = uriBuilder.path("/api/v1/projects/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza el nombre y la descripcion de un proyecto")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {

        return ResponseEntity.ok(projectService.update(id, request));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archiva un proyecto sin tareas pendientes")
    public ResponseEntity<ProjectResponse> archiveProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.archive(id));
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reactiva un proyecto archivado")
    public ResponseEntity<ProjectResponse> reactivateProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.reactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un proyecto y sus tareas asociadas")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
