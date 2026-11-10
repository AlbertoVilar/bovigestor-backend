# Diagramas de sequência

Fonte desta atualização: código local na branch `feat/bovine-ownership-period-persistence`, HEAD `559c745cf8c00bde828b3f53a62575e00e5999d3`, aguardando revisão. **IMPLEMENTED** descreve apenas o comportamento local; **PROPOSED** continua sem implementação. Integrações usam PostgreSQL descartável via Testcontainers; o banco persistente dev não foi validado. Veja [arquitetura](../01-architecture/ARCHITECTURE.md) e [casos de uso](../02-use-cases/USE_CASE_CATALOG.md).

## UC-001 — criar fazenda (IMPLEMENTED)

```mermaid
sequenceDiagram
    actor Client as Cliente HTTP
    participant Web as CreateFarmController
    participant Mapper as FarmWebMapper
    participant Tx as FarmConfig / TransactionTemplate
    participant Service as CreateFarmService
    participant IdPort as FarmIdGenerationPort
    participant SavePort as SaveFarmPort
    participant Domain as Farm
    participant Jpa as Adapters JPA
    participant DB as Banco configurado
    Client->>Web: POST /api/v1/farms (FarmRequest)
    Web->>Web: Bean Validation
    Web->>Mapper: toCommand(request)
    Mapper-->>Web: CreateFarmCommand
    Web->>Tx: CreateFarmUseCase.create(command)
    Tx->>Tx: abre transação
    Tx->>Service: create(command)
    Service->>IdPort: nextId()
    IdPort->>Jpa: implementado por adapter
    Jpa->>DB: SELECT nextval('farm_id_seq')
    DB-->>Jpa: valor da sequência
    Jpa-->>IdPort: FarmId
    IdPort-->>Service: FarmId
    Service->>Domain: Farm.create(id, name, series, clock.instant())
    Domain-->>Service: Farm ACTIVE
    Service->>SavePort: save(farm)
    SavePort->>Jpa: implementado por adapter
    Jpa->>Jpa: FarmJpaMapper.toEntity(farm)
    Jpa->>DB: EntityManager.persist(entity)
    Jpa-->>SavePort: gravação solicitada
    SavePort-->>Service: retorno
    Service-->>Tx: FarmId
    Tx->>DB: commit
    Tx-->>Web: FarmId
    Web-->>Client: 201 {id}
    Note over Web,DB: Em caso de falha ocorre rollback. O handler HTTP responde conforme a causa conhecida.
```

O diagrama mostra chamadas lógicas das portas aos adapters; o código não faz a porta invocar concretamente o adapter. A injeção Spring liga implementação e interface. O SQL de inserção pode ser enviado no flush/commit, não necessariamente no instante de `persist()`.

## UC-004/005 — criar bovino com custódia inicial (IMPLEMENTED localmente; POST somente dev+flag)

