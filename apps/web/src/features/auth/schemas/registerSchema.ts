import { z } from 'zod'
import { passwordSchema } from './passwordSchema'

export const registerSchema = z
  .object({
    fullName: z
      .string()
      .min(1, 'Nome completo é obrigatório')
      .max(150, 'Nome deve ter no máximo 150 caracteres'),
    email: z
      .string()
      .min(1, 'E-mail é obrigatório')
      .email('E-mail inválido'),
    password: passwordSchema,
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'As senhas não conferem',
    path: ['confirmPassword'],
  })

export type RegisterFormData = z.infer<typeof registerSchema>
