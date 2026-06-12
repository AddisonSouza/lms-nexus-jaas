export const authKeys = {
  all: ['auth'] as const,
  register: () => [...authKeys.all, 'register'] as const,
  session: () => [...authKeys.all, 'session'] as const,
  login: () => [...authKeys.all, 'login'] as const,
  confirmEmail: () => [...authKeys.all, 'confirmEmail'] as const,
  resendConfirmation: () => [...authKeys.all, 'resendConfirmation'] as const,
}
