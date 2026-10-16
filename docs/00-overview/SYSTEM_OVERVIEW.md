# Visão geral do BoviGestor

**Referência:** código de `develop` em `0804e609857d22983e9839f7c844b1e45b2b40a0`. Consulte o [estado atual](PROJECT_CURRENT_STATE.md), o [catálogo de casos de uso](../02-use-cases/USE_CASE_CATALOG.md) e as [decisões](../01-architecture/ARCHITECTURE_DECISIONS.md).

## Propósito e problema

O BoviGestor é um backend educacional para evoluir a gestão de fazendas e bovinos de forma rastreável. Hoje, o cadastro de fazendas percorre HTTP, Application, Domain e persistência. O cadastro manual de bovinos já possui regras de domínio e um caso de uso em Java puro, mas ainda não possui execução integrada. A necessidade de controlar titularidade, reprodução, produção e sanidade é **escopo candidato**, não capacidade entregue.

O público-alvo funcional proposto inclui proprietários, operadores e responsáveis técnicos; seus papéis e permissões ainda não foram aprovados ou implementados. O público-alvo de desenvolvimento inclui quem estuda Java, testes, DDD e arquitetura hexagonal.

## Escopo por maturidade

| Área | Hoje | Próximo desenho, sujeito a aprovação |
| --- | --- | --- |
| Fazenda | Cadastro via `POST /api/v1/farms`; ID, nome, série, status e timestamps persistidos. | Consulta, atualização, inativação e vínculos de pessoas. |
| Bovino | Entidade, VOs, regras de cadastro manual, comando, serviço e portas; testes unitários. | Persistência, API, consulta e associação à fazenda. |
| Titularidade | Nenhuma entidade/tabela/fluxo. | Períodos históricos e transferências com ID animal estável. |
| Operação pecuária | Nenhum fluxo. | Reprodução, lactação, ordenha, sanidade, qualidade e indicadores. |
| Segurança e auditoria | Nenhuma identidade/autorização de usuário ou trilha de negócio. | Política de acesso por fazenda e rastreabilidade de ações. |

O escopo não funcional implementado inclui separação de camadas, validações de entrada, transação do cadastro de fazenda, testes automatizados, CI e documentação OpenAPI. Segurança de acesso, observabilidade operacional, metas de desempenho, escalabilidade e compatibilidade PostgreSQL testada ponta a ponta são requisitos propostos ou pendentes de evidência; não há SLA quantitativo aprovado.

## Arquitetura e tecnologia

Java 25, Spring Boot 4.1.1, Maven Wrapper, Spring MVC, Bean Validation, JPA/Hibernate, Flyway, PostgreSQL 17 e H2 para testes/execução local. Domain e Application não importam Spring, JPA nem HTTP. Interfaces de entrada e saída delimitam o caso de uso; Web e Persistence são adapters. O [diagrama arquitetural](../01-architecture/ARCHITECTURE.md) mostra as dependências reais.

O perfil padrão é `dev` (PostgreSQL em `localhost:5433`); `h2` e `test` usam bancos em memória. A configuração base desabilita OpenAPI, enquanto `dev`, `h2` e `test` o habilitam. Não há perfil de produção específico no repositório. A migration V1 cria somente a fazenda e sua sequência.

## Termos e integrações

`FarmId` e `BovineId` são identidades técnicas; `codeSeries` e `BovineInternalCode` são identificadores de negócio distintos. `BovineRegistrationData` agrupa parâmetros da criação, sem persistência própria. Veja o [glossário](BUSINESS_DOMAIN.md).

PostgreSQL é a infraestrutura configurada; não há integração externa operacional. Registros genealógicos, SISBOV, laboratórios e sistemas de identificação são possibilidades a validar, sem contrato aprovado. O [roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md) organiza a evolução em waves pequenas.

## Mapa de leitura

1. [Estado atual](PROJECT_CURRENT_STATE.md) e [vocabulário](BUSINESS_DOMAIN.md): o que existe e o que os termos significam.
2. [Requisitos](../01-requirements/SYSTEM_REQUIREMENTS.md), [casos de uso](../02-use-cases/USE_CASE_CATALOG.md) e [rastreabilidade](../01-requirements/TRACEABILITY_MATRIX.md): necessidade → comportamento → evidência.
3. [Domínio](../03-domain/DOMAIN_MODEL.md), [classes UML](../04-diagrams/CLASS_DIAGRAMS.md) e [sequências](../04-diagrams/SEQUENCE_DIAGRAMS.md): regras e colaboração entre componentes.
4. [Dados](../05-data/DATA_MODEL.md), [arquitetura](../01-architecture/ARCHITECTURE.md) e [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md): persistência, limites e decisões abertas.
5. [Roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md): ordem sugerida das próximas waves.
