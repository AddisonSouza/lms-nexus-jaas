import { z } from 'zod'

const MIN_LENGTH = 8

const criteria = [
  { label: 'uma maiúscula', isMet: (value: string) => /[A-Z]/.test(value) },
  { label: 'uma minúscula', isMet: (value: string) => /[a-z]/.test(value) },
  { label: 'um número', isMet: (value: string) => /\d/.test(value) },
  { label: 'um símbolo', isMet: (value: string) => /[^A-Za-z0-9]/.test(value) },
]

export const passwordSchema = z
  .string()
  .min(MIN_LENGTH, `Senha deve ter no mínimo ${MIN_LENGTH} caracteres`)
  .superRefine((value, ctx) => {
    const missing = criteria.filter((criterion) => !criterion.isMet(value))
    if (missing.length === 0) return

    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      message: `A senha precisa de: ${missing.map((criterion) => criterion.label).join(', ')}`,
    })
  })
