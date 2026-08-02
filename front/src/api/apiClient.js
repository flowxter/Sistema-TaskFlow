/**
 * Cliente HTTP compartido por toda la aplicación.
 *
 * Centralizar el manejo de errores aquí evita repetir la misma lógica en cada
 * llamada y garantiza que el resto del código reciba siempre un `Error` con un
 * mensaje legible, venga del servidor o de la red.
 */

/**
 * En desarrollo la variable no se define y la ruta queda relativa, de modo que
 * el proxy de Vite la reenvia al backend local. En produccion se inyecta la URL
 * del servidor en tiempo de build (Vite sustituye import.meta.env al compilar).
 */
const API_BASE_URL = `${import.meta.env.VITE_API_URL ?? ''}/api/v1`;

/** Error de negocio devuelto por la API, con su código HTTP asociado. */
export class ApiError extends Error {
  constructor(message, status, fieldErrors) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors ?? {};
  }
}

/**
 * Ejecuta una petición y normaliza la respuesta.
 *
 * @param {string} path ruta relativa al prefijo de la API
 * @param {RequestInit} options opciones nativas de fetch
 * @returns {Promise<unknown>} cuerpo ya deserializado, o null en un 204
 */
async function request(path, options = {}) {
  let response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json', ...options.headers },
      ...options,
    });
  } catch {
    // fetch solo rechaza ante fallos de red, no ante códigos 4xx o 5xx.
    throw new ApiError('No se pudo conectar con el servidor. Verifica que la API esté corriendo.', 0);
  }

  if (response.status === 204) {
    return null;
  }

  const body = await response.json().catch(() => null);

  if (!response.ok) {
    throw new ApiError(
      body?.message ?? `La solicitud falló con estado ${response.status}`,
      response.status,
      body?.fieldErrors,
    );
  }

  return body;
}

/** Construye una query string omitiendo los parámetros vacíos. */
export function buildQuery(params) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      query.append(key, value);
    }
  });

  const queryString = query.toString();
  return queryString ? `?${queryString}` : '';
}

export const apiClient = {
  get: (path) => request(path),
  post: (path, body) => request(path, { method: 'POST', body: JSON.stringify(body) }),
  put: (path, body) => request(path, { method: 'PUT', body: JSON.stringify(body) }),
  patch: (path, body) => request(path, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: (path) => request(path, { method: 'DELETE' }),
};
