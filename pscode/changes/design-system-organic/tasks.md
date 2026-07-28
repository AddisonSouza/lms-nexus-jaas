## 1. Fundação de tokens

- [x] 1.1 [FE] Reescrever `:root`/`.dark` em `apps/web/src/index.css` com a paleta Organic (claro + escuro), removendo o wrapper `hsl()` sobre valores OKLCH — variáveis passam a conter a função de cor completa, consumida via `var()` direto
- [x] 1.2 [FE] Adicionar tokens `--space-1..8`, `--radius-sm/md/lg`, `--shadow-sm/md/lg` em `index.css`
- [x] 1.3 [FE] Adicionar fontes Caprasimo (`--font-heading`) e Figtree (`--font-body`) via `@fontsource`, seguindo o padrão já usado para Geist
- [x] 1.4 [FE] Atualizar `tailwind.config.js`: cores via `var(--x)` direto, `fontFamily.heading/body`, `spacing` extra, `borderRadius.sm/md/lg`, `boxShadow.sm/md/lg`
- [x] 1.5 [FE] Grep por opacidade slash (`bg-primary/10`, `hsl(var(--x) / ...)` etc.) sobre variáveis de cor e ajustar os usos que dependiam do formato antigo (confirmado: todos os usos estão em arquivos já cobertos pelas Fases 2-7; serão eliminados quando cada arquivo for restilizado)
- [x] 1.6 [FE] Criar `ThemeProvider` (persistência via Zustand + `localStorage`, consistente com `useAuthStore`) que aplica/remove a classe `dark` na raiz do documento
- [x] 1.7 [FE] Adicionar toggle de tema no `Header`, consumindo o `ThemeProvider`
- [x] 1.8 [FE] Teste unitário do `ThemeProvider`/toggle (alterna classe `dark`, persiste e restaura preferência)

## 2. Primitivos de UI

- [x] 2.1 [FE] Restilizar `components/ui/button.tsx` para pill (`rounded-full`), mantendo variantes/props existentes
- [x] 2.2 [FE] Criar `components/ui/card.tsx` (`Card`, `CardKicker`, `CardTitle`, `CardBody`, `CardMeta`, variantes de elevação `sm/md/lg`)
- [x] 2.3 [FE] Criar `components/ui/badge.tsx` (variantes accent/accent-2/neutral/outline)
- [x] 2.4 [FE] Criar `components/ui/input.tsx` (pill) e `components/ui/textarea.tsx` (radius-md, sem pill, replicando a exceção do protótipo)
- [x] 2.5 [FE] Criar `components/ui/table.tsx`
- [x] 2.6 [FE] Criar `components/ui/segmented.tsx` (`Segmented`/`SegmentedOption`, usado como filtro em várias telas)
- [x] 2.7 [FE] Restilizar `dialog.tsx` e `alert-dialog.tsx` para o visual soft-shadow/cantos grandes do Organic
- [x] 2.8 [FE] Restilizar `sheet.tsx` (drawer lateral) para cantos grandes do lado interno e sombra do Organic
- [x] 2.9 [FE] Restilizar `popover.tsx` (usado pelo dropdown de notificações)
- [x] 2.10 [FE] Testes unitários de render dos novos/restilizados primitivos (Vitest + Testing Library)

## 3. Shell de navegação

- [x] 3.1 [FE] Reskin `Sidebar.tsx`: kicker "Ensino" acima do nav, pill no item ativo, ícones Lucide maiores (sem inventar seção "Convivência" — não existe item de nav equivalente hoje; sem org-switcher — JWT só tem uma organizationId)
- [x] 3.2 [FE] Reskin `Header.tsx`: mantido como barra global (marca + notificações + tema + logout); breadcrumb/título dinâmico por rota não implementado — exigiria mecanismo novo de estado cross-page, fora do escopo de reskin visual. Títulos de página já herdam `font-heading` globalmente via `index.css`
- [x] 3.3 [FE] Reskin `AppShell.tsx`: painel principal com cantos arredondados grandes sobre `--color-surface`
- [x] 3.4 [FE] Restilizar `NotificationBell.tsx`/`NotificationPanel.tsx` para o popover no novo visual

## 4. Autenticação e organização

