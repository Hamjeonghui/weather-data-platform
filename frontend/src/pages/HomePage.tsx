import { useNavigate } from 'react-router-dom'
import { useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../features/auth/store/authStore'

export function HomePage() {
  const user = useAuthStore((state) => state.user)
  const clearAuth = useAuthStore((state) => state.clearAuth)
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  function handleLogout() {
    clearAuth()
    queryClient.clear()
    navigate('/login', { replace: true })
  }

  return (
    <section>
      <h1>Weather Data Platform</h1>
      <p>
        {user?.loginId}님으로 로그인되었습니다. (권한: {user?.role})
      </p>
      <button type="button" onClick={handleLogout}>
        로그아웃
      </button>
    </section>
  )
}
