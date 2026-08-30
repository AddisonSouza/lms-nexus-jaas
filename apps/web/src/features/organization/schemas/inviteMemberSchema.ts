import { z } from 'zod'

export const inviteMemberSchema = z.object({
  email: z.string().min(1, 'Informe o e-mail').email('E-mail inválido'),
  role: z.enum(['GESTOR', 'PROFESSOR', 'ALUNO'], { required_error: 'Selecione o papel' }),
})

export type InviteMemberFormData = z.infer<typeof inviteMemberSchema>
