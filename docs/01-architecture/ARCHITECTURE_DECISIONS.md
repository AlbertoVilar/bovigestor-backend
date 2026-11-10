# Decisões arquiteturais e questões abertas

Base desta atualização: código local na branch `feat/bovine-ownership-period-persistence`, HEAD `559c745cf8c00bde828b3f53a62575e00e5999d3`, aguardando revisão. `CONFIRMED` descreve decisão explícita/estrutura comprovada; `PROPOSED` é recomendação; `OPEN` identifica questões de negócio ou técnicas restantes. Implementação local não equivale a aprovação para produção. Veja [arquitetura](ARCHITECTURE.md) e [roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md).

| ID | Decisão / questão | Estado | Evidência e consequência |
| --- | --- | --- | --- |
| ADR-001 | Hexagonal pragmática, DDD leve; Domain e Application independentes de Spring/JPA/HTTP. | CONFIRMED | Imports e pacotes reais. Ports representam fronteiras usadas; não se criam abstrações sem caso de uso. |
| ADR-002 | `BovineId` é identidade técnica estável, inclusive após transferência futura. | CONFIRMED | `BovineId` é VO do animal; regra arquitetural explícita. Transferência ainda não existe. |
| ADR-003 | Identificadores de negócio não substituem IDs técnicos. | CONFIRMED | `Farm.codeSeries`, `BovineInternalCode` e IDs são distintos. V2 impõe unicidade global de `BovineInternalCode`; o prefixo não identifica a fazenda atual e o código permanece imutável no Domain. |
| ADR-004 | Genealogia aceita referência local ou número externo por genitor; PO/PC exigem ambos os genitores. | CONFIRMED | `Bovine.validateParentage`; verificação externa e ciclos indiretos ainda não existem. |
| ADR-005 | Transação do cadastro de fazenda na composição externa. | CONFIRMED | `FarmConfig` envolve `CreateFarmService` em `TransactionTemplate`; rollback coberto por teste. |
| ADR-006 | Histórico de custódia operacional por períodos associados a `BovineId` e `FarmId`. | PARTIAL local | V5/JPA persistem períodos e V6 limita período aberto; consulta temporal, sobreposição histórica e transferência ainda não existem. |
| ADR-007 | Registrar custódia inicial ao criar bovino. | IMPLEMENTED local; política ampla OPEN | POST exige `farmId` e grava bovino/período atomicamente; entrada retroativa, autorização e elegibilidade de produção continuam pendentes. |
| ADR-008 | Limites dos agregados futuros e das transações. | OPEN | `Farm` e `Bovine` são raízes separadas hoje; decidir se período é agregado próprio, política de consistência e concorrência. |
| ADR-009 | SISBOV e outras identificações externas são dados distintos, não universalmente obrigatórios. | OPEN | Não há campo/integração SISBOV nem requisito regulatório aprovado. Definir aplicabilidade, fonte e validação. |
| ADR-010 | Separar reprodução, lactação, ordenha/produção, sanidade e qualidade por comportamento real. | PROPOSED | Evita concentrar históricos independentes em `Bovine`; nenhum módulo existe. |
| ADR-011 | Reconstituir domínio a partir de JPA sem contornar invariantes. | IMPLEMENTED local | `Bovine.reconstitute`/`Farm.reconstitute` e mappers JPA → Domain existem com testes; política de exposição dos GETs segue pendente. |
| ADR-012 | Records para VOs/DTOs imutáveis; Lombok seletivo; MapStruct somente por necessidade. | CONFIRMED | `FarmId`, `BovineId`, `BovineInternalCode`, commands e DTOs usam records; mappers atuais são manuais. |
| ADR-013 | Identidade própria para cada período histórico de custódia operacional bovino–fazenda. | CONFIRMED; implementação local aguardando revisão cumulativa | `BovineOwnershipPeriodId(Long)` é positivo/imutável; porta e sequence geram IDs e V5 persiste períodos. `BovineId`, `FarmId` e o par não substituem a identidade de cada visita. |
| ADR-014 | Separar identidade técnica permanente, origem cadastral e custódia operacional. | CONFIRMED local | `BovineOrigin` compõe `Bovine` e é persistido/reconstituído; origem e custódia são relações distintas. Questões de origem fora do escopo aprovado permanecem abertas. |

