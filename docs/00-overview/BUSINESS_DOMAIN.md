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
| Origem cadastral do bovino (`BovineOrigin`) | Value Object imutável com `creatorName`, `originFarmName` e `originFarmId`. Os nomes são obrigatórios e normalizados com `strip()`; `originFarmId` é opcional para uma fazenda sem cadastro local. | VO e testes implementados; ainda não integrado ao agregado `Bovine` nem persistido |
| Criador informado | `creatorName` registra o nome informado como criador; no cenário específico de PA sem procedência conhecida, é informado o proprietário da primeira fazenda cadastrante, sem afirmar que seja o criador biológico historicamente conhecido. O VO rejeita nome nulo/em branco e remove espaços nas extremidades. | Campo do VO implementado; identificação civil, chave externa e integração ao cadastro permanecem em aberto |
| Fazenda de origem | `originFarmName` registra o nome informado da fazenda de origem histórica; `originFarmId` pode apontar para um `FarmId` local ou ser `null` quando não há fazenda local correspondente. | Campos do VO implementados; origem não equivale à fazenda de aquisição, cadastro ou custódia atual |
| Categoria genealógica | `PO`, `PC`, `PA` ou `UNREGISTERED`; esta última é explicitamente informada. | Regras atuais implementadas: PO/PC exigem referências para pai e mãe; PO/PC/PA exigem número e associação de registro; PA não exige referências parentais |
| Genitor | Referência local por `BovineId` ou externa por número de registro; para PO/PC exige pai e mãe. | Regra local implementada; consulta externa pendente |
| Custódia operacional por fazenda | Período temporal que representa a fazenda responsável pelo animal no rebanho; pode diferir da fazenda de origem e não representa propriedade jurídica. | Tipos `BovineOwnershipPeriod` e `BovineOwnershipPeriodId` existem no Domain; persistência e fluxo operacional não existem |
| Custódia inicial operacional | Entrada do animal no rebanho de uma fazenda responsável; pode ocorrer em fazenda diferente da origem. | Contrato, autorização, dados obrigatórios e transação ainda pendentes; ADR-007 aberto |
| Registro oficial / SISBOV | Identificação externa distinta de `BovineId` e do código interno. | Estratégia pendente; SISBOV não é universalmente obrigatório |

Transferências futuras devem preservar `BovineId`, origem cadastral e histórico: uma transferência interna encerra um período de custódia e abre outro, sem alterar a origem cadastrada; uma saída para venda externa preserva o histórico e retira o animal do rebanho atual, sem inventar uma fazenda externa no sistema. Essa descrição orienta o desenho, não afirma que esses fluxos estejam implementados.

**Regra específica confirmada para bovino adquirido sem procedência biológica conhecida e classificado como PA após avaliação técnica:** a primeira fazenda cadastrante no BoviGestor estabelece a origem cadastral; seu proprietário é informado como criador (`creatorName`), sem afirmar que seja o criador biológico anterior. A classificação PA e os dados de registro genealógico (RG) são informados conforme a avaliação técnica. O sistema não exige comprovação da procedência biológica anterior nem da idade verdadeira. Referências de pai e mãe não são obrigatórias para PA, e transferências posteriores não modificam essa origem cadastral. Esta regra é limitada a esse cenário e não cria validações ou exigências novas para o cadastro PA.

Para construir `BovineOrigin`, o código exige os nomes informados de criador e fazenda de origem, mas permite `originFarmId == null`; isso não significa que o VO já faça parte do cadastro de todo bovino. A integração do VO ao agregado e a representação de cenários de origem desconhecida diferentes da regra PA acima permanecem pendentes. Também não se infere fazenda atual ou de origem pelo prefixo de `BovineInternalCode`. Para PA, preservam-se as exigências já implementadas de número e associação oficiais, sem acrescentar validações ou exigência de pai/mãe. Categorias genealógicas e módulos veterinários não devem ser ampliados sem validação de negócio; não se pressupõem prazos, índices zootécnicos ou obrigações regulatórias.
