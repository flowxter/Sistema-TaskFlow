import { useCallback, useEffect, useState } from 'react';
import { projectApi } from '../api/projectApi.js';

/**
 * Encapsula la carga y mutación de proyectos.
 *
 * Los componentes que lo consumen no saben nada de fetch, estados de carga ni
 * manejo de errores: reciben datos listos para pintar y funciones para actuar.
 */
export function useProjects() {
  const [projects, setProjects] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadProjects = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const page = await projectApi.list({ size: 50, sort: 'code,asc' });
      setProjects(page.content);
    } catch (apiError) {
      setError(apiError.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProjects();
  }, [loadProjects]);

  const createProject = useCallback(
    async (project) => {
      const created = await projectApi.create(project);
      await loadProjects();
      return created;
    },
    [loadProjects],
  );

  const archiveProject = useCallback(
    async (projectId) => {
      await projectApi.archive(projectId);
      await loadProjects();
    },
    [loadProjects],
  );

  return { projects, isLoading, error, loadProjects, createProject, archiveProject };
}
