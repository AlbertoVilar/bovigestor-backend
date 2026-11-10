# Visão geral do BoviGestor

**Referência:** implementação local na branch `feat/bovine-ownership-period-persistence`, HEAD `559c745cf8c00bde828b3f53a62575e00e5999d3`, ainda aguardando revisão independente. Consulte o [estado atual](PROJECT_CURRENT_STATE.md), o [catálogo de casos de uso](../02-use-cases/USE_CASE_CATALOG.md) e as [decisões](../01-architecture/ARCHITECTURE_DECISIONS.md).

## Propósito e problema

O BoviGestor é um backend educacional para evoluir a gestão de fazendas e bovinos de forma rastreável. O cadastro de fazendas percorre HTTP, Application, Domain e persistência. O cadastro bovino local recebe a fazenda de entrada e grava o bovino com o primeiro período de custódia na mesma transação, somente no perfil `dev` com flag habilitada. Isso não implementa transferência nem autenticação/autorização por fazenda. Reprodução, produção e sanidade são **escopo candidato**, não capacidades entregues.

O público-alvo funcional proposto inclui proprietários, operadores e responsáveis técnicos; seus papéis e permissões ainda não foram aprovados ou implementados. O público-alvo de desenvolvimento inclui quem estuda Java, testes, DDD e arquitetura hexagonal.

## Escopo por maturidade

| Área | Hoje | Próximo desenho, sujeito a aprovação |
| --- | --- | --- |
| Fazenda | Cadastro e consulta por ID via `POST/GET /api/v1/farms`; Domain, JPA e DTOs. GET sem política de autorização aprovada. | Atualização, inativação pela API e vínculos de pessoas. |
| Bovino | Cadastro via `POST /api/v1/bovines`, limitado a `dev`+flag, consulta por ID e reconstituição JPA → Domain. | Atualização, consulta expandida de genealogia e política de exposição/autorização dos GETs. |
| Custódia | V5 persiste períodos; POST cria primeiro período atomicamente; V6 limita a um período aberto por bovino. Não há endpoint de timeline ou transferência. | Consultas de histórico, transferência, entradas/saídas externas e correções temporais. |
| Operação pecuária | Nenhum fluxo. | Reprodução, lactação, ordenha, sanidade, qualidade e indicadores. |
| Segurança e auditoria | Nenhuma identidade/autorização de usuário ou trilha de negócio. | Política de acesso por fazenda e rastreabilidade de ações. |

O escopo não funcional implementado inclui separação de camadas, validações de entrada, transações de criação, testes automatizados, CI e documentação OpenAPI. Testes e migrations cobertos usam PostgreSQL 17 descartável via Testcontainers; conexão/schema do banco persistente dev permanecem não validados. A autenticação/autorização e a exposição dos GETs permanecem pendentes; observabilidade, metas de desempenho e escalabilidade também aguardam evidência. Não há SLA quantitativo aprovado.

## Arquitetura e tecnologia

Java 25, Spring Boot 4.1.1, Maven Wrapper, Spring MVC, Bean Validation, JPA/Hibernate, Flyway e PostgreSQL 17. Testes de integração usam PostgreSQL descartável via Testcontainers; Domain e Application não importam Spring, JPA nem HTTP. Interfaces de entrada e saída delimitam o caso de uso; Web e Persistence são adapters. O cadastro bovino fica condicionado ao perfil `dev` e a uma feature flag; o listener dev usa loopback. O [diagrama arquitetural](../01-architecture/ARCHITECTURE.md) mostra as dependências reais.

O perfil padrão é `dev` (PostgreSQL configurado em `localhost:5433`); o perfil `test` usa PostgreSQL 17 descartável via Testcontainers. A conexão e o schema do banco persistente dev não foram validados. A configuração base desabilita OpenAPI, enquanto `dev` e `test` o habilitam. Não há perfil de produção específico no repositório. O POST bovino exige perfil `dev` e flag; os controllers GET não têm atualmente o mesmo guardrail e não devem ser considerados aprovados para exposição. Testcontainers valida integrações somente no PostgreSQL descartável.

## Termos e integrações

`FarmId` e `BovineId` são identidades técnicas; `codeSeries` e `BovineInternalCode` são identificadores de negócio distintos. `BovineRegistrationData` agrupa parâmetros da criação, sem persistência própria. Veja o [glossário](BUSINESS_DOMAIN.md).

PostgreSQL é a infraestrutura configurada; não há integração externa operacional. Registros genealógicos, SISBOV, laboratórios e sistemas de identificação são possibilidades a validar, sem contrato aprovado. O [roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md) organiza a evolução em waves pequenas.

## Mapa de leitura

1. [Estado atual](PROJECT_CURRENT_STATE.md) e [vocabulário](BUSINESS_DOMAIN.md): o que existe e o que os termos significam.
2. [Requisitos](../01-requirements/SYSTEM_REQUIREMENTS.md), [casos de uso](../02-use-cases/USE_CASE_CATALOG.md) e [rastreabilidade](../01-requirements/TRACEABILITY_MATRIX.md): necessidade → comportamento → evidência.
3. [Domínio](../03-domain/DOMAIN_MODEL.md), [classes UML](../04-diagrams/CLASS_DIAGRAMS.md) e [sequências](../04-diagrams/SEQUENCE_DIAGRAMS.md): regras e colaboração entre componentes.
4. [Dados](../05-data/DATA_MODEL.md), [arquitetura](../01-architecture/ARCHITECTURE.md) e [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md): persistência, limites e decisões abertas.
5. [Roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md): ordem sugerida das próximas waves.
