interface AuthLayoutProps {
  children: React.ReactNode
}

function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="grid min-h-screen grid-cols-1 bg-background text-foreground md:grid-cols-2">
      <div className="relative hidden flex-col justify-between overflow-hidden bg-accent-700 p-8 text-accent-100 md:flex">
        <div className="absolute -right-24 -top-20 h-64 w-64 rounded-full bg-white/10" />
        <div className="absolute -bottom-10 -left-14 h-40 w-40 rounded-full bg-white/5" />
        <div className="relative flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-full bg-accent-100 font-heading text-lg text-accent-800">
            N
          </div>
          <span className="font-heading text-xl">Nexus</span>
        </div>
        <div className="relative max-w-md">
          <h1 className="mb-4 font-heading text-4xl leading-tight">
            Sua turma toda em um lugar quentinho.
          </h1>
          <p className="text-base opacity-85">
            Turmas, disciplinas, tarefas e notas — organizados por tópico, do jeito que
            professor e aluno realmente usam.
          </p>
        </div>
        <div className="relative flex flex-wrap gap-2">
          <span className="rounded-full bg-white/15 px-2.5 py-0.5 text-[11px] tracking-wide text-accent-100">
            Multi-organização
          </span>
          <span className="rounded-full bg-white/15 px-2.5 py-0.5 text-[11px] tracking-wide text-accent-100">
            Entrada por código
          </span>
          <span className="rounded-full bg-white/15 px-2.5 py-0.5 text-[11px] tracking-wide text-accent-100">
            Conteúdo por tópico
          </span>
        </div>
      </div>

      <div className="grid place-items-center p-8">
        <div className="w-full max-w-sm">{children}</div>
      </div>
    </div>
  )
}

export default AuthLayout
