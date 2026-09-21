import { useParams } from 'react-router-dom'
import AnnouncementFeed from '@features/communication/components/AnnouncementFeed'
import ClassroomDetailPage from '@features/classroom/components/ClassroomDetailPage'
import { useClassroomMembers } from '@features/classroom/hooks/useClassroomMembers'
import { useAuthStore } from '@store/authStore'

/**
 * A associação à turma é resolvida aqui, e não dentro do mural: `AnnouncementFeed`
 * vive em `communication` e não pode importar de `classroom`. A camada `app` é o
 * ponto legítimo de composição entre as duas.
 */
function ClassroomDetailRoute() {
  const { id } = useParams<{ id: string }>()
  const userId = useAuthStore((s) => s.userId)
  const { data: members } = useClassroomMembers(id ?? '')

  const membership = members?.find((m) => m.userId === userId)

  return (
    <ClassroomDetailPage
      announcementFeedSlot={
        id && membership ? (
          <AnnouncementFeed
            classroomId={id}
            isMember
            canPost={membership.role === 'PROFESSOR'}
          />
        ) : null
      }
    />
  )
}

export default ClassroomDetailRoute
