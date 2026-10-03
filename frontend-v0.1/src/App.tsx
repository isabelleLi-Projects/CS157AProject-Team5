import MainLayout from './MainLayout'
import LoginPage from './pages/LoginPage'

export default function App() {
  if (window.authPage) {
    return <LoginPage />
  }
  return <MainLayout />
}
