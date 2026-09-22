import { useEffect, useState } from 'react'

/**
 * O mesmo valor, porém só depois de `delay` ms sem mudar.
 *
 * Existe para que uma busca digitada não vire uma requisição por tecla: o
 * componente continua reagindo a cada letra, mas quem consulta o servidor usa
 * este valor atrasado. Vive em `hooks/` porque `classroom` e `curriculum` o
 * consomem, e uma feature não importa da outra.
 */
export function useDebouncedValue<T>(value: T, delay = 300): T {
  const [debouncedValue, setDebouncedValue] = useState(value)

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay)
    return () => clearTimeout(timer)
  }, [value, delay])

  return debouncedValue
}
