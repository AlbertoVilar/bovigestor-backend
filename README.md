# BoviGestor Backend

Backend educacional independente do BoviGestor, criado para apoiar o desenvolvimento da autonomia de Alberto em Java e Spring Boot. O cadastro de fazendas já percorre API, Application, Domain e persistência; o cadastro de bovinos possui Domain e Application, mas ainda não tem adapter de persistência ou API. Consulte a [visão geral](docs/00-overview/SYSTEM_OVERVIEW.md) e o [estado atual](docs/00-overview/PROJECT_CURRENT_STATE.md).

## Stack e versões

- Java 25 LTS (Eclipse Temurin)
- Spring Boot 4.1.1
- Maven Wrapper 3.9.16
- Spring Web MVC
- Spring Data JPA
- PostgreSQL JDBC Driver
- H2 para perfis locais e testes
- Flyway (com suporte PostgreSQL)
- Bean Validation e Springdoc OpenAPI
- Spring Boot Test, JUnit, AssertJ e Mockito

## Pré-requisitos

- JDK 25 instalado e selecionado para a sessão de terminal.
- PostgreSQL para executar a aplicação pelo perfil `dev`.

## Compilar

No diretório `backend`:

```powershell
.\mvnw.cmd compile
```

## Executar testes

```powershell
.\mvnw.cmd test
```

Os testes de integração usam o perfil `test` com H2 em memória, JPA e a migration Flyway V1. Há também testes para o perfil `h2`. Não é necessário iniciar PostgreSQL para executar `verify`; a compatibilidade com PostgreSQL ainda precisa de validação de integração específica.

## Iniciar com PostgreSQL no perfil `dev`

Após criar e configurar um banco PostgreSQL, defina as variáveis de ambiente da sessão:

```powershell
$env:BOVIGESTOR_DB_URL = "jdbc:postgresql://localhost:5433/bovigestor"
$env:BOVIGESTOR_DB_USERNAME = "bovigestor"
$securePassword = Read-Host "Senha do banco de dados" -AsSecureString
$env:BOVIGESTOR_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $securePassword).Password
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

No IntelliJ, abra a configuração de execução da aplicação e adicione `dev` em **Active profiles** (ou `-Dspring.profiles.active=dev` em **VM options**). O perfil `dev` usa exclusivamente PostgreSQL e mantém Flyway ativo.

O perfil `dev` registra instruções SQL e valores de parâmetros no console. Use dados locais apropriados para desenvolvimento.

As credenciais não devem ser adicionadas ao repositório. O arquivo `application-dev.yml` possui somente referências a variáveis de ambiente.

## Estado atual

`POST /api/v1/farms` cadastra uma fazenda e responde HTTP 201 com o ID. O caso de uso de criação de bovinos existe em Java puro, mas não está exposto por HTTP nem persistido. Requisitos, casos de uso, diagramas e próximas waves estão ligados na [visão geral](docs/00-overview/SYSTEM_OVERVIEW.md).
