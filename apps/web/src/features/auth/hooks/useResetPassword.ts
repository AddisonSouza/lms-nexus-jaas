import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { resetPassword } from '../api/auth-api'

export function useResetPassword() {
  const navigate = useNavigate()

  return useMutation({
    mutationFn: ({ token, newPassword }: { token: string; newPassword: string }) =>
      resetPassword(token, newPassword),
    onSuccess: () => {
      navigate('/login')
    },
  })
}
