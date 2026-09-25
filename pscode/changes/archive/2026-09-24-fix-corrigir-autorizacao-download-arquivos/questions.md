# Grill Me
- [x] Como estruturar a checagem? — `FileAccessPort` definido em storage; cada módulo dono implementa a regra do seu `StorageContext`, e o contexto sai do prefixo da chave. Endpoint e front ficam iguais.
- [x] Qual regra por arquivo? — A mesma das telas atuais: material → aluno só se estiver em turma da disciplina, demais papéis da org liberados; anexo de tarefa → aluno só se a tarefa estiver publicada; entrega → o aluno dono ou o professor criador da tarefa; aviso → membro da turma.
- [x] Como validar? — IT (`@QuarkusTest` + Testcontainers) com caso permitido e negado (outra org, usuário sem vínculo) para cada um dos 4 contextos.
