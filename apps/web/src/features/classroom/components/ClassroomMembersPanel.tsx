import { useState } from 'react'
import { UserPlus, Trash2 } from 'lucide-react'
import { useClassroomMembers } from '../hooks/useClassroomMembers'
import { useAddClassroomMember } from '../hooks/useAddClassroomMember'
import { useRemoveClassroomMember } from '../hooks/useRemoveClassroomMember'
import AddMemberDialog from './AddMemberDialog'
import ConfirmDialog from '@components/shared/ConfirmDialog'
import type { AddMemberFormData } from '../schemas/addMemberSchema'
import { Button } from '@components/ui/button'
import { Badge } from '@components/ui/badge'
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '@components/ui/table'

interface Props {
  classroomId: string
  canManage: boolean
}

function ClassroomMembersPanel({ classroomId, canManage }: Props) {
  const [showAdd, setShowAdd] = useState(false)
  const [removeTarget, setRemoveTarget] = useState<string | null>(null)
  const { data: members, isLoading } = useClassroomMembers(classroomId)
  const addMember = useAddClassroomMember(classroomId)
  const removeMember = useRemoveClassroomMember(classroomId)

  const handleAdd = (data: AddMemberFormData) => {
    addMember.mutate(data, { onSuccess: () => setShowAdd(false) })
  }

  const handleConfirmRemove = () => {
    if (!removeTarget) return
    removeMember.mutate(removeTarget, { onSuccess: () => setRemoveTarget(null) })
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h4 className="mb-0">Membros</h4>
        {canManage && (
          <Button size="sm" onClick={() => setShowAdd(true)}>
            <UserPlus className="h-3.5 w-3.5" /> Adicionar
          </Button>
        )}
      </div>

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Carregando...</p>
      ) : members?.length === 0 ? (
        <p className="text-sm text-muted-foreground">Nenhum membro cadastrado.</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Usuário</TableHead>
              <TableHead>Papel</TableHead>
              <TableHead>Ingresso</TableHead>
              {canManage && <TableHead />}
            </TableRow>
          </TableHeader>
          <TableBody>
            {members?.map((m) => (
              <TableRow key={m.id}>
                <TableCell>{m.userName ?? <span className="font-mono text-xs">{m.userId}</span>}</TableCell>
                <TableCell>
                  <Badge variant={m.role === 'PROFESSOR' ? 'accent' : 'neutral'}>
                    {m.role === 'PROFESSOR' ? 'Professor' : 'Aluno'}
                  </Badge>
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {new Date(m.joinedAt).toLocaleDateString('pt-BR')}
                </TableCell>
                {canManage && (
                  <TableCell className="text-right">
                    <button
                      onClick={() => setRemoveTarget(m.userId)}
                      className="text-destructive hover:opacity-70"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <AddMemberDialog
        open={showAdd}
        onClose={() => setShowAdd(false)}
        onSubmit={handleAdd}
        isPending={addMember.isPending}
      />

      <ConfirmDialog
        open={!!removeTarget}
        title="Remover membro"
        description="Remover este membro da turma? Esta ação não pode ser desfeita."
        confirmLabel="Remover"
        onConfirm={handleConfirmRemove}
        onCancel={() => setRemoveTarget(null)}
      />
    </div>
  )
}

export default ClassroomMembersPanel
