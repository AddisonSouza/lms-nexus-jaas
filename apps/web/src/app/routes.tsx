import { createBrowserRouter } from 'react-router-dom'
import RegisterForm from '@features/auth/components/RegisterForm'
import ConfirmEmailCallbackPage from '@features/auth/components/ConfirmEmailCallbackPage'
import LoginPage from '@features/auth/components/LoginPage'
import ForgotPasswordPage from '@features/auth/components/ForgotPasswordPage'
import ResetPasswordPage from '@features/auth/components/ResetPasswordPage'
import PublicRoute from '@components/shared/PublicRoute'
import ProtectedRoute from '@components/shared/ProtectedRoute'
import CreateOrganizationPage from '@features/organization/components/CreateOrganizationPage'
import OrganizationDashboardPage from '@features/organization/components/OrganizationDashboardPage'

export const router = createBrowserRouter([
  {
    path: '/login',
    element: (
      <PublicRoute>
        <LoginPage />
      </PublicRoute>
    ),
  },
  {
    path: '/register',
    element: <RegisterForm />,
  },
  {
    path: '/confirm-email',
    element: <ConfirmEmailCallbackPage />,
  },
  {
    path: '/forgot-password',
    element: <ForgotPasswordPage />,
  },
  {
    path: '/reset-password',
    element: <ResetPasswordPage />,
  },
  {
    path: '/organizations/new',
    element: (
      <ProtectedRoute>
        <CreateOrganizationPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/organizations/:id',
    element: (
      <ProtectedRoute>
        <OrganizationDashboardPage />
      </ProtectedRoute>
    ),
  },
])
