# Anexo de submissão aceita qualquer extensão

**Severidade:** crítico · RF-12, file-storage · origem: bateria E2E 15/09/2026

## Objetivo
Aplicar a lista de tipos permitidos ao anexo de submissão do aluno.
`POST /tasks/{id}/submissions` aceitou `malware.exe` com 201; o mesmo arquivo é
rejeitado com 422 em conteúdo de disciplina.

## Comportamento esperado
- Anexo de submissão aceita só pdf, doc, docx, zip, jpg/jpeg, png (mesma lista de
  `TASK_ATTACHMENT`).
- Fora da lista → 422, nada é armazenado, mensagem exibida no front.
- Front filtra por `accept` e valida antes de enviar.

## Fora do escopo
- Validação por magic bytes; antivírus.
