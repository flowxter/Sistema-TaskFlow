/**
 * Insignia de prioridad o estado.
 *
 * @param {{ variant: string, children: import('react').ReactNode }} props
 */
export function Badge({ variant, children }) {
  return <span className={`badge badge--${variant}`}>{children}</span>;
}

/** Etiquetas legibles para las prioridades que devuelve la API. */
export const PRIORITY_LABELS = {
  LOW: 'Baja',
  MEDIUM: 'Media',
  HIGH: 'Alta',
  CRITICAL: 'Crítica',
};

/** Etiquetas legibles para los estados de tarea. */
export const STATUS_LABELS = {
  BACKLOG: 'Pendiente',
  IN_PROGRESS: 'En curso',
  IN_REVIEW: 'En revisión',
  BLOCKED: 'Bloqueada',
  DONE: 'Terminada',
};
