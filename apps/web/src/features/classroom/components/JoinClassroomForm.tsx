import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { joinClassroomSchema, type JoinClassroomFormData } from '../schemas/joinClassroomSchema'
import { useJoinClassroom } from '../hooks/useJoinClassroom'

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
    <div className="max-w-sm mx-auto space-y-4">
      <h2 className="text-lg font-semibold">Entrar em uma turma</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
        <div>
          <label htmlFor="inviteCode" className="block text-sm font-medium mb-1">
            Código de convite
          </label>
          <input
            id="inviteCode"
            {...register('inviteCode')}
            placeholder="Ex: ABC123"
            className="w-full rounded border px-3 py-2 text-sm font-mono uppercase tracking-widest"
            maxLength={6}
          />
          {errors.inviteCode && (
            <p className="mt-1 text-xs text-destructive">{errors.inviteCode.message}</p>
          )}
        </div>

        {joinClassroom.isError && (
          <p className="text-sm text-destructive">Código inválido ou turma indisponível.</p>
        )}

        <button
          type="submit"
          disabled={joinClassroom.isPending}
          className="w-full rounded bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
        >
          {joinClassroom.isPending ? 'Entrando...' : 'Entrar na turma'}
        </button>
      </form>
    </div>
  )
}

export default JoinClassroomForm
