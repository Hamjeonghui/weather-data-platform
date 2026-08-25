import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { getMe } from '../api/authApi'
import { useAuthStore } from '../store/authStore'

/**
 * 새로고침 시 저장된 accessToken의 유효성을 /api/auth/me로 검증하고
 * 최신 사용자 정보(role 변경 등)로 스토어를 갱신한다.
 */
export function useBootstrapAuth() {
  const accessToken = useAuthStore((state) => state.accessToken)
  const setAuth = useAuthStore((state) => state.setAuth)
  const clearAuth = useAuthStore((state) => state.clearAuth)

  const { data, isError, isFetched } = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: getMe,
    enabled: Boolean(accessToken),
    retry: false,
    staleTime: Infinity,
  })

  useEffect(() => {
    if (data && accessToken) {
      setAuth(accessToken, data)
    }
  }, [data, accessToken, setAuth])

  useEffect(() => {
    if (isError) {
      clearAuth()
    }
  }, [isError, clearAuth])

  const isBootstrapping = Boolean(accessToken) && !isFetched

  return { isBootstrapping }
}
