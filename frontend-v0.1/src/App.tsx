import MainLayout from './MainLayout'
import LoginPage from './pages/LoginPage'
import SpotDetailPage from './pages/SpotDetailPage'
import AdminDashboard from './pages/AdminDashboard'
import StudentHistoryPage from './pages/StudentHistoryPage'

export default function App() {
  if (window.authPage) {
    return <LoginPage />
  }
  // My history page (history.jsp)
  if (window.studentHistory) {
    return <StudentHistoryPage history={window.studentHistory} />
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
