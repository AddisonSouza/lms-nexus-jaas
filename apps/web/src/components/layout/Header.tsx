import { LogOut } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '@features/auth/store/authStore'
import api from '@lib/axios'

function Header() {
  const userId = useAuthStore((s) => s.userId)
  const clearToken = useAuthStore((s) => s.clearToken)
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
      <span className="text-sm font-semibold">LMS Nexus</span>
      <div className="flex items-center gap-3">
        {userId && <span className="text-xs text-muted-foreground">{userId.slice(0, 8)}…</span>}
        <button
          onClick={handleLogout}
          className="flex items-center gap-1.5 rounded border px-3 py-1.5 text-xs hover:bg-muted"
        >
          <LogOut className="h-3.5 w-3.5" />
          Sair
        </button>
      </div>
    </header>
  )
}

export default Header
