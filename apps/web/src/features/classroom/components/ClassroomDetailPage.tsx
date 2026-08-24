import { useState, type ReactNode } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Pencil, Trash2, Copy } from 'lucide-react'
import { useClassroom } from '../hooks/useClassroom'
import { useUpdateClassroom } from '../hooks/useUpdateClassroom'
import { useDeleteClassroom } from '../hooks/useDeleteClassroom'
import { useAuthStore } from '@store/authStore'
import ClassroomFormDialog from './ClassroomFormDialog'
import ClassroomMembersPanel from './ClassroomMembersPanel'
import ConfirmDialog from '@components/shared/ConfirmDialog'
import type { ClassroomFormData } from '../schemas/classroomSchema'
import { Card, CardKicker } from '@components/ui/card'
import { Badge } from '@components/ui/badge'
import { Button } from '@components/ui/button'

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

  const role = useAuthStore((s) => s.role)
  const canManage = role === 'ADMIN_ORG' || role === 'GESTOR'

  const handleUpdate = (data: ClassroomFormData) => {
    updateClassroom.mutate(data, { onSuccess: () => setShowEdit(false) })
  }

  const handleConfirmDelete = () => {
    deleteClassroom.mutate(id!, {
      onSuccess: () => { setShowDeleteConfirm(false); navigate('/classrooms') },
    })
  }

  if (isLoading) {
    return <div className="text-muted-foreground">Carregando...</div>
  }

  if (!classroom) {
    return <div className="text-destructive">Turma não encontrada.</div>
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="flex items-center gap-2">
        <button onClick={() => navigate('/classrooms')} className="text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-5 w-5" />
        </button>
        <h2 className="mb-0 flex-1">{classroom.name}</h2>
        {canManage && (
          <div className="flex gap-2">
            <Button variant="secondary" onClick={() => setShowEdit(true)}>
              <Pencil className="h-4 w-4" /> Editar
            </Button>
            <Button variant="destructive" onClick={() => setShowDeleteConfirm(true)}>
              <Trash2 className="h-4 w-4" /> Excluir
            </Button>
          </div>
        )}
      </div>

      <Card elevation="sm">
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <CardKicker>Período Letivo</CardKicker>
            <p className="mt-0.5 font-medium">{classroom.academicPeriod}</p>
          </div>
          <div>
            <CardKicker>Status</CardKicker>
            <div className="mt-0.5">
              <Badge variant={classroom.status === 'ACTIVE' ? 'accent-2' : 'neutral'}>
                {classroom.status === 'ACTIVE' ? 'Ativa' : 'Arquivada'}
              </Badge>
            </div>
          </div>
          {classroom.inviteCode && (
            <div>
              <CardKicker>Código de Convite</CardKicker>
              <div className="mt-0.5 flex items-center gap-2">
                <span className="font-mono font-medium tracking-widest">{classroom.inviteCode}</span>
                <button
                  onClick={() => navigator.clipboard.writeText(classroom.inviteCode!)}
                  className="text-muted-foreground hover:text-foreground"
                  title="Copiar código"
                >
                  <Copy className="h-4 w-4" />
                </button>
              </div>
            </div>
          )}
          {classroom.description && (
            <div className="col-span-2">
              <CardKicker>Descrição</CardKicker>
              <p className="mt-0.5">{classroom.description}</p>
            </div>
          )}
        </div>
      </Card>

      <Card elevation="sm">
        <ClassroomMembersPanel classroomId={id!} canManage={canManage} />
      </Card>

      <div className="space-y-3">
        <h4 className="mb-0">Mural de Avisos</h4>
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
