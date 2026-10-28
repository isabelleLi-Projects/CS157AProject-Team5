import { defineConfig } from 'vite'

import react from '@vitejs/plugin-react'

import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  base: '/SJSUStudyFinder/',

  plugins: [
    react(),
    tailwindcss(),
  ],

  server: {
    proxy: {
      '/SJSUStudyFinder/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})