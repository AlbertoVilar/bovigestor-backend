# Vocabulário do domínio

Este glossário separa conceitos implementados de hipóteses de modelagem. Regras completas estão no [modelo de domínio](../03-domain/DOMAIN_MODEL.md).

| Termo | Sentido atual | Estado |
| --- | --- | --- |
| Fazenda (`Farm`) | Unidade cadastrada com identidade, nome, série de código e situação. | Implementado |
| `FarmId` | Identificador técnico positivo da fazenda. | Implementado |
| Série da fazenda (`codeSeries`) | Três ou quatro letras maiúsculas; única na tabela `farm`. | Implementado |
| Bovino (`Bovine`) | Entidade de domínio independente de sexo e idade; armazena cadastro e origem informada. | Persistido; POST com custódia inicial somente em `dev`+flag, ainda sem autenticação/autorização |
| `BovineId` | Identidade técnica positiva do animal; deve permanecer estável em transferências futuras. | Tipo implementado; transferência planejada |
| `BovineInternalCode` | Código de negócio imutável no formato `SERIE-AA-SEQUENCIA`, único globalmente; seu prefixo não determina a fazenda atual. | Domain e unicidade global em V2 |
| Origem cadastral do bovino (`BovineOrigin`) | Value Object imutável integrado ao agregado e persistido nos dados de origem do bovino. Os nomes são obrigatórios e normalizados com `strip()`; `originFarmId` é opcional. | Domain, JPA mapping e testes implementados localmente; origem continua distinta da custódia atual |
| Criador informado | `creatorName` registra o nome informado como criador; no cenário específico de PA sem procedência conhecida, é informado o proprietário da primeira fazenda cadastrante, sem afirmar que seja o criador biológico historicamente conhecido. O VO rejeita nome nulo/em branco e remove espaços nas extremidades. | Campo do VO implementado; identificação civil, chave externa e integração ao cadastro permanecem em aberto |
| Fazenda de origem | `originFarmName` registra o nome informado da fazenda de origem histórica; `originFarmId` pode apontar para um `FarmId` local ou ser `null` quando não há fazenda local correspondente. | Campos do VO implementados; origem não equivale à fazenda de aquisição, cadastro ou custódia atual |
| Categoria genealógica | `PO`, `PC`, `PA` ou `UNREGISTERED`; esta última é explicitamente informada. | Regras atuais implementadas: PO/PC exigem referências para pai e mãe; PO/PC/PA exigem número e associação de registro; PA não exige referências parentais |
| Genitor | Referência local por `BovineId` ou externa por número de registro; para PO/PC exige pai e mãe. | Regra local implementada; consulta externa pendente |
| Custódia operacional por fazenda | Período temporal que representa a fazenda responsável pelo animal no rebanho; pode diferir da origem cadastral e não representa propriedade jurídica. | Domain, V5/JPA e V6 implementados localmente; V6 limita um período aberto, sem resolver sobreposição histórica |
| Custódia inicial operacional | Entrada do animal no rebanho da fazenda informada pelo cadastro inicial. | POST exige fazenda existente e ativa e grava bovino/período na mesma transação; limitado a `dev`+flag e sem autorização |
| Registro oficial / SISBOV | Identificação externa distinta de `BovineId` e do código interno. | Estratégia pendente; SISBOV não é universalmente obrigatório |

Transferências ainda não estão implementadas. Quando aprovadas, devem preservar `BovineId`, origem cadastral e histórico: uma transferência interna encerra um período e abre outro; uma saída externa preserva histórico sem inventar fazenda externa. A V6 sozinha não executa essas operações nem impede sobreposição entre períodos encerrados.

**Regra específica confirmada para bovino adquirido sem procedência biológica conhecida e classificado como PA após avaliação técnica:** a primeira fazenda cadastrante no BoviGestor estabelece a origem cadastral; seu proprietário é informado como criador (`creatorName`), sem afirmar que seja o criador biológico anterior. A classificação PA e os dados de registro genealógico (RG) são informados conforme a avaliação técnica. O sistema não exige comprovação da procedência biológica anterior nem da idade verdadeira. Referências de pai e mãe não são obrigatórias para PA, e transferências posteriores não modificam essa origem cadastral. Esta regra é limitada a esse cenário e não cria validações ou exigências novas para o cadastro PA.

O cadastro local recebe `BovineOrigin` no command, integra o VO ao `Bovine`, persiste e reconstitui seus campos. `originFarmId` pode ser nulo; não se infere fazenda atual ou origem pelo prefixo de `BovineInternalCode`. Para PA, permanecem as exigências existentes de número e associação oficiais, sem exigir pai/mãe. Categorias genealógicas e módulos veterinários não devem ser ampliados sem validação de negócio; não se pressupõem prazos, índices zootécnicos ou obrigações regulatórias.
