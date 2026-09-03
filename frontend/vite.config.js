import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Serve the dev server over HTTPS so the page origin is https://localhost:5173.
    // This ensures bearer tokens are never sent over plain HTTP even in development
    // and satisfies the client.js HTTPS guard for the /api proxy fallback.
    // Vite uses a built-in self-signed certificate; accept the browser trust prompt
    // on first visit. The proxy target uses plain HTTP because the connection is
    // loopback-only between Vite and the local Spring backend.
    https: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        credentials: true,
      },
    },
  },
})
