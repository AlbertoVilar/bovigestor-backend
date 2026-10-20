# Diagramas UML de classes

Fonte: implementação da branch `feat/bovine-web-01`, baseada em `7257c2bce524274bad106a5894bde7c8b98a4286`. **Seção A = código existente. Seção B = desenho conceitual PROPOSED para evolução**; `BovineOwnershipPeriod` e `BovineOwnershipPeriodId` já existem apenas no Domain, sem persistência JPA/tabela ou fluxo operacional. `Farm` e `Bovine` são tratadas como raízes de agregados independentes; o limite de agregado de `BovineOwnershipPeriod` permanece em discussão. O código não declara interface Java `AggregateRoot`. Ver [modelo de domínio](../03-domain/DOMAIN_MODEL.md) e [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md).

## A. Modelo atual

### A1. Raiz de agregado `Farm`

```mermaid
classDiagram
    class Farm {
        -FarmId id
        -String name
        -String codeSeries
        -FarmStatus status
        -Instant createdAt
        -Instant updatedAt
        +create(FarmId, String, String, Instant) Farm
        +activate(Instant) void
        +deactivate(Instant) void
        +id() FarmId
        +name() String
        +codeSeries() String
        +status() FarmStatus
        +createdAt() Instant
        +updatedAt() Instant
    }
    class FarmId {
        <<record>>
        +Long value
    }
    class FarmStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
    }
    Farm "1" --> "1" FarmId : id
    Farm "1" --> "1" FarmStatus : status
```

### A2. Raiz de agregado `Bovine` e Value Objects

```mermaid
classDiagram
    class Bovine {
        -BovineId id
        -BovineInternalCode internalCode
        -String name
        -BovineSex sex
        -String breed
        -String coatColor
        -LocalDate birthDate
        -BovineStatus status
        -String earTag
        -String registrationNumber
        -String registrationAssociation
        -BovineRegistrationCategory registrationCategory
        -BovineId fatherId
        -BovineId motherId
        -String fatherRegistrationNumber
        -String motherRegistrationNumber
        -Instant createdAt
        -Instant updatedAt
        +createManually(BovineId, BovineInternalCode, BovineRegistrationData, Instant) Bovine
    }
    class BovineId {
        <<record>>
        +Long value
    }
    class BovineInternalCode {
        <<record>>
        +String value
    }
    class BovineRegistrationData {
        <<record>>
        +String name
        +BovineSex sex
        +String breed
        +String coatColor
        +LocalDate birthDate
        +BovineStatus status
        +String earTag
        +String registrationNumber
        +String registrationAssociation
        +BovineRegistrationCategory registrationCategory
        +BovineId fatherId
        +BovineId motherId
        +String fatherRegistrationNumber
        +String motherRegistrationNumber
    }
    class BovineRegistrationCategory {
        <<enumeration>>
        PO
        PC
        PA
        UNREGISTERED
    }
    class BovineSex {
        <<enumeration>>
        MALE
        FEMALE
    }
    class BovineStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
        DECEASED
        SOLD
    }
    Bovine "1" --> "1" BovineId : id
    Bovine "1" --> "0..1" BovineId : fatherId
    Bovine "1" --> "0..1" BovineId : motherId
    Bovine "1" --> "1" BovineInternalCode : internalCode
    Bovine "1" --> "1" BovineRegistrationCategory : category
    Bovine "1" --> "1" BovineSex : sex
    Bovine "1" --> "1" BovineStatus : status
    Bovine ..> BovineRegistrationData : factory reads
```

`BovineRegistrationData` é parâmetro da factory, **não** campo retido nem entidade persistente. As setas para `BovineId` representam campos de valor, não relacionamentos navegáveis com objetos `Bovine`. `Bovine` oferece accessors públicos de leitura, mas não possui transições públicas de estado nem factory de reconstituição.

### A3. Caso de uso e adapters de fazenda

