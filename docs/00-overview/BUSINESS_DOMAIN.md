# Vocabulário do domínio

Este glossário separa conceitos implementados de hipóteses de modelagem. Regras completas estão no [modelo de domínio](../03-domain/DOMAIN_MODEL.md).

| Termo | Sentido atual | Estado |
| --- | --- | --- |
| Fazenda (`Farm`) | Unidade cadastrada com identidade, nome, série de código e situação. | Implementado |
| `FarmId` | Identificador técnico positivo da fazenda. | Implementado |
| Série da fazenda (`codeSeries`) | Três ou quatro letras maiúsculas; única na tabela `farm`. | Implementado |
| Bovino (`Bovine`) | Entidade de domínio independente de sexo e idade, criada manualmente e persistida no fluxo técnico de desenvolvimento. | Implementado; POST técnico apenas em `dev` |
| `BovineId` | Identidade técnica positiva do animal; deve permanecer estável em transferências futuras. | Tipo implementado; transferência planejada |
| `BovineInternalCode` | Código de negócio imutável no formato `SERIE-AA-SEQUENCIA`, único globalmente; seu prefixo não determina a fazenda atual. | Domain e unicidade global em V2 |
| Origem do bovino (`BovineOrigin`) | Proposta para preservar separadamente informações conhecidas sobre criador original, fazenda de origem histórica e evidências oficiais aplicáveis. A fazenda de origem não é necessariamente o estabelecimento do qual o animal foi adquirido. | Conceito em avaliação; não existe tipo, campo ou persistência implementados |
| Criador original | Pessoa física ou jurídica associada à origem histórica do animal, inclusive quando externa ao BoviGestor. | Identificação, obrigatoriedade e validação pendentes |
| Fazenda de origem | Estabelecimento histórico original do animal; não é necessariamente a fazenda de aquisição, nem a que o cadastra ou mantém atualmente. | Não modelada; obrigatoriedade e tratamento de origem desconhecida pendentes |
| Categoria genealógica | `PO`, `PC`, `PA` ou `UNREGISTERED`; esta última é explicitamente informada. | Implementado |
| Genitor | Referência local por `BovineId` ou externa por número de registro; para PO/PC exige pai e mãe. | Regra local implementada; consulta externa pendente |
| Custódia operacional por fazenda | Período temporal que representa a fazenda responsável pelo animal no rebanho; pode diferir da fazenda de origem e não representa propriedade jurídica. | Tipos `BovineOwnershipPeriod` e `BovineOwnershipPeriodId` existem no Domain; persistência e fluxo operacional não existem |
| Custódia inicial operacional | Entrada do animal no rebanho de uma fazenda responsável; pode ocorrer em fazenda diferente da origem. | Contrato, autorização, dados obrigatórios e transação ainda pendentes; ADR-007 aberto |
| Registro oficial / SISBOV | Identificação externa distinta de `BovineId` e do código interno. | Estratégia pendente; SISBOV não é universalmente obrigatório |

Transferências futuras devem preservar `BovineId`, origem e histórico: uma transferência interna encerra um período de custódia e abre outro; uma saída para venda externa preserva o histórico e retira o animal do rebanho atual, sem inventar uma fazenda externa no sistema. Essa descrição orienta o desenho, não afirma que esses fluxos estejam implementados.

Permanecem sem decisão a obrigatoriedade de criador e fazenda de origem, inclusive para animais adquiridos externamente; a identificação de criadores externos; as fontes e validações dos dados conforme a associação bovina; e o tratamento de origem desconhecida. Também não se infere fazenda atual ou de origem pelo prefixo de `BovineInternalCode`. Categorias genealógicas e módulos veterinários precisam de validação de negócio antes de serem ampliados; não se pressupõem prazos, índices zootécnicos ou obrigações regulatórias.
