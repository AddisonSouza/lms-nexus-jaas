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
import { Textarea } from '@components/ui/textarea'
import { Button } from '@components/ui/button'

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

        <p className="text-sm font-semibold">{taskTitle}</p>
        <p className="text-xs text-muted-foreground">
          Prazo: {new Date(deadline).toLocaleString('pt-BR')}
          {isPastDeadline && (
            <span className="ml-2 font-semibold text-destructive">Prazo expirado</span>
          )}
        </p>

        {isPastDeadline ? (
          <p className="rounded-[var(--radius-md)] bg-accent-100 p-3 text-sm text-accent-800">
            O prazo desta tarefa expirou. Não é mais possível enviar respostas.
          </p>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs text-muted-foreground">Resposta em texto</label>
              <Textarea {...register('textResponse')} rows={5} placeholder="Digite sua resposta aqui..." />
              {errors.textResponse && (
                <p className="text-xs text-destructive">{errors.textResponse.message}</p>
              )}
            </div>

            <div className="space-y-1">
              <label className="text-xs text-muted-foreground">Arquivos (opcional)</label>
              <input
                ref={fileInputRef}
                type="file"
                multiple
                accept=".pdf,.doc,.docx,.zip,.jpg,.jpeg,.png"
                onChange={(e) => setValue('files', Array.from(e.target.files ?? []))}
                className="w-full rounded-[var(--radius-md)] border border-border bg-surface px-3 py-2 text-sm file:mr-2 file:rounded-full file:border-0 file:bg-accent-100 file:px-3 file:py-1 file:text-accent-800"
              />
              <p className="text-xs text-muted-foreground">PDF, DOC, DOCX, ZIP, JPG, PNG — máx. 50MB cada</p>
              {errors.files && <p className="text-xs text-destructive">{errors.files.message}</p>}
            </div>

            <DialogFooter>
              <Button type="button" variant="secondary" onClick={onClose}>
                Cancelar
              </Button>
              <Button type="submit" disabled={isPending}>
                {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                Enviar Resposta
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  )
}

export default SubmissionFormDialog
