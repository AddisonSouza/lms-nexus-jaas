import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Loader2, UserPlus, XCircle } from 'lucide-react'
import { useAuthStore } from '@store/authStore'
import { getInvitationInfo } from '../api/invitation-api'
import { useAcceptInvitation } from '../hooks/useAcceptInvitation'

const roleLabels: Record<string, string> = {
  ADMIN_ORG: 'Administrador',
  GESTOR: 'Gestor',
  PROFESSOR: 'Professor',
  ALUNO: 'Aluno',
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
        <div className="w-full max-w-sm rounded-lg border p-8 shadow-sm text-center">
          <Loader2 className="mx-auto mb-4 h-10 w-10 animate-spin text-primary" />
          <p className="text-sm text-muted-foreground">Carregando convite...</p>
        </div>
      </div>
    )
  }

  if (isError || !invitation) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-background">
        <div className="w-full max-w-sm rounded-lg border p-8 shadow-sm text-center">
          <XCircle className="mx-auto mb-4 h-10 w-10 text-destructive" />
          <h1 className="text-xl font-semibold mb-2">Convite inválido ou expirado</h1>
          <p className="text-sm text-muted-foreground">
            Este convite não é mais válido. Peça ao administrador para enviar um novo.
          </p>
        </div>
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
      <div className="w-full max-w-sm rounded-lg border p-8 shadow-sm text-center space-y-4">
        <UserPlus className="mx-auto h-10 w-10 text-primary" />
        <h1 className="text-xl font-semibold">Você foi convidado!</h1>
        <p className="text-sm text-muted-foreground">
          Organização: <span className="font-medium text-foreground">{invitation.organizationName}</span>
        </p>
        <p className="text-sm text-muted-foreground">
          Papel: <span className="font-medium text-foreground">{roleLabels[invitation.role] ?? invitation.role}</span>
        </p>

        {acceptErrorMessage && (
          <p className="text-sm text-destructive">{acceptErrorMessage}</p>
        )}

        <button
          onClick={() => accept(token)}
          disabled={isAccepting}
          className="flex w-full items-center justify-center gap-2 rounded bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
        >
          {isAccepting && <Loader2 className="h-4 w-4 animate-spin" />}
          Aceitar Convite
        </button>
      </div>
    </div>
  )
}

export default AcceptInvitePage