## ADR-013 — identidade de `BovineOwnershipPeriod`

**Problema identificado:** o primeiro modelo didático de `BovineOwnershipPeriod` não tinha identificador próprio. Em DDD, nem toda classe precisa de ID; porém, a identidade dos períodos históricos não havia sido analisada. Sem identidade individual, duas visitas do mesmo bovino à mesma fazenda podem ser confundidas, e atualizações imutáveis podem ser interpretadas pela persistência como novos registros.

**Decisão aprovada e implementação local:** cada período lógico de custódia operacional entre um bovino e uma fazenda tem identidade própria, representada pelo Value Object imutável `BovineOwnershipPeriodId(Long)`, de valor positivo. `BovineId` continua identificando o animal; `FarmId`, a fazenda. Nenhum deles, nem a combinação `BovineId + FarmId`, identifica unicamente um período, pois um bovino pode retornar à mesma fazenda. O ID é alocado pela porta `BovineOwnershipPeriodIdGenerationPort`, implementada por sequence no adapter; V5 persiste o período e o mapper reconstitui o Domain.

O identificador deve permanecer estável durante todo o ciclo de vida do mesmo período lógico, inclusive quando `closeAt()` produzir uma representação imutável encerrada. Uma nova entrada ou transferência cria outro período e exige outra identidade, inclusive em um retorno à mesma fazenda. O mapeamento atual usa esse VO como chave primária e os testes cobrem persistência/reconstituição do período; transferência e fechamento ainda não são fluxos implementados.

Essa relação representa **custódia/gestão operacional**, não propriedade jurídica, identidade do ator nem autorização. `BovineId` permanece estável. Esta decisão não altera as regras já aprovadas para fazendas inativas: operações rotineiras continuam bloqueadas para todos; operações administrativas permitidas, como transferência de saída por ADMIN para fazenda ativa, mantêm seus controles próprios. Os limites efetivos do período usam `Instant`. A regra de negócio de entrada prevê uma data real `LocalDate` e requer conversão determinística conforme o fuso da fazenda; o fluxo local atual ainda não recebe essa data e usa `Clock.instant()`. Transferências comuns usam o instante de confirmação do sistema; correções retroativas exigem fluxo separado.

**Decisão técnica local de alocação (aguardando revisão cumulativa):** a Application solicita o ID pela porta de saída dedicada, implementada por sequence no adapter de persistência. Isso mantém o Domain puro e entrega identidade à factory antes de `open()`. A decisão não aprova transferência, semântica temporal retroativa ou lançamento.

| Alternativa | Consequências |
| --- | --- |
| Application solicita o ID por uma porta de saída dedicada, implementada por sequência no adapter de persistência. | Implementado localmente; mantém o Domain puro e entrega identidade à factory antes da criação. |
| Banco/JPA gera a chave durante a persistência. | Alternativa histórica não utilizada no código atual. |

**Limite:** a implementação da porta/sequence está local; identidade/reconstituição não implicam que uma operação de fechamento ou transferência exista.

Permanecem sujeitos a decisão/revisão: limite de agregado, política temporal, sobreposição histórica e semântica de transferência. V5/V6, entidade, mappers, geração e testes existem localmente; os gates anteriores são limitados e não equivalem a aprovação cumulativa de produção.

## ADR-014 — identidade técnica, origem cadastral e custódia do bovino

**Problema:** a identidade permanente do animal, os dados cadastrais sobre sua procedência e a fazenda que atualmente o mantém são conceitos distintos. Usar a fazenda atual como origem apagaria a procedência informada; usar a origem como vínculo operacional atribuiria incorretamente o rebanho e os eventos históricos.

