import { Outlet } from 'react-router-dom'
import MinimalHeader from './MinimalHeader'

/**
 * Layout das telas que ficam fora da área logada (boas-vindas, criação de
 * organização, aceite de convite): topo enxuto com saída e conteúdo centralizado.
 */
function SetupShell() {
  return (
    <div className="flex min-h-screen flex-col bg-background">
      <MinimalHeader />
      <main className="flex flex-1 items-center justify-center p-6">
        <Outlet />
      </main>
    </div>
  )
}

export default SetupShell
