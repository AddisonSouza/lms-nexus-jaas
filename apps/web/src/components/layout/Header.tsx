import { LogOut, Moon, Sun } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@store/authStore'
import { useThemeStore } from '@store/themeStore'
import NotificationBell from '@features/communication/components/NotificationBell'
import api from '@lib/axios'

function Header() {
  const userId = useAuthStore((s) => s.userId)
  const clearToken = useAuthStore((s) => s.clearToken)
  const theme = useThemeStore((s) => s.theme)
  const toggleTheme = useThemeStore((s) => s.toggleTheme)
  const navigate = useNavigate()

  const handleLogout = async () => {
    try {
      await api.post('/auth/logout', {}, { withCredentials: true })
    } finally {
      clearToken()
      navigate('/login', { replace: true })
    }
  }

  return (
    <header className="flex h-14 items-center justify-between border-b bg-background px-4">
      <span className="font-heading text-sm">Nexus</span>
      <div className="flex items-center gap-3">
        <NotificationBell />
        <button
          onClick={toggleTheme}
          title="Alternar tema"
          className="flex h-8 w-8 items-center justify-center rounded-full border hover:bg-muted"
        >
          {theme === 'dark' ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </button>
        {userId && <span className="text-xs text-muted-foreground">{userId.slice(0, 8)}…</span>}
        <button
          onClick={handleLogout}
          className="flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-xs hover:bg-muted"
        >
          <LogOut className="h-3.5 w-3.5" />
          Sair
        </button>
      </div>
    </header>
  )
}

export default Header
