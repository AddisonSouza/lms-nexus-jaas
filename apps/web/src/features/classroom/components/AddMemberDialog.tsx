import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { addMemberSchema, type AddMemberFormData } from '../schemas/addMemberSchema'
import { useOrgMemberSearch } from '../hooks/useOrgMemberSearch'
import type { OrgMember } from '../api/org-member-api'
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

interface Props {
  open: boolean
  onClose: () => void
  onSubmit: (data: AddMemberFormData) => void
  isPending: boolean
  /** Quem já está na turma — aparece na busca, marcado e sem poder ser escolhido. */
  existingUserIds?: string[]
}

function memberLabel(member: OrgMember) {
  return member.name ?? member.email ?? member.userId
}

function AddMemberDialog({ open, onClose, onSubmit, isPending, existingUserIds = [] }: Props) {
  const [term, setTerm] = useState('')
  const [picked, setPicked] = useState<OrgMember | null>(null)
  const { data } = useOrgMemberSearch(term, open)
  const members = data?.content ?? []

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<AddMemberFormData>({
    resolver: zodResolver(addMemberSchema),
  })

  const handleClose = () => {
    reset()
    setTerm('')
    setPicked(null)
    onClose()
  }

  const handlePick = (member: OrgMember | null) => {
    setPicked(member)
    setValue('userId', member?.userId ?? '', { shouldValidate: true })
  }

  return (
    <Dialog open={open} onOpenChange={(isOpen) => { if (!isOpen) handleClose() }}>
      <DialogContent className="max-w-sm">
        <DialogHeader>
          <DialogTitle>Adicionar membro</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-1">
            <label htmlFor="add-member-search" className="text-xs text-muted-foreground">
              Pessoa *
            </label>
            <Combobox
              items={members}
              value={picked}
              onValueChange={handlePick}
              itemToStringLabel={memberLabel}
              onInputValueChange={setTerm}
            >
              <ComboboxInput
                id="add-member-search"
                placeholder="Buscar por nome ou e-mail"
                aria-invalid={!!errors.userId}
              />
              <ComboboxContent>
                <ComboboxList>
                  {members.map((member) => {
                    const alreadyMember = existingUserIds.includes(member.userId)
                    return (
                      <ComboboxItem key={member.userId} value={member} disabled={alreadyMember}>
                        <span className="text-foreground">{memberLabel(member)}</span>
                        <span className="text-xs text-muted-foreground">
                          {member.email ?? member.userId}
                          {alreadyMember && ' · já na turma'}
                        </span>
                      </ComboboxItem>
                    )
                  })}
                </ComboboxList>
                <ComboboxEmpty>Ninguém encontrado nesta organização.</ComboboxEmpty>
              </ComboboxContent>
            </Combobox>
            <input type="hidden" {...register('userId')} />
            {errors.userId && (
              <p role="alert" className="text-xs text-destructive">{errors.userId.message}</p>
            )}
          </div>

          <div className="space-y-1">
            <label htmlFor="add-member-role" className="text-xs text-muted-foreground">Papel *</label>
            <select
              {...register('role')}
              id="add-member-role"
              aria-invalid={!!errors.role}
              className="h-9 w-full rounded-full border border-border bg-surface px-3.5 text-sm text-foreground outline-none focus-visible:border-accent"
            >
              <option value="">Selecione...</option>
              <option value="PROFESSOR">Professor</option>
              <option value="ALUNO">Aluno</option>
            </select>
            {errors.role && (
              <p role="alert" className="text-xs text-destructive">{errors.role.message}</p>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="secondary" onClick={handleClose}>
              Cancelar
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
              Adicionar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}

export default AddMemberDialog
