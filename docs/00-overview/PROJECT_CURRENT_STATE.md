# Estado atual do projeto

Este documento descreve o estado local observado na branch `feat/bovine-ownership-period-persistence`, HEAD `559c745cf8c00bde828b3f53a62575e00e5999d3`, durante a preparação BOVI-LOCAL-CONSOLIDATION-FINALIZE-01. As alterações continuam locais e aguardam revisão independente; descrição de código existente não significa aprovação para produção. Consulte [requisitos e rastreabilidade](../01-requirements/TRACEABILITY_MATRIX.md), [decisões](../01-architecture/ARCHITECTURE_DECISIONS.md) e [roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md).

## Implementado no código local

- Java 25, Spring Boot 4.1.1, Maven Wrapper e CI `Backend CI` em pull requests para `develop` e `main`.
- PostgreSQL 17 é usado pelos testes de integração via Testcontainers descartáveis. A política de isolamento rejeita destinos externos e configurações concorrentes antes de criar DataSource/conexão. V1–V6 estão no classpath compartilhado; não há diretórios separados de migrations para H2 e PostgreSQL.
- Cadastro de fazenda: `CreateFarmUseCase`/`CreateFarmService`, adapters JPA e `POST /api/v1/farms`, com `TransactionTemplate`, validação e resposta `201` com ID.
- Cadastro bovino com custódia inicial: `CreateBovineWithInitialOwnershipUseCase` e `CreateBovineWithInitialOwnershipService` consultam a fazenda, rejeitam inexistente/inativa antes de gerar IDs, criam bovino e período com o mesmo instante e persistem ambos na transação externa de `BovineConfig`. O `POST /api/v1/bovines` exige `farmId` e é registrado somente com perfil `dev` e `bovigestor.bovine.creation.enabled=true`.
- Persistência de bovinos e períodos, geração de IDs por sequences, mapeamento JPA nos dois sentidos para Bovine/Farm, e reconstituição de `Farm` preservando status/time zone/timestamps.
- V5 cria `bovine_ownership_period`, FK para bovino/fazenda e CHECK `ended_at IS NULL OR ended_at > started_at`. V6 cria índice único parcial PostgreSQL `uq_bovine_one_open_ownership` para permitir no máximo um período aberto por bovino. A V6 não impede sobreposição entre períodos encerrados e não implementa transferência.
- Consultas locais `GET /api/v1/bovines/{id}` e `GET /api/v1/farms/{id}` retornam os DTOs correspondentes e 404 para recursos ausentes. A política de exposição e autorização destas consultas ainda está pendente; os controllers de consulta não têm guard de profile/flag/autenticação.
- A criação de bovino e fazenda tem validações Web, respostas `ProblemDetail` sanitizadas e testes de rollback. Testes de integração usam PostgreSQL 17 descartável; testes unitários e WebMvc sem persistência continuam sem banco.

## Ainda não implementado ou não aprovado

- Transferência de bovinos entre fazendas, saída/entrada externa, consulta de períodos de custódia, alteração cadastral de bovino e operações de atualização/inativação de fazenda pela API.
- Autenticação, autorização, identidade de usuários, vínculo pessoa–fazenda e isolamento de leitura por fazenda. A existência de rotas GET não constitui aprovação de exposição.
- Reprodução, lactação, ordenhas, sanidade, qualidade do leite, auditoria de negócio e relatórios.
- Ativação de criação operacional em produção. O POST atual continua limitado ao perfil de desenvolvimento e não implementa segurança por usuário/fazenda.

## Evidência e limites

- Os relatórios Surefire já existentes em `target/surefire-reports`, datados de 2026-11-09, contêm 44 suítes e 331 testes, sem falhas, erros ou skips. Isso é uma execução anterior, não o resultado de uma execução da tarefa de documentação; a correspondência byte a byte entre fontes testadas e working tree não está registrada nesses XMLs. A execução de validação desta tarefa deve ser registrada no relatório de revisão.
- A integração é verificada contra PostgreSQL descartável via Testcontainers. A configuração de `application-dev.yml` está preparada, mas conexão/schema do banco persistente dev não foram validados; não se afirma que a aplicação foi iniciada contra ele.
- A V5 atual tem SHA-256 `82C37AF8DFFE1B4F2FCC2400E3A5D4EF1FD8F0C580BA885471042DD1DA3ABCEA` (866 bytes). Um relatório anterior registrou 870 bytes e outro SHA (`9292E15B125D5B17B27C6916B70910ABE5EFD4B5B9C33E950E0C369A28022172`). O SQL publicado é equivalente após normalizar whitespace, mas não há evidência para provar preservação histórica byte a byte ou explicar a diferença. A migration não foi restaurada nem normalizada nesta preparação.
- A atualização documental e o staging local não aprovam o sistema para produção nem autorizam publicação Git.
