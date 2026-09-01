import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/use-auth'
import Navbar from './Navbar'

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return (
    <div className="shell">
      <Navbar />
      <main className="page">
        <div className="container">
          <Outlet />
        </div>
      </main>
    </div>
  )
}

export function GuestRoute() {
  const { isAuthenticated } = useAuth()

  if (isAuthenticated) {
    return <Navigate to="/contacts" replace />
  }

  return <Outlet />
}