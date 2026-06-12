import { renderHook, act, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { createElement } from 'react'
import { useConfirmEmail } from './useConfirmEmail'
import * as authApi from '../api/auth-api'

vi.mock('../api/auth-api')

function wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { mutations: { retry: false } } })
  return createElement(QueryClientProvider, { client: qc }, children)
}

beforeEach(() => vi.clearAllMocks())

describe('useConfirmEmail', () => {
  it('succeeds with 204 (no content)', async () => {
    vi.mocked(authApi.confirmEmail).mockResolvedValue(undefined)

    const { result } = renderHook(() => useConfirmEmail(), { wrapper })

    act(() => result.current.mutate('valid-token'))

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(authApi.confirmEmail).toHaveBeenCalledWith('valid-token')
  })

  it('exposes error on 400 (invalid token)', async () => {
    const err = { response: { status: 400 } }
    vi.mocked(authApi.confirmEmail).mockRejectedValue(err)

    const { result } = renderHook(() => useConfirmEmail(), { wrapper })

    act(() => result.current.mutate('bad-token'))

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect((result.current.error as { response?: { status?: number } })?.response?.status).toBe(400)
  })

  it('exposes error on 409 (already confirmed)', async () => {
    const err = { response: { status: 409 } }
    vi.mocked(authApi.confirmEmail).mockRejectedValue(err)

    const { result } = renderHook(() => useConfirmEmail(), { wrapper })

    act(() => result.current.mutate('old-token'))

    await waitFor(() => expect(result.current.isError).toBe(true))
    expect((result.current.error as { response?: { status?: number } })?.response?.status).toBe(409)
  })
})
