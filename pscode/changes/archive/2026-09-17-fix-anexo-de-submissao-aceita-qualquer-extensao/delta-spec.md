# fix: anexo de submissão aceita qualquer extensão — Delta

## Added
- **`AttachmentTypePolicy`** em `assessment/domain/model`: só aceita o anexo cuja
  **extensão e MIME** concordem entre si — `.pdf .doc .docx .zip .jpg .jpeg .png`
  contra os MIMEs correspondentes. Normaliza caixa e parâmetros
  (`image/png; charset=binary`), e trata nome sem extensão, terminado em ponto ou
  oculto (`.gitignore`) como fora da lista. Fora da lista → 
  `InvalidAttachmentTypeException` (422, já existente).
  - Só o MIME não bastaria: quem envia declara o cabeçalho que quiser. Só a
    extensão também não: renomear é trivial. O par não prova o conteúdo — isso
    seria magic bytes, fora do escopo —, mas fecha os dois enganos de uma linha.
- **Validação de todos os anexos antes do primeiro `store`** em
  `SubmitTaskService` e `EditSubmissionService`: um envio com um arquivo válido e
  outro recusado não deixa o válido órfão no storage, e a edição recusada
  preserva a resposta anterior.
- **Erro da API no diálogo de envio**: `SubmissionFormDialog` recebe `error` e o
  exibe com `role="alert"`, sem fechar — o aluno troca o arquivo ali mesmo. A
  página passa a mensagem traduzida por `apiErrorMessage` e chama `reset()` ao
  fechar, para a recusa não reaparecer na abertura seguinte.
- Testes: `AttachmentTypePolicyTest` (22), os casos de anexo em
  `SubmitTaskServiceTest` e `EditSubmissionServiceTest`, `submission.schema` (12)
  e `SubmissionFormDialog` (4).

## Changed
- **`POST /tasks/{id}/submissions` e `PUT /tasks/{id}/submissions/{id}`** passam
  a recusar com `422` o anexo fora da lista. Antes gravavam qualquer arquivo,
  inclusive executável.
- **`submission.schema.ts`** confere a extensão além do MIME. O `File.type` vem
  vazio quando o sistema não reconhece o arquivo — e `.exe` no Linux é
  justamente um desses casos —, então a checagem só por MIME deixava o envio
  seguir até um 422 que a tela sequer mostrava.
- **`application/x-zip-compressed`** entra na lista (back e front): é como o
  Windows reporta um `.zip`, que antes seria recusado como tipo desconhecido.

## Removed
- Nada.

## Conhecido, fora deste card
- Anexos inválidos gravados **antes** desta correção continuam no banco e no
  storage — o ambiente local tem um `malware.exe` da bateria E2E de 15/09/2026.
  A correção fecha a porta; a limpeza do que já entrou não foi feita.
- Anexo de tarefa do professor (`CreateTaskService`) segue validando só o MIME, e
  o conteúdo de disciplina idem — ambos fora do escopo, junto com magic bytes,
  antivírus e limite de tamanho.
