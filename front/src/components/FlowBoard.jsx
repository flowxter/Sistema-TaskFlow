import { TaskCard } from './TaskCard.jsx';
import { STATUS_LABELS } from './Badge.jsx';

/**
 * Columnas del tablero.
 *
 * El orden refleja el recorrido real del proceso. BLOCKED se marca como desvío
 * porque no es un paso del flujo sino una salida lateral, y la interfaz lo
 * representa con línea punteada para que esa diferencia se vea.
 */
const FLOW_COLUMNS = [
  { status: 'BACKLOG', isDetour: false },
  { status: 'IN_PROGRESS', isDetour: false },
  { status: 'IN_REVIEW', isDetour: false },
  { status: 'DONE', isDetour: false },
  { status: 'BLOCKED', isDetour: true },
];

/**
 * Tablero de tareas agrupadas por estado.
 *
 * @param {{ tasks: Array, onChangeStatus: Function, onDelete: Function }} props
 */
export function FlowBoard({ tasks, onChangeStatus, onDelete }) {
  const tasksByStatus = FLOW_COLUMNS.reduce((groups, column) => {
    groups[column.status] = tasks.filter((task) => task.status === column.status);
    return groups;
  }, {});

  return (
    <div className="flowBoard">
      {FLOW_COLUMNS.map(({ status, isDetour }) => {
        const columnTasks = tasksByStatus[status];
        const columnClassName = isDetour ? 'flowColumn flowColumn--detour' : 'flowColumn';

        return (
          <section key={status} className={columnClassName} aria-labelledby={`col-${status}`}>
            <header className="flowColumn__head">
              <div className="flowColumn__node">
                <span className="flowColumn__dot" aria-hidden="true" />
                <h3 id={`col-${status}`} className="flowColumn__label">
                  {STATUS_LABELS[status]}
                </h3>
                <span className="flowColumn__count">{columnTasks.length}</span>
              </div>
              {isDetour && (
                <p className="flowColumn__note">
                  Fuera del carril principal · requiere desbloqueo para continuar
                </p>
              )}
            </header>

            <div className="flowColumn__stack">
              {columnTasks.length === 0 ? (
                <p className="emptyStack">Sin tareas</p>
              ) : (
                columnTasks.map((task) => (
                  <TaskCard
                    key={task.id}
                    task={task}
                    onChangeStatus={onChangeStatus}
                    onDelete={onDelete}
                  />
                ))
              )}
            </div>
          </section>
        );
      })}
    </div>
  );
}
