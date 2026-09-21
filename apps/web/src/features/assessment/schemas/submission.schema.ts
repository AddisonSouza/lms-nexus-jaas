import { z } from 'zod'

const ALLOWED_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/zip',
  'application/x-zip-compressed',
  'image/jpeg',
  'image/png',
]

const ALLOWED_EXTENSIONS = ['pdf', 'doc', 'docx', 'zip', 'jpg', 'jpeg', 'png']

const TYPE_MESSAGE = 'Tipo de arquivo não permitido. Use: PDF, DOC, DOCX, ZIP, JPG, PNG'

/**
 * A extensão é conferida junto com o MIME porque `File.type` vem vazio quando o
 * sistema não reconhece o arquivo — e um `.exe` é justamente um dos casos em que
 * ele vem vazio no Linux. Mesma regra do `AttachmentTypePolicy` no back: sem
 * isso a tela deixaria passar o que a API recusaria com 422.
 */
function isAllowed(file: File): boolean {
  const dot = file.name.lastIndexOf('.')
  const extension = dot > 0 ? file.name.slice(dot + 1).toLowerCase() : ''
  if (!ALLOWED_EXTENSIONS.includes(extension)) return false

  // MIME ausente: a extensão já foi conferida e o back valida de novo.
  if (!file.type) return true
  return ALLOWED_TYPES.includes(file.type.split(';')[0].trim().toLowerCase())
}

export const submissionSchema = z
  .object({
    textResponse: z.string().optional(),
    files: z
      .array(z.instanceof(File))
      .optional()
      .refine((files) => !files || files.every(isAllowed), { message: TYPE_MESSAGE }),
  })
  .refine(
    (data) =>
      (data.textResponse && data.textResponse.trim().length > 0) ||
      (data.files && data.files.length > 0),
    { message: 'Informe um texto ou anexe pelo menos um arquivo', path: ['textResponse'] }
  )

export type SubmissionFormData = z.infer<typeof submissionSchema>
