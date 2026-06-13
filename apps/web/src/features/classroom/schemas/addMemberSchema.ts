import { z } from 'zod'

export const addMemberSchema = z.object({
  userId: z.string().min(1, 'Selecione um membro'),
  role: z.enum(['PROFESSOR', 'ALUNO'], { required_error: 'Selecione o papel' }),
})

export type AddMemberFormData = z.infer<typeof addMemberSchema>
