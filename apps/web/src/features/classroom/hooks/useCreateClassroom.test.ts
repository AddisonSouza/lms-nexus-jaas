import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useCreateClassroom } from './useCreateClassroom'
import * as classroomApi from '../api/classroom-api'

vi.mock('../api/classroom-api')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false }, queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useCreateClassroom', () => {
  it('calls createClassroom and invalidates list query on success', async () => {
    vi.mocked(classroomApi.createClassroom).mockResolvedValue({
      id: 'cls-1',
      name: 'Turma A',
      description: null,
      academicPeriod: '2025/1',
      status: 'ACTIVE',
      inviteCode: 'ABC123',
      organizationId: 'org-1',
      createdAt: '',
    })

    const { result } = renderHook(() => useCreateClassroom(), { wrapper })

    act(() => result.current.mutate({ name: 'Turma A', academicPeriod: '2025/1' }))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(classroomApi.createClassroom).toHaveBeenCalledWith({ name: 'Turma A', academicPeriod: '2025/1' })
  })

  it('exposes error on failure', async () => {
    vi.mocked(classroomApi.createClassroom).mockRejectedValue({ response: { status: 400 } })

    const { result } = renderHook(() => useCreateClassroom(), { wrapper })
    act(() => result.current.mutate({ name: '', academicPeriod: '' }))

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
