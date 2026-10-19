import { createRoot } from 'react-dom/client'
import { ClerkProvider } from '@clerk/react'
import App from './App.jsx'
import './index.css'

const PUBLISHABLE_KEY = import.meta.env.VITE_CLERK_PUBLISHABLE_KEY

const root = createRoot(document.getElementById('root'))

if (!PUBLISHABLE_KEY) {
  root.render(
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100vh', fontFamily: 'sans-serif', padding: '20px', textAlign: 'center', backgroundColor: '#0f172a', color: '#f8fafc' }}>
      <h2 style={{ color: '#ef4444', marginBottom: '12px' }}>Configuration Error</h2>
      <p style={{ maxWidth: '480px', color: '#94a3b8', lineHeight: '1.6' }}>
        <strong>VITE_CLERK_PUBLISHABLE_KEY</strong> is missing.
        <br />
        Please add it in your Vercel Project Settings &rarr; Environment Variables, then redeploy.
      </p>
    </div>
  )
} else {
  root.render(
    <ClerkProvider publishableKey={PUBLISHABLE_KEY}>
      <App />
    </ClerkProvider>
  )
}

