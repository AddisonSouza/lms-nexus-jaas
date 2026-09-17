# Questions — fix tarefa não fecha após o prazo

- [x] **Como produzir o CLOSED?** → Derivado na leitura (`Task.effectiveStatus()`
  no domínio). Sem job, sem `quarkus-scheduler`, sem migration; o banco continua
  guardando `PUBLISHED`.
- [x] **Tarefa encerrada some das listas do aluno?** → Não. As queries passam a
  aceitar `PUBLISHED` e `CLOSED`; o aluno continua vendo a tarefa vencida e a
  nota dela (RF-14). Só o botão de enviar some.
- [x] **Qual erro ao submeter fora do prazo?** → Manter `422 DeadlineExpired`.
  A checagem de prazo vem antes da de status, preservando o `API_CONTRACT.md`.
- [x] **Badge do professor?** → Traduzir todos os status num mapa único
  (Rascunho / Publicada / Encerrada / Avaliada), eliminando o enum cru da tela.
- [x] **Badge do aluno?** → Badge extra "Encerrada" ao lado do badge de
  submissão, quando `status === 'CLOSED'`.