```mermaid
classDiagram
    class CreateFarmUseCase {
        <<interface>>
        +create(CreateFarmCommand) FarmId
    }
    class CreateFarmCommand {
        <<record>>
        +String name
        +String codeSeries
    }
    class CreateFarmService {
        -FarmIdGenerationPort farmIdGenerationPort
        -SaveFarmPort saveFarmPort
        -Clock clock
        +create(CreateFarmCommand) FarmId
    }
    class FarmIdGenerationPort {
        <<interface>>
        +nextId() FarmId
    }
    class SaveFarmPort {
        <<interface>>
        +save(Farm) void
    }
    class CreateFarmController {
        -CreateFarmUseCase useCase
        +create(FarmRequest) ResponseEntity
    }
    class FarmRequest {
        <<record>>
        +String name
        +String codeSeries
    }
    class CreateFarmResponse {
        <<record>>
        +Long id
    }
    class FarmResponse {
        <<record>>
        +Long id
        +String name
        +String codeSeries
        +String status
        +Instant createdAt
        +Instant updatedAt
    }
    class FarmWebMapper {
        +toCommand(FarmRequest) CreateFarmCommand
    }
    class FarmWebExceptionHandler
    class OpenApiConfig
    class FarmConfig
    class JpaFarmIdGenerationAdapter {
        +nextId() FarmId
    }
    class JpaSaveFarmAdapter {
        +save(Farm) void
    }
    class FarmJpaMapper {
        +toEntity(Farm) FarmJpaEntity
    }
    class FarmJpaEntity {
        -Long id
        -String name
        -String codeSeries
        -FarmStatus status
        -Instant createdAt
        -Instant updatedAt
    }
    CreateFarmService ..|> CreateFarmUseCase
    JpaFarmIdGenerationAdapter ..|> FarmIdGenerationPort
    JpaSaveFarmAdapter ..|> SaveFarmPort
    CreateFarmController --> CreateFarmUseCase : calls
    CreateFarmController ..> FarmRequest : receives
    CreateFarmController ..> CreateFarmResponse : returns
    CreateFarmController ..> FarmWebMapper : calls
    FarmWebMapper ..> CreateFarmCommand : creates
    CreateFarmService --> FarmIdGenerationPort : calls
    CreateFarmService --> SaveFarmPort : calls
    CreateFarmService ..> Farm : creates
    JpaSaveFarmAdapter ..> FarmJpaMapper : calls
    FarmJpaMapper ..> FarmJpaEntity : creates
    FarmConfig ..> CreateFarmService : constructs
    FarmConfig ..> CreateFarmUseCase : exposes transaction wrapper
```

`FarmResponse` está desenhado por existir no código, mas não está ligado ao controller: nenhuma consulta foi implementada. `FarmWebExceptionHandler` e `OpenApiConfig` são componentes Web existentes sem associação de domínio.

### A4. Caso de uso bovino — Application

```mermaid
classDiagram
    class CreateBovineUseCase {
        <<interface>>
        +create(CreateBovineCommand) BovineId
    }
    class CreateBovineCommand {
        <<record>>
        +String name
        +String internalCode
        +BovineSex sex
        +String breed
        +String coatColor
        +LocalDate birthDate
        +BovineStatus status
        +String earTag
        +String registrationNumber
        +String registrationAssociation
        +BovineRegistrationCategory registrationCategory
        +BovineId fatherId
        +BovineId motherId
        +String fatherRegistrationNumber
        +String motherRegistrationNumber
    }
    class CreateBovineService {
        -BovineIdGenerationPort bovineIdGenerationPort
        -SaveBovinePort saveBovinePort
        -Clock clock
        +create(CreateBovineCommand) BovineId
    }
    class BovineIdGenerationPort {
        <<interface>>
        +nextId() BovineId
    }
    class SaveBovinePort {
        <<interface>>
        +save(Bovine) void
    }
    CreateBovineService ..|> CreateBovineUseCase
    CreateBovineService --> BovineIdGenerationPort : calls
    CreateBovineService --> SaveBovinePort : calls
    CreateBovineService ..> BovineRegistrationData : maps command
    CreateBovineService ..> Bovine : creates
```

