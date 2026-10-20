# Visão geral do BoviGestor

**Referência:** implementação da branch `feat/bovine-web-01`, baseada em `7257c2bce524274bad106a5894bde7c8b98a4286`, mais a camada Web local descrita neste documento. Consulte o [estado atual](PROJECT_CURRENT_STATE.md), o [catálogo de casos de uso](../02-use-cases/USE_CASE_CATALOG.md) e as [decisões](../01-architecture/ARCHITECTURE_DECISIONS.md).

## Propósito e problema

O BoviGestor é um backend educacional para evoluir a gestão de fazendas e bovinos de forma rastreável. O cadastro de fazendas percorre HTTP, Application, Domain e persistência. O cadastro técnico de bovinos também percorre essas camadas no ambiente local `dev`, mas não cria titularidade inicial nem aplica autorização por fazenda; portanto não é ainda um fluxo operacional completo. Reprodução, produção e sanidade são **escopo candidato**, não capacidades entregues.

O público-alvo funcional proposto inclui proprietários, operadores e responsáveis técnicos; seus papéis e permissões ainda não foram aprovados ou implementados. O público-alvo de desenvolvimento inclui quem estuda Java, testes, DDD e arquitetura hexagonal.

## Escopo por maturidade

| Área | Hoje | Próximo desenho, sujeito a aprovação |
| --- | --- | --- |
| Fazenda | Cadastro via `POST /api/v1/farms`; ID, nome, série, status e timestamps persistidos. | Consulta, atualização, inativação e vínculos de pessoas. |
| Bovino | Domain, Application, persistência V2 e `POST /api/v1/bovines` para smoke técnico somente em `dev`; resposta 201 com ID. | Titularidade inicial atômica, autorização, consulta e fluxo operacional. |
| Titularidade | `BovineOwnershipPeriod` e `BovineOwnershipPeriodId` existem no Domain; não há tabela JPA, persistência ou fluxo operacional. | Persistir períodos, criar titularidade inicial atomicamente e implementar transferências com `BovineId` estável, após as decisões abertas. |
| Operação pecuária | Nenhum fluxo. | Reprodução, lactação, ordenha, sanidade, qualidade e indicadores. |
| Segurança e auditoria | Nenhuma identidade/autorização de usuário ou trilha de negócio. | Política de acesso por fazenda e rastreabilidade de ações. |

O escopo não funcional implementado inclui separação de camadas, validações de entrada, transação do cadastro de fazenda, testes automatizados, CI e documentação OpenAPI. A compatibilidade PostgreSQL do fluxo atualmente implementado foi validada por Testcontainers e smoke local; isso não cobre recursos futuros, como persistência de titularidade. Segurança de acesso, observabilidade operacional, metas de desempenho e escalabilidade permanecem requisitos propostos ou pendentes de evidência; não há SLA quantitativo aprovado.

## Arquitetura e tecnologia

Java 25, Spring Boot 4.1.1, Maven Wrapper, Spring MVC, Bean Validation, JPA/Hibernate, Flyway, PostgreSQL 17 e H2 para testes/execução local. Domain e Application não importam Spring, JPA nem HTTP. Interfaces de entrada e saída delimitam o caso de uso; Web e Persistence são adapters. O cadastro bovino fica condicionado ao perfil `dev` e a uma feature flag; o listener dev usa loopback. O [diagrama arquitetural](../01-architecture/ARCHITECTURE.md) mostra as dependências reais.

O perfil padrão é `dev` (PostgreSQL em `localhost:5433`); `h2` e `test` usam bancos em memória. A configuração base desabilita OpenAPI, enquanto `dev`, `h2` e `test` o habilitam. Não há perfil de produção específico no repositório; um deploy de produção deve ativar um perfil distinto de `dev`. V1 cria a fazenda e V2 cria o bovino. Testcontainers valida PostgreSQL 17 descartável.

## Termos e integrações

`FarmId` e `BovineId` são identidades técnicas; `codeSeries` e `BovineInternalCode` são identificadores de negócio distintos. `BovineRegistrationData` agrupa parâmetros da criação, sem persistência própria. Veja o [glossário](BUSINESS_DOMAIN.md).

PostgreSQL é a infraestrutura configurada; não há integração externa operacional. Registros genealógicos, SISBOV, laboratórios e sistemas de identificação são possibilidades a validar, sem contrato aprovado. O [roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md) organiza a evolução em waves pequenas.

## Mapa de leitura

1. [Estado atual](PROJECT_CURRENT_STATE.md) e [vocabulário](BUSINESS_DOMAIN.md): o que existe e o que os termos significam.
2. [Requisitos](../01-requirements/SYSTEM_REQUIREMENTS.md), [casos de uso](../02-use-cases/USE_CASE_CATALOG.md) e [rastreabilidade](../01-requirements/TRACEABILITY_MATRIX.md): necessidade → comportamento → evidência.
3. [Domínio](../03-domain/DOMAIN_MODEL.md), [classes UML](../04-diagrams/CLASS_DIAGRAMS.md) e [sequências](../04-diagrams/SEQUENCE_DIAGRAMS.md): regras e colaboração entre componentes.
4. [Dados](../05-data/DATA_MODEL.md), [arquitetura](../01-architecture/ARCHITECTURE.md) e [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md): persistência, limites e decisões abertas.
5. [Roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md): ordem sugerida das próximas waves.
