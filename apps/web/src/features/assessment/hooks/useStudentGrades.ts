import { useQuery } from '@tanstack/react-query'
import { listStudentGrades } from '../api/submissions'
import { submissionKeys } from '../api/query-keys'

export function useStudentGrades() {
  return useQuery({
    queryKey: submissionKeys.myGrades(),
    queryFn: listStudentGrades,
  })
}
