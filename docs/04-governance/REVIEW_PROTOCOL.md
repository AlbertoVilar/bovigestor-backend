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

## Executar, não apenas reproduzir instruções

Quando o revisor emitir uma instrução explícita e autorizada dentro do escopo delegado pelo usuário, o executor deve iniciá-la imediatamente. Reconhecer, resumir, traduzir ou repetir a instrução não é conclusão de tarefa.

- Executar todos os passos autorizados: implementação, validação e coleta de evidências.
- Em `CORRECTION`, corrigir e reenviar as evidências; em `PASS` com próximo passo definido, executá-lo dentro das permissões declaradas; em `BLOCKER`, parar a operação bloqueada e relatar a causa.
- Não solicitar confirmação redundante para trabalho já autorizado, nem exceder o escopo ou operações independentemente restritas.
- Se faltar permissão, ferramenta ou canal de comunicação, relatar o impedimento preciso; não alegar execução nem interpretar ausência de resposta como aprovação.

Estados operacionais: `AUTHORIZED_TO_EXECUTE` inicia trabalho; `WAITING_FOR_GATE` só se aplica após relatório enviado sem decisão; `BLOCKED` exige impedimento real. O ciclo é: instrução do revisor → executar → validar → reportar → aguardar o próximo gate.
