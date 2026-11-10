# Atores e catálogo de casos de uso

Referência desta atualização: código local em `feat/bovine-ownership-period-persistence`, HEAD `559c745cf8c00bde828b3f53a62575e00e5999d3`, aguardando revisão independente. `IMPLEMENTED` descreve apenas o alcance local testado; não aprova produção nem exposição segura.

## Atores

**Confirmado tecnicamente:** um cliente HTTP pode chamar `POST /api/v1/farms`; não há autenticação. `POST /api/v1/bovines` existe somente no perfil `dev`, com feature flag, exige `farmId` e grava custódia inicial na mesma transação; também não há autenticação/autorização. `GET /api/v1/farms/{id}` e `GET /api/v1/bovines/{id}` existem sem guards de profile/flag. A política de exposição e os papéis de usuário permanecem pendentes; estas rotas locais não constituem aprovação de produção.

**Propostos:** administrador, proprietário de fazenda, operador, responsável técnico/veterinário e usuário de consulta. Sistema externo de registro/identificação é ator secundário possível, sem integração atual. A permissão de cada papel precisa de decisão de produto e segurança.

## Índice dos casos de uso

| UC | Módulo e objetivo | Estado | RF |
| --- | --- | --- | --- |
| UC-001 | Fazendas: cadastrar fazenda | IMPLEMENTED | RF-001, RF-002 |
| UC-002 | Fazendas: consultar fazenda | IMPLEMENTED local; exposição/autorização pendentes | RF-003 |
| UC-003 | Fazendas: atualizar/ativar/desativar | PROPOSED; métodos de status somente no domínio | RF-004 |
| UC-004 | Bovinos: cadastrar com custódia inicial | IMPLEMENTED local; POST limitado a `dev`+flag, sem autorização | RF-005, RF-006, RF-007, RF-008 |
| UC-005 | Titularidade: registrar período inicial | IMPLEMENTED no fluxo inicial; histórico/transferência não incluídos | RF-008 |
| UC-006 | Bovinos: consultar bovino por ID | IMPLEMENTED local; política de exposição pendente | RF-009 |
| UC-007 | Bovinos: consultar genealogia e histórico | PARTIAL: resposta inclui referências de genitores; não há navegação genealógica nem histórico de custódia | RF-009, RF-011 |
| UC-008 | Bovinos: atualizar cadastro/situação | PROPOSED | RF-010 |
| UC-009 | Titularidade: consultar titularidade atual/histórico | PROPOSED; V5/V6 schema existe, sem consulta | RF-011 |
| UC-010 | Titularidade: transferir entre fazendas | PROPOSED | RF-012 |
| UC-011 | Titularidade: registrar saída ou entrada externa | PROPOSED | RF-013 |
| UC-012 | Administração: gerenciar vínculos de pessoas/fazendas | PROPOSED | RF-014 |
| UC-013 | Reprodução: registrar cobertura/inseminação/IATF | PROPOSED | RF-015 |
| UC-014 | Reprodução: diagnóstico, parto ou perda | PROPOSED | RF-016 |
| UC-015 | Lactação: iniciar/encerrar período | PROPOSED | RF-017 |
| UC-016 | Leite: registrar ordenha e consultar produção | PROPOSED | RF-018 |
| UC-017 | Sanidade: registrar evento, medicação e carência | PROPOSED | RF-019 |
| UC-018 | Qualidade: triagem, CCS/CBT e consultas | PROPOSED | RF-020 |
| UC-019 | Segurança: gerenciar usuários e permissões | PROPOSED | RF-021 |
| UC-020 | Administração: consultar auditoria | PROPOSED | RF-022 |
| UC-021 | Relatórios: consultar indicadores | PROPOSED | RF-023 |

## UC-001 — Cadastrar fazenda (implementado)

- **Objetivo:** registrar uma fazenda e devolver seu ID técnico.
- **Ator principal:** cliente HTTP; papel de usuário de negócio ainda indefinido. **Secundários:** banco configurado, via adapter JPA.
- **Pré-condições:** aplicação em execução, DataSource disponível e migration V1 aplicada.
- **Gatilho:** `POST /api/v1/farms` com JSON `name` e `codeSeries`.
- **Fluxo principal:** (1) `FarmRequest` é validado; (2) `FarmWebMapper` produz `CreateFarmCommand`; (3) `FarmConfig` abre transação em `TransactionTemplate`; (4) `CreateFarmService` obtém `FarmId`, cria `Farm` ativa com `Clock` e chama `SaveFarmPort`; (5) o adapter converte para `FarmJpaEntity` e persiste; (6) o commit conclui; (7) controller responde 201 com `{ "id": ... }`.
- **Alternativas:** `codeSeries` aceita três ou quatro letras maiúsculas; `FarmResponse` é usado pelo GET de consulta de fazenda.
- **Exceções:** entrada inválida/JSON ausente ou inválido → 400; método não permitido → 405; tipo de conteúdo não suportado → 415; SQLState `23505` → 409 genérico; falha inesperada → 500 sanitizado. Erros de persistência durante commit impedem a resposta 201.
- **Pós-condições:** uma linha `farm` com status `ACTIVE` e timestamps iniciais iguais; falha transacional deixa a gravação sem efeito.
- **Regras:** `FarmId` positivo; nome não nulo/em branco; série `[A-Z]{3,4}`; unicidade da série no banco; nome HTTP até 255 caracteres.
- **Requisitos:** RF-001, RF-002, RNF-002, RNF-003, RNF-005. **Aceitação:** testes `CreateFarmControllerHttpTest`, `CreateFarmControllerIntegrationTest`, `CreateFarmServiceIntegrationTest` e `CreateFarmTransactionRollbackIntegrationTest` comprovam os pontos respectivos.

