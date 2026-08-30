import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { joinClassroomSchema, type JoinClassroomFormData } from '../schemas/joinClassroomSchema'
import { useJoinClassroom } from '../hooks/useJoinClassroom'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

/**
 * O back-end devolve um `errorCode` estável por caso; a tela traduz cada um.
 * Um código de outra organização volta como INVALID_INVITE_CODE de propósito,
 * para não revelar que a turma existe em outro lugar.
 */
function joinErrorMessage(error: unknown): string {
  const code = (error as { response?: { data?: { error?: string } } })?.response?.data?.error
  switch (code) {
    case 'INVALID_INVITE_CODE':
      return 'Código não encontrado. Confira com quem enviou — ele vale só dentro da sua organização.'
    case 'CLASSROOM_ARCHIVED':
      return 'Esta turma está arquivada e não aceita novas entradas.'
    default:
      return 'Não foi possível entrar na turma. Tente de novo em instantes.'
  }
}

function JoinClassroomForm() {
  const navigate = useNavigate()
  const joinClassroom = useJoinClassroom()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<JoinClassroomFormData>({
    resolver: zodResolver(joinClassroomSchema),
  })

  const onSubmit = (data: JoinClassroomFormData) => {
    joinClassroom.mutate(data.inviteCode, {
      onSuccess: (classroom) => navigate(`/classrooms/${classroom.id}`),
    })
  }

  return (
    <div className="mx-auto max-w-sm space-y-4">
      <h3>Entrar em uma turma</h3>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
        <div>
          <label htmlFor="inviteCode" className="mb-1 block text-xs text-muted-foreground">
            Código de convite
          </label>
          <Input
            id="inviteCode"
            {...register('inviteCode')}
            placeholder="Ex: ABC123"
            className="font-mono uppercase tracking-widest"
            maxLength={6}
          />
          {errors.inviteCode && <p className="mt-1 text-xs text-destructive">{errors.inviteCode.message}</p>}
        </div>

        {joinClassroom.isError && (
          <p className="text-sm text-destructive">{joinErrorMessage(joinClassroom.error)}</p>
        )}

        <Button type="submit" disabled={joinClassroom.isPending} className="w-full">
          {joinClassroom.isPending ? 'Entrando...' : 'Entrar na turma'}
        </Button>
      </form>
    </div>
  )
}

export default JoinClassroomForm
