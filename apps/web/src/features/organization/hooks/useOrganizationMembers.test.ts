import { renderHook, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useOrganizationMembers } from './useOrganizationMembers'
import { useRemoveMember } from './useRemoveMember'
import { useChangeMemberRole } from './useChangeMemberRole'
import { useInviteMember } from './useInviteMember'
import * as orgApi from '../api/organization-api'
import { organizationKeys } from '../api/query-keys'

vi.mock('../api/organization-api')

let queryClient: QueryClient

function wrapper({ children }: { children: React.ReactNode }) {
  return createElement(QueryClientProvider, { client: queryClient }, children)
}

beforeEach(() => {
  vi.clearAllMocks()
  queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })
})

const member = {
  id: 'm-1',
  userId: 'user-1',
  name: 'Ana Silva',
  email: 'ana@test.com',
  role: 'PROFESSOR' as const,
  joinedAt: '2026-01-01T10:00:00',
  owner: false,
}

describe('useOrganizationMembers', () => {
  it('exposes the members of the organization', async () => {
    vi.mocked(orgApi.listMembers).mockResolvedValue([member])

    const { result } = renderHook(() => useOrganizationMembers('org-1'), { wrapper })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(orgApi.listMembers).toHaveBeenCalledWith('org-1')
    expect(result.current.data?.[0].name).toBe('Ana Silva')
  })

  it('exposes the error when the request fails', async () => {
    vi.mocked(orgApi.listMembers).mockRejectedValue(new Error('boom'))

    const { result } = renderHook(() => useOrganizationMembers('org-1'), { wrapper })

    await waitFor(() => expect(result.current.isError).toBe(true))
  })

  it('does not fetch without an organization', () => {
    renderHook(() => useOrganizationMembers(''), { wrapper })

    expect(orgApi.listMembers).not.toHaveBeenCalled()
  })
})

describe('member mutations', () => {
  it('invites a member and refreshes the list', async () => {
    vi.mocked(orgApi.inviteMember).mockResolvedValue(undefined)
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries')

    const { result } = renderHook(() => useInviteMember('org-1'), { wrapper })
    result.current.mutate({ email: 'novo@test.com', role: 'ALUNO' })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(orgApi.inviteMember).toHaveBeenCalledWith('org-1', { email: 'novo@test.com', role: 'ALUNO' })
    expect(invalidate).toHaveBeenCalledWith({ queryKey: organizationKeys.members('org-1') })
  })

  it('changes a role and refreshes the list', async () => {
    vi.mocked(orgApi.changeMemberRole).mockResolvedValue(undefined)
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries')

    const { result } = renderHook(() => useChangeMemberRole('org-1'), { wrapper })
    result.current.mutate({ userId: 'user-1', role: 'GESTOR' })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(orgApi.changeMemberRole).toHaveBeenCalledWith('org-1', 'user-1', 'GESTOR')
    expect(invalidate).toHaveBeenCalledWith({ queryKey: organizationKeys.members('org-1') })
  })

  it('removes a member and refreshes the list', async () => {
    vi.mocked(orgApi.removeMember).mockResolvedValue(undefined)
    const invalidate = vi.spyOn(queryClient, 'invalidateQueries')

    const { result } = renderHook(() => useRemoveMember('org-1'), { wrapper })
    result.current.mutate('user-1')

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(orgApi.removeMember).toHaveBeenCalledWith('org-1', 'user-1')
    expect(invalidate).toHaveBeenCalledWith({ queryKey: organizationKeys.members('org-1') })
  })
})
