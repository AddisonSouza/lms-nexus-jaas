import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useCreateSubject } from './useCreateSubject'
import * as subjectApi from '../api/subject-api'

vi.mock('../api/subject-api')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false }, queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useCreateSubject', () => {
  it('calls createSubject and resolves on success', async () => {
    vi.mocked(subjectApi.createSubject).mockResolvedValue({
      id: 'sub-1',
      name: 'Matemática',
      code: 'MAT01',
      description: null,
      workloadHours: 60,
      organizationId: 'org-1',
      classroomIds: [],
      teacherMemberIds: [],
      createdAt: '',
    })

    const { result } = renderHook(() => useCreateSubject(), { wrapper })
    act(() => result.current.mutate({ name: 'Matemática', code: 'MAT01', workloadHours: 60 }))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(subjectApi.createSubject).toHaveBeenCalledWith(
      { name: 'Matemática', code: 'MAT01', workloadHours: 60 },
      expect.any(Object),
    )
  })

  it('exposes error on failure', async () => {
    vi.mocked(subjectApi.createSubject).mockRejectedValue({ response: { status: 422 } })

    const { result } = renderHook(() => useCreateSubject(), { wrapper })
    act(() => result.current.mutate({ name: '' }))

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
