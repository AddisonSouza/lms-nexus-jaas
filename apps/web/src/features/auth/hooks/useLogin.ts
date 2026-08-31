import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { loginUser } from '../api/auth-api'
import { useAuthStore } from '@store/authStore'
import type { LoginFormData } from '../schemas/loginSchema'

/**
 * @param inviteToken convite que trouxe o usuário até aqui, se houver: depois de
 * entrar ele volta para o aceite em vez de cair na raiz.
 */
export function useLogin(inviteToken?: string | null) {
  const setToken = useAuthStore((s) => s.setToken)
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (data: LoginFormData) => loginUser(data),
    onSuccess: ({ accessToken }) => {
      setToken(accessToken)
      navigate(inviteToken ? `/invitations/${encodeURIComponent(inviteToken)}/accept` : '/')
    },
  })
}
