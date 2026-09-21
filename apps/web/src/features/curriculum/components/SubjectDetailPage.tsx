import { useState, type ReactNode } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, BookOpenCheck, Plus, Trash2, Users } from 'lucide-react'
import { useAuthStore } from '@store/authStore'
import { useSubject } from '../hooks/useSubject'
import { useSubjectContents } from '../hooks/useSubjectContents'
import { useTopics } from '../hooks/useTopics'
import { useCreateTopic } from '../hooks/useCreateTopic'
import { useUpdateTopic } from '../hooks/useUpdateTopic'
import { useDeleteTopic } from '../hooks/useDeleteTopic'
import { useCreateContent } from '../hooks/useCreateContent'
import { useDeleteContent } from '../hooks/useDeleteContent'
import { useOrgClassrooms } from '../hooks/useOrgClassrooms'
import { useTeacherCandidates } from '../hooks/useTeacherCandidates'
import { useLinkClassroom } from '../hooks/useLinkClassroom'
import { useUnlinkClassroom } from '../hooks/useUnlinkClassroom'
import { useAssignTeacher } from '../hooks/useAssignTeacher'
import { useRemoveTeacher } from '../hooks/useRemoveTeacher'
import TopicList from './TopicList'
import TopicFormDialog from './TopicFormDialog'
import ContentFormDialog from './ContentFormDialog'
import LinkClassroomDialog from './LinkClassroomDialog'
import AssignTeacherDialog from './AssignTeacherDialog'
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
import { apiErrorMessage } from '@lib/api-error'
import { Button } from '@components/ui/button'
import { Badge } from '@components/ui/badge'
import { roleLabel } from '@lib/roles'

interface SubjectDetailPageProps {
  dashboardSlot?: ReactNode
}

