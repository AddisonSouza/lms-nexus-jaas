import { useNavigate } from 'react-router-dom'
import { logoutUser } from '../api/auth-api'
import { useAuthStore } from '@store/authStore'

/**
 * Encerra a sessão e leva ao login. A sessão local é limpa mesmo que a chamada
 * ao servidor falhe — deixar o usuário preso num estado autenticado inválido é
 * pior do que um refresh token órfão, que expira sozinho.
 */
export function useLogout() {
  const clearToken = useAuthStore((s) => s.clearToken)
  const navigate = useNavigate()

  return async () => {
    try {
      await logoutUser()
    } catch {
      // Servidor fora do ar não pode impedir a saída — o erro morre aqui para
      // não virar uma rejeição não tratada no handler do clique.
    } finally {
      clearToken()
      navigate('/login', { replace: true })
    }
  }
}
