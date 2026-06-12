import { useMutation } from '@tanstack/react-query'
import { resendConfirmation } from '../api/auth-api'

export function useResendConfirmation() {
  return useMutation({
    mutationFn: (email: string) => resendConfirmation(email),
  })
}
