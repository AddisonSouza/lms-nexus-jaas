import { describe, it, expect, beforeEach } from 'vitest'
import { useAuthStore } from './authStore'

// Real JWTs are base64url over UTF-8 JSON — encode the same way so accented
// names and the `-`/`_` alphabet are exercised.
function fakeJwt(payload: object) {
  const bytes = new TextEncoder().encode(JSON.stringify(payload))
  const base64url = btoa(String.fromCharCode(...bytes))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')
  return `header.${base64url}.signature`
}

const token = fakeJwt({
  sub: 'user-1',
  org: 'org-1',
  groups: ['ADMIN_ORG'],
  name: 'Ana Souza',
  email: 'ana@test.com',
})

beforeEach(() => {
  useAuthStore.getState().clearToken()
})

describe('authStore', () => {
  it('opens a session from the token claims', () => {
    useAuthStore.getState().setToken(token)

    const s = useAuthStore.getState()
    expect(s.isAuthenticated).toBe(true)
    expect(s.userId).toBe('user-1')
    expect(s.userName).toBe('Ana Souza')
    expect(s.userEmail).toBe('ana@test.com')
    expect(s.organizationId).toBe('org-1')
    expect(s.role).toBe('ADMIN_ORG')
    expect(s.signedOutByUser).toBe(false)
  })

  it('keeps accents in the name and the other claims intact', () => {
    useAuthStore.getState().setToken(
      fakeJwt({ sub: 'user-2', org: 'org-1', groups: ['PROFESSOR'], name: 'João Conceição', email: 'joao@test.com' }),
    )

    const s = useAuthStore.getState()
    expect(s.userName).toBe('João Conceição')
    expect(s.role).toBe('PROFESSOR')
  })

  it('leaves name and e-mail empty when the token has none', () => {
    useAuthStore.getState().setToken(fakeJwt({ sub: 'user-1', groups: [] }))

    const s = useAuthStore.getState()
    expect(s.userName).toBeNull()
    expect(s.userEmail).toBeNull()
  })

  it('marks a sign-out chosen by the user', () => {
    useAuthStore.getState().setToken(token)

    useAuthStore.getState().signOut()

    const s = useAuthStore.getState()
    expect(s.isAuthenticated).toBe(false)
    expect(s.accessToken).toBeNull()
    expect(s.userId).toBeNull()
    expect(s.userName).toBeNull()
    expect(s.userEmail).toBeNull()
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
    expect(useAuthStore.getState().userName).toBeNull()
    expect(useAuthStore.getState().signedOutByUser).toBe(false)
  })

  it('forgets the sign-out once a new session starts', () => {
    useAuthStore.getState().signOut()

    useAuthStore.getState().setToken(token)

    expect(useAuthStore.getState().signedOutByUser).toBe(false)
  })
})
