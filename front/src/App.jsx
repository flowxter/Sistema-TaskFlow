import { useEffect, useState } from 'react';
import { useProjects } from './hooks/useProjects.js';
import { useTasks } from './hooks/useTasks.js';
import { ProjectRail } from './components/ProjectRail.jsx';
import { ProjectMetrics } from './components/ProjectMetrics.jsx';
import { FlowBoard } from './components/FlowBoard.jsx';
import { ProjectDialog } from './components/ProjectDialog.jsx';
import { TaskDialog } from './components/TaskDialog.jsx';

const EMPTY_FILTERS = { status: '', priority: '', search: '' };

/**
 * Contenedor de la aplicación.
 *
 * Solo compone: la carga de datos vive en los hooks y la presentación en los
 * componentes. Aquí queda únicamente el estado de la interfaz —qué proyecto
 * está seleccionado y qué diálogo está abierto—.
 */
export default function App() {
  const { projects, isLoading: isLoadingProjects, error: projectsError, createProject } =
    useProjects();

  const [selectedProjectId, setSelectedProjectId] = useState(null);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [openDialog, setOpenDialog] = useState(null);

  // Selecciona el primer proyecto en cuanto llega la lista, para que la vista
  // nunca arranque vacía sin motivo.
  useEffect(() => {
    if (selectedProjectId === null && projects.length > 0) {
      setSelectedProjectId(projects[0].id);
    }
  }, [projects, selectedProjectId]);

  const {
    tasks,
    metrics,
    isLoading: isLoadingTasks,
    error: tasksError,
    createTask,
    changeTaskStatus,
    deleteTask,
  } = useTasks(selectedProjectId, filters);

  const selectedProject = projects.find((project) => project.id === selectedProjectId);
  const isArchived = selectedProject?.status === 'ARCHIVED';

  const updateFilter = (field) => (event) =>
    setFilters((current) => ({ ...current, [field]: event.target.value }));

  return (
    <div className="appShell">
      <header className="appHeader">
        <div className="appHeader__brand">
          <h1 className="appHeader__mark">
            Task<span>Flow</span>
          </h1>
          <span className="appHeader__tagline">Gestión de proyectos · Spring Boot + React</span>
        </div>
        <p className="appHeader__meta">
          API <a href="http://localhost:8080/swagger-ui.html">/swagger-ui.html</a>
        </p>
      </header>

      <ProjectRail
        projects={projects}
        selectedProjectId={selectedProjectId}
        onSelect={setSelectedProjectId}
        onCreate={() => setOpenDialog('project')}
      />

      <main className="workspace">
        {projectsError && (
          <div className="notice notice--error">
            <p className="notice__title">No se pudieron cargar los proyectos</p>
            <p className="notice__text">{projectsError}</p>
          </div>
        )}

        {isLoadingProjects && <p className="notice__text">Cargando proyectos…</p>}

        {selectedProject && (
          <>
            <div className="workspace__header">
              <div>
                <h2 className="workspace__heading">{selectedProject.name}</h2>
                <p className="workspace__subheading">
                  {selectedProject.description ?? 'Este proyecto no tiene descripción.'}
                </p>
              </div>

              <button
                type="button"
                className="button button--primary"
                onClick={() => setOpenDialog('task')}
                disabled={isArchived}
                title={isArchived ? 'Un proyecto archivado no admite tareas nuevas' : undefined}
              >
                Nueva tarea
              </button>
            </div>

            <ProjectMetrics metrics={metrics} />

            <div className="filterBar">
              <input
                className="field field--grow"
                placeholder="Buscar por título o descripción"
                value={filters.search}
                onChange={updateFilter('search')}
                aria-label="Buscar tareas"
              />

              <select
                className="field"
                value={filters.status}
                onChange={updateFilter('status')}
                aria-label="Filtrar por estado"
              >
                <option value="">Todos los estados</option>
                <option value="BACKLOG">Pendiente</option>
                <option value="IN_PROGRESS">En curso</option>
                <option value="IN_REVIEW">En revisión</option>
                <option value="BLOCKED">Bloqueada</option>
                <option value="DONE">Terminada</option>
              </select>

              <select
                className="field"
                value={filters.priority}
                onChange={updateFilter('priority')}
                aria-label="Filtrar por prioridad"
              >
                <option value="">Todas las prioridades</option>
                <option value="LOW">Baja</option>
                <option value="MEDIUM">Media</option>
                <option value="HIGH">Alta</option>
                <option value="CRITICAL">Crítica</option>
              </select>
            </div>

            {tasksError && (
              <div className="notice notice--error">
                <p className="notice__title">Algo falló al trabajar con las tareas</p>
                <p className="notice__text">{tasksError}</p>
              </div>
            )}

            {isLoadingTasks ? (
              <p className="notice__text">Cargando tareas…</p>
            ) : (
              <FlowBoard
                tasks={tasks}
                onChangeStatus={changeTaskStatus}
                onDelete={deleteTask}
              />
            )}
          </>
        )}
      </main>

      {openDialog === 'project' && (
        <ProjectDialog onSubmit={createProject} onClose={() => setOpenDialog(null)} />
      )}

      {openDialog === 'task' && (
        <TaskDialog onSubmit={createTask} onClose={() => setOpenDialog(null)} />
      )}
    </div>
  );
}
