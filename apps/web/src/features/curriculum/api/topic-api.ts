import api from '@lib/axios'
import type { Topic } from '../types'

export async function listTopics(subjectId: string): Promise<Topic[]> {
  const res = await api.get<Topic[]>(`/subjects/${subjectId}/topics`)
  return res.data
}

export async function createTopic(subjectId: string, title: string): Promise<Topic> {
  const res = await api.post<Topic>(`/subjects/${subjectId}/topics`, { title })
  return res.data
}

export async function updateTopic(subjectId: string, topicId: string, title: string): Promise<Topic> {
  const res = await api.put<Topic>(`/subjects/${subjectId}/topics/${topicId}`, { title })
  return res.data
}

export async function deleteTopic(subjectId: string, topicId: string): Promise<void> {
  await api.delete(`/subjects/${subjectId}/topics/${topicId}`)
}

export async function reorderTopics(subjectId: string, topicIds: string[]): Promise<Topic[]> {
  const res = await api.put<Topic[]>(`/subjects/${subjectId}/topics/reorder`, { topicIds })
  return res.data
}
