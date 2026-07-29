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
import { Input } from '@components/ui/input'
import { Textarea } from '@components/ui/textarea'
import { Button } from '@components/ui/button'

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
            <label className="text-xs text-muted-foreground">Conteúdo *</label>
            <Textarea {...register('content')} rows={4} placeholder="Escreva o aviso para a turma..." />
            {errors.content && <p className="text-xs text-destructive">{errors.content.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Link externo (opcional)</label>
            <Input {...register('externalUrl')} placeholder="https://..." />
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Título do link (opcional)</label>
            <Input {...register('linkTitle')} placeholder="Ex: Material de apoio" />
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Arquivos (opcional)</label>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              onChange={(e) => setValue('files', Array.from(e.target.files ?? []))}
              className="w-full rounded-[var(--radius-md)] border border-border bg-surface px-3 py-2 text-sm file:mr-2 file:rounded-full file:border-0 file:bg-accent-100 file:px-3 file:py-1 file:text-accent-800"
            />
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              {isEditing ? 'Salvar' : 'Publicar'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default AnnouncementForm
