import { useState } from 'react';

const EMPTY_PROJECT = { code: '', name: '', description: '' };

/**
 * Formulario de creación de proyectos.
 *
 * El código se normaliza a mayúsculas mientras se escribe porque el backend lo
 * exige así: corregirlo aquí evita un rechazo de validación evitable.
 */
export function ProjectDialog({ onSubmit, onClose }) {
  const [form, setForm] = useState(EMPTY_PROJECT);
  const [fieldErrors, setFieldErrors] = useState({});
  const [isSaving, setIsSaving] = useState(false);

  const updateField = (field) => (event) => {
    const value = field === 'code' ? event.target.value.toUpperCase() : event.target.value;
    setForm((current) => ({ ...current, [field]: value }));
  };

  const handleSubmit = async () => {
    setIsSaving(true);
    setFieldErrors({});

    try {
      await onSubmit({ ...form, description: form.description || null });
      onClose();
    } catch (error) {
      setFieldErrors(
        Object.keys(error.fieldErrors ?? {}).length > 0
          ? error.fieldErrors
          : { code: error.message },
      );
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div
      className="dialogBackdrop"
      role="dialog"
      aria-modal="true"
      aria-labelledby="projectDialogTitle"
    >
      <div className="dialog">
        <h3 id="projectDialogTitle" className="dialog__title">
          Nuevo proyecto
        </h3>

        <div className="dialog__form">
          <div>
            <label className="dialog__label" htmlFor="projectCode">
              Código
            </label>
            <input
              id="projectCode"
              className="field"
              style={{ width: '100%', fontFamily: 'var(--font-mono)' }}
              value={form.code}
              onChange={updateField('code')}
              placeholder="TF-CORE"
            />
            {fieldErrors.code && <p className="notice__text">{fieldErrors.code}</p>}
          </div>

          <div>
            <label className="dialog__label" htmlFor="projectName">
              Nombre
            </label>
            <input
              id="projectName"
              className="field"
              style={{ width: '100%' }}
              value={form.name}
              onChange={updateField('name')}
              placeholder="Plataforma TaskFlow"
            />
            {fieldErrors.name && <p className="notice__text">{fieldErrors.name}</p>}
          </div>

          <div>
            <label className="dialog__label" htmlFor="projectDescription">
              Descripción
            </label>
            <textarea
              id="projectDescription"
              className="field"
              style={{ width: '100%', minHeight: '70px' }}
              value={form.description}
              onChange={updateField('description')}
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
              disabled={isSaving || form.code.trim() === '' || form.name.trim() === ''}
            >
              {isSaving ? 'Guardando…' : 'Crear proyecto'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
