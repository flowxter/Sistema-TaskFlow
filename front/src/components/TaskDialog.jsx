import { useState } from 'react';

const EMPTY_TASK = {
  title: '',
  description: '',
  priority: 'MEDIUM',
  assignee: '',
  dueDate: '',
};

/**
 * Formulario de creación de tareas.
 *
 * Los errores por campo que devuelve el backend se muestran junto al control
 * correspondiente: repetir la validación completa en el cliente crearía dos
 * fuentes de verdad que se desincronizan con el tiempo.
 */
export function TaskDialog({ onSubmit, onClose }) {
  const [form, setForm] = useState(EMPTY_TASK);
  const [fieldErrors, setFieldErrors] = useState({});
  const [isSaving, setIsSaving] = useState(false);

  const updateField = (field) => (event) =>
    setForm((current) => ({ ...current, [field]: event.target.value }));

  const handleSubmit = async () => {
    setIsSaving(true);
    setFieldErrors({});

    try {
      await onSubmit({
        ...form,
        description: form.description || null,
        assignee: form.assignee || null,
        dueDate: form.dueDate || null,
      });
      onClose();
    } catch (error) {
      setFieldErrors(
        Object.keys(error.fieldErrors ?? {}).length > 0
          ? error.fieldErrors
          : { title: error.message },
      );
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="dialogBackdrop" role="dialog" aria-modal="true" aria-labelledby="taskDialogTitle">
      <div className="dialog">
        <h3 id="taskDialogTitle" className="dialog__title">
          Nueva tarea
        </h3>

        <div className="dialog__form">
          <div>
            <label className="dialog__label" htmlFor="taskTitle">
              Título
            </label>
            <input
              id="taskTitle"
              className="field field--grow"
              style={{ width: '100%' }}
              value={form.title}
              onChange={updateField('title')}
              placeholder="Implementar filtros dinámicos"
            />
            {fieldErrors.title && <p className="notice__text">{fieldErrors.title}</p>}
          </div>

          <div>
            <label className="dialog__label" htmlFor="taskDescription">
              Descripción
            </label>
            <textarea
              id="taskDescription"
              className="field"
              style={{ width: '100%', minHeight: '80px' }}
              value={form.description}
              onChange={updateField('description')}
            />
          </div>

          <div className="dialog__row">
            <div>
              <label className="dialog__label" htmlFor="taskPriority">
                Prioridad
              </label>
              <select
                id="taskPriority"
                className="field"
                style={{ width: '100%' }}
                value={form.priority}
                onChange={updateField('priority')}
              >
                <option value="LOW">Baja</option>
                <option value="MEDIUM">Media</option>
                <option value="HIGH">Alta</option>
                <option value="CRITICAL">Crítica</option>
              </select>
            </div>

            <div>
              <label className="dialog__label" htmlFor="taskDueDate">
                Fecha límite
              </label>
              <input
                id="taskDueDate"
                type="date"
                className="field"
                style={{ width: '100%' }}
                value={form.dueDate}
                onChange={updateField('dueDate')}
              />
            </div>
          </div>

          <div>
            <label className="dialog__label" htmlFor="taskAssignee">
              Responsable
            </label>
            <input
              id="taskAssignee"
              className="field"
              style={{ width: '100%' }}
              value={form.assignee}
              onChange={updateField('assignee')}
              placeholder="Nombre de quien la ejecuta"
            />
          </div>

          <div className="dialog__actions">
            <button type="button" className="button" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button
              type="button"
              className="button button--primary"
              onClick={handleSubmit}
              disabled={isSaving || form.title.trim() === ''}
            >
              {isSaving ? 'Guardando…' : 'Crear tarea'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
