import { create } from 'zustand'

interface AuthState {
  accessToken: string | null
  organizationId: string | null
  isAuthenticated: boolean
  setToken: (token: string) => void
  setOrganization: (token: string, organizationId: string) => void
  clearToken: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: localStorage.getItem('access_token'),
  organizationId: localStorage.getItem('organization_id'),
  isAuthenticated: !!localStorage.getItem('access_token'),

  setToken: (token) => {
    localStorage.setItem('access_token', token)
    set({ accessToken: token, isAuthenticated: true })
  },

  setOrganization: (token, organizationId) => {
    localStorage.setItem('access_token', token)
    localStorage.setItem('organization_id', organizationId)
    set({ accessToken: token, organizationId, isAuthenticated: true })
  },

  clearToken: () => {
    localStorage.removeItem('access_token')
    localStorage.removeItem('organization_id')
    set({ accessToken: null, organizationId: null, isAuthenticated: false })
  },
}))
