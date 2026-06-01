# ADR-010 — Lombok Obrigatório

**Status:** Aceito  
**Data:** Maio 2026

## Contexto
Decidir a estratégia para eliminar boilerplate Java (getters, setters, construtores, builders).

## Decisão
Lombok obrigatório em todas as classes Java do projeto.

## Justificativa
- Elimina boilerplate e mantém o código focado na lógica de negócio
- Regras claras por tipo de classe evitam uso incorreto

## Regras de Uso
| Anotação | Onde usar |
|---|---|
| `@Value` | Value Objects de domínio (imutáveis) |
| `@Builder` | Domain Entities e Application DTOs |
| `@Data` | JPA Entities (infrastructure apenas) |
| `@RequiredArgsConstructor` | Application Services e Adapters |
| `@Slf4j` | Qualquer classe que precisa de logging |

## Consequências
- `@Data` e `@Entity` são proibidos no pacote `domain/`
- `@EqualsAndHashCode(onlyExplicitlyIncluded=true)` obrigatório em JPA Entities
- Ver regras LOM-01 a LOM-07 em DECISIONS.md
