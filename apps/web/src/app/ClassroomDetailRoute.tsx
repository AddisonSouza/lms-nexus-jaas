import { useParams } from 'react-router-dom'
import AnnouncementFeed from '@features/communication/components/AnnouncementFeed'
import ClassroomDetailPage from '@features/classroom/components/ClassroomDetailPage'

function ClassroomDetailRoute() {
  const { id } = useParams<{ id: string }>()

  return (
    <ClassroomDetailPage
      announcementFeedSlot={id ? <AnnouncementFeed classroomId={id} /> : null}
    />
  )
}

export default ClassroomDetailRoute
