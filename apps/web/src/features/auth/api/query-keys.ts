export const authKeys = {
  all: ['auth'] as const,
  register: () => [...authKeys.all, 'register'] as const,
}
