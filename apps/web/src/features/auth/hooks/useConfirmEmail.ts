import { useMutation } from '@tanstack/react-query'
import { confirmEmail } from '../api/auth-api'

export function useConfirmEmail() {
  return useMutation({
    mutationFn: (token: string) => confirmEmail(token),
  })
}
