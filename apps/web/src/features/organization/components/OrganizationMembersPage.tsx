import { useState } from 'react'
import { Users, Trash2, UserPlus, CheckCircle2 } from 'lucide-react'
import { useParams } from 'react-router-dom'
import { useOrganizationMembers } from '../hooks/useOrganizationMembers'
import { useRemoveMember } from '../hooks/useRemoveMember'
import { useInviteMember } from '../hooks/useInviteMember'
import { roleLabels } from '../roles'
import type { OrganizationMember } from '../api/organization-api'
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

  const { data: members, isLoading, isError, isFetching, refetch } = useOrganizationMembers(organizationId)
  const removeMember = useRemoveMember(organizationId)
  const inviteMember = useInviteMember(organizationId)

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
                    <Badge variant={m.owner ? 'accent' : 'neutral'}>{roleLabels[m.role]}</Badge>
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
