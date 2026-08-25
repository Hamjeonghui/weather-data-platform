import { useState } from 'react'
import type { FormEvent } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useLogin } from '../hooks/useLogin'
import { useAuthStore } from '../store/authStore'
import { getApiErrorMessage } from '../../../lib/apiClient'

export function LoginPage() {
  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')

  const isAuthenticated = useAuthStore((state) => Boolean(state.accessToken))
  const navigate = useNavigate()
  const location = useLocation()
  const loginMutation = useLogin()

  if (isAuthenticated) {
    const redirectTo = (location.state as { from?: string } | null)?.from ?? '/'
    return <Navigate to={redirectTo} replace />
  }

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    loginMutation.mutate(
      { loginId, password },
      {
        onSuccess: () => {
          const redirectTo = (location.state as { from?: string } | null)?.from ?? '/'
          navigate(redirectTo, { replace: true })
        },
      },
    )
  }

  return (
    <section className="login-page">
      <form className="login-form" onSubmit={handleSubmit}>
        <h1>로그인</h1>

        <label htmlFor="loginId">로그인 아이디</label>
        <input
          id="loginId"
          name="loginId"
          type="text"
          autoComplete="username"
          value={loginId}
          onChange={(event) => setLoginId(event.target.value)}
          required
        />

        <label htmlFor="password">비밀번호</label>
        <input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />

        {loginMutation.isError && (
          <p className="login-error" role="alert">
            {getApiErrorMessage(loginMutation.error)}
          </p>
        )}

        <button type="submit" disabled={loginMutation.isPending}>
          {loginMutation.isPending ? '로그인 중...' : '로그인'}
        </button>
      </form>
    </section>
  )
}
