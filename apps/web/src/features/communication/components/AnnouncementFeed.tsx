import { useState } from 'react'
import { Plus } from 'lucide-react'
import { useAuthStore } from '@store/authStore'
import { useAnnouncements } from '../hooks/useAnnouncements'
import { useCreateAnnouncement, useUpdateAnnouncement, useDeleteAnnouncement } from '../hooks/useAnnouncementMutations'
import AnnouncementCard from './AnnouncementCard'
import AnnouncementForm from './AnnouncementForm'
import ConfirmDialog from '@components/shared/ConfirmDialog'
import type { Announcement } from '../types'
import type { AnnouncementFormData } from '../schemas/announcementSchema'

interface Props {
  classroomId: string
}

function AnnouncementFeed({ classroomId }: Props) {
  const userId = useAuthStore((s) => s.userId)
  const role = useAuthStore((s) => s.role)
  const canPost = role === 'PROFESSOR'

  const { data: announcements = [], isLoading, isError } = useAnnouncements(classroomId)
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

  return (
    <div className="space-y-4">
      {canPost && (
        <div className="flex justify-end">
          <button
            onClick={() => setShowForm(true)}
            className="flex items-center gap-1 rounded bg-primary px-3 py-1.5 text-sm text-primary-foreground hover:opacity-90"
          >
            <Plus className="h-4 w-4" /> Novo Aviso
          </button>
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
        onClose={() => setShowForm(false)}
        onSubmit={handleCreate}
        isPending={createAnnouncement.isPending}
      />

      <AnnouncementForm
        open={!!editing}
        announcement={editing}
        onClose={() => setEditing(null)}
        onSubmit={handleUpdate}
        isPending={updateAnnouncement.isPending}
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
