/**
 * Franja de indicadores del proyecto.
 *
 * El medidor de avance usa el mismo porcentaje que calcula el backend, para que
 * nunca haya dos versiones de la misma cifra.
 */
export function ProjectMetrics({ metrics }) {
  if (!metrics) {
    return null;
  }

  return (
    <section className="metrics" aria-label="Indicadores del proyecto">
      <div className="metric">
        <span className="metric__value">{metrics.totalTasks}</span>
        <span className="metric__label">Tareas</span>
      </div>

      <div className="metric">
        <span className="metric__value">{metrics.completedTasks}</span>
        <span className="metric__label">Terminadas</span>
      </div>

      <div className={metrics.overdueTasks > 0 ? 'metric metric--alert' : 'metric'}>
        <span className="metric__value">{metrics.overdueTasks}</span>
        <span className="metric__label">Vencidas</span>
      </div>

      <div className="metricGauge">
        <span className="metric__label">Avance · {metrics.completionRate}%</span>
        <div
          className="metricGauge__track"
          role="progressbar"
          aria-valuenow={metrics.completionRate}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label="Porcentaje de tareas terminadas"
        >
          <div className="metricGauge__fill" style={{ width: `${metrics.completionRate}%` }} />
        </div>
      </div>
    </section>
  );
}
