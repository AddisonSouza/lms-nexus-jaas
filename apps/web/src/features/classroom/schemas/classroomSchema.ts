import { z } from 'zod'

export const classroomSchema = z.object({
  name: z.string().min(2, 'Nome deve ter ao menos 2 caracteres').max(255, 'Nome muito longo'),
  description: z.string().max(2000, 'Descrição muito longa').optional(),
  academicPeriod: z.string().min(1, 'Período letivo é obrigatório').max(100, 'Período muito longo'),
  status: z.enum(['ACTIVE', 'ARCHIVED']).optional(),
})

export type ClassroomFormData = z.infer<typeof classroomSchema>
