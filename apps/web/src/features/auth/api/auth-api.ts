import { z } from 'zod'
import api from '@lib/axios'
import type { RegisterFormData } from '../schemas/registerSchema'
import type { LoginFormData } from '../schemas/loginSchema'

const registerResponseSchema = z.object({
  userId: z.string(),
  email: z.string(),
  status: z.enum(['PENDING_CONFIRMATION', 'ACTIVE', 'SUSPENDED']),
})

const loginResponseSchema = z.object({
  accessToken: z.string(),
})

export type RegisterResponse = z.infer<typeof registerResponseSchema>
export type LoginResponse = z.infer<typeof loginResponseSchema>

export async function registerUser(data: RegisterFormData): Promise<RegisterResponse> {
  const response = await api.post('/auth/register', {
    fullName: data.fullName,
    email: data.email,
    password: data.password,
  })
  return registerResponseSchema.parse(response.data)
}

export async function loginUser(data: LoginFormData): Promise<LoginResponse> {
  const response = await api.post('/auth/login', data, {
    withCredentials: true,
  })
  return loginResponseSchema.parse(response.data)
}

export async function logoutUser(): Promise<void> {
  await api.post('/auth/logout', {}, { withCredentials: true })
}

export async function refreshTokens(organizationId?: string): Promise<LoginResponse> {
  const response = await api.post(
    '/auth/refresh',
    organizationId ? { organizationId } : {},
    { withCredentials: true },
  )
  return loginResponseSchema.parse(response.data)
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
