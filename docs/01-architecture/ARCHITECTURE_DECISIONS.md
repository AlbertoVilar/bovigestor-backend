# Decisões arquiteturais e questões abertas

Base: `develop` em `0804e609857d22983e9839f7c844b1e45b2b40a0`. `CONFIRMED` descreve decisão explícita ou estrutura comprovada; `PROPOSED` é recomendação para revisão; `OPEN` exige decisão antes de contratos/migrations. Uma proposta documentada não se torna regra de domínio. Veja [arquitetura](ARCHITECTURE.md) e [roadmap](../07-planning/IMPLEMENTATION_ROADMAP.md).

| ID | Decisão / questão | Estado | Evidência e consequência |
| --- | --- | --- | --- |
| ADR-001 | Hexagonal pragmática, DDD leve; Domain e Application independentes de Spring/JPA/HTTP. | CONFIRMED | Imports e pacotes reais. Ports representam fronteiras usadas; não se criam abstrações sem caso de uso. |
| ADR-002 | `BovineId` é identidade técnica estável, inclusive após transferência futura. | CONFIRMED | `BovineId` é VO do animal; regra arquitetural explícita. Transferência ainda não existe. |
| ADR-003 | Identificadores de negócio não substituem IDs técnicos. | CONFIRMED | `Farm.codeSeries`, `BovineInternalCode` e IDs são tipos/campos distintos. Vínculo do código bovino à série da fazenda não é aplicado. |
| ADR-004 | Genealogia aceita referência local ou número externo por genitor; PO/PC exigem ambos os genitores. | CONFIRMED | `Bovine.validateParentage`; verificação externa e ciclos indiretos ainda não existem. |
| ADR-005 | Transação do cadastro de fazenda na composição externa. | CONFIRMED | `FarmConfig` envolve `CreateFarmService` em `TransactionTemplate`; rollback coberto por teste. |
| ADR-006 | Histórico de titularidade por períodos associados a `BovineId` e `FarmId`. | PROPOSED | Preserva identidade do animal e permite consultas temporais; ainda sem classe/tabela ou regra temporal aprovada. |
| ADR-007 | Registrar titularidade inicial ao criar bovino. | OPEN | `CreateBovineCommand` não tem `FarmId`; alternativas e recomendação abaixo. |
| ADR-008 | Limites dos agregados futuros e das transações. | OPEN | `Farm` e `Bovine` são raízes separadas hoje; decidir se período é agregado próprio, política de consistência e concorrência. |
| ADR-009 | SISBOV e outras identificações externas são dados distintos, não universalmente obrigatórios. | OPEN | Não há campo/integração SISBOV nem requisito regulatório aprovado. Definir aplicabilidade, fonte e validação. |
| ADR-010 | Separar reprodução, lactação, ordenha/produção, sanidade e qualidade por comportamento real. | PROPOSED | Evita concentrar históricos independentes em `Bovine`; nenhum módulo existe. |
| ADR-011 | Reconstituir domínio a partir de JPA sem contornar invariantes. | OPEN | `FarmJpaMapper` hoje só mapeia para entidade JPA; `Bovine` não oferece factory de reconstituição ou leitura pública. Projetar com casos de consulta e testes antes de implementar. |
| ADR-012 | Records para VOs/DTOs imutáveis; Lombok seletivo; MapStruct somente por necessidade. | CONFIRMED | `FarmId`, `BovineId`, `BovineInternalCode`, commands e DTOs usam records; mappers atuais são manuais. |
| ADR-013 | Identidade própria para cada período histórico de custódia operacional bovino–fazenda. | CONFIRMED (tipo); OPEN (geração e persistência) | O VO de domínio escolhido é `BovineOwnershipPeriodId(Long)`, positivo e imutável. `BovineId`, `FarmId` e o par não identificam uma visita histórica específica. |

## ADR-013 — identidade de `BovineOwnershipPeriod`

**Problema identificado:** o primeiro modelo didático de `BovineOwnershipPeriod` não tinha identificador próprio. Em DDD, nem toda classe precisa de ID; porém, a identidade dos períodos históricos não havia sido analisada. Sem identidade individual, duas visitas do mesmo bovino à mesma fazenda podem ser confundidas, e atualizações imutáveis podem ser interpretadas pela persistência como novos registros.

**Decisão aprovada para o desenho de domínio (ainda não implementada):** cada período lógico de custódia operacional entre um bovino e uma fazenda terá identidade própria, representada pelo Value Object imutável `BovineOwnershipPeriodId`, inicialmente `record BovineOwnershipPeriodId(Long value)` e com valor positivo, seguindo o padrão de `BovineId` e `FarmId`. `BovineId` continua identificando o animal; `FarmId`, a fazenda. Nenhum deles, nem a combinação `BovineId + FarmId`, identifica unicamente um período, pois um bovino pode retornar à mesma fazenda.

O identificador permanece estável durante todo o ciclo de vida do mesmo período lógico, inclusive quando `closeAt()` produzir uma representação imutável encerrada. Uma nova entrada ou transferência cria outro período e exige outra identidade, inclusive em um retorno à mesma fazenda. A persistência deverá mapear explicitamente esse VO à chave primária e distinguir atualização do mesmo registro de inserção de um novo período; a cópia fechada não pode gerar duplicação acidental.

