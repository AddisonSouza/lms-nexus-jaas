export const organizationKeys = {
  all: ['organizations'] as const,
  lists: () => [...organizationKeys.all, 'list'] as const,
  members: (organizationId: string) => [...organizationKeys.all, organizationId, 'members'] as const,
}
