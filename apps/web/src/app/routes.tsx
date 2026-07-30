import { createBrowserRouter, Navigate } from 'react-router-dom'
import LoginPage from '@features/auth/components/LoginPage'
import RegisterPage from '@features/auth/components/RegisterPage'
import ConfirmEmailCallbackPage from '@features/auth/components/ConfirmEmailCallbackPage'
import ForgotPasswordPage from '@features/auth/components/ForgotPasswordPage'
import ResetPasswordPage from '@features/auth/components/ResetPasswordPage'
import PublicRoute from '@components/shared/PublicRoute'
import ProtectedRoute from '@components/shared/ProtectedRoute'
import AppShell from '@components/layout/AppShell'
import SetupShell from '@components/layout/SetupShell'
import CreateOrganizationPage from '@features/organization/components/CreateOrganizationPage'
import WelcomePage from '@features/onboarding/components/WelcomePage'
import AcceptInvitePage from '@features/invitation/components/AcceptInvitePage'
import ClassroomListPage from '@features/classroom/components/ClassroomListPage'
import SubjectListPage from '@features/curriculum/components/SubjectListPage'
import TaskListPage from '@features/assessment/components/TaskListPage'
import StudentTaskListPage from '@features/assessment/components/StudentTaskListPage'
import RootRedirect from './RootRedirect'
import RequireOrganization from './RequireOrganization'
import OrganizationRoute from './OrganizationRoute'
import SubjectDetailRoute from './SubjectDetailRoute'
import ClassroomDetailRoute from './ClassroomDetailRoute'

export const router = createBrowserRouter([
  // Public routes
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
    element: (
      <PublicRoute>
        <RegisterPage />
      </PublicRoute>
    ),
  },
  {
    path: '/confirm-email',
    element: <ConfirmEmailCallbackPage />,
  },
  {
    path: '/forgot-password',
    element: (
      <PublicRoute>
        <ForgotPasswordPage />
      </PublicRoute>
    ),
  },
  {
    path: '/reset-password',
    element: (
      <PublicRoute>
        <ResetPasswordPage />
      </PublicRoute>
    ),
  },
  // No AppShell (org setup flow): topo enxuto com saída, conteúdo centralizado.
  // O aceite de convite é público — o próprio MinimalHeader esconde o "Sair".
  {
    element: <SetupShell />,
    children: [
      { path: '/invitations/:token/accept', element: <AcceptInvitePage /> },
      {
        path: '/welcome',
        element: (
          <ProtectedRoute>
            <WelcomePage />
          </ProtectedRoute>
        ),
      },
      {
        path: '/organizations/new',
        element: (
          <ProtectedRoute>
            <CreateOrganizationPage />
          </ProtectedRoute>
        ),
      },
    ],
  },
  // Protected: with AppShell layout
  {
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      { path: '/', element: <RootRedirect /> },
      // Tudo daqui para baixo depende de uma organização: sem ela, o guard
      // mostra o estado vazio em vez da tela quebrada.
      {
        element: <RequireOrganization />,
        children: [
          { path: '/organizations/:id', element: <OrganizationRoute /> },
          { path: '/classrooms', element: <ClassroomListPage /> },
          { path: '/classrooms/:id', element: <ClassroomDetailRoute /> },
          { path: '/curriculum', element: <SubjectListPage /> },
          { path: '/curriculum/:subjectId', element: <SubjectDetailRoute /> },
          {
            path: '/assessment/tasks',
            element: (
              <ProtectedRoute roles={['PROFESSOR', 'ADMIN_ORG', 'GESTOR']}>
                <TaskListPage />
              </ProtectedRoute>
            ),
          },
          {
            path: '/assessment/student-tasks',
            element: (
              <ProtectedRoute roles={['ALUNO']}>
                <StudentTaskListPage />
              </ProtectedRoute>
            ),
          },
        ],
      },
    ],
  },

  // Catch-all
  { path: '*', element: <Navigate to="/" replace /> },
])