function SubjectDetailPage({ dashboardSlot }: SubjectDetailPageProps) {
  const { subjectId } = useParams<{ subjectId: string }>()
  const id = subjectId!

  const role = useAuthStore((s) => s.role)
  const userId = useAuthStore((s) => s.userId)
  const canManage = role === 'PROFESSOR' || role === 'ADMIN_ORG' || role === 'GESTOR'
  // Vincular turma e atribuir professor é decisão de quem administra a
  // organização — o professor gerencia o conteúdo, não os vínculos.
  const canManageLinks = role === 'ADMIN_ORG' || role === 'GESTOR'

  const { data: subject } = useSubject(id)
  const { data: grouped, isLoading } = useSubjectContents(id)
  const { data: topics = [] } = useTopics(id)

  const [showCreateTopic, setShowCreateTopic] = useState(false)
  const [editTopic, setEditTopic] = useState<{ id: string; title: string } | null>(null)
  const [deleteTopicTarget, setDeleteTopicTarget] = useState<{ id: string; title: string } | null>(null)
  const [defaultTopicId, setDefaultTopicId] = useState<string | undefined>()
  const [showCreateContent, setShowCreateContent] = useState(false)
  const [editContent, setEditContent] = useState<SubjectContent | null>(null)
  const [deleteContentTarget, setDeleteContentTarget] = useState<SubjectContent | null>(null)
  const [showLinkClassroom, setShowLinkClassroom] = useState(false)
  const [showAssignTeacher, setShowAssignTeacher] = useState(false)
  const [unlinkTarget, setUnlinkTarget] = useState<{ id: string; name: string } | null>(null)
  const [removeTeacherTarget, setRemoveTeacherTarget] = useState<{ id: string; name: string } | null>(null)

  const createTopic = useCreateTopic(id)
  const updateTopic = useUpdateTopic(id)
  const deleteTopic = useDeleteTopic(id)
  const createContent = useCreateContent(id)
  const deleteContent = useDeleteContent(id)
  const linkClassroom = useLinkClassroom(id)
  const unlinkClassroom = useUnlinkClassroom(id)
  const assignTeacher = useAssignTeacher(id)
  const removeTeacher = useRemoveTeacher(id)

  // As listas da organização resolvem os nomes: `GET /subjects/{id}` devolve só
  // os ids do vínculo. Quem não administra não as consulta.
  const { data: orgClassrooms = [] } = useOrgClassrooms(canManageLinks)
  const { data: orgTeachers = [] } = useTeacherCandidates(canManageLinks)

  const linkedClassroomIds = subject?.classroomIds ?? []
  const assignedMemberIds = subject?.teacherMemberIds ?? []
  // `GetProfessorDashboardService` só devolve dados para quem leciona esta
  // disciplina. Papel de professor na organização não basta: sem este casamento
  // sobrava o cabeçalho "Dashboard da Disciplina" sobre um bloco vazio.
  const teachesThisSubject = !!userId && (subject?.teacherUserIds ?? []).includes(userId)

  const linkedClassrooms = linkedClassroomIds.map((classroomId) => {
    const found = orgClassrooms.find((c) => c.id === classroomId)
    return { id: classroomId, name: found?.name ?? classroomId, archived: found?.status === 'ARCHIVED' }
  })

  const assignedTeachers = assignedMemberIds.map((memberId) => {
    const found = orgTeachers.find((m) => m.id === memberId)
    return {
      id: memberId,
      name: found?.name ?? found?.email ?? memberId,
      role: found?.role ?? null,
    }
  })

  const handleLinkClassroom = (classroomId: string) => {
    linkClassroom.mutate({ classroomId }, { onSuccess: () => setShowLinkClassroom(false) })
  }

  const handleConfirmUnlink = () => {
    if (!unlinkTarget) return
    unlinkClassroom.mutate(unlinkTarget.id, { onSuccess: () => setUnlinkTarget(null) })
  }

  const handleAssignTeacher = (memberId: string) => {
    assignTeacher.mutate({ memberId }, { onSuccess: () => setShowAssignTeacher(false) })
  }

  const handleConfirmRemoveTeacher = () => {
    if (!removeTeacherTarget) return
    removeTeacher.mutate(removeTeacherTarget.id, { onSuccess: () => setRemoveTeacherTarget(null) })
  }

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
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="flex items-center gap-3">
        <Link to="/curriculum" className="text-muted-foreground hover:text-foreground">
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <div className="flex items-center gap-2">
          <BookOpenCheck className="h-5 w-5 text-accent" />
          {/* Sem o nome, quem chega de uma lista de disciplinas não sabe em qual entrou. */}
          <h2 className="mb-0">{subject?.name ?? 'Conteúdo da Disciplina'}</h2>
          {subject?.code && <span className="text-sm text-muted-foreground">{subject.code}</span>}
        </div>
      </div>

      {dashboardSlot && teachesThisSubject && (
        <div>
          <h4 className="mb-2 text-muted-foreground">Dashboard da Disciplina</h4>
          {dashboardSlot}
        </div>
      )}

      {canManageLinks && (
        <section className="space-y-4 rounded-2xl border border-border p-4">
          <div className="flex items-center gap-2">
            <Users className="h-4 w-4 text-accent" />
            <h4 className="mb-0 text-muted-foreground">Turmas e Professores</h4>
          </div>

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h5 className="mb-0 text-sm">Turmas</h5>
              <Button variant="secondary" onClick={() => setShowLinkClassroom(true)}>
                <Plus className="h-4 w-4" /> Vincular turma
              </Button>
            </div>
            {linkedClassrooms.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                Nenhuma turma vinculada. Sem turma, os alunos não veem esta disciplina.
              </p>
            ) : (
              <ul className="space-y-1">
                {linkedClassrooms.map((classroom) => (
                  <li key={classroom.id} className="flex items-center justify-between gap-2 text-sm">
                    <span className="flex items-center gap-2">
                      {classroom.name}
                      {/* A turma pode ser arquivada depois de vinculada — o vínculo continua valendo. */}
                      {classroom.archived && <Badge variant="neutral">Arquivada</Badge>}
                    </span>
                    <Button
                      variant="ghost"
                      aria-label={`Desvincular ${classroom.name}`}
                      onClick={() => setUnlinkTarget({ id: classroom.id, name: classroom.name })}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h5 className="mb-0 text-sm">Professores</h5>
              <Button variant="secondary" onClick={() => setShowAssignTeacher(true)}>
                <Plus className="h-4 w-4" /> Atribuir professor
              </Button>
            </div>
            {assignedTeachers.length === 0 ? (
              <p className="text-sm text-muted-foreground">
                Nenhum professor atribuído. Sem professor, ninguém cria tarefas aqui.
              </p>
            ) : (
              <ul className="space-y-1">
                {assignedTeachers.map((teacher) => (
                  <li key={teacher.id} className="flex items-center justify-between gap-2 text-sm">
                    <span className="flex items-center gap-2">
                      {teacher.name}
                      {teacher.role && (
                        <span className="text-xs text-muted-foreground">{roleLabel(teacher.role)}</span>
                      )}
                    </span>
                    <Button
                      variant="ghost"
                      aria-label={`Remover ${teacher.name}`}
                      onClick={() => setRemoveTeacherTarget({ id: teacher.id, name: teacher.name })}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </section>
      )}

      <div className="flex items-center justify-between">
        <h4 className="mb-0 text-muted-foreground">Tópicos e Materiais</h4>
        {canManage && (
          <Button onClick={() => setShowCreateTopic(true)}>
            <Plus className="h-4 w-4" /> Novo Tópico
          </Button>
        )}
      </div>

      {isLoading ? (
        <p className="text-sm text-muted-foreground">Carregando conteúdo...</p>
      ) : (
        <TopicList
          topicsWithContents={grouped?.topics ?? []}
          canManage={canManage}
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
        onClose={() => { setShowCreateContent(false); setDefaultTopicId(undefined); createContent.reset() }}
        onSubmit={handleCreateContent}
        isPending={createContent.isPending}
        error={createContent.isError ? apiErrorMessage(createContent.error) : null}
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

      <LinkClassroomDialog
        open={showLinkClassroom}
        onClose={() => { setShowLinkClassroom(false); linkClassroom.reset() }}
        onSubmit={handleLinkClassroom}
        isPending={linkClassroom.isPending}
        linkedClassroomIds={linkedClassroomIds}
        error={linkClassroom.isError ? apiErrorMessage(linkClassroom.error) : null}
      />

      <AssignTeacherDialog
        open={showAssignTeacher}
        onClose={() => { setShowAssignTeacher(false); assignTeacher.reset() }}
        onSubmit={handleAssignTeacher}
        isPending={assignTeacher.isPending}
        assignedMemberIds={assignedMemberIds}
        error={assignTeacher.isError ? apiErrorMessage(assignTeacher.error) : null}
      />

      <ConfirmDialog
        open={!!unlinkTarget}
        title="Desvincular turma"
        description={`Desvincular "${unlinkTarget?.name}" desta disciplina? Os alunos da turma deixam de ver o conteúdo.`}
        confirmLabel="Desvincular"
        error={unlinkClassroom.isError ? apiErrorMessage(unlinkClassroom.error) : null}
        onConfirm={handleConfirmUnlink}
        onCancel={() => { setUnlinkTarget(null); unlinkClassroom.reset() }}
      />

      <ConfirmDialog
        open={!!removeTeacherTarget}
        title="Remover professor"
        description={`Remover "${removeTeacherTarget?.name}" desta disciplina? Ele deixa de criar tarefas aqui.`}
        confirmLabel="Remover"
        error={removeTeacher.isError ? apiErrorMessage(removeTeacher.error) : null}
        onConfirm={handleConfirmRemoveTeacher}
        onCancel={() => { setRemoveTeacherTarget(null); removeTeacher.reset() }}
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
            <Button variant="secondary" onClick={() => setEditContent(null)}>
              Fechar
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}

export default SubjectDetailPage
