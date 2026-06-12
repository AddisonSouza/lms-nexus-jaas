import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { createOrganizationSchema, type CreateOrganizationFormData } from '../schemas/createOrganizationSchema'
import { useCreateOrganization } from '../hooks/useCreateOrganization'

function CreateOrganizationForm() {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreateOrganizationFormData>({
    resolver: zodResolver(createOrganizationSchema),
  })

  const { mutate: create, isPending, error } = useCreateOrganization()

  const onSubmit = (data: CreateOrganizationFormData) => create(data)

  const errorMessage =
    (error as { response?: { status?: number } })?.response?.status === 409
      ? 'Você já possui uma organização com esse nome.'
      : error
        ? 'Erro ao criar organização. Tente novamente.'
        : null

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="w-full max-w-sm space-y-4 rounded-lg border p-6 shadow-sm"
    >
      <h1 className="text-2xl font-semibold">Criar Organização</h1>

      {errorMessage && (
        <p className="text-sm text-destructive">{errorMessage}</p>
      )}

      <div className="space-y-1">
        <label htmlFor="org-name" className="text-sm font-medium">Nome</label>
        <input
          {...register('name')}
          id="org-name"
          type="text"
          placeholder="Ex: Escola Municipal Centro"
          className="w-full rounded border px-3 py-2 text-sm"
        />
        {errors.name && (
          <p className="text-xs text-destructive">{errors.name.message}</p>
        )}
      </div>

      <div className="space-y-1">
        <label htmlFor="org-desc" className="text-sm font-medium">
          Descrição <span className="text-muted-foreground">(opcional)</span>
        </label>
        <textarea
          {...register('description')}
          id="org-desc"
          rows={3}
          placeholder="Descreva brevemente sua organização"
          className="w-full rounded border px-3 py-2 text-sm"
        />
        {errors.description && (
          <p className="text-xs text-destructive">{errors.description.message}</p>
        )}
      </div>

      <button
        type="submit"
        disabled={isPending}
        className="flex w-full items-center justify-center gap-2 rounded bg-primary px-4 py-2 text-sm font-medium text-primary-foreground disabled:opacity-50"
      >
        {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
        Criar Organização
      </button>
    </form>
  )
}

export default CreateOrganizationForm
