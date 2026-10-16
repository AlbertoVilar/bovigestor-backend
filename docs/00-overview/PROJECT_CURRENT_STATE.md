# Estado atual do projeto

Referência: `develop` em `0804e609857d22983e9839f7c844b1e45b2b40a0` (ENG-DOC-01). Este inventário descreve código existente; propostas estão no [roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md).

## Implementado

- Java 25, Spring Boot 4.1.1, Maven Wrapper e CI `Backend CI` em PRs para `develop` e `main`. O último `verify` pós-merge executou 120 testes sem falhas, erros ou ignorados.
- Ambiente PostgreSQL 17 em Docker Compose, exposto em `127.0.0.1:5433`; perfis `dev` (PostgreSQL), `h2` e `test` (H2 em memória). Flyway possui somente `V1__create_farm_schema.sql`.
- Domínio: `Farm`, `FarmId`, `FarmStatus`, `Bovine`, `BovineId`, `BovineInternalCode`, `BovineRegistrationData`, `BovineRegistrationCategory`, `BovineSex` e `BovineStatus`.
- Cadastro de fazenda: `CreateFarmUseCase`/`CreateFarmService`, portas de geração de ID e gravação, adaptadores JPA, `FarmJpaEntity`, mapper, `FarmConfig` com `TransactionTemplate` e `POST /api/v1/farms` com HTTP 201 e corpo contendo somente `id`.
- Validação HTTP da fazenda, respostas sanitizadas 400/405/409/415/500 e OpenAPI nos perfis `dev`, `h2` e `test`. `FarmResponse` existe como DTO reservado, sem endpoint de consulta.
- Cadastro manual de bovino: `CreateBovineCommand`, `CreateBovineUseCase`, `CreateBovineService`, portas de geração de ID e gravação, factory de domínio e testes unitários. O serviço ainda não está ligado a adaptadores, bean Spring, banco ou HTTP.
- Métodos de ativação/desativação de `Farm` existem no domínio, mas não há casos de uso ou endpoints para essas ações.

## Ainda não implementado

- Persistência, migration, controller e configuração de execução do cadastro de bovinos.
- Consulta, atualização ou desativação de fazenda pela API; consulta e atualização de bovinos.
- Identidade de proprietário/usuário, autorização, vínculo fazenda–bovino, histórico de titularidade e transferência.
- Reprodução, lactação, ordenhas, sanidade, qualidade do leite, auditoria de negócio e relatórios.

## Limitações verificadas

- Os testes de integração usam H2; não há teste automatizado de integração com PostgreSQL.
- `Bovine` não expõe métodos públicos de leitura nem reconstituição a partir de JPA. Suas referências aos genitores não são validadas contra um repositório.
- `CreateBovineCommand` não recebe `FarmId`; não há regra implementada de titularidade inicial.
- `BovineInternalCode` valida formato, mas o código não o relaciona à série de uma fazenda nem garante unicidade em banco.
- Há avisos não bloqueantes conhecidos do Flyway para a versão do H2 e de anexação dinâmica do Mockito/JDK.