O diagrama mostra o limite Application. A composição transacional, os adapters JPA e o adapter Web estão no diagrama A5.

### A5. Fluxo Web e persistência técnica de bovino (perfil dev)

```mermaid
classDiagram
    class CreateBovineController {
        -CreateBovineUseCase useCase
        +create(BovineRequest) ResponseEntity
    }
    class BovineRequest {
        <<record>>
        +String name
        +String internalCode
        +BovineSex sex
        +String breed
        +String coatColor
        +LocalDate birthDate
        +BovineStatus status
        +String earTag
        +String registrationNumber
        +String registrationAssociation
        +BovineRegistrationCategory registrationCategory
        +Long fatherId
        +Long motherId
        +String fatherRegistrationNumber
        +String motherRegistrationNumber
    }
    class BovineWebMapper {
        +toCommand(BovineRequest) CreateBovineCommand
    }
    class CreateBovineResponse {
        <<record>>
        +Long id
    }
    class CreateBovineUseCase {
        <<interface>>
        +create(CreateBovineCommand) BovineId
    }
    class BovineConfig {
        +createBovineUseCase(...) CreateBovineUseCase
    }
    class CreateBovineService {
        +create(CreateBovineCommand) BovineId
    }
    class BovineIdGenerationPort {
        <<interface>>
        +nextId() BovineId
    }
    class SaveBovinePort {
        <<interface>>
        +save(Bovine) void
    }
    class JpaBovineIdGenerationAdapter {
        +nextId() BovineId
    }
    class JpaSaveBovineAdapter {
        +save(Bovine) void
    }
    class BovineJpaMapper {
        +toEntity(Bovine) BovineJpaEntity
    }
    class BovineJpaEntity {
        -Long id
        -String internalCode
        -String name
        -BovineSex sex
        -BovineStatus status
        -Instant createdAt
        -Instant updatedAt
    }
    CreateBovineController --> CreateBovineUseCase : calls
    CreateBovineController ..> BovineRequest : receives
    CreateBovineController ..> BovineWebMapper : calls
    CreateBovineController ..> CreateBovineResponse : returns
    BovineWebMapper ..> CreateBovineCommand : creates
    BovineConfig ..> CreateBovineService : constructs
    BovineConfig ..> CreateBovineUseCase : exposes TransactionTemplate wrapper
    CreateBovineService ..|> CreateBovineUseCase
    CreateBovineService --> BovineIdGenerationPort : calls
    CreateBovineService --> SaveBovinePort : calls
    JpaBovineIdGenerationAdapter ..|> BovineIdGenerationPort
    JpaSaveBovineAdapter ..|> SaveBovinePort
    JpaSaveBovineAdapter ..> BovineJpaMapper : calls
    BovineJpaMapper ..> BovineJpaEntity : creates
```

`CreateBovineController` só existe no contexto `dev` quando a feature flag está habilitada; o bind dev usa loopback. A relação com JPA termina em `BovineJpaEntity`, sem referência direta à fazenda. Não há titularidade inicial ou autorização operacional nesse fluxo.

## B. Modelo conceitual futuro — PROPOSED

Os tipos `BovineOwnershipPeriod` e `BovineOwnershipPeriodId` já existem no Domain; os demais conceitos abaixo são candidatos. Cardinalidades descrevem hipóteses para discussão; associações não definem automaticamente cascata JPA, persistência ou transação. O `BovineId` continua estável e os vínculos referenciam IDs técnicos.

### B1. Titularidade e pessoas

