# Requisitos do sistema

Base de evidência: `develop` em `0804e609857d22983e9839f7c844b1e45b2b40a0`. Consulte o [catálogo de casos de uso](../02-use-cases/USE_CASE_CATALOG.md) e a [matriz de rastreabilidade](TRACEABILITY_MATRIX.md).

`IMPLEMENTED` significa comportamento comprovado no alcance descrito; não implica API ou integração quando o alcance é Domain/Application. `APPROVED` significa direção explícita já estabelecida, ainda não entregue por completo. `PROPOSED` é hipótese para validação. Prioridades são sugestão de sequência técnica, não prazo nem compromisso de produto. `—` indica ausência de dependência interna identificada.

## Requisitos funcionais

| ID e nome | Descrição e módulo | Prioridade | Status | Dependências | Critérios de aceitação verificáveis | UC |
| --- | --- | --- | --- | --- | --- | --- |
| RF-001 Cadastrar fazenda | Receber nome e série e persistir fazenda ativa; Fazendas. | Alta | IMPLEMENTED | — | `POST /api/v1/farms` válido devolve 201 com apenas `id`; linha persistida com status `ACTIVE` e timestamps. | UC-001 |
| RF-002 Proteger dados de fazenda | Rejeitar nome inválido e série fora de 3–4 letras maiúsculas; impedir duplicidade da série; Fazendas. | Alta | IMPLEMENTED | RF-001 | HTTP 400 para entrada inválida; 409 genérico para série duplicada; constraint de unicidade no banco. | UC-001 |
| RF-003 Consultar fazenda | Obter dados de fazenda por ID em endpoint a definir; Fazendas. | Média | PROPOSED | RF-001 | Consulta por ID retorna dados persistidos; resposta para ID inexistente definida antes de implementar. | UC-002 |
| RF-004 Alterar ou desativar fazenda | Expor manutenção controlada da fazenda; Fazendas. | Média | PROPOSED | RF-001, RF-003 | Mudanças autorizadas persistem; transições respeitam cronologia. `Farm.activate/deactivate` já existem somente no domínio. | UC-003 |
| RF-005 Cadastrar bovino manualmente | Receber dados, gerar `BovineId`, criar agregado e solicitar gravação; Bovinos. | Alta | APPROVED | RF-006; fluxo técnico HTTP limitado a `dev` | `POST /api/v1/bovines` devolve ID e grava via `SaveBovinePort`; titularidade/autorização operacional seguem pendentes. | UC-004 |
| RF-006 Validar identificação e registro bovino | Exigir identidade/código, sexo, status e categoria; aplicar regras genealógicas existentes; Bovinos. | Alta | IMPLEMENTED | — | VO rejeita código fora do formato; PO/PC exigem pai e mãe por ID local ou número externo; PO/PC/PA exigem número e associação oficiais; `UNREGISTERED` rejeita esses dois dados. Cobertura unitária. | UC-004 |
| RF-007 Persistir bovino | Mapear agregado para armazenamento sem acoplar Domain/Application a JPA; Bovinos. | Alta | IMPLEMENTED | RF-005 | V2, adapters JPA e teste HTTP/PostgreSQL confirmam gravação técnica; consulta/reconstituição continuam fora deste requisito. | UC-004 |
| RF-008 Registrar titularidade inicial | Vincular bovino a fazenda na entrada inicial; Titularidade. | Alta | PROPOSED | RF-001, RF-005, ADR pendente | Um bovino criado recebe período inicial válido, se a decisão for aprovada; criação e vínculo são atômicos. | UC-005 |
| RF-009 Consultar bovino e genealogia | Consultar cadastro, genitores e histórico sem expor entidades JPA; Bovinos. | Média | PROPOSED | RF-007 | Consulta retorna estado permitido e referências genealógicas existentes; dados externos ausentes não são inventados. | UC-006, UC-007 |
| RF-010 Atualizar cadastro/situação bovina | Alterar atributos e situações por operações com invariantes explícitas; Bovinos. | Média | PROPOSED | RF-007, regras de transição | Alteração válida persiste; mudança inválida falha sem estado parcial. | UC-008 |
| RF-011 Consultar titularidade | Consultar fazenda atual e períodos anteriores do bovino; Titularidade. | Alta | PROPOSED | RF-008 | Períodos retornam em ordem temporal; no máximo um período atual por bovino. | UC-009 |
| RF-012 Transferir bovino | Encerrar período atual e iniciar outro sem trocar `BovineId`; Titularidade. | Alta | PROPOSED | RF-008, RF-011, segurança | Transação única altera os períodos; não há simultaneidade de titulares ativos; falha reverte tudo. | UC-010 |
| RF-013 Registrar saída/entrada externa | Registrar movimentos além de transferência interna; Titularidade. | Média | PROPOSED | RF-011, política de propriedade | Movimento mantém histórico e define estado sem titular local, se admitido pelo negócio. | UC-011 |
| RF-014 Gerir vínculos de pessoas e fazendas | Associar proprietário, operador e responsável a fazendas; Administração. | Alta | PROPOSED | modelo de identidade e autorização | Vínculos com validade e permissões aprovadas podem ser criados e revogados; acesso indevido é negado. | UC-012 |
| RF-015 Registrar evento reprodutivo | Registrar cobertura/inseminação e protocolo IATF, se aprovados; Reprodução. | Média | PROPOSED | RF-007, RF-011, regras técnicas | Evento guarda animal, data, tipo, origem e responsável conforme política aprovada. | UC-013 |
| RF-016 Registrar diagnóstico e desfecho | Registrar diagnóstico gestacional, parto, aborto/perda; Reprodução. | Média | PROPOSED | RF-015 | Histórico preserva eventos e impede transições incompatíveis segundo regras aprovadas. | UC-014 |
| RF-017 Gerir lactação | Iniciar/encerrar lactação ou secagem; Lactação. | Média | PROPOSED | RF-016, regras técnicas | Períodos e transições são validados conforme critérios zootécnicos aprovados. | UC-015 |
| RF-018 Registrar ordenha e produção | Registrar ordenha e consultar produção individual/por fazenda; Leite. | Média | PROPOSED | RF-017, RF-011 | Produção é atribuída ao animal e à titularidade na data; totais reproduzem registros de origem. | UC-016 |
| RF-019 Registrar sanidade e medicação | Guardar eventos e medicamentos, incluindo carência quando aplicável; Sanidade. | Média | PROPOSED | RF-007, regras técnicas | Histórico é rastreável; impedimento de comercialização só é calculado após aprovação da regra. | UC-017 |
| RF-020 Registrar qualidade do leite | Guardar triagem de mastite e resultados CCS/CBT; Qualidade. | Média | PROPOSED | RF-018, regras laboratoriais | Resultado mantém amostra, data, origem e unidade/limite aprovados. | UC-018 |
| RF-021 Gerir usuários e permissões | Administrar identidade, papéis e acesso às fazendas; Segurança. | Alta | PROPOSED | RF-014, política de acesso | Operações autenticadas respeitam permissões e escopo de fazenda definidos. | UC-019 |
| RF-022 Auditar operações | Consultar quem alterou dados relevantes e quando; Administração. | Média | PROPOSED | RF-021, política de retenção | Evento de auditoria preserva ator, ação, alvo e instante sem expor segredos. | UC-020 |
| RF-023 Consultar indicadores | Produzir indicadores de rebanho, reprodução, leite e sanidade; Relatórios. | Baixa | PROPOSED | RF-011, RF-016, RF-018, RF-019 | Cálculo é reproduzível a partir dos eventos persistidos e critérios aprovados. | UC-021 |

