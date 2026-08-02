-- Datos de arranque para desarrollo y demostracion.
-- Solo se ejecutan con el perfil por defecto (H2 en memoria).

INSERT INTO projects (code, name, description, status, created_at, updated_at) VALUES
('TF-CORE',  'Plataforma TaskFlow',      'Nucleo de la plataforma: API REST, autenticacion y modelo de datos.', 'ACTIVE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TF-WEB',   'Cliente web',              'Interfaz React que consume la API de TaskFlow.',                      'ACTIVE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TF-DATA',  'Analitica de proyectos',   'Reportes de productividad y tableros de indicadores.',                'ACTIVE',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('TF-LEGACY','Migracion sistema previo', 'Traslado de datos del sistema anterior. Cerrado en 2025.',            'ARCHIVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tasks (title, description, status, priority, assignee, due_date, completed_at, project_id, created_at, updated_at) VALUES
('Modelar entidades del dominio',      'Definir Project y Task con sus relaciones e indices.',            'DONE',        'HIGH',     'Juan Mafla',   DATEADD('DAY', -12, CURRENT_DATE), CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Implementar maquina de estados',     'Validar transiciones permitidas entre estados de tarea.',         'DONE',        'CRITICAL', 'Juan Mafla',   DATEADD('DAY', -8, CURRENT_DATE),  CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Filtros dinamicos con Specification','Permitir combinar estado, prioridad y responsable.',              'IN_REVIEW',   'HIGH',     'Juan Mafla',   DATEADD('DAY',  2, CURRENT_DATE),  NULL,              1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Manejo global de errores',           'Respuesta unica de error para toda la API.',                      'IN_PROGRESS', 'MEDIUM',   'Juan Mafla',   DATEADD('DAY',  5, CURRENT_DATE),  NULL,              1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Autenticacion con JWT',              'Proteger los endpoints de escritura.',                            'BACKLOG',     'CRITICAL', NULL,           DATEADD('DAY', 14, CURRENT_DATE),  NULL,              1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Definir cache de segundo nivel',     'Evaluar Hibernate cache para consultas de solo lectura.',         'BLOCKED',     'LOW',      NULL,           DATEADD('DAY', -3, CURRENT_DATE),  NULL,              1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Tablero de tareas',                  'Vista principal con columnas por estado.',                        'IN_PROGRESS', 'HIGH',     'Juan Mafla',   DATEADD('DAY',  4, CURRENT_DATE),  NULL,              2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Formulario de proyecto',             'Alta y edicion con validacion en cliente.',                       'DONE',        'MEDIUM',   'Juan Mafla',   DATEADD('DAY', -5, CURRENT_DATE),  CURRENT_TIMESTAMP, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Estados vacios y de error',          'Mensajes claros cuando no hay datos o falla la API.',             'BACKLOG',     'MEDIUM',   NULL,           DATEADD('DAY',  9, CURRENT_DATE),  NULL,              2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Accesibilidad por teclado',          'Foco visible y navegacion completa sin raton.',                   'BACKLOG',     'LOW',      NULL,           DATEADD('DAY', 20, CURRENT_DATE),  NULL,              2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Consulta agregada de indicadores',   'Conteo por estado en una sola consulta SQL.',                     'DONE',        'HIGH',     'Juan Mafla',   DATEADD('DAY', -2, CURRENT_DATE),  CURRENT_TIMESTAMP, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Exportar reporte a CSV',             'Descarga del historico de tareas cerradas.',                      'IN_PROGRESS', 'MEDIUM',   'Juan Mafla',   DATEADD('DAY', -1, CURRENT_DATE),  NULL,              3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Grafico de avance por sprint',       'Serie temporal de tareas completadas.',                           'BACKLOG',     'LOW',      NULL,           DATEADD('DAY', 25, CURRENT_DATE),  NULL,              3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Migrar tabla de usuarios',           'Traslado completo desde el sistema anterior.',                    'DONE',        'CRITICAL', 'Juan Mafla',   DATEADD('DAY', -60, CURRENT_DATE), CURRENT_TIMESTAMP, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Validar integridad de datos',        'Comparacion origen y destino registro por registro.',             'DONE',        'HIGH',     'Juan Mafla',   DATEADD('DAY', -55, CURRENT_DATE), CURRENT_TIMESTAMP, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
