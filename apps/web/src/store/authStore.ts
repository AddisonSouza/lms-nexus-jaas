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
  // True only after the user chose to sign out. Losing the session any other way
  // (expired refresh, fresh load without a cookie) leaves it false, so screens can
  // tell "the user left" apart from "the session is gone".
  signedOutByUser: boolean
  setToken: (token: string) => void
  clearToken: () => void
  signOut: () => void
}

const signedOutState = {
  accessToken: null,
  role: null,
  userId: null,
  organizationId: null,
  isAuthenticated: false,
  isBootstrapping: false,
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  role: null,
  userId: null,
  organizationId: null,
  isAuthenticated: false,
  isBootstrapping: true,
  signedOutByUser: false,

  setToken: (token) => {
    const payload = decodeJwtPayload(token)
    set({
      accessToken: token,
      isAuthenticated: true,
      isBootstrapping: false,
      signedOutByUser: false,
      role: payload.groups?.[0] ?? null,
      userId: payload.sub ?? null,
      organizationId: payload.org ?? null,
    })
  },

  clearToken: () => {
    set({ ...signedOutState, signedOutByUser: false })
  },

  signOut: () => {
    set({ ...signedOutState, signedOutByUser: true })
  },
}))
