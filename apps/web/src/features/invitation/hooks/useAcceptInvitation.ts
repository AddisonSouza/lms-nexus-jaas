import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { acceptInvitation } from '../api/invitation-api'
import { switchOrganization } from '@lib/session'
import { useAuthStore } from '@store/authStore'

export function useAcceptInvitation(organizationId: string) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const setToken = useAuthStore((s) => s.setToken)

  return useMutation({
    mutationFn: (token: string) => acceptInvitation(token),
    onSuccess: async () => {
      // Aceitar um convite é entrar numa organização nova: sem trocar o token, o
      // JWT continua apontando para a organização anterior (ou nenhuma) e a tela
      // de destino responde 403. Mesmo passo de useCreateOrganization.
      setToken(await switchOrganization(organizationId))
      queryClient.clear()
      navigate(`/organizations/${organizationId}`)
    },
  })
}
