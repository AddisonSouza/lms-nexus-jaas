import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, BookOpen, LogIn } from 'lucide-react'
import { useClassrooms } from '../hooks/useClassrooms'
import { useCreateClassroom } from '../hooks/useCreateClassroom'
import { useAuthStore } from '@store/authStore'
import ClassroomFormDialog from './ClassroomFormDialog'
import JoinClassroomForm from './JoinClassroomForm'
import type { ClassroomFormData } from '../schemas/classroomSchema'

function ClassroomListPage() {
  const [showCreate, setShowCreate] = useState(false)
  const [showJoin, setShowJoin] = useState(false)
  const { data: classrooms, isLoading } = useClassrooms()
  const createClassroom = useCreateClassroom()

  const role = useAuthStore((s) => s.role)
  const canManage = role === 'ADMIN_ORG' || role === 'GESTOR'

  const handleCreate = (data: ClassroomFormData) => {
    createClassroom.mutate(data, { onSuccess: () => setShowCreate(false) })
  }

  return (
    <div className="container mx-auto max-w-4xl p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <BookOpen className="h-6 w-6" />
          <h1 className="text-2xl font-semibold">Turmas</h1>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setShowJoin((v) => !v)}
            className="flex items-center gap-2 rounded border px-4 py-2 text-sm hover:bg-muted"
          >
            <LogIn className="h-4 w-4" /> Entrar via código
          </button>
          {canManage && (
            <button
              onClick={() => setShowCreate(true)}
              className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground"
            >
              <Plus className="h-4 w-4" /> Nova Turma
            </button>
          )}
        </div>
      </div>

      {showJoin && (
        <div className="rounded-lg border p-4">
          <JoinClassroomForm />
        </div>
      )}

      {isLoading ? (
        <p className="text-muted-foreground">Carregando turmas...</p>
      ) : classrooms?.length === 0 ? (
        <p className="text-muted-foreground">Nenhuma turma encontrada.</p>
      ) : (
        <table className="w-full text-sm border rounded-lg overflow-hidden">
          <thead className="bg-muted">
            <tr>
              <th className="px-4 py-3 text-left font-medium">Nome</th>
              <th className="px-4 py-3 text-left font-medium">Período</th>
              <th className="px-4 py-3 text-left font-medium">Status</th>
            </tr>
          </thead>
          <tbody>
            {classrooms?.map((c) => (
              <tr key={c.id} className="border-t hover:bg-muted/50">
                <td className="px-4 py-3">
                  <Link to={`/classrooms/${c.id}`} className="font-medium hover:underline">
                    {c.name}
                  </Link>
                </td>
                <td className="px-4 py-3 text-muted-foreground">{c.academicPeriod}</td>
                <td className="px-4 py-3">
                  <span className={`rounded px-2 py-0.5 text-xs font-medium ${
                    c.status === 'ACTIVE'
                      ? 'bg-primary/10 text-primary'
                      : 'bg-muted text-muted-foreground'
                  }`}>
                    {c.status === 'ACTIVE' ? 'Ativa' : 'Arquivada'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <ClassroomFormDialog
        open={showCreate}
        onClose={() => setShowCreate(false)}
        onSubmit={handleCreate}
        isPending={createClassroom.isPending}
        title="Nova Turma"
      />
    </div>
  )
}

export default ClassroomListPage
