import { renderHook, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useOrganizations } from './useOrganizations'
import * as orgApi from '../api/organization-api'

vi.mock('../api/organization-api')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useOrganizations', () => {
  it('exposes the organizations of the authenticated user', async () => {
    vi.mocked(orgApi.listOrganizations).mockResolvedValue([
      { id: 'org-1', name: 'Alfa', role: 'ADMIN_ORG' },
      { id: 'org-2', name: 'Beta', role: 'ALUNO' },
    ])

    const { result } = renderHook(() => useOrganizations(), { wrapper })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toHaveLength(2)
    expect(result.current.data?.[0].name).toBe('Alfa')
  })

  it('exposes the error when the request fails', async () => {
    vi.mocked(orgApi.listOrganizations).mockRejectedValue(new Error('boom'))

    const { result } = renderHook(() => useOrganizations(), { wrapper })

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
