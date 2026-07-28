import { Outlet } from 'react-router-dom'
import Header from './Header'
import Sidebar from './Sidebar'

function AppShell() {
  return (
    <div className="flex h-screen flex-col bg-surface">
      <Header />
      <div className="flex flex-1 overflow-hidden">
        <Sidebar />
        <main className="flex-1 overflow-y-auto rounded-tl-[calc(var(--radius-lg)*1.4)] bg-background shadow-md">
          <div className="p-6 sm:p-8">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}

export default AppShell
