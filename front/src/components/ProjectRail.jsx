/**
 * Panel lateral con la lista de proyectos.
 *
 * @param {{ projects: Array, selectedProjectId: number|null,
 *           onSelect: Function, onCreate: Function }} props
 */
export function ProjectRail({ projects, selectedProjectId, onSelect, onCreate }) {
  return (
    <nav className="projectRail" aria-label="Proyectos">
      <div className="projectRail__header">
        <h2 className="projectRail__title">Proyectos</h2>
        <button type="button" className="button button--step" onClick={onCreate}>
          Nuevo
        </button>
      </div>

      <ul className="projectRail__list">
        {projects.map((project) => {
          const isArchived = project.status === 'ARCHIVED';
          const className = isArchived ? 'projectCard projectCard--archived' : 'projectCard';

          return (
            <li key={project.id}>
              <button
                type="button"
                className={className}
                aria-current={project.id === selectedProjectId}
                onClick={() => onSelect(project.id)}
              >
                <span className="projectCard__code">{project.code}</span>
                <span className="projectCard__name">{project.name}</span>
                {isArchived && <span className="projectCard__code">Archivado</span>}
              </button>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
