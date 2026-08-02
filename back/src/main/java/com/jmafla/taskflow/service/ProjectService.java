package com.jmafla.taskflow.service;

import com.jmafla.taskflow.domain.model.ProjectStatus;
import com.jmafla.taskflow.web.dto.request.ProjectRequest;
import com.jmafla.taskflow.web.dto.response.PageResponse;
import com.jmafla.taskflow.web.dto.response.ProjectMetricsResponse;
import com.jmafla.taskflow.web.dto.response.ProjectResponse;
import org.springframework.data.domain.Pageable;

/**
 * Contrato de operaciones sobre proyectos.
 *
 * <p>Se declara como interfaz para que el controlador dependa de la abstraccion
 * y no de la implementacion. Esto permite sustituirla en pruebas y cumple el
 * principio de inversion de dependencias.</p>
 */
public interface ProjectService {

    PageResponse<ProjectResponse> findAll(ProjectStatus status, String search, Pageable pageable);

    ProjectResponse findById(Long id);

    ProjectResponse create(ProjectRequest request);

    ProjectResponse update(Long id, ProjectRequest request);

    ProjectResponse archive(Long id);

    ProjectResponse reactivate(Long id);

    void delete(Long id);

    ProjectMetricsResponse calculateMetrics(Long id);
}
