import { z } from 'zod'

export const joinClassroomSchema = z.object({
  inviteCode: z
    .string()
    .length(6, 'O código deve ter exatamente 6 caracteres')
    .regex(/^[A-Z0-9]{6}$/, 'Código inválido — use apenas letras maiúsculas e números'),
})

export type JoinClassroomFormData = z.infer<typeof joinClassroomSchema>
