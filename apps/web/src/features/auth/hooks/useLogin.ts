import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { loginUser } from '../api/auth-api'
import { useAuthStore } from '../store/authStore'
import type { LoginFormData } from '../schemas/loginSchema'

export function useLogin() {
  const setToken = useAuthStore((s) => s.setToken)
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (data: LoginFormData) => loginUser(data),
    onSuccess: ({ accessToken }) => {
      setToken(accessToken)
      navigate('/')
    },
  })
}
