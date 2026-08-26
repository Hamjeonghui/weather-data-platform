import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../features/auth/store/authStore'
import { HomePage } from '../pages/HomePage'

/**
 * ADMIN은 수집 대시보드("/admin")를 기본 화면으로 사용한다.
 * 사용자용 조회 API가 아직 없어 그 외 역할은 HomePage를 그대로 보여준다.
 */
export function RoleBasedHome() {
  const role = useAuthStore((state) => state.user?.role)

  if (role === 'ADMIN') {
    return <Navigate to="/admin" replace />
  }

  return <HomePage />
}
