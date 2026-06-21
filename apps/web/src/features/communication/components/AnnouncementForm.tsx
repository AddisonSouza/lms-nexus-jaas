import { useEffect, useRef } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { announcementSchema, type AnnouncementFormData } from '../schemas/announcementSchema'
import type { Announcement } from '../types'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@components/ui/dialog'

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

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>{isEditing ? 'Editar Aviso' : 'Publicar Aviso'}</DialogTitle>
        </DialogHeader>

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

          <DialogFooter>
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
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default AnnouncementForm
