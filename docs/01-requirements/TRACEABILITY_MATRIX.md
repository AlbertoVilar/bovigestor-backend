# Matriz de rastreabilidade

Base: `develop` em `0804e609857d22983e9839f7c844b1e45b2b40a0`. O nome de classe sem qualificador aponta para o tipo Java existente; `planejado` significa que **não** há componente ou teste correspondente. Critérios completos: [requisitos](SYSTEM_REQUIREMENTS.md) e [casos de uso](../02-use-cases/USE_CASE_CATALOG.md).

| Requisito | UC | Componente existente ou planejado | Evidência de teste existente ou futura | Estado / lacuna |
| --- | --- | --- | --- | --- |
| RF-001 | UC-001 | `CreateFarmController`, `CreateFarmUseCase`, `CreateFarmService`, `FarmConfig`, `JpaSaveFarmAdapter` | `CreateFarmControllerIntegrationTest`, `CreateFarmServiceIntegrationTest` | IMPLEMENTED, fluxo completo |
| RF-002 | UC-001 | `FarmRequest`, `Farm`, `FarmWebExceptionHandler`, V1 | `CreateFarmControllerHttpTest`, `CreateFarmControllerIntegrationTest`, `H2ProfileTests`, `FarmTest` | IMPLEMENTED; CHECK da V1 não impõe regex da série |
| RF-003 | UC-002 | `FarmResponse` existe, sem caso de uso/controller de consulta | Planejado | PROPOSED |
| RF-004 | UC-003 | `Farm.activate/deactivate` existem apenas no Domain | `FarmTest`; integração planejada | PROPOSED como fluxo completo |
| RF-005 | UC-004 | `CreateBovineCommand`, `CreateBovineUseCase`, `CreateBovineService`, `SaveBovinePort` | `CreateBovineServiceTest` comprova retorno/uso da porta; integração planejada | APPROVED; gravação real ausente |
| RF-006 | UC-004 | `Bovine`, `BovineId`, `BovineInternalCode`, `BovineRegistrationData` | `BovineTest`, `BovineIdTest`, `BovineInternalCodeTest` | IMPLEMENTED no domínio |
| RF-007 | UC-004 | `SaveBovinePort` existe; adapter/JPA/migration planejados | Teste de integração planejado | PROPOSED |
| RF-008 | UC-005 | `BovineOwnershipPeriod` e caso de uso planejados | Teste de atomicidade planejado | PROPOSED; ADR-007 aberto |
| RF-009 | UC-006/007 | Consulta/reconstituição planejadas; `Bovine` sem getters | Testes de consulta planejados | PROPOSED |
| RF-010 | UC-008 | Atualização de `Bovine` planejada | Testes de transição planejados | PROPOSED |
| RF-011 | UC-009 | Períodos/consulta planejados | Testes de linha do tempo planejados | PROPOSED |
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
| RNF-002 | UC-001/004 | VOs, factories e constraints de V1 | `FarmTest`, `BovineTest`, `H2ProfileTests` | IMPLEMENTED no alcance atual |
| RNF-003 | UC-001 | `FarmConfig`/`TransactionTemplate` | `CreateFarmTransactionRollbackIntegrationTest` | IMPLEMENTED para Farm |
| RNF-004 | Todos | `.github/workflows/backend-ci.yml`, Maven Wrapper | `verify`: 120 testes no baseline | IMPLEMENTED |
| RNF-005 | UC-001 | `OpenApiConfig`, anotações Web, perfis YAML | `CreateFarmControllerIntegrationTest` | IMPLEMENTED para POST Farm |
| RNF-006 | UC-019 | Segurança planejada | Testes de autorização planejados | PROPOSED |
| RNF-007 | UC-006/009 | Escopo por fazenda planejado | Testes multiusuário planejados | PROPOSED |
| RNF-008 | UC-020 | Auditoria/retensão planejadas | Testes de proteção planejados | PROPOSED |
| RNF-009 | Todos | Logs Web atuais; observabilidade operacional planejada | Testes/inspeção planejados | PROPOSED como requisito completo |
| RNF-010 | Consultas | Índices e medição planejados | Medição/carga planejada | PROPOSED |
| RNF-011 | Todos | Estratégia de escala planejada | Validação de carga planejada | PROPOSED |
| RNF-012 | UC-001/004 | Configuração PostgreSQL/H2 existente | H2 testado; integração PostgreSQL planejada | PROPOSED como compatibilidade comprovada |

Os testes de `CreateBovineServiceTest` usam mocks de portas e não examinam todos os campos do agregado salvo. `Bovine` ainda não tem API de leitura; a futura prova de mapeamento/persistência precisa de estratégia de reconstituição aprovada. A matriz não converte cobertura parcial em entrega HTTP.
