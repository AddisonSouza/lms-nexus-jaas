import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { usePublishTask } from './usePublishTask'
import * as tasksApi from '../api/tasks'
import type { Task } from '../types'

vi.mock('../api/tasks')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false }, queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

const mockPublishedTask: Task = {
  id: 'task-1',
  subjectId: 'sub-1',
  organizationId: 'org-1',
  createdBy: 'user-1',
  title: 'Tarefa',
  description: 'Enunciado',
  deadline: '2026-12-31T23:59:00',
  maxScore: null,
  status: 'PUBLISHED',
  attachments: [],
  createdAt: '',
  updatedAt: null,
}

beforeEach(() => vi.clearAllMocks())

describe('usePublishTask', () => {
  it('calls publishTask with taskId and resolves on success', async () => {
    vi.mocked(tasksApi.publishTask).mockResolvedValue(mockPublishedTask)

    const { result } = renderHook(() => usePublishTask(), { wrapper })
    act(() => result.current.mutate('task-1'))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(tasksApi.publishTask).toHaveBeenCalledWith('task-1')
  })

  it('exposes error when task not found or already published', async () => {
    vi.mocked(tasksApi.publishTask).mockRejectedValue({ response: { status: 409 } })

    const { result } = renderHook(() => usePublishTask(), { wrapper })
    act(() => result.current.mutate('task-1'))

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
