import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { refreshTokens } from '../api/auth-api'
import { useAuthStore } from '../store/authStore'

export function useSessionInit() {
  const { isAuthenticated, setToken, clearToken } = useAuthStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (isAuthenticated) return

    refreshTokens()
      .then(({ accessToken }) => setToken(accessToken))
      .catch(() => {
        clearToken()
        navigate('/login', { replace: true })
      })
  }, []) // eslint-disable-line react-hooks/exhaustive-deps
}
