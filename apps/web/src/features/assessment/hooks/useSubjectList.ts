import { useQuery } from '@tanstack/react-query'
import api from '@lib/axios'

interface SubjectOption {
  id: string
  name: string
}

async function fetchSubjects(): Promise<SubjectOption[]> {
  const res = await api.get<SubjectOption[]>('/subjects')
  return res.data
}

export function useSubjectList() {
  return useQuery({
    queryKey: ['assessment', 'subjects'],
    queryFn: fetchSubjects,
  })
}
