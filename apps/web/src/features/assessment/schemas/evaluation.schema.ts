import { z } from 'zod'

export const evaluationSchema = z.object({
  grade: z.preprocess(
    (v) => (v === '' || v === undefined || (typeof v === 'number' && isNaN(v)) ? null : v),
    z.number({ invalid_type_error: 'Nota inválida' }).min(0, 'Nota não pode ser negativa').nullable(),
  ),
  feedback: z.string().min(1, 'Feedback é obrigatório'),
})

export type EvaluationFormData = z.infer<typeof evaluationSchema>
