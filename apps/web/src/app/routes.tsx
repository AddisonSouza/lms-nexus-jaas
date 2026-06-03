import { createBrowserRouter } from 'react-router-dom'
import RegisterForm from '@features/auth/components/RegisterForm'
import EmailConfirmationPage from '@features/auth/components/EmailConfirmationPage'
import LoginPage from '@features/auth/components/LoginPage'
import PublicRoute from '@components/shared/PublicRoute'

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
    element: <EmailConfirmationPage />,
  },
])
