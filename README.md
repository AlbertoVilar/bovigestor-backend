# BoviGestor Backend

Backend educacional independente do BoviGestor, criado para apoiar o desenvolvimento da autonomia de Alberto em Java e Spring Boot. O cadastro técnico de bovinos já percorre API, Application, Domain e persistência no ambiente local de desenvolvimento. Ele ainda não é um cadastro operacional: não cria titularidade inicial e não possui autenticação/autorização por fazenda. Consulte a [visão geral](docs/00-overview/SYSTEM_OVERVIEW.md) e o [estado atual](docs/00-overview/PROJECT_CURRENT_STATE.md).

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

Os testes de integração usam H2 em memória e PostgreSQL 17 descartável via Testcontainers. Para executar a suíte completa, mantenha um Docker Engine disponível (Rancher Desktop, por exemplo). O perfil `dev` usa o PostgreSQL local em `127.0.0.1:5433`.

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

### Cadastro técnico de bovino (somente desenvolvimento)

O `POST /api/v1/bovines` só é registrado no perfil `dev`, com `bovigestor.bovine.creation.enabled=true`. A configuração base define a flag como `false`, e o perfil de desenvolvimento habilita o smoke test e vincula o servidor a `127.0.0.1`. Não habilite essa operação em produção: o fluxo ainda não cria o primeiro período de titularidade, não identifica a fazenda responsável e não aplica autenticação/autorização.

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
  "motherRegistrationNumber": null
}
```

Uma requisição válida responde `201 Created` com `{"id": 123}` (o valor é gerado pelo banco). Campos inválidos e referências locais inexistentes respondem `400`; `internalCode` duplicado globalmente responde `409`; falhas inesperadas respondem `500` sem detalhes internos. A documentação gerada fica em `http://localhost:8080/swagger-ui/index.html` e `http://localhost:8080/v3/api-docs` quando o perfil `dev` está ativo.

## Estado atual

`POST /api/v1/farms` cadastra uma fazenda e responde HTTP 201 com o ID. `POST /api/v1/bovines` executa o cadastro técnico somente no perfil local `dev`; a titularidade e a autorização operacional permanecem em aberto. Consulte os [requisitos](docs/01-requirements/TRACEABILITY_MATRIX.md), [decisões](docs/01-architecture/ARCHITECTURE_DECISIONS.md) e [próximas waves](docs/07-planning/IMPLEMENTATION_ROADMAP.md).
