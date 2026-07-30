import { Outlet } from 'react-router-dom'
import { useAuthStore } from '@store/authStore'
import NoOrganizationState from '@features/onboarding/components/NoOrganizationState'

// Vive em app/ (e não numa feature) para que nenhuma feature precise importar
// `onboarding` só para se proteger — o roteador é quem aplica o guard.
function RequireOrganization() {
  const organizationId = useAuthStore((s) => s.organizationId)

  if (!organizationId) return <NoOrganizationState />

  return <Outlet />
}

export default RequireOrganization
