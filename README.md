# BoviGestor Backend

Backend educacional independente do BoviGestor, criado para apoiar o desenvolvimento da autonomia de Alberto em Java e Spring Boot. Este repositório começa deliberadamente como um scaffold: ainda não há entidades, regras de negócio, APIs, repositórios ou casos de uso.

## Stack e versões

- Java 25 LTS (Eclipse Temurin)
- Spring Boot 4.1.1
- Maven Wrapper 3.9.16
- Spring Web MVC
- Spring Data JPA
- PostgreSQL JDBC Driver
- Flyway (com suporte PostgreSQL)
- Spring Boot Test, JUnit e Mockito

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

Os testes usam o perfil `test`, que desativa a autoconfiguração de DataSource, JPA e Flyway. Isso evita a necessidade de PostgreSQL durante esta etapa e não o substitui por H2.

## Iniciar com PostgreSQL no perfil `dev`

Após criar e configurar um banco PostgreSQL, defina as variáveis de ambiente da sessão:

```powershell
$env:BOVIGESTOR_DB_URL = "jdbc:postgresql://localhost:5432/bovigestor"
$env:BOVIGESTOR_DB_USERNAME = "seu_usuario"
$env:BOVIGESTOR_DB_PASSWORD = "sua_senha"
$env:SPRING_PROFILES_ACTIVE = "dev"
.\mvnw.cmd spring-boot:run
```

No IntelliJ, abra a configuração de execução da aplicação e adicione `dev` em **Active profiles** (ou `-Dspring.profiles.active=dev` em **VM options**). O perfil `dev` usa exclusivamente PostgreSQL e mantém Flyway ativo.

O perfil `dev` também registra as instruções SQL e seus valores de parâmetros no console. No scaffold atual não há entidades nem consultas; portanto, haverá SQL visível quando essas operações forem implementadas.

As credenciais não devem ser adicionadas ao repositório. O arquivo `application-dev.yml` possui somente referências a variáveis de ambiente.

## Estado atual

Scaffold inicial do backend, sem funcionalidades de negócio.
