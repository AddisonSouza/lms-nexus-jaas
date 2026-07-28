import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import { Card, CardKicker, CardTitle, CardBody, CardMeta } from './card'
import { Badge } from './badge'
import { Table, TableHeader, TableBody, TableRow, TableHead, TableCell } from './table'

describe('Card', () => {
  it('renders its composed content', () => {
    render(
      <Card elevation="sm">
        <CardKicker>A entregar</CardKicker>
        <CardTitle>3</CardTitle>
        <CardBody>1 vence hoje</CardBody>
        <CardMeta>atualizado agora</CardMeta>
      </Card>
    )

    expect(screen.getByText('A entregar')).toBeTruthy()
    expect(screen.getByText('3')).toBeTruthy()
    expect(screen.getByText('1 vence hoje')).toBeTruthy()
    expect(screen.getByText('atualizado agora')).toBeTruthy()
  })
})

describe('Badge', () => {
  it('renders its label for each variant', () => {
    render(
      <>
        <Badge variant="accent">Publicada</Badge>
        <Badge variant="accent-2">Saudável</Badge>
        <Badge variant="neutral">Rascunho</Badge>
        <Badge variant="outline">MAT101</Badge>
      </>
    )

    expect(screen.getByText('Publicada')).toBeTruthy()
    expect(screen.getByText('Saudável')).toBeTruthy()
    expect(screen.getByText('Rascunho')).toBeTruthy()
    expect(screen.getByText('MAT101')).toBeTruthy()
  })
})

describe('Table', () => {
  it('renders headers and row data', () => {
    render(
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Turma</TableHead>
            <TableHead>Status</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow>
            <TableCell>3º A — Manhã</TableCell>
            <TableCell>Ativa</TableCell>
          </TableRow>
        </TableBody>
      </Table>
    )

    expect(screen.getByRole('columnheader', { name: 'Turma' })).toBeTruthy()
    expect(screen.getByRole('cell', { name: '3º A — Manhã' })).toBeTruthy()
  })
})