## UC-004 — Cadastrar bovino com custódia inicial (local, limitado)

- **Objetivo:** criar e persistir um bovino e seu primeiro período de custódia, retornando `BovineId`.
- **Ator:** cliente HTTP; POST registrado somente em `dev` com feature flag. Não há identidade de usuário ou autorização por fazenda.
- **Pré-condições:** perfil `dev`, flag ativa, banco alcançável e V1–V6 aplicadas; request contém `farmId` e `origin`.
- **Gatilho:** `POST /api/v1/bovines` com o contrato `BovineRequest`.
- **Fluxo principal implementado localmente:** Bean Validation; mapper produz `CreateBovineWithInitialOwnershipCommand`; `BovineConfig` abre transação externa; serviço consulta a fazenda, rejeita inexistente/inativa antes de gerar IDs, cria bovino/período com o mesmo instante, persiste ambos e responde `201` com `{ "id": ... }`.
- **Alternativas implementadas:** categoria `UNREGISTERED` explícita sem número/associação oficial; PO/PC admitem referências de pai e mãe por IDs locais ou números externos; PA dispensa genitores.
- **Exceções HTTP:** Bean Validation/JSON inválido ou regra de cadastro inválida → 400; genitor local inexistente → 400; `internalCode` duplicado → 409; falha inesperada → 500 sanitizado.
- **Limitações explícitas:** sem autenticação/autorização ou aprovação para produção; entrada usa `Clock.instant()` e não implementa data retroativa; transferência não existe; conexão/schema persistentes dev não foram validados.
- **Requisitos:** RF-005–008 implementados localmente no alcance descrito. **Evidências:** testes Domain/Application, `BovineWebMapperTest`, `CreateBovineControllerHttpTest`, `CreateBovineProfileGuardIntegrationTest`, `CreateBovineWithInitialOwnershipPostgresIntegrationTest` e rollback.

## UC-005 — Registrar titularidade inicial (implementado no fluxo de criação local)

- **Objetivo:** associar animal a fazenda no início do histórico, sem alterar `BovineId`.
- **Ator principal proposto:** operador ou proprietário autorizado. **Secundários:** persistência de bovino/fazenda.
- **Pré-condições locais:** POST em `dev`+flag, request válido e fazenda existente/ativa. Não há autorização de usuário.
- **Fluxo implementado:** dentro da transação externa, consulta fazenda, cria bovino e primeiro período com o mesmo instante e persiste ambos.
- **Constraint:** V6 PostgreSQL limita a um período aberto por bovino; não impede sobreposições entre períodos encerrados.
- **Limites:** sem endpoint isolado do período, timeline, transferência, data retroativa ou aprovação para produção; banco persistente dev não validado.
- **Evidência:** testes de serviço, PostgreSQL Testcontainers e rollback foram reportados; revisão independente do conjunto cumulativo permanece pendente.

## UC-010 — Transferir bovino entre fazendas (proposta prioritária)

- **Objetivo:** registrar transferência sem recriar a identidade do animal.
- **Ator principal proposto:** usuário autorizado conforme política futura. **Secundários:** fazendas de origem/destino.
- **Pré-condições propostas:** titularidade atual conhecida, destino válido e autorização; convenção de tempo de vigência definida. **Gatilho:** solicitação de transferência.
- **Fluxo principal proposto:** verificar estado e titularidade na data; encerrar período anterior; iniciar período de destino; registrar evento de transferência, se o evento for aprovado; concluir em uma transação.
- **Alternativas:** transferência futura, retroativa ou saída externa dependem de decisão de negócio.
- **Exceções propostas:** sobreposição de períodos, titularidade divergente, destino inválido e conflito concorrente; não há contrato HTTP definido.
- **Pós-condições propostas:** `BovineId` inalterado, histórico preservado e no máximo uma titularidade ativa.
- **Requisitos:** RF-012, RNF-007. **Aceitação futura:** teste de rollback, concorrência e linha do tempo sem sobreposição.
- **Estado:** PROPOSED; não existe implementação.

## UC-009 — Consultar titularidade atual e histórico (proposta prioritária)

