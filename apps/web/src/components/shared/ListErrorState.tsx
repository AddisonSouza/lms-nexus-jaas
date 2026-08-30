import { AlertTriangle } from 'lucide-react'
import { Card } from '@components/ui/card'
import { Button } from '@components/ui/button'

interface Props {
  /** O que não pôde ser carregado, em minúsculas: "as turmas", "as disciplinas". */
  subject: string
  onRetry: () => void
  isRetrying?: boolean
}

/**
 * Estado de falha das listagens. Sem ele a tela cai no ramo da tabela com
 * `data` indefinido e desenha zero linhas, tornando um erro indistinguível de
 * uma lista realmente vazia.
 */
function ListErrorState({ subject, onRetry, isRetrying = false }: Props) {
  return (
    <Card elevation="sm" role="alert" className="items-center p-8 text-center">
      <div className="flex h-14 w-14 items-center justify-center rounded-full bg-accent-100 text-accent-800">
        <AlertTriangle className="h-6 w-6" />
      </div>
      <h3 className="font-heading text-lg">Não foi possível carregar {subject}</h3>
      <p className="text-sm text-muted-foreground">
        A conexão falhou ou sua sessão pode ter expirado. Tente de novo — se
        persistir, entre novamente.
      </p>
      <Button variant="secondary" onClick={onRetry} disabled={isRetrying}>
        {isRetrying ? 'Tentando...' : 'Tentar de novo'}
      </Button>
    </Card>
  )
}

export default ListErrorState
