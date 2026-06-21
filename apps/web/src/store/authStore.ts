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
  // True until the initial silent-refresh on app boot resolves, so guards can
  // wait instead of bouncing a still-logged-in user to /login on a page reload.
  isBootstrapping: boolean
  setToken: (token: string) => void
  clearToken: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  role: null,
  userId: null,
  organizationId: null,
  isAuthenticated: false,
  isBootstrapping: true,

  setToken: (token) => {
    const payload = decodeJwtPayload(token)
    set({
      accessToken: token,
      isAuthenticated: true,
      isBootstrapping: false,
      role: payload.groups?.[0] ?? null,
      userId: payload.sub ?? null,
      organizationId: payload.org ?? null,
    })
  },

  clearToken: () => {
    set({
      accessToken: null,
      role: null,
      userId: null,
      organizationId: null,
      isAuthenticated: false,
      isBootstrapping: false,
    })
  },
}))