Essa relação representa **custódia/gestão operacional**, não propriedade jurídica, identidade do ator nem autorização. `BovineId` permanece estável. Esta decisão não altera as regras já aprovadas para fazendas inativas: operações rotineiras continuam bloqueadas para todos; operações administrativas permitidas, como transferência de saída por ADMIN para fazenda ativa, mantêm seus controles próprios. Os limites efetivos do período permanecem `Instant`; o cadastro inicial recebe a data real de entrada como `LocalDate`, e sua conversão determinística para `Instant` conforme o fuso configurado da fazenda ainda requer convenção técnica explícita. Transferências comuns usam o instante de confirmação do sistema; correções retroativas exigem fluxo separado.

**Questão OPEN — alocação do identificador:** ainda não foi decidido quem gera o `Long`, nem quando e em qual fronteira. Alternativas a avaliar antes da implementação:

| Alternativa | Consequências |
| --- | --- |
| Application solicita o ID por uma porta de saída dedicada, implementada por sequência no adapter de persistência. | Mantém o Domain puro e entrega identidade à factory antes da criação; requer contrato/composição/adapter e define quando a alocação ocorre na transação. É coerente com o padrão atual de `FarmIdGenerationPort`, mas ainda precisa de revisão. |
| Banco/JPA gera a chave durante a persistência. | Centraliza a sequência no banco, mas a entidade de domínio precisaria existir antes de receber sua identidade; requer estratégia explícita para criação, fechamento imutável e reconstituição, e pode confundir novo registro com atualização. |

**Recomendação técnica para revisão, não decisão:** avaliar primeiro uma porta de geração na Application com sequência no adapter, pois `BovineOwnershipPeriodId` pertence ao modelo de domínio e precisa existir antes de `open()`/`closeAt()` produzir o objeto. Não implementar essa recomendação sem decisão explícita sobre atribuição, transação e reconstituição.

Ainda precisam de decisão/revisão antes de código ou schema: limite de agregado, reconstituição segura, mapeamento Domain–JPA, PK/FK/uniqueness, semântica INSERT/UPDATE, constraints temporais, concorrência e testes de integridade. Nenhuma migration, entidade JPA ou fluxo de geração foi aprovado por este ADR.

## ADR-007 — vínculo inicial entre bovino e fazenda

**Situação observada:** `CreateBovineService` obtém ID, monta `Bovine` e chama `SaveBovinePort`, mas `CreateBovineCommand` não contém fazenda. Não há `BovineOwnershipPeriod`, tabela de titularidade nem transação configurada para o bovino. Não inferir a fazenda a partir do prefixo de `BovineInternalCode`.

| Alternativa | Benefício | Custo/risco |
| --- | --- | --- |
| Acrescentar identificação da fazenda ao comando de cadastro e gravar bovino + primeiro período juntos. | Cadastro já nasce com titularidade rastreável e falha atômica. | Muda contrato e exige validar fazenda/autorização, período e transação. |
| Criar bovino e vincular em comando posterior obrigatório. | Casos de entrada legada podem ser tratados separadamente. | Estado intermediário sem titular pode ser indesejado; precisa de política explícita. |
| Derivar fazenda da sessão/usuário e registrar primeiro período. | Menos dados explícitos no comando. | Ainda não há autenticação nem vínculo usuário–fazenda; ambiguidade para múltiplas fazendas. |

**Recomendação técnica, não decisão:** após definir quem pode cadastrar e como se identifica a fazenda, incluir a fazenda explicitamente no contrato de entrada ou contexto de autorização e persistir bovino e titularidade inicial na **mesma transação externa**. Confirmar se animais sem titular local são permitidos antes de exigir o vínculo universalmente. Não alterar o comando nesta documentação.

## ADR-006/008 — períodos, concorrência e fronteiras

**Diretrizes já aprovadas e desenho ainda aberto:** `BovineOwnershipPeriod` representa um período de custódia operacional entre `BovineId` e `FarmId`, não propriedade jurídica. Seus limites efetivos usam `Instant` e intervalos semiabertos `[início, fim)`. No vínculo inicial, o usuário informa a data real de entrada como `LocalDate`, distinta de `recordedAt` e `Bovine.createdAt`; a conversão determinística para o limite `Instant`, conforme o fuso da fazenda, ainda exige convenção técnica explícita e não deve inventar uma hora observada. Transferências internas comuns usam o instante de confirmação (`Clock.instant()`), sem retroatividade livre; correções retroativas exigem fluxo separado. Fechar o período anterior e abrir o seguinte deve ser atômico, preservar `BovineId` e manter a atribuição histórica. Regras de fazenda inativa e autorização seguem a matriz de ator/ação/status já decidida; em particular, ADMIN pode executar a transferência administrativa de saída aprovada para destino ativo, mas não há bypass geral para operações rotineiras.

**Ainda em desenho:** garantir no máximo um período atual, impedir sobreposições, tratar concorrência e decidir limite de agregado, persistência, histórico/correções e eventual registro `OwnershipTransfer`. Um índice único parcial PostgreSQL em `bovine_id` quando `ended_at IS NULL` é candidato para limitar períodos abertos; não substitui a proteção de sobreposição nem resolve sozinho transferências concorrentes. Fechamento e abertura devem ocorrer na mesma transação. `OwnershipTransfer` separado só se justifica se houver dados/fluxo próprios de auditoria; ainda não foi decidido. A identidade individual de cada período está especificada na ADR-013; geração e mapeamento persistente permanecem abertos.

## ADR-009/010/011 — validações futuras

SISBOV, limiares de CCS/CBT, carência, gestação e secagem dependem de requisitos e validação técnica/regulatória. Entidades futuras (`Pregnancy`, `Lactation`, `MilkProduction`, `HealthEvent`, `MilkQualityTest`) são nomes conceituais, não classes existentes. A reconstituição JPA deve preservar invariantes sem usar setters irrestritos, mas a factory ou mecanismo concreto permanece aberto.
