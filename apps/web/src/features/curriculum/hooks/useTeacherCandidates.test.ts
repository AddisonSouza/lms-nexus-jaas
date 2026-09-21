import { renderHook, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useTeacherCandidates } from './useTeacherCandidates'
import * as orgMemberApi from '../api/org-member-api'
import { useAuthStore } from '@store/authStore'

vi.mock('../api/org-member-api')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => {
  vi.clearAllMocks()
  useAuthStore.setState({ organizationId: 'org-1' })
})

describe('useTeacherCandidates', () => {
  it('keeps only the members the API accepts as teacher', async () => {
    vi.mocked(orgMemberApi.listOrgMembers).mockResolvedValue([
      { id: 'm-1', userId: 'u-1', name: 'Ana', email: 'ana@test.com', role: 'PROFESSOR' },
      { id: 'm-2', userId: 'u-2', name: 'Bruno', email: 'bruno@test.com', role: 'GESTOR' },
      { id: 'm-3', userId: 'u-3', name: 'Caio', email: 'caio@test.com', role: 'ADMIN_ORG' },
      { id: 'm-4', userId: 'u-4', name: 'Duda', email: 'duda@test.com', role: 'ALUNO' },
    ])

    const { result } = renderHook(() => useTeacherCandidates(), { wrapper })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.map((m) => m.id)).toEqual(['m-1', 'm-2', 'm-3'])
    expect(orgMemberApi.listOrgMembers).toHaveBeenCalledWith('org-1')
  })

  it('does not fetch without an organization in the session', () => {
    useAuthStore.setState({ organizationId: null })
    renderHook(() => useTeacherCandidates(), { wrapper })
    expect(orgMemberApi.listOrgMembers).not.toHaveBeenCalled()
  })
})
