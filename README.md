# Java API REST — TaskFlow

API REST de gestión de proyectos y tareas construida con **Spring Boot 3 + Java 21**, con un
cliente **React** que la consume. El repositorio separa backend y frontend en carpetas
independientes para que cada uno se despliegue por su cuenta.

```
Java-API-REST/
├── back/     API REST — Spring Boot 3, Java 21, JPA, H2 / PostgreSQL
└── front/    Cliente web — React 18 + Vite
```

---

## Qué demuestra este proyecto

No es un CRUD generado. Cada decisión está tomada a propósito y documentada en el código:

| Aspecto | Cómo se resolvió |
|---|---|
| **Arquitectura en capas** | `controller → service → repository`. El controlador no contiene reglas de negocio y el repositorio no conoce el dominio. |
| **Máquina de estados** | Las transiciones válidas de una tarea viven en el enum `TaskStatus` (patrón State), no dispersas en condicionales. |
| **Filtros dinámicos** | Patrón Specification con JPA. Los criterios se encadenan sin escribir un método por combinación de parámetros. |
| **Contrato estable** | DTOs como `record` inmutables y paginación propia: la API no expone entidades ni clases internas de Spring. |
| **Errores coherentes** | `@RestControllerAdvice` traduce cada excepción de dominio a un cuerpo de error único. Ningún controlador tiene `try/catch`. |
| **Invariantes del dominio** | La entidad `Project` protege su propio estado: la relación bidireccional solo se modifica por `addTask()`. |
| **Pruebas** | Unitarias con Mockito para la lógica y de integración con MockMvc para el camino HTTP completo. |
| **Documentación viva** | OpenAPI generado desde el código: no se puede desactualizar. |

---

## Cómo ejecutarlo

### Backend

Requiere **JDK 21** (con `JAVA_HOME` apuntando a él). No necesita Maven ni base de datos
instalados: el wrapper descarga Maven la primera vez y la API arranca con H2 en memoria.

```bash
cd back
./mvnw spring-boot:run     # Windows: .\mvnw.cmd spring-boot:run
```

| Recurso | URL |
|---|---|
| API | http://localhost:8080/api/v1 |
| Documentación interactiva | http://localhost:8080/swagger-ui.html |
| Consola de la base de datos | http://localhost:8080/h2-console |

Al arrancar se cargan cuatro proyectos y quince tareas de ejemplo (`data.sql`), incluyendo casos
límite: tareas vencidas, bloqueadas y un proyecto archivado.

**Con PostgreSQL:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

### Frontend

Requiere **Node 18+**. Con el backend corriendo:

```bash
cd front
npm install
npm run dev
```

Disponible en http://localhost:5173. Vite redirige `/api` al puerto 8080, así que el cliente usa
rutas relativas y no depende del host del servidor.

### Pruebas

```bash
cd back
./mvnw test
```

---

## Endpoints

### Proyectos

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/projects` | Lista paginada, filtrable por estado y texto |
| `GET` | `/api/v1/projects/{id}` | Detalle de un proyecto |
| `GET` | `/api/v1/projects/{id}/metrics` | Indicadores de avance |
| `POST` | `/api/v1/projects` | Crea un proyecto |
| `PUT` | `/api/v1/projects/{id}` | Actualiza nombre y descripción |
| `PATCH` | `/api/v1/projects/{id}/archive` | Archiva si no hay tareas abiertas |
| `PATCH` | `/api/v1/projects/{id}/reactivate` | Reactiva un proyecto archivado |
| `DELETE` | `/api/v1/projects/{id}` | Elimina el proyecto y sus tareas |

### Tareas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/v1/projects/{projectId}/tasks` | Lista filtrable por estado, prioridad, responsable, texto y vencimiento |
| `POST` | `/api/v1/projects/{projectId}/tasks` | Crea una tarea |
| `GET` | `/api/v1/tasks/{id}` | Detalle de una tarea |
| `PUT` | `/api/v1/tasks/{id}` | Actualiza los datos, sin incluir el estado |
| `PATCH` | `/api/v1/tasks/{id}/status` | Aplica una transición validada |
| `DELETE` | `/api/v1/tasks/{id}` | Elimina la tarea |

---

## Reglas de negocio

Son la parte que distingue este proyecto de un CRUD. Todas se validan en la capa de servicio,
no en el controlador, de modo que se cumplen sin importar por dónde llegue la petición.

**Transiciones de estado.** Una tarea no puede saltar de `BACKLOG` a `DONE`: debe pasar por
desarrollo y revisión. El error 409 informa cuáles son los estados permitidos desde el actual.

```
BACKLOG → IN_PROGRESS → IN_REVIEW → DONE
              ↓              ↓
              └──→ BLOCKED ←─┘
```

