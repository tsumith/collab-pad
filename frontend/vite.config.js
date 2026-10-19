import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  const clerkKey = env.CLERK_PUBLISHABLE_KEY || env.VITE_CLERK_PUBLISHABLE_KEY || process.env.CLERK_PUBLISHABLE_KEY || process.env.VITE_CLERK_PUBLISHABLE_KEY || ''
  const apiUrl = env.API_GATEWAY_URL || env.VITE_API_GATEWAY_URL || process.env.API_GATEWAY_URL || process.env.VITE_API_GATEWAY_URL || ''

  return {
    plugins: [react()],
    server: {
      port: 3000,
    },
    define: {
      'import.meta.env.CLERK_PUBLISHABLE_KEY': JSON.stringify(clerkKey),
      'import.meta.env.API_GATEWAY_URL': JSON.stringify(apiUrl),
    }
  }
})