**Decisões de negócio confirmadas:** `BovineId` identifica o mesmo animal ao longo de sua vida e não muda em transferências. A origem cadastral não é a custódia atual nem necessariamente o local de aquisição. Cada bovino operacional deve entrar no sistema associado a uma fazenda válida; criação operacional e abertura do primeiro período devem ser atômicas. Transferência interna encerra o período da fazenda de saída e abre o da fazenda de destino, preservando `BovineId` e o histórico. Venda/saída externa preserva o bovino e seus registros históricos, encerra o vínculo local aplicável e remove o animal do rebanho local atual. Custódia por fazenda não determina propriedade jurídica nem autorização do usuário.

**Decisão de negócio confirmada — cenário específico de PA:** para bovino adquirido sem procedência biológica conhecida e classificado como PA mediante avaliação técnica, a primeira fazenda cadastrante no BoviGestor estabelece a origem cadastral. O proprietário dessa fazenda é informado como criador; `creatorName` representa o nome informado no cadastro, não necessariamente o criador biológico historicamente conhecido. A classificação PA e o RG são informados segundo a avaliação técnica. Não se exige comprovação da procedência biológica anterior nem da idade verdadeira; referências de pai e mãe não são obrigatórias. Transferências posteriores preservam e não alteram a origem cadastral. Esta regra não se generaliza a outras categorias ou cenários.

**Estado de implementação local:** `BovineOrigin` é um Value Object `record` com `creatorName`, `originFarmName` e `originFarmId`; nomes são validados/normalizados, e `originFarmId` é opcional. O VO compõe `Bovine`, é recebido pelo contrato de cadastro e persiste/reconstitui por `BovineOriginEmbeddable`/mappers. A custódia inicial é um vínculo temporal separado; o POST `dev` exige `farmId` operacional e cria o primeiro período junto com o animal. A integração local aguarda revisão independente e não aprova produção.

O VO exige um nome informado para o criador e para a fazenda de origem quando é construído. O contrato local atual de criação recebe e persiste `BovineOrigin`; o requisito de nomes não afirma a veracidade histórica desses dados. Para cenários diferentes do PA adquirido sem procedência conhecida, a identificação de criadores externos e a representação de dados de origem desconhecida ou incompletos continuam em aberto. Não há identificação externa única para o criador nem fonte documental validada definida para esses outros cenários.

**Registro genealógico e PA:** preservar as regras existentes de `Bovine.createManually`: PO e PC exigem uma referência para pai e mãe, cada uma local por `BovineId` ou externa por número; PO, PC e PA exigem número de registro e associação. PA não exige referências parentais no comportamento atual. Esta atualização documental não acrescenta campos, validações ou requisitos para PA. `UNREGISTERED` continua sendo informado explicitamente e não admite número/associação oficiais.

**Custódia inicial e tempo:** a fazenda responsável pela entrada no rebanho pode diferir da fazenda de origem. A regra de negócio aprovada prevê informar a data real de entrada como data de calendário, mesmo se anterior ao cadastro. O modelo do período usa `Instant`; uma conversão determinística para esse limite requer convenção técnica explícita. O fluxo local atual não recebe essa data: usa `Clock.instant()` como `startedAt`. Logo, ele não implementa entrada retroativa nem a conversão aprovada. Transferências internas ordinárias e correções retroativas também não estão implementadas.

**Pendências restantes:** definir fontes/evidências de origem para categorias e cenários fora da regra PA confirmada, identificação de criadores externos e representação de origem desconhecida. A origem é obrigatória no contrato implementado; qualquer mudança de obrigatoriedade requer decisão própria. A política de autorização/exposição e a conversão de data real de entrada para `Instant` não estão concluídas pelo fluxo atual, que usa `Clock.instant()`.

