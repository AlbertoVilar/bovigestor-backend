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

## ADR-007 — vínculo inicial entre bovino e fazenda

**Situação observada:** `CreateBovineService` obtém ID, monta `Bovine` e chama `SaveBovinePort`, mas `CreateBovineCommand` não contém fazenda. Não há `BovineOwnershipPeriod`, tabela de titularidade nem transação configurada para o bovino. Não inferir a fazenda a partir do prefixo de `BovineInternalCode`.

| Alternativa | Benefício | Custo/risco |
| --- | --- | --- |
| Acrescentar identificação da fazenda ao comando de cadastro e gravar bovino + primeiro período juntos. | Cadastro já nasce com titularidade rastreável e falha atômica. | Muda contrato e exige validar fazenda/autorização, período e transação. |
| Criar bovino e vincular em comando posterior obrigatório. | Casos de entrada legada podem ser tratados separadamente. | Estado intermediário sem titular pode ser indesejado; precisa de política explícita. |
| Derivar fazenda da sessão/usuário e registrar primeiro período. | Menos dados explícitos no comando. | Ainda não há autenticação nem vínculo usuário–fazenda; ambiguidade para múltiplas fazendas. |

**Recomendação técnica, não decisão:** após definir quem pode cadastrar e como se identifica a fazenda, incluir a fazenda explicitamente no contrato de entrada ou contexto de autorização e persistir bovino e titularidade inicial na **mesma transação externa**. Confirmar se animais sem titular local são permitidos antes de exigir o vínculo universalmente. Não alterar o comando nesta documentação.

## ADR-006/008 — períodos, concorrência e fronteiras

**Proposta:** `BovineOwnershipPeriod` referencia `BovineId` e `FarmId` por IDs, com início e fim de vigência. O modelo deve garantir no máximo um período atual, impedir sobreposições e determinar semântica dos limites temporais. Um índice único parcial PostgreSQL sobre `bovine_id` quando `ended_at IS NULL` é candidato para a primeira garantia; não substitui validação de sobreposição completa nem define retroatividade. Transferência deve encerrar o período anterior e criar o seguinte em uma transação, preservando `BovineId`. `OwnershipTransfer` como registro separado só se justifica se houver dados/fluxo próprios de auditoria; ainda não foi decidido.

## ADR-009/010/011 — validações futuras

SISBOV, limiares de CCS/CBT, carência, gestação e secagem dependem de requisitos e validação técnica/regulatória. Entidades futuras (`Pregnancy`, `Lactation`, `MilkProduction`, `HealthEvent`, `MilkQualityTest`) são nomes conceituais, não classes existentes. A reconstituição JPA deve preservar invariantes sem usar setters irrestritos, mas a factory ou mecanismo concreto permanece aberto.
