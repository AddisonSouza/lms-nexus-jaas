import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore } from './authStore'

function fakeJwt(payload: object) {
  return `header.${btoa(JSON.stringify(payload))}.signature`
}

const token = fakeJwt({ sub: 'user-1', org: 'org-1', groups: ['ADMIN_ORG'] })

beforeEach(() => {
  useAuthStore.getState().clearToken()
})

describe('authStore', () => {
  it('opens a session from the token claims', () => {
    useAuthStore.getState().setToken(token)

    const s = useAuthStore.getState()
    expect(s.isAuthenticated).toBe(true)
    expect(s.userId).toBe('user-1')
    expect(s.organizationId).toBe('org-1')
    expect(s.role).toBe('ADMIN_ORG')
    expect(s.signedOutByUser).toBe(false)
  })

  it('marks a sign-out chosen by the user', () => {
    useAuthStore.getState().setToken(token)

    useAuthStore.getState().signOut()

    const s = useAuthStore.getState()
    expect(s.isAuthenticated).toBe(false)
    expect(s.accessToken).toBeNull()
    expect(s.userId).toBeNull()
    expect(s.organizationId).toBeNull()
    expect(s.isBootstrapping).toBe(false)
    expect(s.signedOutByUser).toBe(true)
  })

  // Sessão expirada (refresh que falha) também limpa o token, mas não é uma
  // saída escolhida — a tela de aceite deve continuar guardando o convite.
  it('does not mark a lost session as a sign-out', () => {
    useAuthStore.getState().setToken(token)

    useAuthStore.getState().clearToken()

    expect(useAuthStore.getState().isAuthenticated).toBe(false)
    expect(useAuthStore.getState().signedOutByUser).toBe(false)
  })

  it('forgets the sign-out once a new session starts', () => {
    useAuthStore.getState().signOut()

    useAuthStore.getState().setToken(token)

    expect(useAuthStore.getState().signedOutByUser).toBe(false)
  })
})
