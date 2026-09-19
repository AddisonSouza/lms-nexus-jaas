import { useEffect, useState } from 'react'
import { rateLimitSeconds } from '../schemas/rateLimitSchema'

/**
 * Segundos restantes do bloqueio, contados na tela a partir do `Retry-After` da
 * recusa. O prazo vem do servidor (o erro da mutation); o que é estado local
 * aqui é só o relógio, para o botão reabrir sozinho quando o tempo passar.
 */
export function useRateLimitCountdown(error: unknown): number {
  const seconds = rateLimitSeconds(error)
  const [remaining, setRemaining] = useState(0)

  useEffect(() => {
    if (seconds === null) {
      setRemaining(0)
      return
    }

    setRemaining(seconds)
    const timer = setInterval(() => {
      setRemaining((current) => (current <= 1 ? 0 : current - 1))
    }, 1000)

    return () => clearInterval(timer)
    // `error` entra na lista porque duas recusas seguidas podem trazer o mesmo
    // número de segundos — sem ele, a contagem não reiniciaria.
  }, [seconds, error])

  return remaining
}
