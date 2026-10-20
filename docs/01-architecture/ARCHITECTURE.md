# Arquitetura hexagonal atual

Referência: implementação da branch `feat/bovine-web-01`, baseada em `7257c2bce524274bad106a5894bde7c8b98a4286`. O BoviGestor usa arquitetura hexagonal pragmática e DDD leve. Consulte o [estado atual](../00-overview/PROJECT_CURRENT_STATE.md), o [modelo de domínio](../03-domain/DOMAIN_MODEL.md) e as [decisões](ARCHITECTURE_DECISIONS.md).

## Pacotes e responsabilidades

```text
com.bovigestor
├── domain.model                    Farm, Bovine, VOs, enums e regras
├── application.port.in             commands e interfaces de caso de uso
├── application.port.out            contratos de ID e gravação
├── application.service             coordenação pura em Java
├── adapter.in.web                  controllers, DTOs, mappers, erros e OpenAPI
├── adapter.out.persistence          entities, mappers e adapters JPA de Farm/Bovine
└── config                          composição Spring e fronteiras transacionais
```

`domain.exceptions` e `application.exceptions` contêm apenas `package-info.java`; não há hierarquias de exceção personalizadas. `FarmResponse` é um DTO reservado sem endpoint de leitura. O fluxo bovino tem gravação e criação Web, mas ainda não tem consulta/reconstituição nem histórico de titularidade.

```mermaid
flowchart LR
    Client[Cliente HTTP] --> Controller[CreateFarmController]
    Controller --> WebMapper[FarmWebMapper]
    WebMapper --> Command[CreateFarmCommand]
    Controller --> InPort[CreateFarmUseCase]
    Config[FarmConfig / TransactionTemplate] -. compõe .-> InPort
    InPort --> Service[CreateFarmService]
    Service --> Farm[Farm / Domain]
    Service --> OutPorts[FarmIdGenerationPort / SaveFarmPort]
    JpaAdapters[Adapters JPA Farm] -. implementam .-> OutPorts
    JpaAdapters --> Mapper[FarmJpaMapper / FarmJpaEntity]
    Mapper --> DB[(farm + farm_id_seq)]
```

O fluxo bovino tem as mesmas fronteiras, habilitadas somente no perfil local `dev`:

```mermaid
flowchart LR
    Client[Cliente HTTP local] --> BovineController[CreateBovineController]
    BovineController --> BovineWebMapper[BovineWebMapper]
    BovineWebMapper --> BovineCommand[CreateBovineCommand]
    BovineController --> BovineUseCase[CreateBovineUseCase]
    BovineConfig[BovineConfig / TransactionTemplate] -. compõe .-> BovineUseCase
    BovineUseCase --> BovineService[CreateBovineService]
    BovineService --> Bovine[Bovine / Domain]
    BovineService --> BovinePorts[BovineIdGenerationPort / SaveBovinePort]
    BovineAdapters[Adapters JPA Bovine] -. implementam .-> BovinePorts
    BovineAdapters --> BovineMapper[BovineJpaMapper / BovineJpaEntity]
    BovineMapper --> BovineDB[(bovine + bovine_id_seq)]
```

`CreateBovineController` só é registrado com perfil `dev` e `bovigestor.bovine.creation.enabled=true`; a configuração base deixa a flag desligada. O perfil dev também vincula o HTTP a `127.0.0.1`. Esse guardrail restringe a rota técnica, mas não substitui autenticação/autorização quando o cadastro operacional for projetado.

As setas de chamada não invertem a direção das dependências de código: controller importa a porta de entrada; service importa Domain e portas de saída; adapters implementam portas da Application. Domain e Application não importam Spring, JPA, Hibernate ou HTTP. O Web mapper converte `Long` para `BovineId`; não acessa persistência nem implementa regras genealógicas.

## Percurso de criação e injeção

