import { apiClient, buildQuery } from './apiClient.js';

/**
 * Operaciones sobre proyectos.
 *
 * Este módulo es la única parte del cliente que conoce las rutas del backend.
 * Si un endpoint cambia, se ajusta aquí y nada más.
 */
export const projectApi = {
  list: (filters = {}) => apiClient.get(`/projects${buildQuery(filters)}`),

  getById: (projectId) => apiClient.get(`/projects/${projectId}`),

  getMetrics: (projectId) => apiClient.get(`/projects/${projectId}/metrics`),

  create: (project) => apiClient.post('/projects', project),

  update: (projectId, project) => apiClient.put(`/projects/${projectId}`, project),

  archive: (projectId) => apiClient.patch(`/projects/${projectId}/archive`),

  reactivate: (projectId) => apiClient.patch(`/projects/${projectId}/reactivate`),

  remove: (projectId) => apiClient.delete(`/projects/${projectId}`),
};
