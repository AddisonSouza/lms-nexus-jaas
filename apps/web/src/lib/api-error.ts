import { z } from 'zod'

/**
 * Tradutor único das recusas da API. Vive fora das features porque todo
 * formulário precisa dele e features não podem importar umas às outras.
 *
 * A API devolve dois formatos: `{ "error": ... }` para regra de negócio e
 * `{ "errors": [...] }` para validação 422. O campo `error` nem sempre é um
 * código — parte das exceções manda o próprio texto (`getMessage()`), então o
 * helper distingue código de mensagem antes de decidir o que mostrar.
 */

const errorBodySchema = z.union([
  z.object({ error: z.string() }),
  z.object({ errors: z.array(z.string()) }),
])

const CODE_MESSAGES: Record<string, string> = {
  // organização e convites
  ALREADY_A_MEMBER: 'Você já é membro desta organização.',
  CANNOT_CHANGE_OWNER_ROLE: 'O papel do dono da organização não pode ser alterado.',
  CANNOT_REMOVE_OWNER: 'O dono da organização não pode ser removido.',
  INVITATION_ALREADY_USED: 'Este convite já foi utilizado.',
  INVITATION_CANCELLED: 'Este convite foi cancelado pelo administrador. Peça um novo convite.',
  INVITATION_EXPIRED: 'Este convite expirou.',
  INVITATION_NOT_FOR_THIS_USER: 'Este convite foi enviado para outro e-mail. Entre com a conta convidada.',
  INVITATION_NOT_FOUND: 'Convite não encontrado.',
  INVITATION_NOT_PENDING: 'Este convite não está mais pendente.',
  MEMBER_NOT_FOUND: 'Membro não encontrado nesta organização.',
  NOT_AN_ORGANIZATION_MEMBER: 'Você não faz parte desta organização.',
  ORGANIZATION_NAME_ALREADY_EXISTS: 'Já existe uma organização com esse nome.',
  ROLE_NOT_ASSIGNABLE: 'Este papel não pode ser atribuído.',

  // turmas e disciplinas
  CLASSROOM_ARCHIVED: 'Esta turma está arquivada.',
  CLASSROOM_MEMBER_NOT_FOUND: 'Membro não encontrado nesta turma.',
  CLASSROOM_NOT_FOUND: 'Turma não encontrada.',
  INVALID_INVITE_CODE: 'Código de turma inválido.',
  MEMBER_NOT_IN_ORGANIZATION: 'Este usuário não faz parte da organização.',

  // tarefas e submissões
  DEADLINE_EXPIRED: 'O prazo desta tarefa já encerrou.',
  DEADLINE_NOT_IN_FUTURE: 'O prazo precisa ser uma data futura.',
  EMPTY_SUBMISSION: 'Escreva uma resposta ou anexe um arquivo antes de enviar.',
  GRADE_EXCEEDS_MAX_SCORE: 'A nota não pode ser maior que a pontuação máxima da tarefa.',
  GRADE_NOT_ALLOWED: 'Esta tarefa não aceita nota.',
  SUBMISSION_ALREADY_EVALUATED: 'Esta submissão já foi avaliada.',
  SUBMISSION_ALREADY_EXISTS: 'Você já enviou uma resposta para esta tarefa.',
  SUBMISSION_NOT_FOUND: 'Submissão não encontrada.',
  TASK_FORBIDDEN: 'Você não tem permissão para esta tarefa.',
  TASK_NOT_FOUND: 'Tarefa não encontrada.',

  // sessão e conta
  DASHBOARD_ACCESS_DENIED: 'Você não tem permissão para ver este painel.',
  EMAIL_ALREADY_CONFIRMED: 'Este e-mail já foi confirmado. Faça login normalmente.',
  INVALID_CONFIRMATION_TOKEN: 'Link de confirmação inválido ou expirado.',
  RESEND_RATE_LIMIT_EXCEEDED: 'Muitos reenvios. Aguarde uma hora e tente de novo.',
  SESSION_STALE: 'Sua sessão mudou. Entre de novo para continuar.',
}

// Exceções de upload mandam o MIME type junto, em inglês ("File type not
// allowed: application/x-msdownload"). O usuário só precisa saber que o
// arquivo não serve.
const FILE_TYPE_PREFIXES = ['File type not allowed', 'Attachment type not allowed']
const FILE_TYPE_MESSAGE = 'Este tipo de arquivo não é permitido.'

const STATUS_MESSAGES: Record<number, string> = {
  400: 'Requisição inválida. Revise os dados e tente de novo.',
  401: 'Sua sessão expirou. Entre de novo para continuar.',
  403: 'Você não tem permissão para esta ação.',
  404: 'Não encontramos o que você procura.',
  409: 'Esta ação conflita com o estado atual. Recarregue a página.',
  410: 'Este link não está mais disponível.',
  422: 'Dados inválidos. Revise os campos e tente de novo.',
  429: 'Muitas tentativas. Aguarde um pouco e tente de novo.',
}

const FALLBACK = 'Não foi possível concluir a ação. Tente de novo em instantes.'

/** `campo: mensagem` → `mensagem`. O prefixo do Bean Validation não tem espaço. */
function stripFieldPrefix(entry: string): string {
  return entry.replace(/^[^\s:]+:\s*/, '')
}

/** Texto para o usuário tem espaço e minúscula; `TASK_FORBIDDEN` e `Unauthorized` não. */
function isHumanMessage(value: string): boolean {
  return /\s/.test(value) && /[a-zà-ú]/.test(value)
}

/**
 * Mensagem em PT-BR para uma recusa da API. `overrides` troca o texto de um
 * código específico na tela que chama — sem mexer no mapa compartilhado.
 */
export function apiErrorMessage(error: unknown, overrides?: Record<string, string>): string {
  if (!error) return FALLBACK

  const response = (error as { response?: { status?: number; data?: unknown } }).response
  const status = response?.status
  const statusMessage = (status && STATUS_MESSAGES[status]) || FALLBACK

  const body = errorBodySchema.safeParse(response?.data)
  if (!body.success) return statusMessage

  if ('errors' in body.data) {
    const messages = body.data.errors.map(stripFieldPrefix).filter(Boolean)
    return messages.length > 0 ? messages.join(' ') : statusMessage
  }

  const value = body.data.error
  const mapped = overrides?.[value] ?? CODE_MESSAGES[value]
  if (mapped) return mapped

  if (FILE_TYPE_PREFIXES.some((prefix) => value.startsWith(prefix))) return FILE_TYPE_MESSAGE

  return isHumanMessage(value) ? value : statusMessage
}
