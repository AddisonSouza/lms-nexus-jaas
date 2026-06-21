import { useEffect, useRef } from 'react'
import axios from 'axios'
import { RouterProvider } from 'react-router-dom'
import { API_BASE_URL } from '@lib/axios'
import { useAuthStore } from '@store/authStore'
import { router } from './routes'

/**
 * On a fresh page load the access token (held only in memory) is gone, so we try a
 * silent refresh using the httpOnly refresh cookie before the route guards run.
 * Guards stay in a loading state while `isBootstrapping` is true.
 */
function AuthBootstrap() {
  const ran = useRef(false)

  useEffect(() => {
    if (ran.current) return
    ran.current = true

    axios
      .post<{ accessToken: string }>(`${API_BASE_URL}/auth/refresh`, {}, { withCredentials: true })
      .then(({ data }) => useAuthStore.getState().setToken(data.accessToken))
      .catch(() => useAuthStore.getState().clearToken())
  }, [])

  return <RouterProvider router={router} />
}

export default AuthBootstrap