```mermaid
sequenceDiagram
    actor Client as Cliente HTTP local
    participant Web as CreateBovineController
    participant Mapper as BovineWebMapper
    participant UseCase as CreateBovineWithInitialOwnershipUseCase
    participant Config as BovineConfig / TransactionTemplate
    participant Service as CreateBovineWithInitialOwnershipService
    participant FarmPort as FindFarmPort
    participant IdPort as BovineIdGenerationPort
    participant Code as BovineInternalCode
    participant Domain as Bovine
    participant SavePort as SaveBovinePort + SaveBovineOwnershipPeriodPort
    participant Jpa as Adapters JPA bovino/fazenda/período
    participant DB as PostgreSQL descartável em teste
    Client->>Web: POST /api/v1/bovines (BovineRequest)
    Web->>Web: Bean Validation
    Web->>Mapper: toCommand(request)
    Mapper-->>Web: CreateBovineWithInitialOwnershipCommand
    Web->>UseCase: create(command)
    Config->>Config: abrir transação
    Config->>Service: create(command)
    Service->>FarmPort: findById(farmId)
    FarmPort->>Jpa: consultar fazenda
    Jpa->>DB: EntityManager.find(FarmJpaEntity)
    DB-->>Jpa: fazenda ou ausência
    Jpa-->>FarmPort: resultado
    FarmPort-->>Service: fazenda ativa
    Service->>IdPort: nextId()
    IdPort->>Jpa: implementação da porta
    Jpa->>DB: SELECT nextval('bovine_id_seq')
    DB-->>Jpa: valor da sequência
    Jpa-->>IdPort: BovineId
    IdPort-->>Service: BovineId
    Service->>Code: new(command.internalCode())
    Service->>Service: toRegistrationData(command)
    Service->>Domain: createManually(id, code, data+origin, clock.instant())
    Domain-->>Service: Bovine validado
    Service->>SavePort: save(bovine)
    SavePort->>Jpa: implementação da porta
    Jpa->>DB: EntityManager.persist(entity)
    Jpa-->>SavePort: persistência solicitada
    SavePort-->>Service: retorno
    Service->>Service: abrir OwnershipPeriod com mesmo instante
    Service->>SavePort: save(period)
    SavePort->>Jpa: implementar porta do período
    Jpa->>DB: persistir período aberto
    DB-->>Jpa: persistência aceita
    Jpa-->>SavePort: retorno
    Service-->>Config: BovineId
    Config->>DB: commit da transação externa
    Config-->>Web: BovineId
    Web-->>Client: 201 {id}
    Note over Web,DB: Fazenda inexistente/inativa falha antes de gerar IDs. Erro após flush causa rollback. POST somente dev+flag; não há autenticação/autorização.
```

O teste de criação PostgreSQL usa Testcontainers descartável. Testes de rollback confirmam que falha ao salvar o período desfaz o bovino. O POST usa `Clock.instant()` como início; entrada retroativa não está implementada. O fluxo local não aprova exposição de produção. A transferência e a consulta de timeline seguem futuras.

## UC-010 — transferir entre fazendas (PROPOSED)

```mermaid
sequenceDiagram
    actor Requester as Usuário autorizado proposto
    participant Tx as Configuração transacional futura
    participant App as Application Service de transferência futuro
    participant OwnerPort as Porta de titularidade futura
    participant FarmPort as Porta de consulta Farm futura
    participant Jpa as Adapters JPA futuros
    participant DB as PostgreSQL futuro
    Requester->>Tx: transferir(bovineId, destino, instante)
    Tx->>Tx: abrir transação externa
    Tx->>App: executar transferência
    App->>OwnerPort: obter período atual na data
    OwnerPort->>Jpa: implementação da porta
    Jpa->>DB: consultar período
    DB-->>Jpa: período atual
    Jpa-->>OwnerPort: período atual
    OwnerPort-->>App: período atual
    App->>FarmPort: validar destino e acesso
    FarmPort->>Jpa: implementação da porta
    Jpa->>DB: consultar dados necessários
    DB-->>Jpa: resultado
    Jpa-->>FarmPort: resultado
    FarmPort-->>App: destino e acesso válidos
    App->>OwnerPort: encerrar período anterior
    OwnerPort->>Jpa: implementação da porta
    Jpa->>DB: atualizar período anterior
    App->>OwnerPort: iniciar período do destino
    OwnerPort->>Jpa: implementação da porta
    Jpa->>DB: persistir novo período
    alt sem conflito temporal
        App-->>Tx: transferência confirmada, mesmo BovineId
        Tx->>Tx: confirmar transação externa
        Tx-->>Requester: confirmação
    else conflito ou falha
        App-->>Tx: propagar falha
        Tx->>Tx: desfazer transação externa
        Tx-->>Requester: erro a definir
    end
```

Concorrência, vigência retroativa e saídas externas ainda precisam de regra aprovada; índice único parcial para período aberto é apenas candidato.

