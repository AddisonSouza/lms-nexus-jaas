import { useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { logoutUser } from '../api/auth-api'
import { useAuthStore } from '@store/authStore'

/**
 * Encerra a sessão e leva ao login. A sessão local é limpa mesmo que a chamada
 * ao servidor falhe — deixar o usuário preso num estado autenticado inválido é
 * pior do que um refresh token órfão, que expira sozinho.
 *
 * Usa `signOut()`, e não `clearToken()`, para as telas saberem que a pessoa
 * escolheu sair: a de aceite de convite não deve levar o convite ao login (#210).
 *
 * Também esvazia o cache do React Query: as chaves não dependem do usuário, e a
 * próxima conta a entrar leria os dados desta — como as organizações no seletor
 * (#217).
 */
export function useLogout() {
  const signOut = useAuthStore((s) => s.signOut)
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  return async () => {
    try {
      await logoutUser()
    } catch {
      // Servidor fora do ar não pode impedir a saída — o erro morre aqui para
      // não virar uma rejeição não tratada no handler do clique.
    } finally {
      signOut()
      queryClient.clear()
      navigate('/login', { replace: true })
    }
  }
}
