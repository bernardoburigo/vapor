import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

// A porta 5173 é uma das origens liberadas no CORS do backend (app.cors.allowed-origins);
// strictPort evita subir em outra porta, que o backend bloquearia.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: { port: 5173, strictPort: true },
  test: {
    environment: 'node',
    include: ['src/**/*.test.ts'],
  },
})
