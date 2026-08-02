package com.jmafla.taskflow.repository.specification;

import com.jmafla.taskflow.domain.model.Project;
import com.jmafla.taskflow.domain.model.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

/** Filtros componibles para consultas de proyectos. */
public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    private static Specification<Project> alwaysTrue() {
        return (root, query, builder) -> builder.conjunction();
    }

    public static Specification<Project> hasStatus(ProjectStatus status) {
        if (status == null) {
            return alwaysTrue();
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    /** Busqueda parcial sobre codigo y nombre. */
    public static Specification<Project> matchesText(String term) {
        if (term == null || term.isBlank()) {
            return alwaysTrue();
        }
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("code")), pattern),
                builder.like(builder.lower(root.get("name")), pattern));
    }
}
