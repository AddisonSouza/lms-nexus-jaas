import { create } from 'zustand'

interface JwtPayload {
  sub?: string
  org?: string
  groups?: string[]
}

function decodeJwtPayload(token: string): JwtPayload {
  try {
    return JSON.parse(atob(token.split('.')[1]))
  } catch {
    return {}
  }
}

interface AuthState {
  accessToken: string | null
  role: string | null
  userId: string | null
  organizationId: string | null
  isAuthenticated: boolean
  setToken: (token: string) => void
  setOrganization: (token: string, organizationId: string) => void
  clearToken: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  role: null,
  userId: null,
  organizationId: null,
  isAuthenticated: false,

  setToken: (token) => {
    const payload = decodeJwtPayload(token)
    set({
      accessToken: token,
      isAuthenticated: true,
      role: payload.groups?.[0] ?? null,
      userId: payload.sub ?? null,
      organizationId: payload.org ?? null,
    })
  },

  setOrganization: (token, organizationId) => {
    const payload = decodeJwtPayload(token)
    set({
      accessToken: token,
      isAuthenticated: true,
      role: payload.groups?.[0] ?? null,
      userId: payload.sub ?? null,
      organizationId: organizationId,
    })
  },

  clearToken: () => {
    set({ accessToken: null, role: null, userId: null, organizationId: null, isAuthenticated: false })
  },
}))
