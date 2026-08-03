import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * El proxy redirige al backend en desarrollo. Asi el codigo del cliente usa
 * rutas relativas y no necesita conocer el host del servidor, que cambia entre
 * entornos. En produccion el equivalente son los rewrites de vercel.json, de
 * modo que las mismas rutas funcionan en ambos lados.
 */
const backend = {
  target: 'http://localhost:8080',
  changeOrigin: true,
};

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': backend,
      // La documentacion se sirve desde el backend, no desde Vite.
      '/swagger-ui': backend,
      '/v3/api-docs': backend,
    },
  },
});
