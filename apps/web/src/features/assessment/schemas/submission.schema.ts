import { z } from 'zod'

const ALLOWED_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/zip',
  'image/jpeg',
  'image/png',
]

export const submissionSchema = z
  .object({
    textResponse: z.string().optional(),
    files: z
      .array(z.instanceof(File))
      .optional()
      .refine(
        (files) => !files || files.every((f) => ALLOWED_TYPES.includes(f.type)),
        { message: 'Tipo de arquivo não permitido. Use: PDF, DOC, DOCX, ZIP, JPG, PNG' }
      ),
  })
  .refine(
    (data) =>
      (data.textResponse && data.textResponse.trim().length > 0) ||
      (data.files && data.files.length > 0),
    { message: 'Informe um texto ou anexe pelo menos um arquivo', path: ['textResponse'] }
  )

export type SubmissionFormData = z.infer<typeof submissionSchema>
