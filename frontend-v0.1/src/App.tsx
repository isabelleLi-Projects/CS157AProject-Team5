import { useState } from 'react'
import MainLayout from './MainLayout'
import LoginPage from './pages/LoginPage'
import type { SessionUser } from './api/auth'

export default function App() {
  const [user, setUser] = useState<SessionUser | null>(null)

  if (!user) {
    return <LoginPage onAuthenticated={setUser} />
  }

  return <MainLayout />
}