import api from '@lib/axios'
import type { RegisterFormData } from '../schemas/registerSchema'

export interface RegisterResponse {
  userId: string
  email: string
  status: 'PENDING_CONFIRMATION' | 'ACTIVE' | 'SUSPENDED'
}

export async function registerUser(data: RegisterFormData): Promise<RegisterResponse> {
  const response = await api.post<RegisterResponse>('/auth/register', {
    fullName: data.fullName,
    email: data.email,
    password: data.password,
  })
  return response.data
}
