# Modelo relacional e evolução proposta

Fonte atual: migrations [V1](../../src/main/resources/db/migration/V1__create_farm_schema.sql) e [V2](../../src/main/resources/db/migration/V2__create_bovine_schema.sql), entities JPA e implementação da branch `feat/bovine-web-01`. A seção **existente** descreve a base; a seção **PROPOSED** é planejamento, sem SQL executado. Ver [ADRs](../01-architecture/ARCHITECTURE_DECISIONS.md) e [modelo de domínio](../03-domain/DOMAIN_MODEL.md).

## Schema existente — V1 e V2

```mermaid
erDiagram
    farm {
        BIGINT id PK
        VARCHAR name
        VARCHAR code_series UK
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
    bovine {
        BIGINT id PK
        VARCHAR internal_code UK
        VARCHAR name
        VARCHAR sex
        VARCHAR breed
        VARCHAR coat_color
        DATE birth_date
        VARCHAR status
        VARCHAR ear_tag
        VARCHAR registration_number
        VARCHAR registration_association
        VARCHAR registration_category
        BIGINT father_id FK
        BIGINT mother_id FK
        VARCHAR father_registration_number
        VARCHAR mother_registration_number
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
    bovine }o--|| bovine : father_id / mother_id
```

V1 cria `farm_id_seq`; V2 cria `bovine_id_seq` (ambas iniciam em 1 e incrementam em 1). As sequences geram identidades técnicas positivas separadas dos códigos de negócio.

`farm.id` é PK, `NOT NULL` e `> 0`; `name` é `VARCHAR(255) NOT NULL` com `TRIM(name)` não vazio; `code_series` é `VARCHAR(4) NOT NULL UNIQUE`; `status` é `VARCHAR(20) NOT NULL` limitado a `ACTIVE`/`INACTIVE`; timestamps são `TIMESTAMP WITH TIME ZONE NOT NULL`. A expressão `[A-Z]{3,4}` é validada no Domain/Web, não por CHECK na V1.

`bovine` tem 18 colunas alinhadas a `BovineJpaEntity`; `id` é PK positiva; `internal_code` tem constraint global `uq_bovine_internal_code`; sexo, status e categoria são texto restrito aos enums existentes; timestamps são `TIMESTAMP WITH TIME ZONE`. `father_id` e `mother_id` são FKs opcionais autorreferenciadas e não podem apontar para o próprio animal. A V2 também impõe nome não branco, nascimento não posterior a `created_at`, coerência entre categoria e dados de registro, e referências parentais exigidas para PO/PC. Referências externas são campos de texto independentes, sem FK.

As entities JPA são separadas de `Farm`/`Bovine`; mappers manuais convertem Domain → JPA. Não há coluna `farm_id` em `bovine`: o vínculo temporal permanece para uma futura tabela de períodos de titularidade.

## Modelo PROPOSED — identidade, genealogia e titularidade

Somente os novos conceitos/tabelas abaixo são candidatos para discussão; `farm` e `bovine` já existem. O mesmo `bovine.id` técnico permanece válido depois de transferência; a fazenda atual **não** faz parte da identidade do bovino. O vínculo temporal ficaria em períodos separados.

```mermaid
erDiagram
    farm ||--o{ bovine_ownership_period : holds
    bovine ||--o{ bovine_ownership_period : has_history
    bovine ||--o{ bovine_parent_reference : child
    bovine o|--o{ bovine_parent_reference : possible_local_parent
    bovine ||--o{ ownership_transfer : subject_if_needed
    farm o|--o{ ownership_transfer : source_farm_id
    farm o|--o{ ownership_transfer : destination_farm_id
    farm ||--o{ farm_user_link : access_scope
    user_account ||--o{ farm_user_link : membership
    farm {
        BIGINT id PK
        VARCHAR code_series UK
    }
    bovine_parent_reference {
        BIGINT id PK
        BIGINT child_bovine_id FK
        BIGINT local_parent_bovine_id FK
        VARCHAR external_registration_number
        VARCHAR parent_role
    }
    bovine_ownership_period {
        BIGINT id PK
        BIGINT bovine_id FK
        BIGINT farm_id FK
        TIMESTAMPTZ started_at
        TIMESTAMPTZ ended_at
    }
    ownership_transfer {
        BIGINT id PK
        BIGINT bovine_id FK
        BIGINT source_farm_id FK
        BIGINT destination_farm_id FK
        TIMESTAMPTZ occurred_at
    }
    user_account {
        BIGINT id PK
    }
    farm_user_link {
        BIGINT id PK
        BIGINT farm_id FK
        BIGINT user_id FK
    }
```

