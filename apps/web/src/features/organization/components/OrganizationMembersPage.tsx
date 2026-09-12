import { useState } from 'react'
import { Users, Trash2, UserPlus, CheckCircle2, Mail, Send, Ban } from 'lucide-react'
import { useParams } from 'react-router-dom'
import { useOrganizationMembers } from '../hooks/useOrganizationMembers'
import { useRemoveMember } from '../hooks/useRemoveMember'
import { useInviteMember } from '../hooks/useInviteMember'
import { useChangeMemberRole } from '../hooks/useChangeMemberRole'
import { useOrganizationInvitations } from '../hooks/useOrganizationInvitations'
import { useCancelInvitation } from '../hooks/useCancelInvitation'
import { roleLabels, assignableRoles, isAssignableRole } from '../roles'
import { invitationStatusLabels, invitationStatusBadge } from '../invitations'
import type { OrganizationMember, OrganizationInvitation, AssignableRole } from '../api/organization-api'
import InviteMemberDialog from './InviteMemberDialog'
import type { InviteMemberFormData } from '../schemas/inviteMemberSchema'
import ConfirmDialog from '@components/shared/ConfirmDialog'
import ListErrorState from '@components/shared/ListErrorState'
import { Card } from '@components/ui/card'
import { Button } from '@components/ui/button'
import { Badge } from '@components/ui/badge'
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '@components/ui/table'

