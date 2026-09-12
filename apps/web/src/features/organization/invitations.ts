import type { InvitationStatus } from './api/organization-api'

export const invitationStatusLabels: Record<InvitationStatus, string> = {
  PENDING: 'Pendente',
  USED: 'Aceito',
  EXPIRED: 'Expirado',
  CANCELLED: 'Cancelado',
}

/** Só o pendente ainda pede atenção do admin; aceito é o desfecho feliz; o resto é histórico. */
export const invitationStatusBadge: Record<InvitationStatus, 'accent' | 'accent-2' | 'neutral'> = {
  PENDING: 'accent',
  USED: 'accent-2',
  EXPIRED: 'neutral',
  CANCELLED: 'neutral',
}
