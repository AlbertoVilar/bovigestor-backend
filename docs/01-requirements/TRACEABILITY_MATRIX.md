# Matriz de rastreabilidade

Base: checkout local da branch `feat/bovine-ownership-period-persistence`, HEAD `559c745cf8c00bde828b3f53a62575e00e5999d3`, ainda aguardando revisão independente. `IMPLEMENTED` descreve apenas o comportamento e a camada indicada; não aprova produção nem exposição de endpoint. Critérios completos: [requisitos](SYSTEM_REQUIREMENTS.md) e [casos de uso](../02-use-cases/USE_CASE_CATALOG.md).

| Requisito | UC | Componente existente ou planejado | Evidência de teste existente ou futura | Estado / lacuna |
| --- | --- | --- | --- | --- |
| RF-001 | UC-001 | `CreateFarmController`, `CreateFarmUseCase`, `CreateFarmService`, `FarmConfig`, `JpaSaveFarmAdapter` | `CreateFarmControllerIntegrationTest`, `CreateFarmServiceIntegrationTest` | IMPLEMENTED, fluxo completo |
| RF-002 | UC-001 | `FarmRequest`, `Farm`, `FarmWebExceptionHandler`, V1 | `CreateFarmControllerHttpTest`, `CreateFarmControllerIntegrationTest`, `PostgresProfileTests`, `FarmTest` | IMPLEMENTED; CHECK da V1 não impõe regex da série |
| RF-003 | UC-002 | `FindFarmController`, `FindFarmByIdUseCase`, `FindFarmService`, `FindFarmPort`, `JpaFindFarmAdapter`, `FarmResponse` | `FindFarmControllerHttpTest`, adapter/service/mapper tests | IMPLEMENTED localmente para GET por ID e 404; política de exposição/autorização pendente |
| RF-004 | UC-003 | `Farm.activate/deactivate` existem apenas no Domain | `FarmTest`; integração planejada | PROPOSED como fluxo completo |
| RF-005 | UC-004 | `BovineRequest`, `CreateBovineController`, `CreateBovineWithInitialOwnershipUseCase`, `CreateBovineWithInitialOwnershipService` | Testes de serviço, HTTP, rollback e PostgreSQL Testcontainers | IMPLEMENTED localmente para cadastro com `farmId` e custódia inicial, somente `dev`+flag; autenticação/autorização pendentes |
| RF-006 | UC-004 | `Bovine`, `BovineId`, `BovineInternalCode`, `BovineRegistrationData` | `BovineTest`, `BovineIdTest`, `BovineInternalCodeTest` | IMPLEMENTED no domínio |
| RF-007 | UC-004 | `SaveBovinePort`, adapters JPA, `BovineJpaEntity`, V2 e `BovineConfig` | Integração PostgreSQL/Testcontainers | IMPLEMENTED localmente; `Bovine` é persistido em PostgreSQL descartável |
| RF-008 | UC-005 | `CreateBovineWithInitialOwnershipCommand`, serviço, ports/adapters, `BovineOwnershipPeriodJpaEntity`, V5 e transação em `BovineConfig` | Testes de serviço, criação PostgreSQL, rollback e migração | IMPLEMENTED localmente para primeiro período na entrada via POST `dev`; não implementa transferência nem aprovação de produção |
| RF-009 | UC-006/007 | `FindBovineController`, use case/service/port, `JpaFindBovineAdapter`, `BovineJpaMapper.toDomain`, `BovineResponse` | HTTP, adapter, mapper e PostgreSQL tests | Consulta por ID e reconstituição implementadas localmente; consulta de genealogia/histórico e exposição segura pendentes |
| RF-010 | UC-008 | Atualização de `Bovine` planejada | Testes de transição planejados | PROPOSED |
| RF-011 | UC-009 | V5/V6 persistem períodos e limitam período aberto; não há port/endpoint para consulta do histórico | Testes de migrations/constraints | PARTIAL: schema e unicidade de período aberto implementados; consulta de timeline pendente |
| RF-012 | UC-010 | Transferência planejada | Testes de concorrência/rollback planejados | PROPOSED |
| RF-013 | UC-011 | Entrada/saída externa planejada | Testes de movimentos planejados | PROPOSED |
| RF-014 | UC-012 | Usuários/vínculos de fazenda planejados | Testes de vínculo planejados | PROPOSED |
| RF-015 | UC-013 | Eventos de reprodução planejados | Testes reprodutivos planejados | PROPOSED |
| RF-016 | UC-014 | Diagnóstico/desfecho planejados | Testes cronológicos planejados | PROPOSED |
| RF-017 | UC-015 | Lactação planejada | Testes de períodos planejados | PROPOSED |
| RF-018 | UC-016 | Ordenha/produção planejadas | Testes de agregação planejados | PROPOSED |
| RF-019 | UC-017 | Eventos sanitários planejados | Testes de carência planejados após regra aprovada | PROPOSED |
| RF-020 | UC-018 | Qualidade/CCS/CBT planejadas | Testes de amostra planejados | PROPOSED |
| RF-021 | UC-019 | Identidade/autorização planejadas | Testes de acesso planejados | PROPOSED |
| RF-022 | UC-020 | Auditoria de negócio planejada | Testes de trilha planejados | PROPOSED |
| RF-023 | UC-021 | Indicadores planejados | Testes de cálculo planejados | PROPOSED |
| RNF-001 | UC-001/004 | Pacotes `domain`, `application`, `adapter`, `config` | Inspeção de imports; testes de domínio/Application | IMPLEMENTED na estrutura atual |
| RNF-002 | UC-001/004/005 | VOs, factories e constraints de V1–V6 | Domain tests, constraints/migration PostgreSQL Testcontainers | IMPLEMENTED localmente no escopo coberto; V6 limita apenas períodos abertos |
| RNF-003 | UC-001/004/005 | `FarmConfig`, `BovineConfig`/`TransactionTemplate` | Testes de rollback de Farm e bovino após flush | IMPLEMENTED localmente para os fluxos de criação cobertos |
| RNF-004 | Todos | `.github/workflows/backend-ci.yml`, Maven Wrapper | `verify`: 120 testes no baseline | IMPLEMENTED |
| RNF-005 | UC-001/004/006 | `OpenApiConfig`, anotações Web, perfis YAML | Testes OpenAPI e HTTP | PARTIAL: docs dos contratos locais; exposição segura dos GETs ainda pendente |
| RNF-006 | UC-019 | Segurança planejada | Testes de autorização planejados | PROPOSED |
| RNF-007 | UC-006/009 | Escopo por fazenda planejado | Testes multiusuário planejados | PROPOSED |
| RNF-008 | UC-020 | Auditoria/retensão planejadas | Testes de proteção planejados | PROPOSED |
| RNF-009 | Todos | Logs Web atuais; observabilidade operacional planejada | Testes/inspeção planejados | PROPOSED como requisito completo |
| RNF-010 | Consultas | Índices e medição planejados | Medição/carga planejada | PROPOSED |
| RNF-011 | Todos | Estratégia de escala planejada | Validação de carga planejada | PROPOSED |
| RNF-012 | UC-001/004/005 | Configuração PostgreSQL, V1–V6 e Testcontainers | Testcontainers PostgreSQL 17 | PostgreSQL descartável validado para os fluxos cobertos; conexão/schema persistentes dev não validados |

O código local inclui cadastro com custódia inicial e consultas por ID, mas não transferência nem endpoint de histórico de períodos. A política de segurança/exposição dos GETs e a validação do banco persistente dev permanecem pendentes; esta matriz não promove implementação local a aprovação de produção.
