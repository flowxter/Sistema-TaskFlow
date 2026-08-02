package com.jmafla.taskflow.web.dto.response;

import com.jmafla.taskflow.domain.model.ProjectStatus;

import java.time.Instant;

/** Representacion publica de un proyecto en listados. */
public record ProjectResponse(
        Long id,
        String code,
        String name,
        String description,
        ProjectStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