- [ ] 4.1 [FE] Reskin `LoginPage.tsx`/`LoginForm.tsx` (layout split-screen do protótipo)
- [ ] 4.2 [FE] Reskin `RegisterPage.tsx`/`RegisterForm.tsx`
- [ ] 4.3 [FE] Reskin `ForgotPasswordPage.tsx` e `ResetPasswordPage.tsx`
- [ ] 4.4 [FE] Reskin `EmailConfirmationPage.tsx`/`ConfirmEmailCallbackPage.tsx`/`ResendConfirmationForm.tsx`
- [ ] 4.5 [FE] Reskin `AcceptInvitePage.tsx` (cartão de convite do protótipo)
- [ ] 4.6 [FE] Reskin `CreateOrganizationPage.tsx`/`CreateOrganizationForm.tsx`

## 5. Dashboards por papel

- [ ] 5.1 [FE] Reskin `StudentDashboard.tsx` + `UpcomingTasksList`/`RecentGradesList`/`SubjectAverageGradesList`/`SubmissionStatusSummary`
- [ ] 5.2 [FE] Reskin `GestorDashboard.tsx` + `ClassroomHealthCards`/`AtRiskStudentsList`/`PeriodSelector`
- [ ] 5.3 [FE] Reskin `AdminDashboard.tsx` + `MetricsCards`/`ActivityFeed`
- [ ] 5.4 [FE] Reskin `DashboardCharts.tsx`/`LastTaskGradeChart.tsx`, trocando cores fixas pelos tokens `--color-accent`/`--color-accent-2`
- [ ] 5.5 [FE] Reskin `OrganizationDashboardPage.tsx` (fallback sem papel específico)

## 6. Turmas e disciplinas

- [ ] 6.1 [FE] Reskin `ClassroomListPage.tsx` (tabela + ações "Nova turma"/"Entrar via código")
- [ ] 6.2 [FE] Reskin `JoinClassroomForm.tsx`
- [ ] 6.3 [FE] Reskin `ClassroomDetailPage.tsx` + `AnnouncementFeed`/`AnnouncementCard`/`AnnouncementForm` (mural)
- [ ] 6.4 [FE] Reskin `ClassroomMembersPanel.tsx`/`AddMemberDialog.tsx`
- [ ] 6.5 [FE] Reskin `ClassroomFormDialog.tsx`
- [ ] 6.6 [FE] Reskin `SubjectDetailPage.tsx` + `TopicList.tsx`/`ContentCard.tsx` (acordeão de tópicos com ícone por tipo de conteúdo)
- [ ] 6.7 [FE] Reskin `SubjectFormDialog.tsx`/`TopicFormDialog.tsx`/`ContentFormDialog.tsx`/`AssignTeacherDialog.tsx`/`LinkClassroomDialog.tsx`

## 7. Tarefas e avaliação

- [ ] 7.1 [FE] Reskin `TaskListPage.tsx` (staff) usando `Segmented` para o filtro rascunho/publicada/encerrada
- [ ] 7.2 [FE] Reskin `StudentTaskListPage.tsx` (com contagem regressiva de prazo)
- [ ] 7.3 [FE] Reskin `TaskFormDialog.tsx`
- [ ] 7.4 [FE] Reskin `SubmissionFormDialog.tsx`
- [ ] 7.5 [FE] Reskin `SubmissionListDrawer.tsx`
- [ ] 7.6 [FE] Reskin `EvaluationDialog.tsx`/`GradeFeedbackDrawer.tsx`
- [ ] 7.7 [FE] Reskin `PendingEvaluationsBadge.tsx`

## 8. Verificação final

- [ ] 8.1 [FE] Rodar suíte Vitest completa (`apps/web`) e corrigir apenas asserções de classe/estrutura de DOM quebradas pelo reskin, sem alterar lógica de teste
- [ ] 8.2 [FE] Rodar build/`tsc` do frontend sem erros de tipo após a troca de primitivos
- [ ] 8.3 [FE] Validar visualmente via Playwright MCP os 4 papéis (ALUNO/PROFESSOR/GESTOR/ADMIN_ORG), telas de auth e temas claro/escuro
- [ ] 8.4 [FE] Rodar as skills `arch-review`/`fe-arch-review` e corrigir quaisquer quebras de arquitetura introduzidas (ex.: import cruzado entre features)
