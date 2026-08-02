-- Datos de arranque para el perfil "postgres".
--
-- A diferencia de data.sql (H2, base efimera que se recrea en cada arranque),
-- aqui la base persiste entre despliegues y el script vuelve a ejecutarse cada
-- vez. Por eso todo insert es idempotente: repetirlo no duplica ni falla.
--
-- Las fechas usan INTERVAL en lugar de DATEADD, que solo existe en H2.

INSERT INTO projects (code, name, description, status, created_at, updated_at) VALUES
('TF-CORE',  'Plataforma TaskFlow',      'Nucleo de la plataforma: API REST, autenticacion y modelo de datos.', 'ACTIVE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TF-WEB',   'Cliente web',              'Interfaz React que consume la API de TaskFlow.',                      'ACTIVE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TF-DATA',  'Analitica de proyectos',   'Reportes de productividad y tableros de indicadores.',                'ACTIVE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TF-LEGACY','Migracion sistema previo', 'Traslado de datos del sistema anterior. Cerrado en 2025.',            'ARCHIVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- Las tareas no tienen clave natural, asi que la guarda es la tabla vacia.
-- El project_id se resuelve por codigo: no depender de los ids generados deja
-- el script a salvo de que la secuencia arranque en otro numero.
INSERT INTO tasks (title, description, status, priority, assignee, due_date, completed_at, project_id, created_at, updated_at)
-- due_date se castea a date: CURRENT_DATE +/- INTERVAL devuelve timestamp.
SELECT v.title, v.description, v.status, v.priority, v.assignee, v.due_date::date, v.completed_at, p.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (VALUES
    ('Modelar entidades del dominio',       'Definir Project y Task con sus relaciones e indices.',    'DONE',        'HIGH',     'Juan Mafla', CURRENT_DATE - INTERVAL '12 days', CURRENT_TIMESTAMP, 'TF-CORE'),
    ('Implementar maquina de estados',      'Validar transiciones permitidas entre estados de tarea.', 'DONE',        'CRITICAL', 'Juan Mafla', CURRENT_DATE - INTERVAL  '8 days', CURRENT_TIMESTAMP, 'TF-CORE'),
    ('Filtros dinamicos con Specification', 'Permitir combinar estado, prioridad y responsable.',      'IN_REVIEW',   'HIGH',     'Juan Mafla', CURRENT_DATE + INTERVAL  '2 days', NULL,              'TF-CORE'),
    ('Manejo global de errores',            'Respuesta unica de error para toda la API.',              'IN_PROGRESS', 'MEDIUM',   'Juan Mafla', CURRENT_DATE + INTERVAL  '5 days', NULL,              'TF-CORE'),
    ('Autenticacion con JWT',               'Proteger los endpoints de escritura.',                    'BACKLOG',     'CRITICAL', NULL,         CURRENT_DATE + INTERVAL '14 days', NULL,              'TF-CORE'),
    ('Definir cache de segundo nivel',      'Evaluar Hibernate cache para consultas de solo lectura.', 'BLOCKED',     'LOW',      NULL,         CURRENT_DATE - INTERVAL  '3 days', NULL,              'TF-CORE'),

    ('Tablero de tareas',                   'Vista principal con columnas por estado.',                'IN_PROGRESS', 'HIGH',     'Juan Mafla', CURRENT_DATE + INTERVAL  '4 days', NULL,              'TF-WEB'),
    ('Formulario de proyecto',              'Alta y edicion con validacion en cliente.',               'DONE',        'MEDIUM',   'Juan Mafla', CURRENT_DATE - INTERVAL  '5 days', CURRENT_TIMESTAMP, 'TF-WEB'),
    ('Estados vacios y de error',           'Mensajes claros cuando no hay datos o falla la API.',     'BACKLOG',     'MEDIUM',   NULL,         CURRENT_DATE + INTERVAL  '9 days', NULL,              'TF-WEB'),
    ('Accesibilidad por teclado',           'Foco visible y navegacion completa sin raton.',           'BACKLOG',     'LOW',      NULL,         CURRENT_DATE + INTERVAL '20 days', NULL,              'TF-WEB'),

    ('Consulta agregada de indicadores',    'Conteo por estado en una sola consulta SQL.',             'DONE',        'HIGH',     'Juan Mafla', CURRENT_DATE - INTERVAL  '2 days', CURRENT_TIMESTAMP, 'TF-DATA'),
    ('Exportar reporte a CSV',              'Descarga del historico de tareas cerradas.',              'IN_PROGRESS', 'MEDIUM',   'Juan Mafla', CURRENT_DATE - INTERVAL  '1 days', NULL,              'TF-DATA'),
    ('Grafico de avance por sprint',        'Serie temporal de tareas completadas.',                   'BACKLOG',     'LOW',      NULL,         CURRENT_DATE + INTERVAL '25 days', NULL,              'TF-DATA'),

    ('Migrar tabla de usuarios',            'Traslado completo desde el sistema anterior.',            'DONE',        'CRITICAL', 'Juan Mafla', CURRENT_DATE - INTERVAL '60 days', CURRENT_TIMESTAMP, 'TF-LEGACY'),
    ('Validar integridad de datos',         'Comparacion origen y destino registro por registro.',     'DONE',        'HIGH',     'Juan Mafla', CURRENT_DATE - INTERVAL '55 days', CURRENT_TIMESTAMP, 'TF-LEGACY')
) AS v(title, description, status, priority, assignee, due_date, completed_at, project_code)
JOIN projects p ON p.code = v.project_code
WHERE NOT EXISTS (SELECT 1 FROM tasks);
