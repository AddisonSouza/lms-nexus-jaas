import api from '@lib/axios'
import type { RegisterFormData } from '../schemas/registerSchema'
import type { LoginFormData } from '../schemas/loginSchema'

export interface RegisterResponse {
  userId: string
  email: string
  status: 'PENDING_CONFIRMATION' | 'ACTIVE' | 'SUSPENDED'
}

export interface LoginResponse {
  accessToken: string
}

export async function registerUser(data: RegisterFormData): Promise<RegisterResponse> {
  const response = await api.post<RegisterResponse>('/auth/register', {
    fullName: data.fullName,
    email: data.email,
    password: data.password,
  })
  return response.data
}

export async function loginUser(data: LoginFormData): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>('/auth/login', data, {
    withCredentials: true,
  })
  return response.data
}

export async function logoutUser(): Promise<void> {
  await api.post('/auth/logout', {}, { withCredentials: true })
}

export async function refreshTokens(): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>('/auth/refresh', {}, {
    withCredentials: true,
  })
  return response.data
}

export async function forgotPassword(email: string): Promise<void> {
  await api.post('/auth/forgot-password', { email })
}

export async function resetPassword(token: string, newPassword: string): Promise<void> {
  await api.post('/auth/reset-password', { token, newPassword })
}

export async function confirmEmail(token: string): Promise<void> {
  await api.get('/auth/confirm-email', { params: { token } })
}

export async function resendConfirmation(email: string): Promise<void> {
  await api.post('/auth/resend-confirmation', { email })
}
