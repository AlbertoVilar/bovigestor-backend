# Estado Atual do Projeto

## Implementado

- Scaffold Spring Boot 4.1.1 com Java 25 e Maven Wrapper.
- CI `Backend CI`, executada em Pull Requests para `develop` e `main`.
- Ambiente local PostgreSQL 17 via Docker Compose, exposto somente em `127.0.0.1:5433`.
- Proteções remotas em `main` e `develop`: Pull Request, check `Build and test` estrito, bloqueio de force-push e exclusão.
- BOVI-01A concluída e integrada em `develop`.
- `BovineId` implementado como Java Record imutável, com invariantes de valor obrigatório e positivo.
- Testes de domínio existentes com JUnit 6 e AssertJ para criação válida, rejeição de valores inválidos, igualdade e `hashCode`.
- Estrutura inicial de pacotes da arquitetura hexagonal pragmática: `domain.model`, `application.port.in`, `application.port.out`, `application.service`, `adapter.in.web` e `adapter.out.persistence`.

## Pendente e planejado

- Implementação da entidade de domínio `Bovine` na BOVI-01B; permanece pendente e é responsabilidade do desenvolvedor.
- Casos de uso concretos e seus ports, services e adapters somente quando existirem necessidades reais; a estrutura de pacotes não implica implementar abstrações sem uso.
- Persistência JPA/Flyway e APIs somente quando demandadas pelos exercícios.

## Ainda não implementado ou decidido

- Não existem casos de uso concretos, interfaces de ports, application services, adapters, controllers HTTP, mapeamentos de persistência ou migrations.
- Regras específicas do domínio, estados, fluxos, tabelas e contratos HTTP permanecem sujeitos a exercícios e decisões futuras.

A entidade `Bovine` ainda não existe; a única modelagem de domínio implementada é o Value Object `BovineId`.
