import { useEffect, useRef } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { contentSchema, type ContentFormData } from '../schemas/contentSchema'
import type { Topic, ContentType } from '../types'
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
import { Segmented } from '@components/ui/segmented'

const CONTENT_TYPES: { value: ContentType; label: string }[] = [
  { value: 'VIDEO', label: 'Vídeo' },
  { value: 'LINK', label: 'Link' },
  { value: 'DOCUMENTO', label: 'Documento' },
  { value: 'ARQUIVO', label: 'Arquivo' },
]

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (data: ContentFormData) => void
  isPending: boolean
  topics: Topic[]
  defaultTopicId?: string
  title: string
}

function ContentFormDialog({ open, onClose, onSubmit, isPending, topics, defaultTopicId, title }: Props) {
  const fileInputRef = useRef<HTMLInputElement>(null)

  const {
    register,
    handleSubmit,
    control,
    watch,
    reset,
    setValue,
    formState: { errors },
  } = useForm<ContentFormData>({
    resolver: zodResolver(contentSchema),
    defaultValues: {
      title: '',
      topicId: defaultTopicId ?? '',
      contentType: 'LINK',
      externalUrl: '',
      description: '',
    } as ContentFormData,
  })

  const contentType = watch('contentType')
  const isUrlBased = contentType === 'VIDEO' || contentType === 'LINK'

  useEffect(() => {
    if (open) {
      reset({
        title: '',
        topicId: defaultTopicId ?? topics[0]?.id ?? '',
        contentType: 'LINK',
        externalUrl: '',
        description: '',
      } as ContentFormData)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }, [open, defaultTopicId, topics, reset])

  useEffect(() => {
    if (!isUrlBased) {
      setValue('externalUrl' as never, undefined as never)
    } else {
      setValue('file' as never, undefined as never)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }, [contentType, isUrlBased, setValue])

  const urlError = (errors as Record<string, { message?: string }>).externalUrl
  const fileError = (errors as Record<string, { message?: string }>).file

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Tópico *</label>
            <select
              {...register('topicId')}
              className="h-9 w-full rounded-full border border-border bg-surface px-3.5 text-sm text-foreground outline-none focus-visible:border-accent"
            >
              {topics.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.title}
                </option>
              ))}
            </select>
            {errors.topicId && <p className="text-xs text-destructive">{errors.topicId.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Título *</label>
            <Input {...register('title')} placeholder="Ex: Aula 1 — Introdução" />
            {errors.title && <p className="text-xs text-destructive">{errors.title.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Tipo *</label>
            <Controller
              name="contentType"
              control={control}
              render={({ field }) => (
                <Segmented value={field.value} onValueChange={field.onChange} options={CONTENT_TYPES} />
              )}
            />
          </div>

          {isUrlBased ? (
            <div className="space-y-1">
              <label className="text-xs text-muted-foreground">URL *</label>
              <Input {...register('externalUrl')} type="url" placeholder="https://..." />
              {urlError && <p className="text-xs text-destructive">{urlError.message}</p>}
            </div>
          ) : (
            <div className="space-y-1">
              <label className="text-xs text-muted-foreground">Arquivo *</label>
              <Controller
                name={'file' as never}
                control={control}
                render={({ field: { onChange } }) => (
                  <input
                    ref={fileInputRef}
                    type="file"
                    className="w-full rounded-[var(--radius-md)] border border-border bg-surface px-3 py-2 text-sm file:mr-2 file:rounded-full file:border-0 file:bg-accent-100 file:px-3 file:py-1 file:text-accent-800"
                    onChange={(e) => onChange(e.target.files?.[0])}
                  />
                )}
              />
              {fileError && <p className="text-xs text-destructive">{fileError.message}</p>}
            </div>
          )}

          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Descrição</label>
            <Textarea {...register('description')} rows={2} placeholder="Descrição opcional" />
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Salvar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default ContentFormDialog
