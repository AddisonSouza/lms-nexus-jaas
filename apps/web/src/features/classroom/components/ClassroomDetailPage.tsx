import { useState, type ReactNode } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Pencil, Trash2, Copy, RefreshCw } from 'lucide-react'
import { useClassroom } from '../hooks/useClassroom'
import { useUpdateClassroom } from '../hooks/useUpdateClassroom'
import { useDeleteClassroom } from '../hooks/useDeleteClassroom'
import { useRegenerateInviteCode } from '../hooks/useRegenerateInviteCode'
import { useAuthStore } from '@store/authStore'
import ClassroomFormDialog from './ClassroomFormDialog'
import ClassroomMembersPanel from './ClassroomMembersPanel'
import ConfirmDialog from '@components/shared/ConfirmDialog'
import type { ClassroomFormData } from '../schemas/classroomSchema'

interface ClassroomDetailPageProps {
  announcementFeedSlot?: ReactNode
}

function ClassroomDetailPage({ announcementFeedSlot }: ClassroomDetailPageProps) {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [showEdit, setShowEdit] = useState(false)
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false)

  const { data: classroom, isLoading } = useClassroom(id!)
  const updateClassroom = useUpdateClassroom(id!)
  const deleteClassroom = useDeleteClassroom()
  const regenerateCode = useRegenerateInviteCode(id!)

  const role = useAuthStore((s) => s.role)
  const canManage = role === 'ADMIN_ORG' || role === 'GESTOR'
  const canRegenerateCode = role === 'ADMIN_ORG' || role === 'GESTOR' || role === 'PROFESSOR'

  const handleUpdate = (data: ClassroomFormData) => {
    updateClassroom.mutate(data, { onSuccess: () => setShowEdit(false) })
  }

  const handleConfirmDelete = () => {
    deleteClassroom.mutate(id!, {
      onSuccess: () => { setShowDeleteConfirm(false); navigate('/classrooms') },
    })
  }

  if (isLoading) {
    return <div className="p-6 text-muted-foreground">Carregando...</div>
  }

  if (!classroom) {
    return <div className="p-6 text-destructive">Turma não encontrada.</div>
  }

  return (
    <div className="container mx-auto max-w-4xl p-6 space-y-6">
      <div className="flex items-center gap-2">
        <button onClick={() => navigate('/classrooms')} className="text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-5 w-5" />
        </button>
        <h1 className="text-2xl font-semibold flex-1">{classroom.name}</h1>
        {canManage && (
          <div className="flex gap-2">
            <button
              onClick={() => setShowEdit(true)}
              className="flex items-center gap-1 rounded border px-3 py-1.5 text-sm hover:bg-muted"
            >
              <Pencil className="h-4 w-4" /> Editar
            </button>
            <button
              onClick={() => setShowDeleteConfirm(true)}
              className="flex items-center gap-1 rounded border border-destructive px-3 py-1.5 text-sm text-destructive hover:bg-destructive/10"
            >
              <Trash2 className="h-4 w-4" /> Excluir
            </button>
          </div>
        )}
      </div>

      <div className="rounded-lg border p-4 space-y-3">
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-muted-foreground">Período Letivo</p>
            <p className="font-medium">{classroom.academicPeriod}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Status</p>
            <span className={`inline-block rounded px-2 py-0.5 text-xs font-medium ${
              classroom.status === 'ACTIVE'
                ? 'bg-primary/10 text-primary'
                : 'bg-muted text-muted-foreground'
            }`}>
              {classroom.status === 'ACTIVE' ? 'Ativa' : 'Arquivada'}
            </span>
          </div>
          {classroom.inviteCode && (
            <div>
              <p className="text-muted-foreground">Código de Convite</p>
              <div className="flex items-center gap-2">
                <span className="font-mono font-medium tracking-widest">{classroom.inviteCode}</span>
                <button
                  onClick={() => navigator.clipboard.writeText(classroom.inviteCode!)}
                  className="text-muted-foreground hover:text-foreground"
                  title="Copiar código"
                >
                  <Copy className="h-4 w-4" />
                </button>
                {canRegenerateCode && (
                  <button
                    onClick={() => regenerateCode.mutate()}
                    disabled={regenerateCode.isPending}
                    className="text-muted-foreground hover:text-foreground disabled:opacity-50"
                    title="Regenerar código"
                  >
                    <RefreshCw className={`h-4 w-4 ${regenerateCode.isPending ? 'animate-spin' : ''}`} />
                  </button>
                )}
              </div>
            </div>
          )}
          {classroom.description && (
            <div className="col-span-2">
              <p className="text-muted-foreground">Descrição</p>
              <p>{classroom.description}</p>
            </div>
          )}
        </div>
      </div>

      <div className="rounded-lg border p-4">
        <ClassroomMembersPanel classroomId={id!} canManage={canManage} />
      </div>

      <div className="rounded-lg border p-4 space-y-3">
        <h2 className="text-lg font-semibold">Mural de Avisos</h2>
        {announcementFeedSlot}
      </div>

      <ClassroomFormDialog
        open={showEdit}
        onClose={() => setShowEdit(false)}
        onSubmit={handleUpdate}
        isPending={updateClassroom.isPending}
        defaultValues={classroom}
        title="Editar Turma"
      />

      <ConfirmDialog
        open={showDeleteConfirm}
        title="Excluir turma"
        description={`Excluir a turma "${classroom?.name}"? Esta ação não pode ser desfeita.`}
        confirmLabel="Excluir"
        onConfirm={handleConfirmDelete}
        onCancel={() => setShowDeleteConfirm(false)}
      />
    </div>
  )
}

export default ClassroomDetailPage
