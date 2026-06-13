import { useState } from 'react'
import { UserPlus, Trash2 } from 'lucide-react'
import { useClassroomMembers } from '../hooks/useClassroomMembers'
import { useAddClassroomMember } from '../hooks/useAddClassroomMember'
import { useRemoveClassroomMember } from '../hooks/useRemoveClassroomMember'
import AddMemberDialog from './AddMemberDialog'
import type { AddMemberFormData } from '../schemas/addMemberSchema'

interface Props {
  classroomId: string
  canManage: boolean
}

function ClassroomMembersPanel({ classroomId, canManage }: Props) {
  const [showAdd, setShowAdd] = useState(false)
  const { data: members, isLoading } = useClassroomMembers(classroomId)
  const addMember = useAddClassroomMember(classroomId)
  const removeMember = useRemoveClassroomMember(classroomId)

  const handleAdd = (data: AddMemberFormData) => {
    addMember.mutate(data, { onSuccess: () => setShowAdd(false) })
  }

  const handleRemove = (userId: string) => {
    if (confirm('Remover membro da turma?')) {
      removeMember.mutate(userId)
    }
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h3 className="font-medium">Membros</h3>
        {canManage && (
          <button
            onClick={() => setShowAdd(true)}
            className="flex items-center gap-1 rounded bg-primary px-3 py-1.5 text-xs text-primary-foreground"
          >
            <UserPlus className="h-3 w-3" /> Adicionar
          </button>
        )}
      </div>

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Carregando...</p>
      ) : members?.length === 0 ? (
        <p className="text-sm text-muted-foreground">Nenhum membro cadastrado.</p>
      ) : (
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b text-left text-muted-foreground">
              <th className="pb-2 font-medium">Usuário</th>
              <th className="pb-2 font-medium">Papel</th>
              <th className="pb-2 font-medium">Ingresso</th>
              {canManage && <th className="pb-2" />}
            </tr>
          </thead>
          <tbody>
            {members?.map((m) => (
              <tr key={m.id} className="border-b last:border-0">
                <td className="py-2 font-mono text-xs">{m.userId}</td>
                <td className="py-2">
                  <span className={`rounded px-2 py-0.5 text-xs font-medium ${
                    m.role === 'PROFESSOR'
                      ? 'bg-blue-100 text-blue-700'
                      : 'bg-green-100 text-green-700'
                  }`}>
                    {m.role === 'PROFESSOR' ? 'Professor' : 'Aluno'}
                  </span>
                </td>
                <td className="py-2 text-muted-foreground">
                  {new Date(m.joinedAt).toLocaleDateString('pt-BR')}
                </td>
                {canManage && (
                  <td className="py-2 text-right">
                    <button
                      onClick={() => handleRemove(m.userId)}
                      className="text-destructive hover:opacity-70"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <AddMemberDialog
        open={showAdd}
        onClose={() => setShowAdd(false)}
        onSubmit={handleAdd}
        isPending={addMember.isPending}
      />
    </div>
  )
}

export default ClassroomMembersPanel
