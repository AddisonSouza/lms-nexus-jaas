import { z } from 'zod'

export const registerSchema = z.object({
  fullName: z
    .string()
    .min(1, 'Nome completo é obrigatório')
    .max(150, 'Nome deve ter no máximo 150 caracteres'),
  email: z
    .string()
    .min(1, 'E-mail é obrigatório')
    .email('E-mail inválido'),
  password: z
    .string()
    .min(8, 'Senha deve ter no mínimo 8 caracteres'),
})

export type RegisterFormData = z.infer<typeof registerSchema>