- **Objetivo:** responder qual fazenda detinha o animal em uma data e mostrar períodos anteriores.
- **Ator principal proposto:** proprietário, operador ou usuário de consulta autorizado. **Secundários:** armazenamento de períodos.
- **Pré-condições propostas:** identidade bovina válida, autorização por fazenda e convenção de vigência aprovada. **Gatilho:** consulta por `BovineId`, opcionalmente com instante.
- **Fluxo principal proposto:** validar acesso; buscar períodos; selecionar período aplicável; ordenar histórico; retornar dados permitidos.
- **Alternativas:** animal sem titular local ou com apenas histórico encerrado; formato de resposta ainda a decidir.
- **Exceções propostas:** bovino inexistente, acesso negado ou inconsistência de períodos; códigos HTTP não definidos.
- **Pós-condição:** nenhuma alteração persistida. **Regra:** `BovineId` permanece igual em todos os períodos; não presumir que o status `SOLD` identifica sozinho a fazenda atual.
- **Requisitos:** RF-011, RNF-007. **Aceitação futura:** teste temporal para data interna/limítrofe, histórico ordenado e acesso permitido/negado.
- **Estado:** PROPOSED para consulta; V5 persiste os períodos e V6 limita período aberto, mas não há porta, serviço ou endpoint de timeline.

## UC-012 — Gerenciar vínculos de pessoas e fazendas (proposta prioritária)

- **Objetivo:** registrar e revogar vínculos que fundamentem acesso e responsabilidade.
- **Ator principal proposto:** administrador autorizado. **Secundários:** proprietário, operador e responsável técnico, conforme papel aprovado.
- **Pré-condições propostas:** pessoas/fazendas identificadas e política de papéis aprovada. **Gatilho:** solicitação de vínculo ou revogação.
- **Fluxo principal proposto:** validar ator e alvos; registrar papel, escopo e vigência; persistir; devolver vínculo permitido.
- **Alternativas:** mais de um papel/fazenda por pessoa, se aprovado. **Exceções propostas:** conflito de vigência, alvo inexistente ou acesso indevido; respostas ainda não definidas.
- **Pós-condição proposta:** vínculo consultável para autorização sem apagar histórico. **Regra:** titularidade do animal não se infere automaticamente do papel de usuário.
- **Requisitos:** RF-014, RNF-006/007. **Aceitação futura:** testes de criação, revogação, isolamento entre fazendas e autorização.
- **Estado:** PROPOSED; identidade, endereço e papéis ainda não existem.

## UC-019 — Gerenciar usuários e permissões (proposta prioritária)

- **Objetivo:** controlar quem pode usar cada operação e em qual fazenda.
- **Ator principal proposto:** administrador autorizado. **Secundários:** usuários afetados e mecanismo de identidade a escolher.
- **Pré-condições propostas:** modelo de autenticação, papéis e política de acesso aprovados. **Gatilho:** criação, atualização ou revogação de permissão.
- **Fluxo principal proposto:** autenticar administrador; validar escopo; aplicar alteração; registrar auditoria; refletir permissão nas operações protegidas.
- **Alternativas:** delegação por papel ou vínculo temporal; decisão pendente. **Exceções propostas:** credencial inválida, acesso negado e conflito de permissão; contrato HTTP não definido.
- **Pós-condição proposta:** direitos efetivos coerentes com papéis e vínculos. **Regra:** sem autorização explícita, dados de outra fazenda não devem ficar acessíveis.
- **Requisitos:** RF-021, RNF-006/007. **Aceitação futura:** testes de autorização, revogação e acesso cruzado.
- **Estado:** PROPOSED; o POST atual de fazenda não tem autenticação.

## Demais casos propostos

| UC | Fluxo de alto nível e decisão em aberto |
| --- | --- |
| UC-002 | Buscar fazenda por ID; definir projeção, autorização e resposta para ausência. |
| UC-003 | Alterar dados/estado de fazenda; definir operações permitidas e exposição HTTP. |
| UC-006 | GET por ID e reconstituição existem localmente; política de exposição/autorização ainda pendente. |
| UC-007 | DTO expõe referências de genitores; navegação genealógica e consulta de custódia ainda não existem. |
| UC-008 | Atualizar bovino; definir transições e proteção contra mudanças semânticas de identidade. |
| UC-011 | Registrar saída/entrada externa; definir períodos sem fazenda local. |
| UC-013 | Registrar cobertura, inseminação ou IATF; validar campos e regras com especialista. |
| UC-014 | Registrar diagnóstico, parto, aborto ou perda; validar estados e cronologia. |
| UC-015 | Abrir/encerrar lactação; validar relação com parto e secagem. |
| UC-016 | Registrar ordenha e agregar produção; definir unidade, origem e titularidade na data. |
| UC-017 | Registrar evento sanitário/medicação; validar carência e critérios de descarte com especialista. |
| UC-018 | Registrar triagem e resultados CCS/CBT; definir amostra, laboratório, unidades e limites aplicáveis. |
| UC-020 | Consultar auditoria; definir eventos, retenção e proteção de dados. |
| UC-021 | Consultar indicadores; definir fórmulas apenas após dados e regras aprovados. |

Nenhum dos fluxos propostos implica implementação, obrigação regulatória ou permissão de acesso já existente.