`DONE` es un estado final: no admite salidas.

**Otras reglas:**

- Un proyecto con tareas sin finalizar no se puede archivar.
- Un proyecto archivado no admite crear, editar ni eliminar tareas.
- El código del proyecto es inmutable: identifica al proyecto ante sistemas externos.
- La fecha de cierre se sella automáticamente al llegar a `DONE` y se limpia si la tarea se reabre.

---

## Ejemplos

**Crear un proyecto**

```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Content-Type: application/json" \
  -d '{"code":"TF-API","name":"Servicios de integración","description":"Conectores con sistemas externos"}'
```

**Transición inválida — respuesta 409**

```bash
curl -X PATCH http://localhost:8080/api/v1/tasks/5/status \
  -H "Content-Type: application/json" \
  -d '{"status":"DONE"}'
```

```json
{
  "timestamp": "2026-08-02T14:22:31.442Z",
  "status": 409,
  "error": "Conflict",
  "message": "Transicion invalida de BACKLOG a DONE. Estados permitidos desde BACKLOG: IN_PROGRESS, BLOCKED",
  "path": "/api/v1/tasks/5/status"
}
```

**Filtros combinados**

```bash
curl "http://localhost:8080/api/v1/projects/1/tasks?status=IN_PROGRESS&priority=HIGH&overdue=true"
```

---

## Convenciones de código

- **Nombres:** `PascalCase` para clases, `camelCase` para métodos y variables, `UPPER_SNAKE_CASE`
  para constantes. Sin abreviaturas: `projectRepository`, no `projRepo`.
- **Inyección por constructor**, nunca por campo. Deja las dependencias `final` y permite
  instanciar cualquier servicio en una prueba sin levantar Spring.
- **Javadoc en lo que no es obvio.** Se documenta el *porqué* de una decisión, no lo que el código
  ya dice por sí solo.
- **Transacciones de solo lectura por defecto**, con `@Transactional` explícito en cada escritura.
- **Sin generadores de código en el mapeo.** Los mappers están escritos a mano para que la
  conversión sea visible y depurable.

---

## Despliegue

El backend corre en **Railway** con PostgreSQL y el frontend en **Vercel**. Ambos leen del mismo
repositorio, cada uno apuntando a su carpeta.

### Backend en Railway

El servicio se construye con el [`Dockerfile`](back/Dockerfile) del módulo: build multietapa que
deja una imagen final con solo el JRE y el jar, ejecutada por un usuario sin privilegios.

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `DB_USER` | `${{Postgres.PGUSER}}` |
| `DB_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `SPRING_PROFILES_ACTIVE` | `postgres` |
| `TASKFLOW_CORS_ALLOWED_ORIGINS` | URL del frontend en Vercel |

Railway expone la conexión a Postgres como `DATABASE_URL`, que **no** es una URL JDBC válida
(`postgresql://…` en vez de `jdbc:postgresql://…`). Por eso la URL se compone a partir de las
variables `PG*` mediante referencias entre servicios.

El puerto no se configura: Railway lo inyecta en `PORT` y `application.yml` lo lee de ahí.

### Frontend en Vercel

Con *Root Directory* en `front`. Vercel detecta Vite y no necesita más configuración salvo:

| Variable | Valor |
|---|---|
| `VITE_API_URL` | URL pública del backend en Railway, sin barra final |

Vite sustituye `import.meta.env` **en tiempo de build**, así que un cambio de esta variable exige
volver a desplegar: no basta con guardarla.

### Sobre el esquema y los datos

El perfil `postgres` usa `ddl-auto: update` y carga
[`data-postgres.sql`](back/src/main/resources/data-postgres.sql), que es idempotente: la base
persiste entre despliegues y el script se ejecuta en cada arranque, así que los inserts usan
`ON CONFLICT DO NOTHING` y una guarda sobre tabla vacía. El seed de desarrollo
([`data.sql`](back/src/main/resources/data.sql)) es distinto porque usa `DATEADD`, exclusivo de H2.

---

## Siguientes pasos

Lo que construiría a continuación, en orden de valor:

1. Autenticación con JWT y autorización por rol.
2. Migraciones con Flyway en lugar de `ddl-auto`, para versionar el esquema en producción.
3. `docker-compose` que levante API y PostgreSQL juntos para desarrollo local.
4. Pipeline de CI en GitHub Actions con build, pruebas y reporte de cobertura.
5. Caché de segundo nivel para las consultas de métricas.

---

**Juan José Mafla Pacheco** · Cali, Colombia
[LinkedIn](https://www.linkedin.com/in/juan-jose-mafla-pacheco-937a7b24b/) · juan.mafla@correounivalle.edu.co
