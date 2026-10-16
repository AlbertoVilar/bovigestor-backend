# Arquitetura hexagonal atual

Referência: `develop` em `0804e609857d22983e9839f7c844b1e45b2b40a0`. O BoviGestor usa arquitetura hexagonal pragmática e DDD leve. Consulte o [estado atual](../00-overview/PROJECT_CURRENT_STATE.md), o [modelo de domínio](../03-domain/DOMAIN_MODEL.md) e as [decisões](ARCHITECTURE_DECISIONS.md).

## Pacotes e responsabilidades

```text
com.bovigestor
├── domain.model                    Farm, Bovine, VOs, enums e regras
├── application.port.in             commands e interfaces de caso de uso
├── application.port.out            contratos de ID e gravação
├── application.service             coordenação pura em Java
├── adapter.in.web                  controller, DTO, mapper, erros e OpenAPI
├── adapter.out.persistence          JPA entity, mapper e adapters de Farm
└── config                          composição Spring e fronteira transacional de Farm
```

`domain.exceptions` e `application.exceptions` contêm apenas `package-info.java`; não há hierarquias de exceção personalizadas. `FarmResponse` é um DTO reservado sem endpoint de leitura. O fluxo bovino ainda não tem adapters nem bean de composição.

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

As setas de chamada no fluxo não invertem a direção das dependências de código: controller importa a porta de entrada; service importa Domain e portas de saída; os adapters implementam portas da Application. Domain e Application não importam Spring, JPA, Hibernate ou HTTP. `Bovine` segue os mesmos limites internos, mas seu fluxo não chega ao banco ou à Web.

## Percurso de criação e injeção

`FarmRequest` é validado com Bean Validation e mapeado manualmente para `CreateFarmCommand`; `CreateFarmService` obtém `FarmId`, cria `Farm` usando um `Clock` injetado e invoca `SaveFarmPort`. `JpaSaveFarmAdapter` usa `FarmJpaMapper` e `EntityManager.persist()`. `JpaFarmIdGenerationAdapter` consulta `farm_id_seq`. `FarmConfig` constrói o serviço puro e oferece a implementação de `CreateFarmUseCase` dentro de `TransactionTemplate`; o commit ocorre ao sair dessa fronteira. O adapter de geração de ID também tem anotação transacional própria; ela não substitui a transação externa do caso completo.

`CreateBovineCommand` → `CreateBovineService` → `BovineRegistrationData` → `Bovine.createManually(...)` → `SaveBovinePort` já existe em Java puro. Faltam implementação das portas, composição Spring, transação externa, JPA/migration e controller. Não apresentar a simples chamada à porta como persistência comprovada.

## Erros, dados e ambientes

Na Web de fazendas, `FarmWebExceptionHandler` devolve `ProblemDetail` sanitizado: 400 para Bean Validation/JSON inválido, 405 para método, 415 para media type, 409 para violação de unicidade SQLState `23505` e 500 para erro inesperado. Não expõe SQL ou stack trace no corpo HTTP; detalhes internos são registrados no log. Não há tratamento Web bovino.

Flyway V1 cria `farm` e `farm_id_seq`; JPA mapeia somente `FarmJpaEntity`. O perfil padrão `dev` aponta para PostgreSQL 17 em `localhost:5433`, com credencial de variável de ambiente; `h2` e `test` usam H2 em memória. `spring.jpa.hibernate.ddl-auto=validate` não cria o schema. A configuração base desabilita OpenAPI; `dev`, `h2` e `test` habilitam. Não existe perfil `prod` definido. PostgreSQL ainda não possui cobertura de integração automatizada no repositório.

## Testes e manutenção

Testes de domínio e do `CreateBovineService` exercitam invariantes e portas com JUnit/AssertJ/Mockito. Testes HTTP de fazenda usam MockMvc; testes de integração exercitam H2, Flyway, JPA, transação e rollback. O workflow `Backend CI` executa Maven `verify` em PRs para `develop` e `main`; o baseline pós-merge conhecido é de 120 testes aprovados. Ver [matriz](../01-requirements/TRACEABILITY_MATRIX.md).

Entidades concentram regras que lhes pertencem. Value Objects/DTOs imutáveis usam records quando apropriado. Não adicionar interfaces, Domain Services, Lombok ou MapStruct apenas por formalismo. Lombok, se usado, deve evitar `@Data`, setters irrestritos e igualdade automática sem semântica; MapStruct só se justifica para mapeamento estrutural repetitivo, preservando factories/invariantes. Os mapeamentos existentes são manuais.

Comparação conceitual com o estilo pragmático do CapriGestor: o BoviGestor já estabelece `domain`, `application.port`, `adapter` e `config` no próprio código; pode aproveitar lições de organização por negócio sem copiar pacotes, entidades, regras caprinas ou migrations. Esta comparação não é uma auditoria do código do CapriGestor.

## Convenções de idioma

Nomes de classes, métodos, variáveis e pacotes Java, comentários de código e mensagens de commit são escritos em inglês. Mensagens de exceções de domínio, erros de validação e erros de negócio apresentados ao usuário são escritos em português brasileiro (pt-BR). A documentação técnica e arquitetural é escrita em português brasileiro por padrão; instruções operacionais para agentes podem ser escritas em inglês quando apropriado.

Essa convenção não altera identificadores Java nem contratos técnicos de APIs. Ela também não se aplica a mensagens padrão de exceções de frameworks Java ou bibliotecas externas que o projeto não controla.
