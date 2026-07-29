import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { joinClassroomSchema, type JoinClassroomFormData } from '../schemas/joinClassroomSchema'
import { useJoinClassroom } from '../hooks/useJoinClassroom'
import { Input } from '@components/ui/input'
import { Button } from '@components/ui/button'

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
    joinClassroom.mutate(data.inviteCode.toUpperCase(), {
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
          <p className="text-sm text-destructive">Código inválido ou turma indisponível.</p>
        )}

        <Button type="submit" disabled={joinClassroom.isPending} className="w-full">
          {joinClassroom.isPending ? 'Entrando...' : 'Entrar na turma'}
        </Button>
      </form>
    </div>
  )
}

export default JoinClassroomForm
