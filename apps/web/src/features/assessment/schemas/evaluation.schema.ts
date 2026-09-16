import { z } from 'zod'

/**
 * A nota máxima varia por tarefa, então o schema é construído com ela. O
 * `input type="number"` já tem `max`, mas o navegador só bloqueia o envio — sem
 * dizer o limite. Aqui a mensagem aparece como qualquer outro erro do formulário.
 */
export function createEvaluationSchema(maxScore: number | null) {
  const grade = z
    .number({ invalid_type_error: 'Nota inválida' })
    .min(0, 'Nota não pode ser negativa')

  return z.object({
    grade: z.preprocess(
      (v) => (v === '' || v === undefined || (typeof v === 'number' && isNaN(v)) ? null : v),
      (maxScore != null
        ? grade.max(maxScore, `Nota não pode exceder ${maxScore}`)
        : grade
      ).nullable(),
    ),
    feedback: z.string().min(1, 'Feedback é obrigatório'),
  })
}

export const evaluationSchema = createEvaluationSchema(null)

export type EvaluationFormData = z.infer<typeof evaluationSchema>
