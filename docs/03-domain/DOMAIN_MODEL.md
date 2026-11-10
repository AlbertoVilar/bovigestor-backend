# Modelo de domínio e limites DDD

Base desta atualização: implementação local na branch `feat/bovine-ownership-period-persistence`, HEAD `559c745cf8c00bde828b3f53a62575e00e5999d3`, ainda em revisão. [Diagramas de classes](../04-diagrams/CLASS_DIAGRAMS.md), [dados](../05-data/DATA_MODEL.md) e [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md) complementam este texto.

## Modelo implementado

`Farm` e `Bovine` são entidades identificadas por `FarmId` e `BovineId`, respectivamente, e tratadas aqui como raízes de agregados independentes: suas factories são as entradas públicas para criá-las, e métodos `reconstitute` permitem reconstrução validada a partir de persistência. O código não declara uma interface `AggregateRoot`. O histórico de custódia referencia IDs de bovino e fazenda, sem navegação direta entre agregados.

| Elemento | Tipo e responsabilidade | Invariantes ou limites existentes |
| --- | --- | --- |
| `FarmId` | Value Object `record(Long value)` | Não nulo e positivo; igualdade por valor. |
| `Farm` | Entidade/raiz: ID, nome, série, status, criação e atualização. | `create` inicia `ACTIVE`; nome não nulo/em branco; série `[A-Z]{3,4}`; timestamps de transições não retrocedem. `activate/deactivate` são idempotentes quando já no estado desejado. |
| `FarmStatus` | Enum `ACTIVE`, `INACTIVE`. | Não há fluxo Application para mudança de status. |
| `BovineId` | Value Object `record(Long value)` | Não nulo e positivo; igualdade por valor. Identidade técnica a preservar em futuras transferências. |
| `BovineInternalCode` | Value Object `record(String value)` | Série de 3–4 letras maiúsculas, hífen, dois dígitos, hífen e sequência positiva com ao menos quatro dígitos. Não verifica ano corrente nem série de fazenda; a persistência impõe unicidade global. |
| `Bovine` | Entidade/raiz criada por `createManually` e reconstruída por `reconstitute`; contém `BovineOrigin`. | Exige ID, código, dados, origem e datas; aplica regras de nascimento, genitores e registro descritas abaixo. |
| `BovineRegistrationData` | `record` de 14 parâmetros usado para criar `Bovine`. | É objeto de parâmetros; **não** entidade persistente independente. Sua construção isolada não valida todos os campos; a factory do bovino valida as invariantes. |
| `BovineRegistrationCategory` | Enum `PO`, `PC`, `PA`, `UNREGISTERED`. | Categoria é obrigatória e não recebe default. |
| `BovineSex` | Enum `MALE`, `FEMALE`. | Obrigatório na criação; nenhuma regra reprodutiva derivada foi implementada. |
| `BovineStatus` | Enum `ACTIVE`, `INACTIVE`, `DECEASED`, `SOLD`. | Obrigatório na criação; não existem operações públicas de transição. `SOLD` não executa transferência. |

### Invariantes de `Bovine.createManually`

1. `id`, `internalCode`, `data` e `createdAt` não podem ser nulos; `name` não pode ser nulo/em branco; `sex`, `status` e `registrationCategory` são obrigatórios.
2. `birthDate`, se informado, não pode ser posterior à data UTC extraída de `createdAt`.
3. `fatherId` e `motherId` não podem ser iguais ao ID do próprio bovino. O código ainda não detecta ciclos genealógicos indiretos nem comprova existência dos genitores locais.
4. PO e PC exigem referência de pai e mãe: para cada genitor, basta `BovineId` local **ou** número externo não branco. PA e `UNREGISTERED` não exigem genitores.
5. PO, PC e PA exigem `registrationNumber` e `registrationAssociation` não brancos. `UNREGISTERED` proíbe esses dois dados quando contêm texto. Números de genitores externos são campos distintos desses dados oficiais do próprio animal. PA não exige referências para pai ou mãe; nenhuma regra adicional para PA é introduzida aqui.

Alguns campos são opcionais hoje (`breed`, `coatColor`, `birthDate`, `earTag` e referências genealógicas condicionais). Não converter raça em enum nem exigir SISBOV para todos os animais sem decisão de domínio. `Bovine` armazena `updatedAt = createdAt`, mas ainda não expõe mudanças posteriores. Não há `equals/hashCode` customizado nas entidades.

### Invariantes de `Farm`

`Farm.create(id, name, codeSeries, createdAt)` exige os quatro dados e cria status `ACTIVE`, `updatedAt = createdAt`. `activate/deactivate` só validam o instante em uma transição efetiva; ele não pode anteceder `createdAt` nem `updatedAt`. A exclusividade da série é imposta na tabela `farm`, não apenas pela entidade. A API restringe o nome a 255 caracteres; a factory de domínio verifica somente não nulidade e não brancura.

