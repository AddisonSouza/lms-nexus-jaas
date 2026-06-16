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
import AcceptInvitePage from '@features/invitation/components/AcceptInvitePage'
import ClassroomListPage from '@features/classroom/components/ClassroomListPage'
import ClassroomDetailPage from '@features/classroom/components/ClassroomDetailPage'
import SubjectListPage from '@features/curriculum/components/SubjectListPage'
import SubjectDetailPage from '@features/curriculum/components/SubjectDetailPage'
import TaskListPage from '@features/assessment/components/TaskListPage'
import StudentTaskListPage from '@features/assessment/components/StudentTaskListPage'

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
  {
    path: '/invitations/:token/accept',
    element: <AcceptInvitePage />,
  },
  {
    path: '/classrooms',
    element: (
      <ProtectedRoute>
        <ClassroomListPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/classrooms/:id',
    element: (
      <ProtectedRoute>
        <ClassroomDetailPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/curriculum',
    element: (
      <ProtectedRoute>
        <SubjectListPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/curriculum/:subjectId',
    element: (
      <ProtectedRoute>
        <SubjectDetailPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/assessment/tasks',
    element: (
      <ProtectedRoute>
        <TaskListPage />
      </ProtectedRoute>
    ),
  },
  {
    path: '/assessment/student-tasks',
    element: (
      <ProtectedRoute>
        <StudentTaskListPage />
      </ProtectedRoute>
    ),
  },
])
