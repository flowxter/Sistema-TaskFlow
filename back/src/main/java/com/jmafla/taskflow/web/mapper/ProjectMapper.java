package com.jmafla.taskflow.web.mapper;

import com.jmafla.taskflow.domain.model.Project;
import com.jmafla.taskflow.web.dto.request.ProjectRequest;
import com.jmafla.taskflow.web.dto.response.ProjectResponse;
import org.springframework.stereotype.Component;

/**
 * Traduce entre la entidad {@link Project} y sus DTOs.
 *
 * <p><b>Patron Mapper.</b> Se implementa a mano en lugar de usar un generador
 * para que la conversion sea explicita y depurable: no hay codigo invisible que
 * revisar en una entrevista tecnica.</p>
 */
@Component
public class ProjectMapper {

    /** Convierte la entidad a su representacion publica. */
    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getCode(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }

    /** Construye una entidad nueva a partir de la solicitud. */
    public Project toEntity(ProjectRequest request) {
        return Project.builder()
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .build();
    }
}
