import { z } from 'zod'

export const joinClassroomSchema = z.object({
  // O campo aparenta caixa alta por CSS, mas o valor digitado continua como veio:
  // normalizar antes de validar evita recusar um código correto por causa da caixa.
  inviteCode: z
    .string()
    .trim()
    .toUpperCase()
    .length(6, 'O código deve ter exatamente 6 caracteres')
    .regex(/^[A-Z0-9]{6}$/, 'Código inválido — use apenas letras e números'),
})

export type JoinClassroomFormData = z.infer<typeof joinClassroomSchema>
