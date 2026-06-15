import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, BookOpenCheck, Pencil, Trash2 } from 'lucide-react'
import { useSubjects } from '../hooks/useSubjects'
import { useCreateSubject } from '../hooks/useCreateSubject'
import { useUpdateSubject } from '../hooks/useUpdateSubject'
import { useDeleteSubject } from '../hooks/useDeleteSubject'
import { useAuthStore } from '@features/auth/store/authStore'
import SubjectFormDialog from './SubjectFormDialog'
import type { SubjectFormData } from '../schemas/subjectSchema'
import type { Subject } from '../types'

function SubjectListPage() {
  const [showCreate, setShowCreate] = useState(false)
  const [editTarget, setEditTarget] = useState<Subject | null>(null)

  const { data: subjects, isLoading } = useSubjects()
  const createSubject = useCreateSubject()
  const updateSubject = useUpdateSubject(editTarget?.id ?? '')
  const deleteSubject = useDeleteSubject()

  const token = useAuthStore((s) => s.accessToken)
  const role = token ? (JSON.parse(atob(token.split('.')[1])).groups?.[0] ?? 'ALUNO') : 'ALUNO'
  const canManage = role === 'ADMIN_ORG' || role === 'GESTOR'

  const handleCreate = (data: SubjectFormData) => {
    createSubject.mutate(
      {
        name: data.name,
        code: data.code || undefined,
        description: data.description || undefined,
        workloadHours: data.workloadHours ?? undefined,
      },
      { onSuccess: () => setShowCreate(false) },
    )
  }

  const handleUpdate = (data: SubjectFormData) => {
    if (!editTarget) return
    updateSubject.mutate(
      {
        name: data.name,
        code: data.code || undefined,
        description: data.description || undefined,
        workloadHours: data.workloadHours ?? undefined,
      },
      { onSuccess: () => setEditTarget(null) },
    )
  }

  const handleDelete = (id: string) => {
    if (!confirm('Confirmar exclusão da disciplina?')) return
    deleteSubject.mutate(id)
  }

  return (
    <div className="container mx-auto max-w-4xl p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <BookOpenCheck className="h-6 w-6" />
          <h1 className="text-2xl font-semibold">Disciplinas</h1>
        </div>
        {canManage && (
          <button
            onClick={() => setShowCreate(true)}
            className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground"
          >
            <Plus className="h-4 w-4" /> Nova Disciplina
          </button>
        )}
      </div>

      {isLoading ? (
        <p className="text-muted-foreground">Carregando disciplinas...</p>
      ) : subjects?.length === 0 ? (
        <p className="text-muted-foreground">Nenhuma disciplina encontrada.</p>
      ) : (
        <table className="w-full text-sm border rounded-lg overflow-hidden">
          <thead className="bg-muted">
            <tr>
              <th className="px-4 py-3 text-left font-medium">Nome</th>
              <th className="px-4 py-3 text-left font-medium">Código</th>
              <th className="px-4 py-3 text-left font-medium">Carga Horária</th>
              {canManage && <th className="px-4 py-3 text-right font-medium">Ações</th>}
            </tr>
          </thead>
          <tbody>
            {subjects?.map((s) => (
              <tr key={s.id} className="border-t hover:bg-muted/50">
                <td className="px-4 py-3">
                  <Link to={`/curriculum/${s.id}`} className="font-medium hover:underline">
                    {s.name}
                  </Link>
                </td>
                <td className="px-4 py-3 text-muted-foreground">{s.code ?? '—'}</td>
                <td className="px-4 py-3 text-muted-foreground">
                  {s.workloadHours != null ? `${s.workloadHours}h` : '—'}
                </td>
                {canManage && (
                  <td className="px-4 py-3 text-right">
                    <div className="flex justify-end gap-2">
                      <button
                        onClick={() => setEditTarget(s)}
                        className="text-muted-foreground hover:text-foreground"
                        title="Editar"
                      >
                        <Pencil className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(s.id)}
                        className="text-muted-foreground hover:text-destructive"
                        title="Excluir"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <SubjectFormDialog
        open={showCreate}
        onClose={() => setShowCreate(false)}
        onSubmit={handleCreate}
        isPending={createSubject.isPending}
        title="Nova Disciplina"
      />

      <SubjectFormDialog
        open={!!editTarget}
        onClose={() => setEditTarget(null)}
        onSubmit={handleUpdate}
        isPending={updateSubject.isPending}
        defaultValues={editTarget ?? undefined}
        title="Editar Disciplina"
      />
    </div>
  )
}

export default SubjectListPage
