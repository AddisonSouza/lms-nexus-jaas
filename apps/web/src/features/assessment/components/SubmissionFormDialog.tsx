import { useEffect, useRef } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { submissionSchema, type SubmissionFormData } from '../schemas/submission.schema'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@components/ui/dialog'

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

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Enviar Resposta</DialogTitle>
        </DialogHeader>

        <p className="text-sm font-medium">{taskTitle}</p>
        <p className="text-xs text-muted-foreground">
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
                Enviar Resposta
              </button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  )
}

export default SubmissionFormDialog