`bovine_parent_reference` é **uma alternativa** para os atuais campos `fatherId`, `motherId` e números externos; guardar esses campos diretamente em `bovine` também é possível. Não criar tabela própria sem demanda de consulta e decisão sobre cardinalidade. `ownership_transfer` é opcional; os períodos podem bastar, com auditoria separada. Nesse desenho **PROPOSED**, as duas relações de `ownership_transfer` com `farm` têm papéis distintos: `source_farm_id` indica a fazenda de origem e `destination_farm_id` indica a fazenda de destino. A obrigatoriedade de cada vínculo e o modelo de transferência ainda dependem das ADR-007/008. `user_account`, `farm_user_link` e eventual `owner_party` não têm modelo de identidade aprovado. A cardinalidade ER acima é proposta para orientar discussão, não contrato de JPA.

### Constraints implementadas e decisões futuras

| Tema | Regra implementada ou decisão futura |
| --- | --- |
| Identidade bovina (implementada) | `bovine.id` é PK técnica; `bovine.internal_code` é único globalmente por `uq_bovine_internal_code`. O prefixo do código não determina fazenda. |
| Registro genealógico (implementado) | V2 replica por `CHECK` as coerências locais entre categoria, registros e referências exigidas; o Domain permanece fonte das invariantes de criação. Referências externas não geram FK. |
| Genitor local (implementado) | FKs opcionais para `bovine.id` e checks contra autorreferência. A existência é garantida pelo banco; ciclos indiretos não são verificados. |
| Titularidade ativa | Candidato: índice único parcial PostgreSQL por `bovine_id` quando `ended_at IS NULL`, para no máximo um período aberto. Não impede sobreposição entre períodos fechados. |
| Intervalos temporais | Definir limites inclusivos/exclusivos, fuso, retroatividade e validação de sobreposição. Eventual exclusão temporal PostgreSQL requer projeto e teste de concorrência. |
| Transferência | Fechar período anterior e abrir novo na mesma transação; manter histórico e `BovineId`; definir como tratar saída externa. |
| Exclusão lógica | Ainda não decidida. Não substituir histórico por remoção; definir retenção e status conforme regras de negócio. |
| Rastreabilidade | Timestamps, vínculo da ação com usuário e correlação de eventos são candidatos, sujeitos à política de auditoria e privacidade. |

## Modelo PROPOSED — reprodução, leite, sanidade e qualidade

Cada tabela candidata referencia a identidade técnica bovina. A pertença à fazenda em um evento deve ser obtida pela titularidade **na data do evento**, se essa regra for aprovada, e não por uma coluna permanente `current_farm_id` no animal.

```mermaid
erDiagram
    bovine ||--o{ reproductive_event : records
    bovine ||--o{ pregnancy : experiences
    bovine ||--o{ lactation : has
    bovine ||--o{ milking : produces
    lactation o|--o{ milking : possible_period
    bovine ||--o{ health_event : has
    health_event o|--o{ medication_administration : may_include
    bovine ||--o{ milk_quality_test : sampled_from
    bovine {
        BIGINT id PK
    }
    reproductive_event {
        BIGINT id PK
        BIGINT bovine_id FK
        TIMESTAMPTZ occurred_at
        VARCHAR event_type
    }
    pregnancy {
        BIGINT id PK
        BIGINT bovine_id FK
    }
    lactation {
        BIGINT id PK
        BIGINT bovine_id FK
        TIMESTAMPTZ started_at
        TIMESTAMPTZ ended_at
    }
    milking {
        BIGINT id PK
        BIGINT bovine_id FK
        BIGINT lactation_id FK
        TIMESTAMPTZ occurred_at
    }
    health_event {
        BIGINT id PK
        BIGINT bovine_id FK
        TIMESTAMPTZ occurred_at
    }
    medication_administration {
        BIGINT id PK
        BIGINT health_event_id FK
    }
    milk_quality_test {
        BIGINT id PK
        BIGINT bovine_id FK
        TIMESTAMPTZ sampled_at
    }
```

`MilkProduction` no diagrama de domínio é conceito; `milking` é uma hipótese de granularidade para persistência. Definir unidade/volume, amostra, laboratório, rastreio de lote, protocolos IATF, eventos de parto e medicação somente com requisitos aprovados. Não há prazo gestacional, carência, limite CCS/CBT nem obrigação SISBOV definidos neste documento.

## Estratégia de migração

V1 e V2 já existem e não devem ser recriadas nem editadas retroativamente. Qualquer mudança de schema posterior deve usar nova migration (V3+) com gate próprio, teste H2/PostgreSQL e análise de dados/índices. A compatibilidade atual é validada em PostgreSQL 17 via Testcontainers e pelo smoke test dev; isso não aprova schema de titularidade.