## UC-016 — registrar ordenha (PROPOSED)

```mermaid
sequenceDiagram
    actor Operator as Operador proposto
    participant Tx as Configuração transacional futura
    participant App as Application Service de ordenha futuro
    participant Ownership as Porta de titularidade futura
    participant Lactation as Porta de lactação futura
    participant Milking as Porta de gravação de ordenha futura
    participant Jpa as Adapters JPA futuros
    participant DB as PostgreSQL futuro
    Operator->>Tx: registrar animal, instante e produção
    Tx->>Tx: abrir transação externa
    Tx->>App: executar registro
    App->>Ownership: consultar fazenda na data do evento
    Ownership->>Jpa: implementação da porta
    Jpa->>DB: consultar titularidade temporal
    DB-->>Jpa: fazenda na data
    Jpa-->>Ownership: fazenda na data
    Ownership-->>App: fazenda na data
    App->>Lactation: consultar lactação aplicável
    Lactation->>Jpa: implementação da porta
    Jpa->>DB: consultar lactação
    DB-->>Jpa: lactação aplicável
    Jpa-->>Lactation: lactação aplicável
    Lactation-->>App: lactação aplicável
    App->>Milking: salvar ordenha e referência temporal
    Milking->>Jpa: implementação da porta
    Jpa->>DB: persistir ordenha
    App-->>Tx: registro criado
    Tx->>Tx: confirmar ou desfazer transação
    Tx-->>Operator: resultado
    Note over Operator,DB: Unidade, granularidade e regras de lactação ainda não aprovadas.
```

## UC-017 — registrar medicação e carência (PROPOSED)

```mermaid
sequenceDiagram
    actor Vet as Responsável técnico proposto
    participant Tx as Configuração transacional futura
    participant App as Application Service sanitário futuro
    participant Rule as Política técnica validada futuramente
    participant HealthPort as Porta de gravação sanitária futura
    participant Jpa as Adapter JPA futuro
    participant DB as PostgreSQL futuro
    Vet->>Tx: registrar medicamento e administração
    Tx->>Tx: abrir transação externa
    Tx->>App: executar registro
    App->>Rule: verificar aplicabilidade e carência aprovada
    App->>HealthPort: salvar evento sanitário
    HealthPort->>Jpa: implementação da porta
    Jpa->>DB: persistir evento
    App-->>Tx: registro e impedimento, se houver
    Tx->>Tx: confirmar ou desfazer transação
    Tx-->>Vet: resultado
    Note over Vet,DB: Ainda não existe cálculo de descarte. Os parâmetros dependem de validação técnica e regulatória.
```

## UC-014 — diagnóstico gestacional (PROPOSED)

```mermaid
sequenceDiagram
    actor Vet as Responsável técnico proposto
    participant Tx as Configuração transacional futura
    participant App as Application Service reprodutivo futuro
    participant History as Porta de histórico reprodutivo futura
    participant SavePort as Porta de gravação reprodutiva futura
    participant Jpa as Adapters JPA futuros
    participant DB as PostgreSQL futuro
    Vet->>Tx: registrar diagnóstico e data
    Tx->>Tx: abrir transação externa
    Tx->>App: executar registro
    App->>History: obter contexto reprodutivo do animal
    History->>Jpa: implementação da porta
    Jpa->>DB: consultar histórico
    DB-->>Jpa: contexto
    Jpa-->>History: contexto
    History-->>App: contexto
    App->>App: aplicar regras aprovadas futuramente
    App->>SavePort: salvar evento ou estado definido
    SavePort->>Jpa: implementação da porta
    Jpa->>DB: persistir evento ou estado
    App-->>Tx: resultado
    Tx->>Tx: confirmar ou desfazer transação
    Tx-->>Vet: confirmação ou erro
    Note over Vet,DB: Duração gestacional e transições clínicas ainda não definidas.
```
