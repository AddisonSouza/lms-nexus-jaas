import { useState } from 'react'
import { Plus } from 'lucide-react'
import { useAuthStore } from '@store/authStore'
import { useAnnouncements } from '../hooks/useAnnouncements'
import { useCreateAnnouncement, useUpdateAnnouncement, useDeleteAnnouncement } from '../hooks/useAnnouncementMutations'
import AnnouncementCard from './AnnouncementCard'
import AnnouncementForm from './AnnouncementForm'
import ConfirmDialog from '@components/shared/ConfirmDialog'
import { apiErrorMessage } from '@lib/api-error'
import type { Announcement } from '../types'
import type { AnnouncementFormData } from '../schemas/announcementSchema'
import { Button } from '@components/ui/button'

interface Props {
  classroomId: string
  /**
   * Associação à turma, resolvida pela rota. `ListAnnouncementsService` exige
   * `isMember` seja qual for o papel na organização, então admin e gestor levam
   * 403 aqui — o mural inteiro some para quem não é membro.
   */
  isMember: boolean
  /**
   * Publicar exige ser membro **com papel PROFESSOR na turma**
   * (`PostAnnouncementService`). O papel da organização é largo demais: gestor e
   * admin viam um botão que sempre falhava.
   */
  canPost: boolean
}

function AnnouncementFeed({ classroomId, isMember, canPost }: Props) {
  const userId = useAuthStore((s) => s.userId)

  const { data: announcements = [], isLoading, isError } = useAnnouncements(classroomId, isMember)
  const createAnnouncement = useCreateAnnouncement(classroomId)
  const updateAnnouncement = useUpdateAnnouncement(classroomId)
  const deleteAnnouncement = useDeleteAnnouncement(classroomId)

  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Announcement | null>(null)
  const [deleting, setDeleting] = useState<Announcement | null>(null)

  function handleCreate(data: AnnouncementFormData) {
    createAnnouncement.mutate({ classroomId, ...data }, { onSuccess: () => setShowForm(false) })
  }

  function handleUpdate(data: AnnouncementFormData) {
    if (!editing) return
    updateAnnouncement.mutate({ id: editing.id, ...data }, { onSuccess: () => setEditing(null) })
  }

  function handleConfirmDelete() {
    if (!deleting) return
    deleteAnnouncement.mutate(deleting.id, { onSuccess: () => setDeleting(null) })
  }

  if (!isMember) {
    return null
  }

  return (
    <div className="space-y-4">
      {canPost && (
        <div className="flex justify-end">
          <Button onClick={() => setShowForm(true)}>
            <Plus className="h-4 w-4" /> Novo Aviso
          </Button>
        </div>
      )}

      {isLoading && <p className="text-sm text-muted-foreground">Carregando avisos...</p>}

      {isError && (
        <p className="text-sm text-muted-foreground">Não foi possível carregar os avisos desta turma.</p>
      )}

      {!isLoading && !isError && announcements.length === 0 && (
        <p className="text-sm text-muted-foreground">Nenhum aviso publicado ainda.</p>
      )}

      <div className="space-y-3">
        {announcements.map((announcement) => (
          <AnnouncementCard
            key={announcement.id}
            announcement={announcement}
            canManage={announcement.authorId === userId}
            onEdit={() => setEditing(announcement)}
            onDelete={() => setDeleting(announcement)}
          />
        ))}
      </div>

      <AnnouncementForm
        open={showForm}
        onClose={() => { setShowForm(false); createAnnouncement.reset() }}
        onSubmit={handleCreate}
        isPending={createAnnouncement.isPending}
        error={createAnnouncement.isError ? apiErrorMessage(createAnnouncement.error) : null}
      />

      <AnnouncementForm
        open={!!editing}
        announcement={editing}
        onClose={() => { setEditing(null); updateAnnouncement.reset() }}
        onSubmit={handleUpdate}
        isPending={updateAnnouncement.isPending}
        error={updateAnnouncement.isError ? apiErrorMessage(updateAnnouncement.error) : null}
      />

      <ConfirmDialog
        open={!!deleting}
        title="Excluir aviso"
        description="Excluir este aviso? Esta ação não pode ser desfeita."
        confirmLabel="Excluir"
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleting(null)}
      />
    </div>
  )
}

export default AnnouncementFeed
