import { useEffect, useRef } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, X } from 'lucide-react'
import { announcementSchema, type AnnouncementFormData } from '../schemas/announcementSchema'
import type { Announcement } from '../types'

interface Props {
  open: boolean
  announcement?: Announcement | null
  onClose: () => void
  onSubmit: (data: AnnouncementFormData) => void
  isPending: boolean
}

function AnnouncementForm({ open, announcement, onClose, onSubmit, isPending }: Props) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const isEditing = !!announcement

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<AnnouncementFormData>({
    resolver: zodResolver(announcementSchema),
    defaultValues: { content: '', externalUrl: '', linkTitle: '', files: [] },
  })

  useEffect(() => {
    if (open) {
      reset({ content: announcement?.content ?? '', externalUrl: '', linkTitle: '', files: [] })
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }, [open, announcement, reset])

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-full max-w-lg rounded-lg border bg-background p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">{isEditing ? 'Editar Aviso' : 'Publicar Aviso'}</h2>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label className="text-sm font-medium">Conteúdo *</label>
            <textarea
              {...register('content')}
              rows={4}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Escreva o aviso para a turma..."
            />
            {errors.content && <p className="text-xs text-destructive">{errors.content.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Link externo (opcional)</label>
            <input
              {...register('externalUrl')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="https://..."
            />
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Título do link (opcional)</label>
            <input
              {...register('linkTitle')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Ex: Material de apoio"
            />
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Arquivos (opcional)</label>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              onChange={(e) => setValue('files', Array.from(e.target.files ?? []))}
              className="w-full rounded border px-3 py-2 text-sm"
            />
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button type="button" onClick={onClose} className="rounded border px-4 py-2 text-sm">
              Cancelar
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="flex items-center gap-2 rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50"
            >
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              {isEditing ? 'Salvar' : 'Publicar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AnnouncementForm
