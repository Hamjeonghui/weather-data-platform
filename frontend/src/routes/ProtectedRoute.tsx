import type { ReactElement } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '../features/auth/store/authStore'
import type { Role } from '../types/auth'

interface ProtectedRouteProps {
  children: ReactElement
  requiredRole?: Role
}

export function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const accessToken = useAuthStore((state) => state.accessToken)
  const user = useAuthStore((state) => state.user)
  const location = useLocation()

  if (!accessToken || !user) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }

  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/unauthorized" replace />
  }

  return children
}
