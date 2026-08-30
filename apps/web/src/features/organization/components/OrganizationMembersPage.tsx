import { useState } from 'react'
import { Users, Trash2, UserPlus, CheckCircle2 } from 'lucide-react'
import { useParams } from 'react-router-dom'
import { useOrganizationMembers } from '../hooks/useOrganizationMembers'
import { useRemoveMember } from '../hooks/useRemoveMember'
import { useInviteMember } from '../hooks/useInviteMember'
import { useChangeMemberRole } from '../hooks/useChangeMemberRole'
import { roleLabels, assignableRoles, isAssignableRole } from '../roles'
import type { OrganizationMember, AssignableRole } from '../api/organization-api'
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

  const { data: members, isLoading, isError, isFetching, refetch } = useOrganizationMembers(organizationId)
  const removeMember = useRemoveMember(organizationId)
  const inviteMember = useInviteMember(organizationId)
  const changeRole = useChangeMemberRole(organizationId)

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
          Convite enviado para {invitedEmail}. Ele aparece na lista depois de aceitar.
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

      <InviteMemberDialog
        open={showInvite}
        onClose={() => setShowInvite(false)}
        onSubmit={handleInvite}
        isPending={inviteMember.isPending}
        error={inviteMember.error}
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
