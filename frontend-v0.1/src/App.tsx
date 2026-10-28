import MainLayout from './MainLayout'
import LoginPage from './pages/LoginPage'
import SpotDetailPage from './pages/SpotDetailPage'

export default function App() {
  if (window.authPage) {
    return <LoginPage />
  }
  if (window.spotDetail) {
    return <SpotDetailPage spot={window.spotDetail} />
  }
  return <MainLayout />
}
