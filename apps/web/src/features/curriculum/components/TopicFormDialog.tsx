import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { topicSchema, type TopicFormData } from '../schemas/topicSchema'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@components/ui/dialog'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (data: TopicFormData) => void
  isPending: boolean
  defaultValues?: { title: string }
  title: string
}

function TopicFormDialog({ open, onClose, onSubmit, isPending, defaultValues, title }: Props) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TopicFormData>({
    resolver: zodResolver(topicSchema),
    defaultValues: { title: defaultValues?.title ?? '' },
  })

  useEffect(() => {
    if (open) reset({ title: defaultValues?.title ?? '' })
  }, [open, defaultValues, reset])

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) onClose() }}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label className="text-xs text-muted-foreground">Título *</label>
            <Input {...register('title')} placeholder="Ex: Introdução" autoFocus />
            {errors.title && <p className="text-xs text-destructive">{errors.title.message}</p>}
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

export default TopicFormDialog
