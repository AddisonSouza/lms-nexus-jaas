import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, BookOpen, LogIn, Copy } from 'lucide-react'
import { useClassrooms } from '../hooks/useClassrooms'
import { useCreateClassroom } from '../hooks/useCreateClassroom'
import { useAuthStore } from '@store/authStore'
import ClassroomFormDialog from './ClassroomFormDialog'
import JoinClassroomForm from './JoinClassroomForm'
import type { ClassroomFormData } from '../schemas/classroomSchema'
import { Card } from '@components/ui/card'
import { Badge } from '@components/ui/badge'
import { Button } from '@components/ui/button'
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from '@components/ui/table'

function ClassroomListPage() {
  const [showCreate, setShowCreate] = useState(false)
  const [showJoin, setShowJoin] = useState(false)
  const { data: classrooms, isLoading } = useClassrooms()
  const createClassroom = useCreateClassroom()

  const role = useAuthStore((s) => s.role)
  const canManage = role === 'ADMIN_ORG' || role === 'GESTOR'
  const canSeeInviteCode = canManage || role === 'PROFESSOR'

  const handleCreate = (data: ClassroomFormData) => {
    createClassroom.mutate(data, { onSuccess: () => setShowCreate(false) })
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <BookOpen className="h-6 w-6 text-accent" />
          <h2 className="mb-0">Turmas</h2>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" onClick={() => setShowJoin((v) => !v)}>
            <LogIn className="h-4 w-4" /> Entrar via código
          </Button>
          {canManage && (
            <Button onClick={() => setShowCreate(true)}>
              <Plus className="h-4 w-4" /> Nova Turma
            </Button>
          )}
        </div>
      </div>

      {showJoin && (
        <Card elevation="sm">
          <JoinClassroomForm />
        </Card>
      )}

      {isLoading ? (
        <p className="text-muted-foreground">Carregando turmas...</p>
      ) : classrooms?.length === 0 ? (
        <p className="text-muted-foreground">Nenhuma turma encontrada.</p>
      ) : (
        <Card elevation="sm" className="overflow-hidden p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Período</TableHead>
                {canSeeInviteCode && <TableHead>Código</TableHead>}
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {classrooms?.map((c) => (
                <TableRow key={c.id}>
                  <TableCell>
                    <Link to={`/classrooms/${c.id}`} className="font-medium hover:underline">
                      {c.name}
                    </Link>
                  </TableCell>
                  <TableCell className="text-muted-foreground">{c.academicPeriod}</TableCell>
                  {canSeeInviteCode && (
                    <TableCell>
                      {c.inviteCode && (
                        <div className="flex items-center gap-2">
                          <span className="font-mono font-medium tracking-widest">{c.inviteCode}</span>
                          <button
                            onClick={() => navigator.clipboard.writeText(c.inviteCode!)}
                            className="text-muted-foreground hover:text-foreground"
                            title="Copiar código"
                          >
                            <Copy className="h-4 w-4" />
                          </button>
                        </div>
                      )}
                    </TableCell>
                  )}
                  <TableCell>
                    <Badge variant={c.status === 'ACTIVE' ? 'accent-2' : 'neutral'}>
                      {c.status === 'ACTIVE' ? 'Ativa' : 'Arquivada'}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      )}

      <ClassroomFormDialog
        open={showCreate}
        onClose={() => setShowCreate(false)}
        onSubmit={handleCreate}
        isPending={createClassroom.isPending}
        title="Nova Turma"
      />
    </div>
  )
}

export default ClassroomListPage
