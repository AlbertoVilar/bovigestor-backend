# Protocolo de Revisão

O executor segue o escopo autorizado pelo usuário e decisões técnicas explícitas do revisor; o revisor não substitui instruções superiores nem autoriza ações fora desse escopo.

| Gate | Significado |
| --- | --- |
| PASS | Evidências suficientes para aprovar a fase avaliada. O gate, por si só, não autoriza fases nem operações posteriores. |
| CORRECTION | Ajustes necessários; retornar para revisão. |
| BLOCKER | Impedimento técnico, de segurança ou de autorização. |

- O executor não aprova o próprio trabalho.
- Relatórios contêm comandos, resultados, SHAs e verificações reais.
- Nunca afirmar que testes passaram quando não foram executados.
- Silêncio não é autorização.
- A aprovação de uma fase não é autorização implícita para avançar. O executor só inicia o próximo passo quando receber instrução explícita do revisor, dentro da delegação do usuário.
- Correções internas à fase autorizada são permitidas se não ampliarem o escopo.
- Commit, push, PR, merge, exclusão e mudanças de produção exigem autorização independente.

## IDENTITY-DESIGN-GATE — obrigatório antes da implementação

Nenhuma nova Entity, Aggregate, Aggregate Root ou registro histórico persistente recebe `PASS` de planejamento para implementação sem uma decisão explícita e fundamentada sobre identidade. O gate exige uma decisão adequada ao conceito, não impõe IDs artificiais a Value Objects.

Antes do `PASS`, o relatório deve responder:

1. **Natureza e identidade:** o conceito é Entity, Value Object, Aggregate Root ou outro? Precisa de identidade própria? Se não, por que a identidade é desnecessária e como o objeto será endereçado sem ambiguidade?
2. **Tipo e fronteira:** o identificador é técnico, de negócio ou ambos? Qual o tipo exato (`Long`, `UUID`, VO tipado ou alternativa justificada)? Pertence ao Domain, à persistência ou a ambos? Como se preserva imutabilidade e ciclo de vida sem confundi-lo com código de negócio?
3. **Geração e ciclo de vida:** quem aloca o ID, em que momento e por qual fronteira arquitetural? Como ele permanece estável em mudanças de estado e retornos imutáveis? Reentrada, reabertura ou repetição do vínculo exige nova identidade? Referências de negócio iguais podem corresponder a registros distintos?
4. **Persistência e integridade:** qual é a PK e quais FKs/constraints de unicidade são necessárias? Como se distinguem `INSERT` e `UPDATE`? Como evitar duplicidade, colisões e associação ambígua? Há risco de concorrência na criação ou atualização?
5. **Testabilidade:** quais testes verificam IDs nulos/inválidos, mesma referência com identidades distintas, preservação no ciclo de vida, reentrada, reconstituição e integridade do banco, conforme aplicável?
6. **DDD e arquitetura hexagonal:** a solução respeita as fronteiras Domain–Application–Persistence e mantém a geração na camada correta? Identidade técnica está separada de identificação de negócio? Value Objects não recebem IDs automaticamente.

### Resultado do gate de identidade

- `PASS`: estratégia de identidade decidida, coerente com o conceito e fundamentada; pendências não bloqueantes explicitadas.
- `CORRECTION`: respostas ou decisões de modelagem ainda estão incompletas e precisam ser resolvidas antes da implementação.
- `BLOCKER`: ausência ou ambiguidade de identidade cria risco de integridade, associação incorreta ou persistência ambígua.

Não iniciar implementação se o gate resultar em `CORRECTION` ou `BLOCKER` para uma questão essencial de identidade. Um `PASS` deste gate aprova somente a decisão avaliada; não autoriza automaticamente fases, schema, código ou operações posteriores.

## Executar, não apenas reproduzir instruções

Quando o revisor emitir uma instrução explícita e autorizada dentro do escopo delegado pelo usuário, o executor deve iniciá-la imediatamente. Reconhecer, resumir, traduzir ou repetir a instrução não é conclusão de tarefa.

- Executar todos os passos autorizados: implementação, validação e coleta de evidências.
- Em `CORRECTION`, corrigir e reenviar as evidências; em `PASS` com próximo passo definido, executá-lo dentro das permissões declaradas; em `BLOCKER`, parar a operação bloqueada e relatar a causa.
- Não solicitar confirmação redundante para trabalho já autorizado, nem exceder o escopo ou operações independentemente restritas.
- Se faltar permissão, ferramenta ou canal de comunicação, relatar o impedimento preciso; não alegar execução nem interpretar ausência de resposta como aprovação.

Estados operacionais: `AUTHORIZED_TO_EXECUTE` inicia trabalho; `WAITING_FOR_GATE` só se aplica após relatório enviado sem decisão; `BLOCKED` exige impedimento real. O ciclo é: instrução do revisor → executar → validar → reportar → aguardar o próximo gate.
