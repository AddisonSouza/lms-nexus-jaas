# Grill Me
- [x] Critério de aceite no backend? — Extensão E MIME (ambos precisam estar na lista).
- [x] Quais fluxos? — POST e PUT de submissão; anexo de tarefa do professor fica como está.
- [x] Onde mora a allowlist? — Política de domínio no `assessment`, lançando `InvalidAttachmentTypeException` (422); não importar `InvalidFileTypeException` do `curriculum`.
- [x] O que muda no front? — Exibir o erro 422 no diálogo de submissão e o Zod validar extensão além do MIME.
