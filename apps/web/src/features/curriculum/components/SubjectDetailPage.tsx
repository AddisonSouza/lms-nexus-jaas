import { useState, type ReactNode } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, BookOpenCheck, Plus } from 'lucide-react'
import { useAuthStore } from '@store/authStore'
import { API_BASE_URL } from '@lib/axios'
import { useSubjectContents } from '../hooks/useSubjectContents'
import { useTopics } from '../hooks/useTopics'
import { useCreateTopic } from '../hooks/useCreateTopic'
import { useUpdateTopic } from '../hooks/useUpdateTopic'
import { useDeleteTopic } from '../hooks/useDeleteTopic'
import { useCreateContent } from '../hooks/useCreateContent'
import { useDeleteContent } from '../hooks/useDeleteContent'
import TopicList from './TopicList'
import TopicFormDialog from './TopicFormDialog'
import ContentFormDialog from './ContentFormDialog'
import type { TopicFormData } from '../schemas/topicSchema'
import type { ContentFormData } from '../schemas/contentSchema'
import type { SubjectContent } from '../types'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@components/ui/dialog'
import ConfirmDialog from '@components/shared/ConfirmDialog'

interface SubjectDetailPageProps {
  dashboardSlot?: ReactNode
}

function SubjectDetailPage({ dashboardSlot }: SubjectDetailPageProps) {
  const { subjectId } = useParams<{ subjectId: string }>()
  const id = subjectId!

  const role = useAuthStore((s) => s.role)
  const canManage = role === 'PROFESSOR' || role === 'ADMIN_ORG' || role === 'GESTOR'

  const { data: grouped, isLoading } = useSubjectContents(id)
  const { data: topics = [] } = useTopics(id)

  const [showCreateTopic, setShowCreateTopic] = useState(false)
  const [editTopic, setEditTopic] = useState<{ id: string; title: string } | null>(null)
  const [deleteTopicTarget, setDeleteTopicTarget] = useState<{ id: string; title: string } | null>(null)
  const [defaultTopicId, setDefaultTopicId] = useState<string | undefined>()
  const [showCreateContent, setShowCreateContent] = useState(false)
  const [editContent, setEditContent] = useState<SubjectContent | null>(null)
  const [deleteContentTarget, setDeleteContentTarget] = useState<SubjectContent | null>(null)

  const createTopic = useCreateTopic(id)
  const updateTopic = useUpdateTopic(id)
  const deleteTopic = useDeleteTopic(id)
  const createContent = useCreateContent(id)
  const deleteContent = useDeleteContent(id)

  const handleCreateTopic = (data: TopicFormData) => {
    createTopic.mutate(data.title, { onSuccess: () => setShowCreateTopic(false) })
  }

  const handleUpdateTopic = (data: TopicFormData) => {
    if (!editTopic) return
    updateTopic.mutate(
      { topicId: editTopic.id, title: data.title },
      { onSuccess: () => setEditTopic(null) },
    )
  }

  const handleDeleteTopic = (topicId: string, title: string) => {
    setDeleteTopicTarget({ id: topicId, title })
  }

  const handleConfirmDeleteTopic = () => {
    if (!deleteTopicTarget) return
    deleteTopic.mutate(deleteTopicTarget.id, { onSuccess: () => setDeleteTopicTarget(null) })
  }

  const handleAddContent = (topicId: string) => {
    setDefaultTopicId(topicId)
    setShowCreateContent(true)
  }

  const handleCreateContent = (data: ContentFormData) => {
    createContent.mutate(
      {
        topicId: data.topicId,
        title: data.title,
        contentType: data.contentType,
        externalUrl: (data as { externalUrl?: string }).externalUrl,
        description: data.description || undefined,
        file: (data as { file?: File }).file,
      },
      { onSuccess: () => setShowCreateContent(false) },
    )
  }

  const handleDeleteContent = (content: SubjectContent) => {
    setDeleteContentTarget(content)
  }

  const handleConfirmDeleteContent = () => {
    if (!deleteContentTarget) return
    deleteContent.mutate(deleteContentTarget.id, { onSuccess: () => setDeleteContentTarget(null) })
  }

  return (
    <div className="container mx-auto max-w-4xl p-6 space-y-6">
      <div className="flex items-center gap-3">
        <Link to="/curriculum" className="text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <div className="flex items-center gap-2">
          <BookOpenCheck className="h-5 w-5" />
          <h1 className="text-xl font-semibold">Conteúdo da Disciplina</h1>
        </div>
      </div>

      {dashboardSlot && (
        <div>
          <h2 className="mb-2 text-base font-medium text-muted-foreground">Dashboard da Disciplina</h2>
          {dashboardSlot}
        </div>
      )}

      <div className="flex items-center justify-between">
        <h2 className="text-base font-medium text-muted-foreground">Tópicos e Materiais</h2>
        {canManage && (
          <button
            onClick={() => setShowCreateTopic(true)}
            className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground"
          >
            <Plus className="h-4 w-4" /> Novo Tópico
          </button>
        )}
      </div>

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Carregando conteúdo...</p>
      ) : (
        <TopicList
          topicsWithContents={grouped?.topics ?? []}
          canManage={canManage}
          apiBaseUrl={`${API_BASE_URL}/api`}
          onEditTopic={(topicId, title) => setEditTopic({ id: topicId, title })}
          onDeleteTopic={(topicId, title) => handleDeleteTopic(topicId, title)}
          onAddContent={handleAddContent}
          onEditContent={(content) => setEditContent(content)}
          onDeleteContent={(content) => handleDeleteContent(content)}
        />
      )}

      <TopicFormDialog
        open={showCreateTopic}
        onClose={() => setShowCreateTopic(false)}
        onSubmit={handleCreateTopic}
        isPending={createTopic.isPending}
        title="Novo Tópico"
      />

      <TopicFormDialog
        open={!!editTopic}
        onClose={() => setEditTopic(null)}
        onSubmit={handleUpdateTopic}
        isPending={updateTopic.isPending}
        defaultValues={editTopic ?? undefined}
        title="Editar Tópico"
      />

      <ContentFormDialog
        open={showCreateContent}
        onClose={() => { setShowCreateContent(false); setDefaultTopicId(undefined) }}
        onSubmit={handleCreateContent}
        isPending={createContent.isPending}
        topics={topics}
        defaultTopicId={defaultTopicId}
        title="Novo Conteúdo"
      />

      <ConfirmDialog
        open={!!deleteTopicTarget}
        title="Excluir tópico"
        description={`Excluir "${deleteTopicTarget?.title}" e todos os conteúdos vinculados? Esta ação não pode ser desfeita.`}
        confirmLabel="Excluir"
        onConfirm={handleConfirmDeleteTopic}
        onCancel={() => setDeleteTopicTarget(null)}
      />

      <ConfirmDialog
        open={!!deleteContentTarget}
        title="Excluir conteúdo"
        description={`Excluir "${deleteContentTarget?.title}"? Esta ação não pode ser desfeita.`}
        confirmLabel="Excluir"
        onConfirm={handleConfirmDeleteContent}
        onCancel={() => setDeleteContentTarget(null)}
      />

      <Dialog open={!!editContent} onOpenChange={(isOpen) => { if (!isOpen) setEditContent(null) }}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Editar Conteúdo</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-muted-foreground">
            Edição de conteúdo não disponível para arquivos. Exclua e recrie.
          </p>
          <div className="flex justify-end">
            <button onClick={() => setEditContent(null)} className="rounded border px-4 py-2 text-sm">
              Fechar
            </button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}

export default SubjectDetailPage
