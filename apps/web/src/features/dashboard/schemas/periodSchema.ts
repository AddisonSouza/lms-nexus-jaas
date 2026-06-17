import { z } from 'zod'

export const periodSchema = z
  .object({
    from: z.string().min(1, 'Data inicial obrigatória'),
    to: z.string().min(1, 'Data final obrigatória'),
  })
  .refine((value) => value.from <= value.to, {
    message: 'A data inicial não pode ser posterior à data final',
    path: ['to'],
  })

export type PeriodFormValues = z.infer<typeof periodSchema>
