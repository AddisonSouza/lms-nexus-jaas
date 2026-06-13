import { useQuery } from '@tanstack/react-query'
import { getClassroomMembers } from '../api/classroom-api'
import { classroomKeys } from '../api/query-keys'

export function useClassroomMembers(classroomId: string) {
  return useQuery({
    queryKey: classroomKeys.members(classroomId),
    queryFn: () => getClassroomMembers(classroomId),
    enabled: !!classroomId,
  })
}