`BovineInternalCode` permanece imutável e globalmente único conforme V2. Seu prefixo não determina fazenda de origem nem fazenda atual; não inferir origem ou custódia a partir dele.

## ADR-007 — vínculo inicial entre bovino e fazenda

**Estado local observado:** `CreateBovineWithInitialOwnershipCommand` recebe `farmId` e dados bovinos/origem; o serviço valida fazenda existente/ativa antes de alocar IDs e cria bovino + período; `BovineConfig` envolve o caso inteiro em `TransactionTemplate`; V5/JPA persistem o período. A fazenda de origem não substitui a fazenda responsável pela custódia e nenhuma delas é inferida do prefixo de `BovineInternalCode`. Este fluxo usa o instante corrente do `Clock`, não uma data de entrada retroativa.

| Alternativa | Benefício | Custo/risco |
| --- | --- | --- |
| Acrescentar identificação da fazenda ao comando de cadastro e gravar bovino + primeiro período juntos. | Cadastro já nasce com titularidade rastreável e falha atômica. | Muda contrato e exige validar fazenda/autorização, período e transação. |
| Criar bovino e vincular em comando posterior obrigatório. | Casos de entrada legada podem ser tratados separadamente. | Estado intermediário sem titular pode ser indesejado; precisa de política explícita. |
| Derivar fazenda da sessão/usuário e registrar primeiro período. | Menos dados explícitos no comando. | Ainda não há autenticação nem vínculo usuário–fazenda; ambiguidade para múltiplas fazendas. |

**Implementação local atual:** fazenda operacional explícita no command e gravação atômica do primeiro período. As alternativas acima são histórico do desenho. Permanecem pendentes autenticação/autorização, política de exposição, validade temporal retroativa e decisão sobre operações que não sejam o cadastro inicial. O código não deve ser interpretado como autorização para produção.

## ADR-006/008 — períodos, concorrência e fronteiras

**Diretrizes já aprovadas e desenho ainda aberto:** `BovineOwnershipPeriod` representa um período de custódia operacional entre `BovineId` e `FarmId`, não propriedade jurídica. Seus limites efetivos usam `Instant` e intervalos semiabertos `[início, fim)`. A regra de negócio prevê que o usuário informe a data real de entrada como `LocalDate`, distinta de `recordedAt` e `Bovine.createdAt`; a conversão determinística para `Instant`, conforme o fuso da fazenda, requer convenção técnica explícita e não deve inventar hora observada. O fluxo local ainda usa `Clock.instant()` e não implementa essa data retroativa. Transferências internas comuns devem usar o instante de confirmação (`Clock.instant()`), sem retroatividade livre; correções retroativas exigem fluxo separado. Fechar o período anterior e abrir o seguinte deve ser atômico, preservar `BovineId` e manter a atribuição histórica. Regras de fazenda inativa e autorização seguem a matriz de ator/ação/status já decidida; em particular, ADMIN pode executar a transferência administrativa de saída aprovada para destino ativo, mas não há bypass geral para operações rotineiras.

**Implementação e limites atuais:** V5 persiste períodos; V6 implementa índice único parcial em `bovine_id` quando `ended_at IS NULL`. Testes em PostgreSQL descartável cobrem unicidade e concorrência para abertura. Isso limita período aberto, mas não impede sobreposição entre períodos encerrados e não implementa transferência. Consulta de timeline, correções temporais e eventual `OwnershipTransfer` separado seguem sem implementação; o cadastro inicial já é atômico. A fronteira de agregado permanece em discussão.

## ADR-009/010/011 — validações futuras

SISBOV, limiares de CCS/CBT, carência, gestação e secagem dependem de requisitos e validação técnica/regulatória. Entidades futuras (`Pregnancy`, `Lactation`, `MilkProduction`, `HealthEvent`, `MilkQualityTest`) são nomes conceituais, não classes existentes. A reconstituição JPA deve preservar invariantes sem usar setters irrestritos, mas a factory ou mecanismo concreto permanece aberto.
