import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useAcceptInvitation } from './useAcceptInvitation'
import * as invitationApi from '../api/invitation-api'

vi.mock('../api/invitation-api')

const mockNavigate = vi.fn()
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}))

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useAcceptInvitation', () => {
  it('navigates to org page on success', async () => {
    vi.mocked(invitationApi.acceptInvitation).mockResolvedValue(undefined)

    const { result } = renderHook(() => useAcceptInvitation('org-99'), { wrapper })

    act(() => result.current.mutate('valid-token'))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(invitationApi.acceptInvitation).toHaveBeenCalledWith('valid-token')
    expect(mockNavigate).toHaveBeenCalledWith('/organizations/org-99')
  })

  it('exposes error on 410 (expired invitation)', async () => {
    vi.mocked(invitationApi.acceptInvitation).mockRejectedValue({ response: { status: 410 } })

    const { result } = renderHook(() => useAcceptInvitation('org-99'), { wrapper })

    act(() => result.current.mutate('expired-token'))

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect((result.current.error as { response?: { status?: number } })?.response?.status).toBe(410)
    expect(mockNavigate).not.toHaveBeenCalled()
  })

  it('exposes error on 409 (already a member)', async () => {
    vi.mocked(invitationApi.acceptInvitation).mockRejectedValue({ response: { status: 409 } })

    const { result } = renderHook(() => useAcceptInvitation('org-99'), { wrapper })

    act(() => result.current.mutate('used-token'))

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect((result.current.error as { response?: { status?: number } })?.response?.status).toBe(409)
  })
})