```mermaid
classDiagram
    class Farm {
        <<EXISTING>>
        +FarmId id
    }
    class Bovine {
        <<EXISTING>>
        +BovineId id
    }
    class BovineOwnershipPeriod {
        <<EXISTING_DOMAIN>>
        +BovineOwnershipPeriodId id
        +BovineId bovineId
        +FarmId farmId
        +Instant startedAt
        +Instant endedAt
        +open(id, bovineId, farmId, startedAt) BovineOwnershipPeriod
        +contains(Instant) boolean
        +closeAt(Instant) BovineOwnershipPeriod
    }
    class BovineOwnershipPeriodId {
        <<EXISTING_DOMAIN>>
        +Long value
    }
    class OwnershipTransfer {
        <<PROPOSED_OPTIONAL>>
        +BovineId bovineId
        +Instant occurredAt
    }
    class UserAccount {
        <<PROPOSED>>
    }
    class FarmUserLink {
        <<PROPOSED>>
    }
    class OwnerParty {
        <<PROPOSED>>
    }
    Bovine "1" <-- "0..*" BovineOwnershipPeriod : bovineId
    Farm "1" <-- "0..*" BovineOwnershipPeriod : farmId
    BovineOwnershipPeriod --> BovineOwnershipPeriodId : id
    UserAccount "1" <-- "0..*" FarmUserLink : user
    Farm "1" <-- "0..*" FarmUserLink : farm
    OwnershipTransfer ..> BovineOwnershipPeriod : may link periods
```

`BovineOwnershipPeriod` e `BovineOwnershipPeriodId` são tipos existentes somente no Domain; não há entity JPA, tabela, geração persistente do ID ou caso de uso operacional. `OwnerParty`, `UserAccount` e seus vínculos ainda não têm identidade, campos ou cardinalidade final definidos. Por isso, `OwnerParty` aparece sem associação: decidir se proprietário é usuário, pessoa distinta ou papel no vínculo. `OwnershipTransfer` pode ser desnecessário se os períodos e a auditoria forem suficientes. O período referencia `BovineId` e `FarmId`, sem composição com `Bovine` ou `Farm`; uma transferência candidata atualiza períodos em uma transação.

### B2. Reprodução

```mermaid
classDiagram
    class Bovine {
        <<EXISTING>>
        +BovineId id
    }
    class ReproductiveEvent {
        <<PROPOSED>>
        +BovineId bovineId
        +Instant occurredAt
    }
    class Pregnancy {
        <<PROPOSED>>
        +BovineId bovineId
    }
    Bovine "1" <-- "0..*" ReproductiveEvent : bovineId
    Bovine "1" <-- "0..*" Pregnancy : bovineId
    Pregnancy ..> ReproductiveEvent : may reference events
```

IATF, diagnóstico e desfechos são candidatos a eventos/tipos; campos, período gestacional e regras de estado exigem especialista. `Pregnancy` pode ser projeção de eventos ou entidade própria; decisão aberta.

### B3. Lactação, produção, sanidade e qualidade

```mermaid
classDiagram
    class Bovine {
        <<EXISTING>>
        +BovineId id
    }
    class Lactation {
        <<PROPOSED>>
        +BovineId bovineId
    }
    class MilkProduction {
        <<PROPOSED>>
        +BovineId bovineId
        +Instant occurredAt
    }
    class HealthEvent {
        <<PROPOSED>>
        +BovineId bovineId
        +Instant occurredAt
    }
    class MilkQualityTest {
        <<PROPOSED>>
        +BovineId bovineId
        +Instant sampledAt
    }
    Bovine "1" <-- "0..*" Lactation : bovineId
    Lactation "0..1" <-- "0..*" MilkProduction : possible period
    Bovine "1" <-- "0..*" MilkProduction : bovineId
    Bovine "1" <-- "0..*" HealthEvent : bovineId
    Bovine "1" <-- "0..*" MilkQualityTest : bovineId
```

`MilkProduction` pode representar ordenha individual ou outra granularidade a decidir. A referência à lactação é opcional **apenas na hipótese acima**; não é regra aprovada. Medicação, carência, mastite, CCS/CBT e descarte dependem de critérios técnicos e regulatórios. Cada conjunto de eventos pode exigir seu próprio limite de consistência, sem ampliar automaticamente o agregado `Bovine`.
