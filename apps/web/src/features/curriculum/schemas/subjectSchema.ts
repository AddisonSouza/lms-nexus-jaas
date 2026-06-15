import { z } from 'zod'

export const subjectSchema = z.object({
  name: z.string().min(2, 'Nome deve ter ao menos 2 caracteres').max(255, 'Nome muito longo'),
  code: z.string().max(20, 'Código deve ter no máximo 20 caracteres').optional().or(z.literal('')),
  description: z.string().max(2000, 'Descrição muito longa').optional().or(z.literal('')),
  workloadHours: z
    .number({ invalid_type_error: 'Carga horária deve ser um número' })
    .int('Carga horária deve ser inteira')
    .positive('Carga horária deve ser positiva')
    .optional()
    .nullable(),
})

export type SubjectFormData = z.infer<typeof subjectSchema>
