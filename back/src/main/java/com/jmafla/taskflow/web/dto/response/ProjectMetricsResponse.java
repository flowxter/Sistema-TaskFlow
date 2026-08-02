package com.jmafla.taskflow.web.dto.response;

import com.jmafla.taskflow.domain.model.TaskStatus;

import java.util.Map;

/**
 * Indicadores de avance de un proyecto.
 *
 * @param projectId        identificador del proyecto
 * @param projectCode      codigo del proyecto
 * @param totalTasks       total de tareas registradas
 * @param completedTasks   tareas en estado final
 * @param overdueTasks     tareas vencidas y aun abiertas
 * @param completionRate   porcentaje de avance, con un decimal
 * @param tasksByStatus    conteo por estado, incluyendo estados en cero
 */
public record ProjectMetricsResponse(
        Long projectId,
        String projectCode,
        long totalTasks,
        long completedTasks,
        long overdueTasks,
        double completionRate,
        Map<TaskStatus, Long> tasksByStatus
) {
}
