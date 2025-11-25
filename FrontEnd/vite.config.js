import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://192.168.70.111:8083',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
