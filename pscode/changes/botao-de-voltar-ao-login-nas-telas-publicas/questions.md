# Perguntas — refinamento

- [x] **Onde o controle mora?**
  Componente próprio, `components/shared/BackToLogin.tsx`, inserido página a
  página. O `LoginPage` compartilha o `AuthLayout` e não pode herdar o controle;
  o `/confirm-email` nem usa o `AuthLayout` — um componente serve aos dois.
- [x] **Qual a forma visual?**
  Seta + texto (`← Voltar ao login`) acima do título, com `ArrowLeft` do Lucide —
  mesmo padrão de retorno já usado em `ClassroomDetailPage` e `SubjectDetailPage`.
- [x] **O "Já tem conta? Entrar" do `/register` sai?**
  Fica. São coisas diferentes: um é navegação de retorno, o outro é convite
  contextual para quem já tem conta.
- [x] **Em quais estados do `/confirm-email` o controle aparece?**
  No "Confirme seu e-mail" (sem token), no "Link inválido ou expirado" e no
  "E-mail já confirmado". Fora do loading e do sucesso, que já redirecionam.
