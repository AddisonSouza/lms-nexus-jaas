import { z } from 'zod'

export const createOrganizationSchema = z.object({
  name: z.string().min(2, 'Nome deve ter ao menos 2 caracteres').max(100, 'Nome muito longo'),
  description: z.string().max(500, 'Descrição muito longa').optional(),
})

export type CreateOrganizationFormData = z.infer<typeof createOrganizationSchema>
