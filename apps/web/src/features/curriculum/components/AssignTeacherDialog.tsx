import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2 } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@components/ui/dialog'
import {
  Combobox,
  ComboboxContent,
  ComboboxEmpty,
  ComboboxInput,
  ComboboxItem,
  ComboboxList,
} from '@components/ui/combobox'
import { Button } from '@components/ui/button'
import { roleLabel } from '@lib/roles'
import { useTeacherSearch } from '../hooks/useTeacherSearch'
import type { OrgMember } from '../api/org-member-api'

const schema = z.object({
  memberId: z.string().min(1, 'Escolha um membro'),
})

type FormData = z.infer<typeof schema>

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (memberId: string) => void
  isPending: boolean
  /** Membros já atribuídos à disciplina — ficam fora da lista. */
  assignedMemberIds: string[]
  /** Recusa da API (422 `MEMBER_NOT_A_PROFESSOR`, por exemplo). */
  error?: string | null
}

function memberLabel(member: OrgMember) {
  return member.name ?? member.email ?? member.userId
}

function AssignTeacherDialog({
  open,
  onClose,
  onSubmit,
  isPending,
  assignedMemberIds,
  error,
}: Props) {
  const [term, setTerm] = useState('')
  const [picked, setPicked] = useState<OrgMember | null>(null)

  // Só busca quando o diálogo abre — a página não precisa da lista.
  const { data: candidates = [], isLoading } = useTeacherSearch(term, open)

  const available = candidates.filter((m) => !assignedMemberIds.includes(m.id))
  // "Ninguém disponível" é sobre a disciplina, não sobre a busca: com um termo
  // digitado, lista vazia significa só que aquele termo não achou ninguém.
  const noneAvailable = !isLoading && available.length === 0 && term.trim() === ''

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { memberId: '' },
  })

  const handleClose = () => {
    reset()
    setTerm('')
    setPicked(null)
    onClose()
  }

  const handlePick = (member: OrgMember | null) => {
    setPicked(member)
    setValue('memberId', member?.id ?? '', { shouldValidate: true })
  }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) handleClose() }}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Atribuir Professor</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit((d) => onSubmit(d.memberId))} className="space-y-4">
          <div className="space-y-1">
            <label htmlFor="assign-teacher-member" className="text-xs text-muted-foreground">
              Membro *
            </label>
            <Combobox
              items={available}
              value={picked}
              onValueChange={handlePick}
              itemToStringLabel={memberLabel}
              onInputValueChange={setTerm}
            >
              <ComboboxInput
                id="assign-teacher-member"
                placeholder="Buscar por nome ou e-mail"
                aria-invalid={!!errors.memberId}
                disabled={noneAvailable}
              />
              <ComboboxContent>
                <ComboboxList>
                  {available.map((member) => (
                    <ComboboxItem key={member.id} value={member}>
                      {/* Sem o nome não dá para escolher; o papel separa homônimos. */}
                      <span className="text-foreground">{memberLabel(member)}</span>
                      <span className="text-xs text-muted-foreground">
                        {roleLabel(member.role)}
                      </span>
                    </ComboboxItem>
                  ))}
                </ComboboxList>
                <ComboboxEmpty>Ninguém encontrado para esta busca.</ComboboxEmpty>
              </ComboboxContent>
            </Combobox>
            <input type="hidden" {...register('memberId')} />
            {errors.memberId && (
              <p role="alert" className="text-xs text-destructive">{errors.memberId.message}</p>
            )}
            {noneAvailable && (
              <p className="text-xs text-muted-foreground">
                Nenhum membro disponível para atribuir.
              </p>
            )}
          </div>

          {error && (
            <p role="alert" className="text-sm text-destructive">
              {error}
            </p>
          )}

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={handleClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending || noneAvailable}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Atribuir
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default AssignTeacherDialog
