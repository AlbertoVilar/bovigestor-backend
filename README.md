# BoviGestor Backend

Backend educacional do BoviGestor, criado para apoiar o desenvolvimento da autonomia em Java e Spring Boot. O checkout local implementa cadastro de bovino com período inicial de custódia operacional na mesma transação, além de consultas por ID de bovino e fazenda. A criação é limitada ao perfil `dev` e a uma feature flag; não há autenticação/autorização por fazenda, e a política de exposição das consultas ainda aguarda revisão. O banco persistente dev está configurado, mas conexão e schema não foram validados. Consulte a [visão geral](docs/00-overview/SYSTEM_OVERVIEW.md) e o [estado atual](docs/00-overview/PROJECT_CURRENT_STATE.md).

## Stack e versões

- Java 25 LTS (Eclipse Temurin)
- Spring Boot 4.1.1
- Maven Wrapper 3.9.16
- Spring Web MVC
- Spring Data JPA
- PostgreSQL JDBC Driver
- PostgreSQL para desenvolvimento e testes de integração
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
.\mvnw.cmd -B verify
```

Os testes de integração usam PostgreSQL 17 descartável via Testcontainers; os testes unitários e slices WebMvc continuam sem banco. Para executar a suíte, use `./mvnw verify` ou `mvnw.cmd verify` com Docker Engine disponível. Surefire define uma URL de teste padrão, e a configuração de teste valida as propriedades efetivas de datasource, Flyway, pool e Hibernate antes de inicializar qualquer conexão. URLs que não forem JDBC Testcontainers ou exatamente registradas por um `@Container` são rejeitadas. Execuções pela IDE devem incluir `src/test/resources` e o classpath de teste; a mesma validação é aplicada antes da inicialização. Sem Docker, integrações falham sem fallback.

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

### Cadastro de bovino com custódia inicial (somente desenvolvimento)

O `POST /api/v1/bovines` só é registrado no perfil `dev`, com `bovigestor.bovine.creation.enabled=true`. A configuração base define a flag como `false`, e o perfil de desenvolvimento vincula o servidor a `127.0.0.1`. O request exige `farmId`; o serviço rejeita fazenda inexistente ou inativa, e o bovino e seu primeiro período de custódia são persistidos atomicamente. Isso descreve implementação local, não aprovação para produção. A conexão/schema do PostgreSQL persistente dev não foram validados e o fluxo não aplica autenticação/autorização.

Exemplo para `POST http://localhost:8080/api/v1/bovines`:

```json
{
  "name": "Bovina Teste API",
  "internalCode": "BVL-26-985731",
  "sex": "FEMALE",
  "breed": "Nelore",
  "coatColor": "Branca",
  "birthDate": "2024-05-12",
  "status": "ACTIVE",
  "earTag": "BRINCO-985731",
  "registrationNumber": null,
  "registrationAssociation": null,
  "registrationCategory": "UNREGISTERED",
  "fatherId": null,
  "motherId": null,
  "fatherRegistrationNumber": null,
  "motherRegistrationNumber": null,
  "origin": {
    "creatorName": "Ana Silva",
    "originFarmName": "Fazenda de origem informada",
    "originFarmId": null
  },
  "farmId": 15
}
```

Antes do POST, crie uma fazenda ativa no ambiente local descartável e use seu ID em `farmId`. Uma requisição válida responde `201 Created` com `{"id": 123}` (IDs são alocados por sequence). Campos inválidos e referência local de genitor inexistente respondem `400`; fazenda de ingresso inexistente retorna `404`, fazenda inativa ou `internalCode` duplicado retorna `409`; falhas inesperadas respondem `500` sem detalhes internos. A documentação gerada fica em `http://localhost:8080/swagger-ui/index.html` e `http://localhost:8080/v3/api-docs` quando o perfil `dev` está ativo.

## Estado atual

`POST /api/v1/farms` cadastra uma fazenda e responde HTTP 201 com o ID. O POST de bovino cria custódia inicial em uma única transação, somente em `dev`; transferência e consulta do histórico de períodos ainda não existem. Os GETs de bovino/fazenda estão implementados, mas sua exposição e autorização permanecem pendentes. Use requests de escrita apenas em ambiente local descartável devidamente configurado. Consulte [requisitos](docs/01-requirements/TRACEABILITY_MATRIX.md), [decisões](docs/01-architecture/ARCHITECTURE_DECISIONS.md) e [roadmap](docs/07-planning/IMPLEMENTATION_ROADMAP.md).
