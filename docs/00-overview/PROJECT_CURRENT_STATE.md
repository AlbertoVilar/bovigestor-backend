# Estado atual do projeto

Referência: estado implementado na branch `feat/bovine-web-01`, baseada no merge BOVI-03A `7257c2bce524274bad106a5894bde7c8b98a4286`. Este inventário descreve código existente; propostas estão no [roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md).

## Implementado

- Java 25, Spring Boot 4.1.1, Maven Wrapper e CI `Backend CI` em PRs para `develop` e `main`.
- Ambiente PostgreSQL 17 em Docker Compose, exposto em `127.0.0.1:5433`; perfis `dev` (PostgreSQL), `h2` e `test` (H2 em memória). Flyway possui V1 (fazenda) e V2 (bovino).
- Domínio: `Farm`, `FarmId`, `FarmStatus`, `Bovine`, `BovineId`, `BovineInternalCode`, `BovineRegistrationData`, `BovineRegistrationCategory`, `BovineSex` e `BovineStatus`.
- Cadastro de fazenda: `CreateFarmUseCase`/`CreateFarmService`, portas de geração de ID e gravação, adaptadores JPA, `FarmJpaEntity`, mapper, `FarmConfig` com `TransactionTemplate` e `POST /api/v1/farms` com HTTP 201 e corpo contendo somente `id`.
- Validação HTTP da fazenda, respostas sanitizadas 400/405/409/415/500 e OpenAPI nos perfis `dev`, `h2` e `test`. `FarmResponse` existe como DTO reservado, sem endpoint de consulta.
- Cadastro técnico de bovino: `CreateBovineCommand`, `CreateBovineUseCase`, `CreateBovineService`, portas, adapters JPA, `BovineJpaEntity`, `BovineConfig` transacional, V2 e `POST /api/v1/bovines`. O controller só é registrado com o perfil `dev` e a feature flag habilitada; o servidor dev escuta em `127.0.0.1`.
- DTO Web de bovino com Bean Validation, enums do Domain, limites alinhados às colunas, mapper manual `Long` → `BovineId`, resposta HTTP contendo somente o ID, documentação OpenAPI e erros HTTP sanitizados.
- Testes Web com MockMvc e teste de integração HTTP + PostgreSQL 17 via Testcontainers; a operação de criação dev também foi validada contra o PostgreSQL local.
- Métodos de ativação/desativação de `Farm` existem no domínio, mas não há casos de uso ou endpoints para essas ações.

## Ainda não implementado

- Consulta e reconstituição JPA → Domain de bovinos e atualização cadastral de bovinos.
- Consulta, atualização ou desativação de fazenda pela API; consulta e atualização de bovinos.
- Identidade de proprietário/usuário, autenticação/autorização, vínculo fazenda–bovino, histórico de titularidade e transferência.
- Reprodução, lactação, ordenhas, sanidade, qualidade do leite, auditoria de negócio e relatórios.

## Limitações verificadas

- A suíte de integração combina H2 e PostgreSQL 17 via Testcontainers. A BOVI-03A foi validada com 206 testes, incluindo 30 PostgreSQL; a BOVI-WEB-01 passou posteriormente com 244 testes. O smoke HTTP local contra o PostgreSQL dev foi uma validação manual distinta.
- `CreateBovineCommand` não recebe `FarmId`; o endpoint técnico não cria titularidade inicial nem autoriza uma fazenda.
- Não existe perfil de produção configurado; o controller é restringido explicitamente ao perfil `dev`, a flag é falsa na configuração base e a aplicação dev vincula HTTP a loopback. Não habilitar a flag em produção.
- `BovineInternalCode` é validado no Domain e tem unicidade global por `uq_bovine_internal_code`; o prefixo não determina fazenda atual.
- `Bovine` expõe leitura dos campos, mas ainda não possui adapter/port de consulta nem reconstituição JPA.
- Há avisos não bloqueantes conhecidos do Flyway para a versão do H2 e de anexação dinâmica do Mockito/JDK.
