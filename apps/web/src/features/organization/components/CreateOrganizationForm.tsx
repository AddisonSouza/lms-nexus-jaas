import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Loader2 } from 'lucide-react'
import { createOrganizationSchema, type CreateOrganizationFormData } from '../schemas/createOrganizationSchema'
import { useCreateOrganization } from '../hooks/useCreateOrganization'
import { Input } from '@components/ui/input'
import { Textarea } from '@components/ui/textarea'
import { Button } from '@components/ui/button'

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
    <form onSubmit={handleSubmit(onSubmit)} className="w-full space-y-4">
      {errorMessage && <p className="text-sm text-destructive">{errorMessage}</p>}

      <div className="space-y-1">
        <label htmlFor="org-name" className="text-xs text-muted-foreground">Nome</label>
        <Input {...register('name')} id="org-name" type="text" placeholder="Ex: Escola Municipal Centro" />
        {errors.name && <p className="text-xs text-destructive">{errors.name.message}</p>}
      </div>

      <div className="space-y-1">
        <label htmlFor="org-desc" className="text-xs text-muted-foreground">
          Descrição <span className="opacity-70">(opcional)</span>
        </label>
        <Textarea
          {...register('description')}
          id="org-desc"
          rows={3}
          placeholder="Descreva brevemente sua organização"
        />
        {errors.description && <p className="text-xs text-destructive">{errors.description.message}</p>}
      </div>

      <Button type="submit" disabled={isPending} className="w-full">
        {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
        Criar organização
      </Button>
    </form>
  )
}

export default CreateOrganizationForm
