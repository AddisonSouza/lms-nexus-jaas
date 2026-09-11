import { ArrowLeft } from 'lucide-react'
import { Link } from 'react-router-dom'

interface Props {
  /** Alinhamento próprio, para telas que centralizam o conteúdo do card. */
  className?: string
}

/**
 * Saída das telas públicas de autenticação. Sem ela `/register`,
 * `/forgot-password`, `/reset-password` e `/confirm-email` só voltam ao login
 * pelo botão do navegador — e os estados sem formulário ("E-mail enviado",
 * "Link inválido") não voltam de jeito nenhum.
 */
function BackToLogin({ className }: Props) {
  return (
    <Link
      to="/login"
      className={`inline-flex items-center gap-1.5 text-[13px] text-muted-foreground transition-colors hover:text-foreground ${className ?? ''}`}
    >
      <ArrowLeft className="h-4 w-4" />
      Voltar ao login
    </Link>
  )
}

export default BackToLogin
