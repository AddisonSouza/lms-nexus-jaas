import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { acceptInvitation } from '../api/invitation-api'

export function useAcceptInvitation(organizationId: string) {
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (token: string) => acceptInvitation(token),
    onSuccess: () => {
      navigate(`/organizations/${organizationId}`)
    },
  })
}
