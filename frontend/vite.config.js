import basicSsl from '@vitejs/plugin-basic-ssl'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), basicSsl()],
  server: {
    // Serve the dev server over HTTPS so the page origin is https://localhost:5173.
    // This ensures bearer tokens are never sent over plain HTTP even in development
    // and satisfies the client.js HTTPS guard for the /api proxy fallback.
    // Vite's built-in `https: true` no longer produces a usable certificate, so the
    // basic-ssl plugin supplies a self-signed one; accept the browser trust prompt
    // on first visit. The proxy target uses plain HTTP because the connection is
    // loopback-only between Vite and the local Spring backend.
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        credentials: true,
      },
    },
  },
})
