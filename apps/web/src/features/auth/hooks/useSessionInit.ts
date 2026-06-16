import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { refreshTokens } from '../api/auth-api'
import { useAuthStore } from '../store/authStore'

export function useSessionInit() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const setToken = useAuthStore((s) => s.setToken)
  const clearToken = useAuthStore((s) => s.clearToken)
  const navigate = useNavigate()

  const { isError } = useQuery({
    queryKey: ['session', 'init'],
    queryFn: async () => {
      const { accessToken } = await refreshTokens()
      setToken(accessToken)
      return accessToken
    },
    enabled: !isAuthenticated,
    retry: false,
    staleTime: Infinity,
  })

  useEffect(() => {
    if (isError) {
      clearToken()
      navigate('/login', { replace: true })
    }
  }, [isError, clearToken, navigate])
}
