import { useEffect } from 'react'
import { useThemeStore, applyThemeClass } from '@store/themeStore'

interface ThemeProviderProps {
  children: React.ReactNode
}

function ThemeProvider({ children }: ThemeProviderProps) {
  const theme = useThemeStore((s) => s.theme)

  useEffect(() => {
    applyThemeClass(theme)
  }, [theme])

  return <>{children}</>
}

export default ThemeProvider
