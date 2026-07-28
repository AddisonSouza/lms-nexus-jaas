import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Loader2, UserPlus, XCircle } from 'lucide-react'
import { useAuthStore } from '@store/authStore'
import { getInvitationInfo } from '../api/invitation-api'
import { useAcceptInvitation } from '../hooks/useAcceptInvitation'
import { Card, CardKicker } from '@components/ui/card'
import { Badge } from '@components/ui/badge'
import { Button } from '@components/ui/button'

const roleLabels: Record<string, string> = {
  ADMIN_ORG: 'Administrador',
  GESTOR: 'Gestor',
  PROFESSOR: 'Professor',
  ALUNO: 'Aluno',
}

function organizationInitials(name: string) {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0]?.toUpperCase())
    .join('')
}

function AcceptInvitePage() {
  const { token = '' } = useParams<{ token: string }>()
  const navigate = useNavigate()
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate(`/register?invite=${token}`, { replace: true })
    }
  }, [isAuthenticated, token, navigate])

  const { data: invitation, isPending: isLoadingInfo, isError } = useQuery({
    queryKey: ['invitation', token],
    queryFn: () => getInvitationInfo(token),
    enabled: !!token && isAuthenticated,
    retry: false,
  })

  const { mutate: accept, isPending: isAccepting, error: acceptError } = useAcceptInvitation(
    invitation?.organizationId ?? '',
  )

  if (!isAuthenticated) return null

  if (isLoadingInfo) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <Card elevation="md" className="w-full max-w-sm items-center p-8 text-center">
          <Loader2 className="h-10 w-10 animate-spin text-accent" />
          <p className="text-sm text-muted-foreground">Carregando convite...</p>
        </Card>
      </div>
    )
  }

  if (isError || !invitation) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <Card elevation="md" className="w-full max-w-sm items-center p-8 text-center">
          <div className="flex h-14 w-14 items-center justify-center rounded-full bg-accent-100 text-accent-800">
            <XCircle className="h-6 w-6" />
          </div>
          <h2 className="font-heading text-xl">Convite inválido ou expirado</h2>
          <p className="text-sm text-muted-foreground">
            Este convite não é mais válido. Peça ao administrador para enviar um novo.
          </p>
        </Card>
      </div>
    )
  }

  const acceptErrorStatus = (acceptError as { response?: { status?: number } })?.response?.status
  const acceptErrorMessage =
    acceptErrorStatus === 409
      ? 'Você já é membro desta organização.'
      : acceptErrorStatus === 410
        ? 'Este convite expirou.'
        : acceptError
          ? 'Erro ao aceitar convite. Tente novamente.'
          : null

  return (
    <div className="flex min-h-screen items-center justify-center bg-background">
      <Card elevation="md" className="w-full max-w-sm items-start p-6">
        <div className="flex h-14 w-14 items-center justify-center rounded-full bg-accent-2-100 font-heading text-lg text-accent-2-800">
          {organizationInitials(invitation.organizationName)}
        </div>
        <CardKicker className="flex items-center gap-1.5">
          <UserPlus className="h-3.5 w-3.5" />
          Convite de organização
        </CardKicker>
        <h2 className="font-heading text-xl">{invitation.organizationName} convidou você</h2>

        <div className="flex w-full flex-col gap-1.5 border-y border-border py-3 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Papel</span>
            <Badge variant="accent">{roleLabels[invitation.role] ?? invitation.role}</Badge>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Válido até</span>
            <span>{new Date(invitation.expiresAt).toLocaleDateString('pt-BR')}</span>
          </div>
        </div>

        {acceptErrorMessage && <p className="text-sm text-destructive">{acceptErrorMessage}</p>}

        <Button onClick={() => accept(token)} disabled={isAccepting} className="w-full">
          {isAccepting && <Loader2 className="h-4 w-4 animate-spin" />}
          Aceitar convite
        </Button>
      </Card>
    </div>
  )
}

export default AcceptInvitePage
