import { useMutation } from '@tanstack/react-query'
import { login } from '../api/authApi'
import { useAuthStore } from '../store/authStore'
import type { LoginRequest } from '../../../types/auth'

export function useLogin() {
  const setAuth = useAuthStore((state) => state.setAuth)

  return useMutation({
    mutationFn: (request: LoginRequest) => login(request),
    onSuccess: (data) => {
      setAuth(data.accessToken, data.user)
    },
  })
}
