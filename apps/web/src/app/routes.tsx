import { createBrowserRouter, Navigate } from 'react-router-dom'
import LoginPage from '@features/auth/components/LoginPage'
import RegisterPage from '@features/auth/components/RegisterPage'
import ConfirmEmailCallbackPage from '@features/auth/components/ConfirmEmailCallbackPage'
import ForgotPasswordPage from '@features/auth/components/ForgotPasswordPage'
import ResetPasswordPage from '@features/auth/components/ResetPasswordPage'
import PublicRoute from '@components/shared/PublicRoute'
import ProtectedRoute from '@components/shared/ProtectedRoute'
import AppShell from '@components/layout/AppShell'
import CreateOrganizationPage from '@features/organization/components/CreateOrganizationPage'
import OrganizationDashboardPage from '@features/organization/components/OrganizationDashboardPage'
import AcceptInvitePage from '@features/invitation/components/AcceptInvitePage'
import ClassroomListPage from '@features/classroom/components/ClassroomListPage'
import ClassroomDetailPage from '@features/classroom/components/ClassroomDetailPage'
import SubjectListPage from '@features/curriculum/components/SubjectListPage'
import SubjectDetailPage from '@features/curriculum/components/SubjectDetailPage'
import TaskListPage from '@features/assessment/components/TaskListPage'
import StudentTaskListPage from '@features/assessment/components/StudentTaskListPage'
import RootRedirect from './RootRedirect'

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
  {
    path: '/invitations/:token/accept',
    element: <AcceptInvitePage />,
  },

  // Protected: no AppShell (org setup flow)
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

  // Protected: with AppShell layout
  {
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      { path: '/', element: <RootRedirect /> },
      { path: '/classrooms', element: <ClassroomListPage /> },
      { path: '/classrooms/:id', element: <ClassroomDetailPage /> },
      { path: '/curriculum', element: <SubjectListPage /> },
      { path: '/curriculum/:subjectId', element: <SubjectDetailPage /> },
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

  // Catch-all
  { path: '*', element: <Navigate to="/" replace /> },
])
