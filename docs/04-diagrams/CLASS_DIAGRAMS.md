# Diagramas UML de classes

Fonte desta atualização: código local na branch `feat/bovine-ownership-period-persistence`, HEAD `559c745cf8c00bde828b3f53a62575e00e5999d3`, aguardando revisão independente. **Seção A = componentes existentes localmente. Seção B = desenho conceitual PROPOSED**. `BovineOwnershipPeriod` e seu ID possuem Domain, entity/mapper JPA, V5/V6 e primeiro período no cadastro; consulta de timeline e transferência não existem. O limite de agregado do período segue em discussão. O código não declara interface Java `AggregateRoot`. Ver [modelo de domínio](../03-domain/DOMAIN_MODEL.md) e [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md).

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

`BovineRegistrationData` é parâmetro da factory, **não** entidade persistente. `Bovine` retém `BovineOrigin`, tem accessors e factory `reconstitute` que preserva os dados reconstruídos do JPA; não possui transições públicas de estado. As setas para `BovineId` representam valores, não relacionamentos navegáveis com objetos `Bovine`.

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

`FindFarmController` usa `FarmResponse` e o fluxo de consulta está implementado localmente. A política de exposição/autorização do GET ainda aguarda revisão.

### A4. Application: cadastro operacional e consultas

```mermaid
classDiagram
    class CreateBovineWithInitialOwnershipUseCase {
        <<interface>>
        +create(CreateBovineWithInitialOwnershipCommand) BovineId
    }
    class CreateBovineWithInitialOwnershipService {
        +create(CreateBovineWithInitialOwnershipCommand) BovineId
    }
    class CreateBovineWithInitialOwnershipCommand {
        <<record>>
        +CreateBovineCommand bovineData
        +FarmId farmId
    }
    class FindBovineByIdUseCase {
        <<interface>>
        +findById(BovineId) Bovine
    }
    class FindFarmByIdUseCase {
        <<interface>>
        +findById(FarmId) Farm
    }
    class BovineConfig {
        +createBovineWithInitialOwnershipUseCase(...) UseCase
        +findBovineByIdUseCase(...) FindBovineByIdUseCase
    }
    class FarmConfig {
        +findFarmByIdUseCase(...) FindFarmByIdUseCase
        +transactionTemplate(...) TransactionTemplate
    }
    CreateBovineWithInitialOwnershipService ..|> CreateBovineWithInitialOwnershipUseCase
    BovineConfig ..> CreateBovineWithInitialOwnershipService : composes in TransactionTemplate
    BovineConfig ..> FindBovineByIdUseCase : composes
    FarmConfig ..> FindFarmByIdUseCase : composes
    CreateBovineWithInitialOwnershipService --> FindFarmPort : validates farm
    CreateBovineWithInitialOwnershipService --> BovineIdGenerationPort
    CreateBovineWithInitialOwnershipService --> BovineOwnershipPeriodIdGenerationPort
    CreateBovineWithInitialOwnershipService --> SaveBovinePort
    CreateBovineWithInitialOwnershipService --> SaveBovineOwnershipPeriodPort
    FindBovineByIdUseCase --> FindBovinePort
    FindFarmByIdUseCase --> FindFarmPort
```

O serviço rejeita fazenda ausente/inativa antes de gerar IDs e cria `Bovine` e o primeiro período com o mesmo instante do `Clock`. A composição Spring envolve as duas gravações em uma transação externa.

### A5. Web, queries and persistence (local implementation)

```mermaid
classDiagram
    class CreateBovineController {
        +create(BovineRequest) ResponseEntity
    }
    class FindBovineController {
        +findById(Long) ResponseEntity
    }
    class FindFarmController {
        +findById(Long) ResponseEntity
    }
    class BovineWebMapper {
        +toOperationalCommand(BovineRequest) CreateBovineWithInitialOwnershipCommand
        +toResponse(Bovine) BovineResponse
    }
    class FindBovineControllerHttpTest
    class FindFarmControllerHttpTest
    class BovineJpaMapper {
        +toEntity(Bovine) BovineJpaEntity
        +toDomain(BovineJpaEntity) Bovine
    }
    class FarmJpaMapper {
        +toEntity(Farm) FarmJpaEntity
        +toDomain(FarmJpaEntity) Farm
    }
    class BovineOwnershipPeriodJpaMapper {
        +toEntity(BovineOwnershipPeriod) Entity
        +toDomain(Entity) BovineOwnershipPeriod
    }
    class BovineOwnershipPeriodJpaEntity
    class V5
    class V6
    CreateBovineController --> CreateBovineWithInitialOwnershipUseCase
    CreateBovineController ..> BovineWebMapper
    FindBovineController --> FindBovineByIdUseCase
    FindBovineController ..> BovineWebMapper
    FindFarmController --> FindFarmByIdUseCase
    BovineJpaMapper ..> BovineJpaEntity
    FarmJpaMapper ..> FarmJpaEntity
    BovineOwnershipPeriodJpaMapper ..> BovineOwnershipPeriodJpaEntity
    V5 ..> BovineOwnershipPeriodJpaEntity : schema
    V6 ..> BovineOwnershipPeriodJpaEntity : one open period index
```

O POST `/api/v1/bovines` é registrado somente em `dev` com sua feature flag. Os controllers GET não têm guard de profile/flag/autenticação; exposição e autorização seguem pendentes de revisão separada. Domain e Application permanecem independentes de Spring/JPA/HTTP.

## B. Modelo conceitual futuro — PROPOSED

`BovineOwnershipPeriod` e `BovineOwnershipPeriodId` são implementados localmente no Domain e na persistência V5/V6. Os demais conceitos abaixo são candidatos. Cardinalidades descrevem hipóteses; associações não definem automaticamente cascata JPA ou transação. `BovineId` permanece estável.

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
        <<EXISTING_DOMAIN_PERSISTED>>
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

`BovineOwnershipPeriod` e `BovineOwnershipPeriodId` existem no Domain e no mapeamento/persistência local: V5 cria a tabela e sequence, e V6 limita períodos abertos. O fluxo de cadastro abre o período inicial; leitura da timeline e transferência não existem. `OwnerParty`, `UserAccount` e seus vínculos continuam sem identidade, campos ou cardinalidade final definidos. `OwnershipTransfer` pode ser desnecessário se períodos e auditoria forem suficientes. O período referencia `BovineId` e `FarmId`, sem navegação direta entre agregados.

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
