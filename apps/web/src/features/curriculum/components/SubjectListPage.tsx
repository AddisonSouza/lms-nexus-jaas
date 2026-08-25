import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, BookOpenCheck, Pencil, Trash2 } from 'lucide-react'
import { useSubjects } from '../hooks/useSubjects'
import { useCreateSubject } from '../hooks/useCreateSubject'
import { useUpdateSubject } from '../hooks/useUpdateSubject'
import { useDeleteSubject } from '../hooks/useDeleteSubject'
import { useAuthStore } from '@store/authStore'
import SubjectFormDialog from './SubjectFormDialog'
import ConfirmDialog from '@components/shared/ConfirmDialog'
import type { SubjectFormData } from '../schemas/subjectSchema'
import type { Subject } from '../types'
import { Card } from '@components/ui/card'
import { Button } from '@components/ui/button'
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '@components/ui/table'

function SubjectListPage() {
  const [showCreate, setShowCreate] = useState(false)
  const [editTarget, setEditTarget] = useState<Subject | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<Subject | null>(null)

  const { data: subjects, isLoading } = useSubjects()
  const createSubject = useCreateSubject()
  const updateSubject = useUpdateSubject(editTarget?.id ?? '')
  const deleteSubject = useDeleteSubject()

  const role = useAuthStore((s) => s.role)
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

  const handleConfirmDelete = () => {
    if (!deleteTarget) return
    deleteSubject.mutate(deleteTarget.id, { onSuccess: () => setDeleteTarget(null) })
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <BookOpenCheck className="h-6 w-6 text-accent" />
          <h2 className="mb-0">Disciplinas</h2>
        </div>
        {canManage && (
          <Button onClick={() => setShowCreate(true)}>
            <Plus className="h-4 w-4" /> Nova Disciplina
          </Button>
        )}
      </div>

      {isLoading ? (
        <p className="text-muted-foreground">Carregando disciplinas...</p>
      ) : subjects?.length === 0 ? (
        <p className="text-muted-foreground">Nenhuma disciplina encontrada.</p>
      ) : (
        <Card elevation="sm" className="overflow-hidden p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Código</TableHead>
                <TableHead>Carga Horária</TableHead>
                {canManage && <TableHead className="text-right">Ações</TableHead>}
              </TableRow>
            </TableHeader>
            <TableBody>
              {subjects?.map((s) => (
                <TableRow key={s.id}>
                  <TableCell>
                    <Link to={`/curriculum/${s.id}`} className="font-medium hover:underline">
                      {s.name}
                    </Link>
                  </TableCell>
                  <TableCell className="text-muted-foreground">{s.code ?? '—'}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {s.workloadHours != null ? `${s.workloadHours}h` : '—'}
                  </TableCell>
                  {canManage && (
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <button
                          onClick={() => setEditTarget(s)}
                          className="text-muted-foreground hover:text-foreground"
                          title="Editar"
                        >
                          <Pencil className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => setDeleteTarget(s)}
                          className="text-muted-foreground hover:text-destructive"
                          title="Excluir"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
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

      <ConfirmDialog
        open={!!deleteTarget}
        title="Excluir disciplina"
        description={`Confirmar exclusão de "${deleteTarget?.name}"? Esta ação não pode ser desfeita.`}
        confirmLabel="Excluir"
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}

export default SubjectListPage
