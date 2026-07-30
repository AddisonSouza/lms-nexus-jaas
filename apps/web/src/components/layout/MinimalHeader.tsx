import { LogOut } from 'lucide-react'
import { useAuthStore } from '@store/authStore'
import { useLogout } from '@features/auth/hooks/useLogout'

/**
 * Topo enxuto das telas fora do AppShell. A ação de sair só aparece com sessão:
 * `/invitations/:token/accept` é rota pública e pode ser aberta deslogado.
 */
function MinimalHeader() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const userId = useAuthStore((s) => s.userId)
  const handleLogout = useLogout()

  return (
    <header className="flex h-14 shrink-0 items-center justify-between bg-surface px-4">
      <div className="flex items-center gap-2.5">
        <div className="flex h-[30px] w-[30px] items-center justify-center rounded-full bg-accent font-heading text-[13px] text-accent-foreground">
          N
        </div>
        <span className="font-heading text-base">Nexus</span>
      </div>

      {isAuthenticated && (
        <div className="flex items-center gap-2">
          {userId && <span className="text-xs text-muted-foreground">{userId.slice(0, 8)}…</span>}
          <button
            onClick={handleLogout}
            title="Sair"
            className="flex h-8 w-8 items-center justify-center rounded-full border border-border hover:bg-muted"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      )}
    </header>
  )
}

export default MinimalHeader