## Requisitos não funcionais

| ID e nome | Descrição e módulo | Prioridade | Status | Dependências | Critérios de aceitação verificáveis | UC relacionado |
| --- | --- | --- | --- | --- | --- | --- |
| RNF-001 Isolamento arquitetural | Domain/Application livres de Spring, JPA e HTTP; Arquitetura. | Alta | IMPLEMENTED | — | Imports atuais preservam a direção; adapters dependem das portas. | UC-001, UC-004 |
| RNF-002 Integridade de dados | Validar invariantes e usar constraints nas tabelas implementadas; Dados. | Alta | IMPLEMENTED | RF-001, RF-006, RF-007 | Testes cobrem Domain e constraints de `farm`/`bovine` em H2 e PostgreSQL. | UC-001, UC-004 |
| RNF-003 Atomicidade transacional | Cadastro técnico de fazenda e bovino é atômico; Dados. | Alta | IMPLEMENTED | RF-001, RF-005, RF-007 | `TransactionTemplate` envolve os casos; testes comprovam rollback. | UC-001, UC-004 |
| RNF-004 Testabilidade e CI | Testes unitários/integração e `verify` em PRs; Qualidade. | Alta | IMPLEMENTED | — | `Backend CI` executa `verify`; esta wave adiciona testes de Web e PostgreSQL. | Todos |
| RNF-005 Contrato OpenAPI local | Documentar endpoints implementados e expor docs nos perfis locais/teste; API. | Média | IMPLEMENTED | RF-001, RF-005, RF-007 | `/v3/api-docs` documenta Farm e Bovine; Bovine declara restrição `dev` e respostas HTTP. | UC-001, UC-004 |
| RNF-006 Segurança de acesso | Autenticar/autorização conforme papel e fazenda; Segurança. | Alta | PROPOSED | RF-021 | Negação de acesso indevido comprovada por testes quando política for aprovada. | UC-019 |
| RNF-007 Isolamento entre fazendas | Evitar leitura/alteração cruzada indevida; Segurança. | Alta | PROPOSED | RF-008, RF-014, RNF-006 | Acesso a dados de outra fazenda é negado em testes multiusuário. | UC-006, UC-009 |
| RNF-008 Auditoria e retenção | Rastrear ações relevantes com política de retenção; Governança. | Média | PROPOSED | RF-022 | Trilhas consultáveis, protegidas e política de retenção aprovada. | UC-020 |
| RNF-009 Observabilidade | Correlacionar falhas e operações sem vazar dados; Operação. | Média | PROPOSED | política de logs | Logs/métricas permitem diagnóstico com dados sensíveis protegidos. | Todos |
| RNF-010 Desempenho | Definir metas após identificar consultas e volumes reais; Operação. | Média | PROPOSED | RF-009, RF-011, RF-018 | Plano de medição e índices validados; nenhum SLA numérico presumido. | Consultas |
| RNF-011 Escalabilidade | Evoluir conforme carga medida; Operação. | Baixa | PROPOSED | RNF-010 | Estratégia técnica aprovada com evidência de carga, sem capacidade inventada. | Todos |
| RNF-012 Portabilidade e PostgreSQL | Manter perfis locais/teste e validar execução no PostgreSQL; Dados. | Alta | IMPLEMENTED no escopo de criação | RF-007, migrations | Testcontainers PostgreSQL 17 e smoke no perfil `dev` validam schema, Hibernate e criação; não cobre recursos futuros. | UC-001, UC-004 |

Requisitos médicos, zootécnicos, legais e regulatórios (incluindo SISBOV, CCS/CBT, gestação e carência) permanecem `PROPOSED` até validação com especialista e fonte normativa aplicável. Não há prazos ou limites numéricos aprovados.
