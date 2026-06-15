import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useCreateTask } from './useCreateTask'
import * as tasksApi from '../api/tasks'
import type { Task } from '../types'

vi.mock('../api/tasks')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false }, queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

const mockTask: Task = {
  id: 'task-1',
  subjectId: 'sub-1',
  organizationId: 'org-1',
  createdBy: 'user-1',
  title: 'Tarefa',
  description: 'Enunciado',
  deadline: '2026-12-31T23:59:00',
  maxScore: null,
  status: 'DRAFT',
  attachments: [],
  createdAt: '',
  updatedAt: null,
}

beforeEach(() => vi.clearAllMocks())

describe('useCreateTask', () => {
  it('calls createTask and resolves on success', async () => {
    vi.mocked(tasksApi.createTask).mockResolvedValue(mockTask)

    const { result } = renderHook(() => useCreateTask(), { wrapper })
    act(() => result.current.mutate({ subjectId: 'sub-1', title: 'Tarefa', description: 'Enunciado', deadline: '2026-12-31T23:59' }))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(tasksApi.createTask).toHaveBeenCalledTimes(1)
  })

  it('exposes error on failure', async () => {
    vi.mocked(tasksApi.createTask).mockRejectedValue({ response: { status: 400 } })

    const { result } = renderHook(() => useCreateTask(), { wrapper })
    act(() => result.current.mutate({ subjectId: 'sub-1', title: '', description: '', deadline: '' }))

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