`FarmRequest` é validado com Bean Validation e mapeado manualmente para `CreateFarmCommand`; `CreateFarmService` obtém `FarmId`, cria `Farm` usando um `Clock` injetado e invoca `SaveFarmPort`. `JpaSaveFarmAdapter` usa `FarmJpaMapper` e `EntityManager.persist()`. `JpaFarmIdGenerationAdapter` consulta `farm_id_seq`. `FarmConfig` constrói o serviço puro e oferece a implementação de `CreateFarmUseCase` dentro de `TransactionTemplate`; o commit ocorre ao sair dessa fronteira. O adapter de geração de ID também tem anotação transacional própria; ela não substitui a transação externa do caso completo.

`CreateBovineCommand` → `CreateBovineService` → `BovineRegistrationData` → `Bovine.createManually(...)` → `SaveBovinePort` permanece Java puro. `BovineConfig` compõe o caso de uso em `TransactionTemplate`; adapters JPA implementam geração e gravação; Flyway V2 cria a tabela e a sequência. A prova de HTTP/PostgreSQL confirma o caminho técnico, não a titularidade operacional.

## Erros, dados e ambientes

O `FarmWebExceptionHandler` global devolve `ProblemDetail` sanitizado: 400 para Bean Validation/JSON inválido, `IllegalArgumentException` de regras de cadastro e referências locais inexistentes (SQLState `23503`); 404 para rota/recurso ausente; 405 para método; 415 para media type; 409 para unicidade (SQLState `23505`); e 500 para erro inesperado. Não expõe SQL ou stack trace no corpo HTTP; falhas inesperadas são registradas no log.

Flyway V1 cria `farm` e `farm_id_seq`; V2 cria `bovine` e `bovine_id_seq`; JPA mapeia ambas as entities. O perfil `dev` aponta para PostgreSQL 17 em `localhost:5433`, com credencial de variável de ambiente; `h2` e `test` usam H2 em memória. `spring.jpa.hibernate.ddl-auto=validate` não cria o schema. A configuração base desabilita OpenAPI e a feature de criação bovina; `dev`, `h2` e `test` habilitam OpenAPI. Não existe perfil `prod` definido. Integração PostgreSQL é coberta por Testcontainers e o smoke test dev foi executado contra o banco local.

## Testes e manutenção

Testes de domínio e Application exercitam invariantes e portas com JUnit/AssertJ/Mockito. Testes HTTP usam MockMvc para Farm e Bovine; integrações exercitam H2, Flyway, JPA, transação/rollback e PostgreSQL via Testcontainers. O workflow `Backend CI` executa Maven `verify` em PRs para `develop` e `main`. Ver [matriz](../01-requirements/TRACEABILITY_MATRIX.md).

Entidades concentram regras que lhes pertencem. Value Objects/DTOs imutáveis usam records quando apropriado. Não adicionar interfaces, Domain Services, Lombok ou MapStruct apenas por formalismo. Lombok, se usado, deve evitar `@Data`, setters irrestritos e igualdade automática sem semântica; MapStruct só se justifica para mapeamento estrutural repetitivo, preservando factories/invariantes. Os mapeamentos existentes são manuais.

Comparação conceitual com o estilo pragmático do CapriGestor: o BoviGestor já estabelece `domain`, `application.port`, `adapter` e `config` no próprio código; pode aproveitar lições de organização por negócio sem copiar pacotes, entidades, regras caprinas ou migrations. Esta comparação não é uma auditoria do código do CapriGestor.

## Convenções de idioma

Nomes de classes, métodos, variáveis e pacotes Java, comentários de código e mensagens de commit são escritos em inglês. Mensagens de exceções de domínio, erros de validação e erros de negócio apresentados ao usuário são escritos em português brasileiro (pt-BR). A documentação técnica e arquitetural é escrita em português brasileiro por padrão; instruções operacionais para agentes podem ser escritas em inglês quando apropriado.

Essa convenção não altera identificadores Java nem contratos técnicos de APIs. Ela também não se aplica a mensagens padrão de exceções de frameworks Java ou bibliotecas externas que o projeto não controla.
