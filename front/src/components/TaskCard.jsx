import { Badge, PRIORITY_LABELS, STATUS_LABELS } from './Badge.jsx';

/** Formatea una fecha ISO al formato corto local. */
function formatDueDate(dueDate) {
  if (!dueDate) {
    return 'Sin fecha';
  }
  return new Date(`${dueDate}T00:00:00`).toLocaleDateString('es-CO', {
    day: '2-digit',
    month: 'short',
  });
}

/**
 * Tarjeta de una tarea.
 *
 * Los botones de transición se construyen a partir de `allowedTransitions`, que
 * llega calculado desde el backend. El cliente no duplica la máquina de estados:
 * solo pinta lo que el dominio autoriza.
 */
export function TaskCard({ task, onChangeStatus, onDelete }) {
  const cardClassName = task.overdue ? 'taskCard taskCard--overdue' : 'taskCard';

  return (
    <article className={cardClassName}>
      <h4 className="taskCard__title">{task.title}</h4>

      <div className="taskCard__meta">
        <Badge variant={task.priority.toLowerCase()}>{PRIORITY_LABELS[task.priority]}</Badge>
        <span>{task.assignee ?? 'Sin asignar'}</span>
        <span aria-hidden="true">·</span>
        <span>{formatDueDate(task.dueDate)}</span>
        {task.overdue && <Badge variant="overdue">Vencida</Badge>}
      </div>

      <div className="taskCard__actions">
        {task.allowedTransitions.map((nextStatus) => (
          <button
            key={nextStatus}
            type="button"
            className="button button--step"
            onClick={() => onChangeStatus(task.id, nextStatus)}
          >
            → {STATUS_LABELS[nextStatus]}
          </button>
        ))}

        {task.allowedTransitions.length === 0 && <Badge variant="done">Cerrada</Badge>}

        <button
          type="button"
          className="button button--ghost"
          onClick={() => onDelete(task.id)}
          aria-label={`Eliminar la tarea ${task.title}`}
        >
          Eliminar
        </button>
      </div>
    </article>
  );
}
