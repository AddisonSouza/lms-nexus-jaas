import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { registerUser } from '../api/auth-api'
import { rateLimitMessageFor } from '../schemas/rateLimitSchema'
import type { RegisterFormData } from '../schemas/registerSchema'

export function useRegister() {
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (data: RegisterFormData) => registerUser(data),
    onSuccess: () => {
      navigate('/confirm-email')
    },
  })
}

export function getRegisterError(error: unknown): string | null {
  if (!axios.isAxiosError(error)) return null

  // O bloqueio por tentativas alcança o cadastro junto com o resto de `/auth`,
  // e "erro ao criar conta" não diria ao usuário que basta esperar.
  const rateLimited = rateLimitMessageFor(error)
  if (rateLimited) return rateLimited

  const status = error.response?.status
  const data = error.response?.data

  if (status === 409) return data?.error ?? 'E-mail já em uso'
  if (status === 422) {
    const errors: string[] = data?.errors ?? []
    return errors.length > 0 ? errors.join(', ') : data?.error ?? 'Dados inválidos'
  }

  return 'Erro ao criar conta. Tente novamente.'
}
