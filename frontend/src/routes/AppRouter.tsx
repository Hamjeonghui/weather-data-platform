import { Navigate, Route, Routes } from 'react-router-dom'
import { useBootstrapAuth } from '../features/auth/hooks/useBootstrapAuth'
import { LoginPage } from '../features/auth/components/LoginPage'
import { HomePage } from '../pages/HomePage'
import { AdminPage } from '../pages/AdminPage'
import { UnauthorizedPage } from '../pages/UnauthorizedPage'
import { ProtectedRoute } from './ProtectedRoute'

export function AppRouter() {
  const { isBootstrapping } = useBootstrapAuth()

  if (isBootstrapping) {
    return <div className="app-loading">로딩 중...</div>
  }

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <HomePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin"
        element={
          <ProtectedRoute requiredRole="ADMIN">
            <AdminPage />
          </ProtectedRoute>
        }
      />
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
