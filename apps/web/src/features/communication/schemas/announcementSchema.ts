import { z } from 'zod'

export const announcementSchema = z.object({
  content: z.string().min(1, 'O conteúdo do aviso é obrigatório'),
  externalUrl: z.string().optional(),
  linkTitle: z.string().optional(),
  files: z.array(z.instanceof(File)).optional(),
})

export type AnnouncementFormData = z.infer<typeof announcementSchema>