function OrganizationMembersPage() {
  const { id: organizationId = '' } = useParams<{ id: string }>()
  const [removeTarget, setRemoveTarget] = useState<OrganizationMember | null>(null)
  const [showInvite, setShowInvite] = useState(false)
  const [invitedEmail, setInvitedEmail] = useState<string | null>(null)
  const [roleError, setRoleError] = useState<string | null>(null)
  const [cancelTarget, setCancelTarget] = useState<OrganizationInvitation | null>(null)
  const [invitationError, setInvitationError] = useState<{ id: string; message: string } | null>(null)

  const { data: members, isLoading, isError, isFetching, refetch } = useOrganizationMembers(organizationId)
  const removeMember = useRemoveMember(organizationId)
  const inviteMember = useInviteMember(organizationId)
  const changeRole = useChangeMemberRole(organizationId)
  const invitationsQuery = useOrganizationInvitations(organizationId)
  const invitations = invitationsQuery.data
  const cancelInvitation = useCancelInvitation(organizationId)

  // Reenviar é reconvidar: o back-end cancela o pendente anterior e manda um link novo.
  const handleResend = (invitation: OrganizationInvitation) => {
    if (!isAssignableRole(invitation.role)) return
    setInvitationError(null)
    setInvitedEmail(null)
    inviteMember.mutate(
      { email: invitation.email, role: invitation.role },
      {
        onSuccess: () => setInvitedEmail(invitation.email),
        onError: (error) => {
          const status = (error as { response?: { status?: number } }).response?.status
          setInvitationError({
            id: invitation.id,
            message:
              status === 409
                ? 'Esse e-mail já pertence a um membro desta organização.'
                : 'Não foi possível reenviar o convite.',
          })
        },
      },
    )
  }

  const handleConfirmCancel = () => {
    if (!cancelTarget) return
    const target = cancelTarget
    setInvitationError(null)
    cancelInvitation.mutate(target.id, {
      onSuccess: () => setCancelTarget(null),
      onError: () => {
        setCancelTarget(null)
        setInvitationError({ id: target.id, message: 'Não foi possível cancelar o convite.' })
      },
    })
  }

  const handleRoleChange = (userId: string, role: AssignableRole) => {
    setRoleError(null)
    changeRole.mutate({ userId, role }, { onError: () => setRoleError(userId) })
  }

  const handleInvite = (data: InviteMemberFormData) => {
    inviteMember.mutate(data, {
      onSuccess: () => {
        setInvitedEmail(data.email)
        setShowInvite(false)
      },
    })
  }

  const openInvite = () => {
    setInvitedEmail(null)
    inviteMember.reset()
    setShowInvite(true)
  }

  const handleConfirmRemove = () => {
    if (!removeTarget) return
    removeMember.mutate(removeTarget.userId, { onSuccess: () => setRemoveTarget(null) })
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Users className="h-6 w-6 text-accent" />
          <h2 className="mb-0">Membros</h2>
        </div>
        <Button onClick={openInvite}>
          <UserPlus className="h-4 w-4" /> Convidar
        </Button>
      </div>

      {invitedEmail && (
        <p role="status" className="flex items-center gap-2 text-sm text-muted-foreground">
          <CheckCircle2 className="h-4 w-4 text-accent" />
          Convite enviado para {invitedEmail}.
        </p>
      )}

      {isLoading ? (
        <p className="text-muted-foreground">Carregando membros...</p>
      ) : isError ? (
        <ListErrorState subject="os membros" onRetry={() => void refetch()} isRetrying={isFetching} />
      ) : members?.length === 0 ? (
        <p className="text-muted-foreground">Nenhum membro nesta organização.</p>
      ) : (
        <Card elevation="sm" className="overflow-hidden p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>E-mail</TableHead>
                <TableHead>Papel</TableHead>
                <TableHead>Ingresso</TableHead>
                <TableHead className="text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {members?.map((m) => (
                <TableRow key={m.id}>
                  <TableCell className="font-medium">
                    {m.name ?? <span className="font-mono text-xs text-muted-foreground">{m.userId}</span>}
                  </TableCell>
                  <TableCell className="text-muted-foreground">{m.email ?? '—'}</TableCell>
                  <TableCell>
                    {/* O papel do criador é fixo — o back-end responde 403 a qualquer troca. */}
                    {m.owner || !isAssignableRole(m.role) ? (
                      <Badge variant={m.owner ? 'accent' : 'neutral'}>{roleLabels[m.role]}</Badge>
                    ) : (
                      <>
                        <select
                          value={m.role}
                          aria-label={`Papel de ${m.name ?? m.email ?? m.userId}`}
                          disabled={changeRole.isPending}
                          onChange={(e) => handleRoleChange(m.userId, e.target.value as AssignableRole)}
                          className="h-8 rounded-full border border-border bg-surface px-3 text-sm text-foreground outline-none focus-visible:border-accent disabled:opacity-60"
                        >
                          {assignableRoles.map((role) => (
                            <option key={role} value={role}>
                              {roleLabels[role]}
                            </option>
                          ))}
                        </select>
                        {roleError === m.userId && (
                          <p role="alert" className="mt-1 text-xs text-destructive">
                            Não foi possível alterar o papel.
                          </p>
                        )}
                      </>
                    )}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {new Date(m.joinedAt).toLocaleDateString('pt-BR')}
                  </TableCell>
                  <TableCell className="text-right">
                    {/* O criador da organização não pode ser removido — o back-end responde 403. */}
                    {m.owner ? (
                      <span className="text-xs text-muted-foreground">Criador</span>
                    ) : (
                      <button
                        onClick={() => setRemoveTarget(m)}
                        className="text-muted-foreground hover:text-destructive"
                        title="Remover da organização"
                        aria-label={`Remover ${m.name ?? m.email ?? 'membro'}`}
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      )}

      <section aria-labelledby="invitations-heading" className="space-y-3">
        <div className="flex items-center gap-2">
          <Mail className="h-5 w-5 text-accent" />
          <h3 id="invitations-heading" className="mb-0">
            Convites
          </h3>
        </div>

        {invitationsQuery.isLoading ? (
          <p className="text-muted-foreground">Carregando convites...</p>
        ) : invitationsQuery.isError ? (
          <ListErrorState
            subject="os convites"
            onRetry={() => void invitationsQuery.refetch()}
            isRetrying={invitationsQuery.isFetching}
          />
        ) : invitations?.length === 0 ? (
          <p className="text-muted-foreground">Nenhum convite enviado.</p>
        ) : (
          <Card elevation="sm" className="overflow-hidden p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>E-mail</TableHead>
                  <TableHead>Papel</TableHead>
                  <TableHead>Estado</TableHead>
                  <TableHead>Convidado por</TableHead>
                  <TableHead>Enviado em</TableHead>
                  <TableHead className="text-right">Ações</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {invitations?.map((invitation) => (
                  <TableRow key={invitation.id}>
                    <TableCell className="font-medium">{invitation.email}</TableCell>
                    <TableCell className="text-muted-foreground">{roleLabels[invitation.role]}</TableCell>
                    <TableCell>
                      <Badge variant={invitationStatusBadge[invitation.status]}>
                        {invitationStatusLabels[invitation.status]}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">{invitation.invitedByName ?? '—'}</TableCell>
                    <TableCell className="text-muted-foreground">
                      {new Date(invitation.createdAt).toLocaleDateString('pt-BR')}
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-3">
                        {/* Aceito já virou membro: reenviar só daria 409. */}
                        {invitation.status !== 'USED' && isAssignableRole(invitation.role) && (
                          <button
                            onClick={() => handleResend(invitation)}
                            disabled={inviteMember.isPending}
                            className="text-muted-foreground hover:text-accent disabled:opacity-60"
                            title="Reenviar convite"
                            aria-label={`Reenviar convite para ${invitation.email}`}
                          >
                            <Send className="h-4 w-4" />
                          </button>
                        )}
                        {/* Só o pendente tem um link valendo para desfazer — o back-end responde 409 aos demais. */}
                        {invitation.status === 'PENDING' && (
                          <button
                            onClick={() => setCancelTarget(invitation)}
                            className="text-muted-foreground hover:text-destructive"
                            title="Cancelar convite"
                            aria-label={`Cancelar convite para ${invitation.email}`}
                          >
                            <Ban className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                      {invitationError?.id === invitation.id && (
                        <p role="alert" className="mt-1 text-xs text-destructive">
                          {invitationError.message}
                        </p>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Card>
        )}
      </section>

      <InviteMemberDialog
        open={showInvite}
        onClose={() => setShowInvite(false)}
        onSubmit={handleInvite}
        isPending={inviteMember.isPending}
        error={inviteMember.error}
      />

      <ConfirmDialog
        open={!!cancelTarget}
        title="Cancelar convite"
        description={`Cancelar o convite para ${cancelTarget?.email ?? 'este e-mail'}? O link enviado deixa de valer.`}
        confirmLabel="Cancelar convite"
        onConfirm={handleConfirmCancel}
        onCancel={() => setCancelTarget(null)}
      />

      <ConfirmDialog
        open={!!removeTarget}
        title="Remover membro"
        description={`Remover ${removeTarget?.name ?? removeTarget?.email ?? 'este membro'} da organização? Ele perde o acesso imediatamente.`}
        confirmLabel="Remover"
        onConfirm={handleConfirmRemove}
        onCancel={() => setRemoveTarget(null)}
      />
    </div>
  )
}

export default OrganizationMembersPage
