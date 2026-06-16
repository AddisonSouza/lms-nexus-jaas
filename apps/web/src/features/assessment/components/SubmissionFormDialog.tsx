import { useEffect, useRef } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2, X } from 'lucide-react'
import { submissionSchema, type SubmissionFormData } from '../schemas/submission.schema'

interface Props {
  open: boolean
  taskTitle: string
  deadline: string
  onClose: () => void
  onSubmit: (data: SubmissionFormData) => void
  isPending: boolean
}

function SubmissionFormDialog({ open, taskTitle, deadline, onClose, onSubmit, isPending }: Props) {
  const fileInputRef = useRef<HTMLInputElement>(null)
  const isPastDeadline = new Date() > new Date(deadline)

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<SubmissionFormData>({
    resolver: zodResolver(submissionSchema),
    defaultValues: { textResponse: '', files: [] },
  })

  useEffect(() => {
    if (open) {
      reset({ textResponse: '', files: [] })
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }, [open, reset])

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div className="w-full max-w-lg rounded-lg border bg-background p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold">Enviar Resposta</h2>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-4 w-4" />
          </button>
        </div>

        <p className="mb-1 text-sm font-medium">{taskTitle}</p>
        <p className="mb-4 text-xs text-muted-foreground">
          Prazo: {new Date(deadline).toLocaleString('pt-BR')}
          {isPastDeadline && (
            <span className="ml-2 font-semibold text-destructive">Prazo expirado</span>
          )}
        </p>

        {isPastDeadline ? (
          <p className="rounded border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
            O prazo desta tarefa expirou. Não é mais possível enviar respostas.
          </p>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1">
              <label className="text-sm font-medium">Resposta em texto</label>
              <textarea
                {...register('textResponse')}
                rows={5}
                className="w-full rounded border px-3 py-2 text-sm"
                placeholder="Digite sua resposta aqui..."
              />
              {errors.textResponse && (
                <p className="text-xs text-destructive">{errors.textResponse.message}</p>
              )}
            </div>

            <div className="space-y-1">
              <label className="text-sm font-medium">Arquivos (opcional)</label>
              <input
                ref={fileInputRef}
                type="file"
                multiple
                accept=".pdf,.doc,.docx,.zip,.jpg,.jpeg,.png"
                onChange={(e) => setValue('files', Array.from(e.target.files ?? []))}
                className="w-full rounded border px-3 py-2 text-sm"
              />
              <p className="text-xs text-muted-foreground">PDF, DOC, DOCX, ZIP, JPG, PNG — máx. 50MB cada</p>
              {errors.files && <p className="text-xs text-destructive">{errors.files.message}</p>}
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
                Enviar Resposta
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

export default SubmissionFormDialog
