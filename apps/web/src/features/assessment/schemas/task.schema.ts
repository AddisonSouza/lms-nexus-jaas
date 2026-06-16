import { z } from 'zod'

const ALLOWED_TYPES = ['application/pdf', 'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/zip', 'image/jpeg', 'image/png']

export const taskSchema = z.object({
  subjectId: z.string().min(1, 'Disciplina é obrigatória'),
  title: z.string().min(1, 'Título é obrigatório').max(255, 'Título muito longo'),
  description: z.string().min(1, 'Enunciado é obrigatório'),
  deadline: z
    .string()
    .min(1, 'Prazo é obrigatório')
    .refine((v) => new Date(v) > new Date(), { message: 'Prazo deve ser uma data futura' }),
  maxScore: z
    .number({ invalid_type_error: 'Pontuação deve ser um número' })
    .positive('Pontuação deve ser positiva')
    .optional()
    .nullable(),
  files: z
    .array(z.instanceof(File))
    .optional()
    .refine(
      (files) =>
        !files ||
        files.every((f) => ALLOWED_TYPES.includes(f.type)),
      { message: 'Tipo de arquivo não permitido. Use: PDF, DOC, DOCX, ZIP, JPG, PNG' }
    ),
})

export type TaskFormData = z.infer<typeof taskSchema>
