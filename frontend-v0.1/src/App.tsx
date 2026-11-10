import MainLayout from './MainLayout'
import LoginPage from './pages/LoginPage'
import SpotDetailPage from './pages/SpotDetailPage'
import AdminDashboard from './pages/AdminDashboard'

export default function App() {
  if (window.authPage) {
    return <LoginPage />
  }
  if (window.spotDetail) {
    return <SpotDetailPage spot={window.spotDetail} />
  }
  // displays the admin dashboard when the user role is "admin"
  if (window.currentUser?.role?.toLowerCase() === 'admin') {
    return <AdminDashboard />
  }
  return <MainLayout />
}
