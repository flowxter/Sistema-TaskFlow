package com.jmafla.taskflow.service;

import com.jmafla.taskflow.domain.model.TaskPriority;
import com.jmafla.taskflow.domain.model.TaskStatus;
import com.jmafla.taskflow.web.dto.request.TaskRequest;
import com.jmafla.taskflow.web.dto.response.PageResponse;
import com.jmafla.taskflow.web.dto.response.TaskResponse;
import org.springframework.data.domain.Pageable;

/** Contrato de operaciones sobre tareas. */
public interface TaskService {

    PageResponse<TaskResponse> findByProject(Long projectId,
                                             TaskStatus status,
                                             TaskPriority priority,
                                             String assignee,
                                             String search,
                                             Boolean overdue,
                                             Pageable pageable);

    TaskResponse findById(Long id);

    TaskResponse create(Long projectId, TaskRequest request);

    TaskResponse update(Long id, TaskRequest request);

    /** Aplica una transicion de estado validada por la maquina de estados. */
    TaskResponse changeStatus(Long id, TaskStatus targetStatus);

    void delete(Long id);
}
