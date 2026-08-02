import { useCallback, useEffect, useState } from 'react';
import { taskApi } from '../api/taskApi.js';
import { projectApi } from '../api/projectApi.js';

/**
 * Gestiona las tareas y las métricas del proyecto seleccionado.
 *
 * Tareas y métricas se recargan juntas porque cualquier cambio de estado altera
 * ambas: mostrarlas desincronizadas confundiría al usuario.
 *
 * @param {number|null} projectId proyecto activo
 * @param {object} filters filtros aplicados en la barra superior
 */
export function useTasks(projectId, filters) {
  const [tasks, setTasks] = useState([]);
  const [metrics, setMetrics] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const { status, priority, search } = filters;

  const loadTasks = useCallback(async () => {
    if (!projectId) {
      setTasks([]);
      setMetrics(null);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const [page, projectMetrics] = await Promise.all([
        taskApi.listByProject(projectId, { status, priority, search, size: 100 }),
        projectApi.getMetrics(projectId),
      ]);

      setTasks(page.content);
      setMetrics(projectMetrics);
    } catch (apiError) {
      setError(apiError.message);
    } finally {
      setIsLoading(false);
    }
  }, [projectId, status, priority, search]);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  const createTask = useCallback(
    async (task) => {
      await taskApi.create(projectId, task);
      await loadTasks();
    },
    [projectId, loadTasks],
  );

  const changeTaskStatus = useCallback(
    async (taskId, nextStatus) => {
      await taskApi.changeStatus(taskId, nextStatus);
      await loadTasks();
    },
    [loadTasks],
  );

  const deleteTask = useCallback(
    async (taskId) => {
      await taskApi.remove(taskId);
      await loadTasks();
    },
    [loadTasks],
  );

  return { tasks, metrics, isLoading, error, loadTasks, createTask, changeTaskStatus, deleteTask };
}
