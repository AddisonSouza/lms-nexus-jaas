import { renderHook, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useOrgClassrooms } from './useOrgClassrooms'
import * as orgClassroomApi from '../api/org-classroom-api'

vi.mock('../api/org-classroom-api')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useOrgClassrooms', () => {
  it('returns the organization classrooms', async () => {
    vi.mocked(orgClassroomApi.listOrgClassrooms).mockResolvedValue([
      { id: 'c-1', name: 'Turma A', status: 'ACTIVE' },
      { id: 'c-2', name: 'Turma B', status: 'ARCHIVED' },
    ])

    const { result } = renderHook(() => useOrgClassrooms(), { wrapper })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toHaveLength(2)
  })

  it('does not fetch while disabled', () => {
    renderHook(() => useOrgClassrooms(false), { wrapper })
    expect(orgClassroomApi.listOrgClassrooms).not.toHaveBeenCalled()
  })
})
