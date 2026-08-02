import { apiClient, buildQuery } from './apiClient.js';

/** Operaciones sobre tareas. */
export const taskApi = {
  listByProject: (projectId, filters = {}) =>
    apiClient.get(`/projects/${projectId}/tasks${buildQuery(filters)}`),

  create: (projectId, task) => apiClient.post(`/projects/${projectId}/tasks`, task),

  update: (taskId, task) => apiClient.put(`/tasks/${taskId}`, task),

  /** Solicita una transición de estado; el backend valida si es permitida. */
  changeStatus: (taskId, status) => apiClient.patch(`/tasks/${taskId}/status`, { status }),

  remove: (taskId) => apiClient.delete(`/tasks/${taskId}`),
};
