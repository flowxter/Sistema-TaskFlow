import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

/**
 * El proxy redirige /api al backend en desarrollo. Asi el codigo del cliente
 * usa rutas relativas y no necesita conocer el host del servidor, que cambia
 * entre entornos.
 */
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
