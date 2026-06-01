import { createBrowserRouter } from 'react-router-dom'
import RegisterForm from '@features/auth/components/RegisterForm'
import EmailConfirmationPage from '@features/auth/components/EmailConfirmationPage'

export const router = createBrowserRouter([
  {
    path: '/register',
    element: <RegisterForm />,
  },
  {
    path: '/confirm-email',
    element: <EmailConfirmationPage />,
  },
])
