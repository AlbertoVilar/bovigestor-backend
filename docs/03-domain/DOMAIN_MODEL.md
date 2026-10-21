# Modelo de domínio e limites DDD

Base: implementação da branch `feat/bovine-web-01`, baseada em `7257c2bce524274bad106a5894bde7c8b98a4286`. [Diagramas de classes](../04-diagrams/CLASS_DIAGRAMS.md), [dados](../05-data/DATA_MODEL.md) e [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md) complementam este texto.

## Modelo implementado

`Farm` e `Bovine` são entidades identificadas por `FarmId` e `BovineId`, respectivamente, e tratadas aqui como raízes de agregados independentes: suas factories são as entradas públicas para criá-las. O código não declara uma interface `AggregateRoot`. Nenhum relacionamento direto `Bovine` → `Farm` existe hoje. `Farm` e `Bovine` possuem métodos públicos de leitura; `Bovine` ainda não tem alteração de estado ou reconstituição pública a partir de JPA.

| Elemento | Tipo e responsabilidade | Invariantes ou limites existentes |
| --- | --- | --- |
| `FarmId` | Value Object `record(Long value)` | Não nulo e positivo; igualdade por valor. |
| `Farm` | Entidade/raiz: ID, nome, série, status, criação e atualização. | `create` inicia `ACTIVE`; nome não nulo/em branco; série `[A-Z]{3,4}`; timestamps de transições não retrocedem. `activate/deactivate` são idempotentes quando já no estado desejado. |
| `FarmStatus` | Enum `ACTIVE`, `INACTIVE`. | Não há fluxo Application para mudança de status. |
| `BovineId` | Value Object `record(Long value)` | Não nulo e positivo; igualdade por valor. Identidade técnica a preservar em futuras transferências. |
| `BovineInternalCode` | Value Object `record(String value)` | Série de 3–4 letras maiúsculas, hífen, dois dígitos, hífen e sequência positiva com ao menos quatro dígitos. Não verifica ano corrente nem série de fazenda; a persistência impõe unicidade global. |
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

As duas entidades não compartilham uma transação de domínio nem navegam uma pela outra. `Bovine` referencia genitores por `BovineId`, não por objetos `Bovine`; referências externas são strings. A persistência impõe FK aos genitores locais, mas não detecta ciclos genealógicos indiretos. `CreateBovineService` coordena criação e chamada à porta de persistência; `BovineConfig` fornece a fronteira externa transacional. O comando atual não carrega fazenda operacional nem dados de procedência.

**Modelo de Domain implementado; uso operacional e persistência ainda pendentes:** `BovineOwnershipPeriod`, identificado por `BovineOwnershipPeriodId`, representa a relação temporal entre `BovineId` e `FarmId`, mantendo estável a identidade do animal. O período pode formar agregado próprio, separado de `Bovine`, para controlar concorrência e cronologia sem carregar o histórico no bovino; essa fronteira segue em discussão. Não há persistência JPA/tabela nem fluxo de criação ou transferência. Criar o bovino e o primeiro período na mesma transação permanece uma recomendação técnica, pendente da decisão do contrato de entrada. Ver [ADR pendente](../01-architecture/ARCHITECTURE_DECISIONS.md).

### Identidade, origem e custódia

São conceitos distintos e não devem ser deduzidos uns dos outros:

| Conceito | Estado no modelo atual | Entendimento/proposta |
| --- | --- | --- |
| Identidade do bovino | `BovineId` é sua identidade técnica; `BovineInternalCode` é um identificador de negócio imutável e globalmente único na persistência; `registrationNumber` é um identificador de registro genealógico externo. | `BovineId` permanece estável em mudança de fazenda e não se confunde com o código interno ou o número de registro. O prefixo do código interno não identifica fazenda atual nem de origem. |
| Origem histórica | Não existe campo ou tipo de origem em `Bovine`, e não há persistência de origem. | `BovineOrigin` é uma proposta de Value Object associado a `Bovine`, ainda não implementada. Poderá agrupar dados conhecidos do criador original (pessoa física ou jurídica), fazenda de origem histórica e identificadores/evidências oficiais quando aplicáveis. A fazenda da qual o animal foi adquirido pode ser diferente da fazenda de origem. |
| Custódia operacional | `BovineOwnershipPeriod` e seu identificador existem no Domain; não há tabela JPA nem fluxo operacional. | O período registra a fazenda que mantém o animal em determinado intervalo. A fazenda responsável pela entrada no rebanho pode ser diferente da fazenda de origem. |

No cadastro operacional, propõe-se validar a fazenda responsável pela entrada e criar o primeiro período de custódia na mesma transação externa da criação do bovino. O endpoint técnico atual, restrito ao perfil `dev`, não estabelece esse vínculo. Transferir o animal entre fazendas não cria outro `Bovine` nem altera sua origem histórica: encerra-se o período vigente e inicia-se outro, preservando `BovineId` e os períodos anteriores. Eventuais retificações da origem deverão ocorrer por operação específica e auditável, nunca como consequência automática de uma transferência; essa é uma diretriz proposta, não uma operação implementada. Venda externa preserva o histórico e encerra a presença no rebanho atual; não abre período para fazenda externa não representada no sistema. Custódia por fazenda não determina propriedade jurídica por pessoa física ou jurídica.

Continuam abertas as decisões sobre obrigatoriedade e conteúdo de origem, inclusive em aquisição externa; identificação de criadores externos ao BoviGestor; fontes e validação dos dados por associação bovina; tratamento de origem desconhecida; limite de agregado e reconstituição; integração ao contrato operacional; persistência; autorização; e conversão temporal da entrada inicial. Não se decidiu tabela exclusiva, `@Embeddable`, entidade independente ou outro mapeamento JPA.

## Consistência atual e futura

Hoje, regras dentro de `Farm`/`Bovine` são síncronas e locais. Unicidade de `Farm.codeSeries` e unicidade global de `BovineInternalCode` usam constraints de banco; `TransactionTemplate` envolve os cadastros. O endpoint HTTP técnico do bovino fica apenas no perfil dev e não cria titularidade nem aplica autorização. Titularidade exclusiva, ausência de ciclos genealógicos e consistência entre eventos futuros ainda dependem de implementação ou políticas externas.

Nenhum serviço de domínio foi criado. Introduzi-lo somente quando houver uma regra que não pertença naturalmente a um agregado. Períodos gestacionais, descarte de leite, CCS/CBT e obrigações regulatórias precisam de validação profissional antes de virarem invariantes.
