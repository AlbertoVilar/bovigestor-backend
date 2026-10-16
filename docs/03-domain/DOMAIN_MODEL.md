# Modelo de domínio e limites DDD

Base: código em `develop` (`0804e609857d22983e9839f7c844b1e45b2b40a0`). [Diagramas de classes](../04-diagrams/CLASS_DIAGRAMS.md), [dados](../05-data/DATA_MODEL.md) e [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md) complementam este texto.

## Modelo implementado

`Farm` e `Bovine` são entidades identificadas por `FarmId` e `BovineId`, respectivamente, e tratadas aqui como raízes de agregados independentes: suas factories são as entradas públicas para criá-las. O código não declara uma interface `AggregateRoot`. Nenhum relacionamento direto `Bovine` → `Farm` existe hoje. `Farm` possui métodos públicos de leitura e de mudança de status; `Bovine` expõe apenas `createManually(...)`, sem getters, alteração de estado ou reconstituição pública.

| Elemento | Tipo e responsabilidade | Invariantes ou limites existentes |
| --- | --- | --- |
| `FarmId` | Value Object `record(Long value)` | Não nulo e positivo; igualdade por valor. |
| `Farm` | Entidade/raiz: ID, nome, série, status, criação e atualização. | `create` inicia `ACTIVE`; nome não nulo/em branco; série `[A-Z]{3,4}`; timestamps de transições não retrocedem. `activate/deactivate` são idempotentes quando já no estado desejado. |
| `FarmStatus` | Enum `ACTIVE`, `INACTIVE`. | Não há fluxo Application para mudança de status. |
| `BovineId` | Value Object `record(Long value)` | Não nulo e positivo; igualdade por valor. Identidade técnica a preservar em futuras transferências. |
| `BovineInternalCode` | Value Object `record(String value)` | Série de 3–4 letras maiúsculas, hífen, dois dígitos, hífen e sequência positiva com ao menos quatro dígitos. Não verifica ano corrente, série de fazenda ou unicidade. |
| `Bovine` | Entidade/raiz criada por `createManually`. | Exige ID, código, dados, data de criação, nome, sexo, status e categoria. Regras de nascimento, genitores e registro descritas abaixo. |
| `BovineRegistrationData` | `record` de 14 parâmetros usado para criar `Bovine`. | É objeto de parâmetros; **não** entidade persistente independente. Sua construção isolada não valida todos os campos; a factory do bovino valida as invariantes. |
| `BovineRegistrationCategory` | Enum `PO`, `PC`, `PA`, `UNREGISTERED`. | Categoria é obrigatória e não recebe default. |
| `BovineSex` | Enum `MALE`, `FEMALE`. | Obrigatório na criação; nenhuma regra reprodutiva derivada foi implementada. |
| `BovineStatus` | Enum `ACTIVE`, `INACTIVE`, `DECEASED`, `SOLD`. | Obrigatório na criação; não existem operações públicas de transição. `SOLD` não executa transferência. |

### Invariantes de `Bovine.createManually`

1. `id`, `internalCode`, `data` e `createdAt` não podem ser nulos; `name` não pode ser nulo/em branco; `sex`, `status` e `registrationCategory` são obrigatórios.
2. `birthDate`, se informado, não pode ser posterior à data UTC extraída de `createdAt`.
3. `fatherId` e `motherId` não podem ser iguais ao ID do próprio bovino. O código ainda não detecta ciclos genealógicos indiretos nem comprova existência dos genitores locais.
4. PO e PC exigem referência de pai e mãe: para cada genitor, basta `BovineId` local **ou** número externo não branco. PA e `UNREGISTERED` não exigem genitores.
5. PO, PC e PA exigem `registrationNumber` e `registrationAssociation` não brancos. `UNREGISTERED` proíbe esses dois dados quando contêm texto. Números de genitores externos são campos distintos desses dados oficiais do próprio animal.

Alguns campos são opcionais hoje (`breed`, `coatColor`, `birthDate`, `earTag` e referências genealógicas condicionais). Não converter raça em enum nem exigir SISBOV para todos os animais sem decisão de domínio. `Bovine` armazena `updatedAt = createdAt`, mas ainda não expõe mudanças posteriores. Não há `equals/hashCode` customizado nas entidades.

### Invariantes de `Farm`

`Farm.create(id, name, codeSeries, createdAt)` exige os quatro dados e cria status `ACTIVE`, `updatedAt = createdAt`. `activate/deactivate` só validam o instante em uma transição efetiva; ele não pode anteceder `createdAt` nem `updatedAt`. A exclusividade da série é imposta na tabela `farm`, não apenas pela entidade. A API restringe o nome a 255 caracteres; a factory de domínio verifica somente não nulidade e não brancura.

## Fronteiras e referências

As duas entidades não compartilham uma transação de domínio nem navegam uma pela outra. `Bovine` referencia genitores por `BovineId`, não por objetos `Bovine`; referências externas são strings. Isso preserva fronteiras de agregados, mas ainda não garante a integridade referencial dos genitores. `CreateBovineService` coordena criação e chamada à porta de persistência, sem transação configurada e sem fazenda de origem.

**Proposta sujeita a aprovação:** uma futura titularidade temporal (`BovineOwnershipPeriod`) registra o vínculo entre `BovineId` e `FarmId` por intervalo de vigência, mantendo o ID do animal constante. O período pode formar agregado próprio, separado da entidade bovina, para controlar concorrência e cronologia sem carregar todo o histórico em `Bovine`. Criar bovino e primeiro período na mesma transação é recomendação técnica, pendente da decisão de contrato de entrada. Ver [ADR pendente](../01-architecture/ARCHITECTURE_DECISIONS.md).

## Consistência atual e futura

Hoje, regras dentro de `Farm`/`Bovine` são síncronas e locais. Unicidade de `Farm.codeSeries` usa constraint de banco, e `TransactionTemplate` envolve o cadastro de fazenda. Unicidade de `BovineInternalCode`, titularidade exclusiva, ausência de ciclos genealógicos e consistência entre eventos futuros exigiriam persistência/serviços ou políticas externas; ainda não estão implementadas.

Nenhum serviço de domínio foi criado. Introduzi-lo somente quando houver uma regra que não pertença naturalmente a um agregado. Períodos gestacionais, descarte de leite, CCS/CBT e obrigações regulatórias precisam de validação profissional antes de virarem invariantes.
