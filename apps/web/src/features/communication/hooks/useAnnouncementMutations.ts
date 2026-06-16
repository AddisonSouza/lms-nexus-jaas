import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createAnnouncement, updateAnnouncement, deleteAnnouncement } from '../api/announcements'
import { announcementKeys } from '../api/query-keys'

export function useCreateAnnouncement(classroomId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createAnnouncement,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: announcementKeys.byClassroom(classroomId) })
    },
  })
}

export function useUpdateAnnouncement(classroomId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: updateAnnouncement,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: announcementKeys.byClassroom(classroomId) })
    },
  })
}

export function useDeleteAnnouncement(classroomId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: deleteAnnouncement,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: announcementKeys.byClassroom(classroomId) })
    },
  })
}