## Fronteiras e referências

As entidades não navegam uma pela outra. `Bovine` referencia genitores por `BovineId`, não por objetos `Bovine`; referências externas são strings. A persistência impõe FK aos genitores locais, mas não detecta ciclos genealógicos indiretos. `CreateBovineWithInitialOwnershipService` coordena a consulta da fazenda e a criação de bovino/período; `BovineConfig` fornece a fronteira transacional externa. O comando leva dados de origem e `farmId` operacional; o `Bovine` mantém origem cadastral, não a fazenda de custódia atual.

`BovineOwnershipPeriod`, identificado por `BovineOwnershipPeriodId`, representa em Domain e persistência a relação temporal entre `BovineId` e `FarmId`, mantendo estável a identidade do animal. V5 cria sua tabela e V6 limita a um o número de períodos abertos por bovino. O cadastro local cria bovino e primeiro período dentro da mesma transação externa. A fronteira do período como agregado próprio, consulta temporal, sobreposição histórica e transferência continuam em aberto ou não implementadas; consulte [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md).

### Identidade, origem e custódia

São conceitos distintos e não devem ser deduzidos uns dos outros:

| Conceito | Estado no modelo atual | Entendimento/proposta |
| --- | --- | --- |
| Identidade do bovino | `BovineId` é sua identidade técnica; `BovineInternalCode` é um identificador de negócio imutável e globalmente único na persistência; `registrationNumber` é um identificador de registro genealógico externo. | `BovineId` permanece estável em mudança de fazenda e não se confunde com o código interno ou o número de registro. O prefixo do código interno não identifica fazenda atual nem de origem. |
| Origem cadastral | `BovineOrigin` é um Value Object imutável integrado ao `Bovine` e aos mappers de persistência, com `creatorName`, `originFarmName` e `originFarmId`; os nomes são obrigatórios/não brancos e normalizados, enquanto o ID local pode ser nulo. | Registra a origem informada; não equivale à fazenda de entrada ou custódia. |
| Custódia operacional | `BovineOwnershipPeriod`/`BovineOwnershipPeriodId` são persistidos em V5/JPA; o cadastro local grava o primeiro período e V6 limita períodos abertos. | O período registra a fazenda que mantém o animal em determinado intervalo. O período inicial pode apontar para fazenda diferente da origem cadastral. Não há endpoint de histórico nem transferência. |

**Exceção de negócio confirmada, limitada a bovino adquirido sem procedência biológica conhecida e classificado como PA mediante avaliação técnica:** a primeira fazenda cadastrante no BoviGestor estabelece a origem cadastral, e o nome de seu proprietário é informado como criador. A classificação PA e o RG são informados conforme a avaliação técnica, sem exigência de comprovar procedência biológica anterior ou idade verdadeira. PA não exige referências de pai ou mãe. A transferência posterior não altera essa origem cadastral. Esta regra não adiciona validações além do comportamento local descrito nesta documentação.

O cadastro local exige fazenda existente e ativa e grava bovino e primeiro período atomicamente. O instante desse primeiro período vem do `Clock` do sistema; um fluxo com data real de entrada retroativa ainda não existe. Transferir o animal entre fazendas ainda não está implementado. Quando houver gate próprio, a operação não deve criar outro `Bovine` nem alterar a origem cadastral. Custódia por fazenda não determina propriedade jurídica por pessoa física ou jurídica.

Continuam abertas decisões de origem para categorias e cenários diferentes da exceção PA acima, incluindo fontes/evidências e representação de origem desconhecida. O mecanismo atual persiste o VO de origem em campos embutidos na entity bovina; novas alterações de mapeamento dependem de gate. Também seguem pendentes a política de autorização, a exposição dos GETs, entrada retroativa, timeline de custódia e transferência.

## Consistência atual e futura

Hoje, regras dentro de `Farm`/`Bovine` são síncronas e locais. Unicidade de `Farm.codeSeries` e de `BovineInternalCode` usam constraints de banco; `TransactionTemplate` envolve os cadastros. O POST bovino fica apenas no perfil dev+flag e cria período inicial sem autorização por usuário. V6 impõe no máximo um período aberto, mas não protege sobreposições históricas nem implementa transferência. O banco persistente dev permanece não validado.

Nenhum serviço de domínio foi criado. Introduzi-lo somente quando houver uma regra que não pertença naturalmente a um agregado. Períodos gestacionais, descarte de leite, CCS/CBT e obrigações regulatórias precisam de validação profissional antes de virarem invariantes.
