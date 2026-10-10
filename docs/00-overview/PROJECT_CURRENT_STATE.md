# Estado Atual do Projeto

## Implementado

- Scaffold Spring Boot 4.1.1 com Java 25 e Maven Wrapper.
- CI `Backend CI`, executada em Pull Requests para `develop` e `main`.
- Ambiente local PostgreSQL 17 via Docker Compose, exposto somente em `127.0.0.1:5433`.
- Proteções remotas em `main` e `develop`: Pull Request, check `Build and test` estrito, bloqueio de force-push e exclusão.

## Planejado

- Modelagem incremental do domínio bovino, começando pelo exercício BOVI-01.
- Arquitetura hexagonal pragmática quando houver casos de uso e fronteiras reais.
- Persistência JPA/Flyway e APIs somente quando demandadas pelos exercícios.

## Ainda não decidido

- Regras específicas do domínio, estados, fluxos, tabelas e contratos HTTP.
- Estrutura concreta de pacotes de domínio, aplicação e adapters.

Não há entidades, casos de uso, repositórios, migrations ou APIs implementados neste momento.
