## Why

**RF-01 | Módulo: `identity` | Impacto: fullstack (backend + frontend)**

O sistema não possui nenhum mecanismo de criação de contas. Sem cadastro de usuário, nenhum outro fluxo do LMS pode ser iniciado — não há organização, turma ou conteúdo sem um usuário autenticado. Esta é a porta de entrada obrigatória do sistema e o pré-requisito para todos os demais RFs do MVP.

## What Changes

- Novo endpoint público `POST /auth/register` no módulo `identity`
- `RegisterUserUseCase` (Port de entrada) + `RegisterUserService` (implementação)
- `User` como Aggregate Root em `domain/model/` com Value Objects `Email`, `FullName`, `UserId`
- `UserRegisteredEvent` publicado via CDI para que outros módulos (ex: `communication`) possam reagir
- `UserJpaEntity` em `infrastructure/persistence/` com migration Flyway `V001__create_users_table.sql`
- `EmailPort` (Port de saída) + `QuarkusMailAdapter` para envio do e-mail de confirmação com token de 24h
- `RegisterForm` em `features/auth` no frontend com validação Zod + React Hook Form
- Página de feedback pós-cadastro ("Confirme seu e-mail")

## Capabilities

### New Capabilities

- `user-registration`: Cadastro de usuário com nome completo, e-mail e senha. Cria conta com status `PENDING_CONFIRMATION` e dispara e-mail de confirmação com token de 24h.

### Modified Capabilities

_(nenhuma — nenhuma spec existente é afetada)_

## Non-goals

- Autenticação (login/logout) → RF-02
- Confirmação de e-mail (validação do token) → RF-04
- Vínculo do usuário a uma organização → RF-05 e RF-06
- Recuperação de senha → RF-03
- Social login / OAuth externo

## Impact

**Backend (`apps/api`):**
- Novo pacote `module/identity/` com todas as camadas (domain, application, infrastructure, interfaces)
- Nova tabela `users` (migration `V001__create_users_table.sql`)
- Dependência de serviço de e-mail via SMTP (configurável por variável de ambiente)

**Frontend (`apps/web`):**
- Nova rota pública `/register` em `routes.tsx`
- Componente `RegisterForm` + `registerSchema.ts` (Zod) em `features/auth`
- Query key e mutation em `features/auth/api/`

**Infraestrutura:**
- Variáveis de ambiente: `MAIL_HOST`, `MAIL_PORT`, `MAIL_FROM` no `.env.example`
