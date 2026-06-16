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
} from '@features/components/ui/dialog'

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
            <label className="text-sm font-medium">Tópico *</label>
            <select {...register('topicId')} className="w-full rounded border px-3 py-2 text-sm">
              {topics.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.title}
                </option>
              ))}
            </select>
            {errors.topicId && <p className="text-xs text-destructive">{errors.topicId.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Título *</label>
            <input
              {...register('title')}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Ex: Aula 1 — Introdução"
            />
            {errors.title && <p className="text-xs text-destructive">{errors.title.message}</p>}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium">Tipo *</label>
            <Controller
              name="contentType"
              control={control}
              render={({ field }) => (
                <div className="flex flex-wrap gap-2">
                  {CONTENT_TYPES.map((ct) => (
                    <button
                      key={ct.value}
                      type="button"
                      onClick={() => field.onChange(ct.value)}
                      className={`rounded border px-3 py-1.5 text-sm ${
                        field.value === ct.value
                          ? 'border-primary bg-primary text-primary-foreground'
                          : 'hover:bg-muted'
                      }`}
                    >
                      {ct.label}
                    </button>
                  ))}
                </div>
              )}
            />
          </div>

          {isUrlBased ? (
            <div className="space-y-1">
              <label className="text-sm font-medium">URL *</label>
              <input
                {...register('externalUrl')}
                type="url"
                className="w-full rounded border px-3 py-2 text-sm"
                placeholder="https://..."
              />
              {urlError && <p className="text-xs text-destructive">{urlError.message}</p>}
            </div>
          ) : (
            <div className="space-y-1">
              <label className="text-sm font-medium">Arquivo *</label>
              <Controller
                name={'file' as never}
                control={control}
                render={({ field: { onChange } }) => (
                  <input
                    ref={fileInputRef}
                    type="file"
                    className="w-full rounded border px-3 py-2 text-sm file:mr-2 file:rounded file:border-0 file:bg-muted file:px-2 file:py-1 file:text-sm"
                    onChange={(e) => onChange(e.target.files?.[0])}
                  />
                )}
              />
              {fileError && <p className="text-xs text-destructive">{fileError.message}</p>}
            </div>
          )}

          <div className="space-y-1">
            <label className="text-sm font-medium">Descrição</label>
            <textarea
              {...register('description')}
              rows={2}
              className="w-full rounded border px-3 py-2 text-sm"
              placeholder="Descrição opcional"
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
              Salvar
            </button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default ContentFormDialog
