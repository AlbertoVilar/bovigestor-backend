# Vocabulário do domínio

Este glossário separa conceitos implementados de hipóteses de modelagem. Regras completas estão no [modelo de domínio](../03-domain/DOMAIN_MODEL.md).

| Termo | Sentido atual | Estado |
| --- | --- | --- |
| Fazenda (`Farm`) | Unidade cadastrada com identidade, nome, série de código e situação. | Implementado |
| `FarmId` | Identificador técnico positivo da fazenda. | Implementado |
| Série da fazenda (`codeSeries`) | Três ou quatro letras maiúsculas; única na tabela `farm`. | Implementado |
| Bovino (`Bovine`) | Entidade de domínio independente de sexo e idade, criada manualmente. | Implementado em memória |
| `BovineId` | Identidade técnica positiva do animal; deve permanecer estável em transferências futuras. | Tipo implementado; transferência planejada |
| `BovineInternalCode` | Código de negócio no formato `SERIE-AA-SEQUENCIA`; a associação à fazenda ainda não é aplicada. | Formato implementado; vínculo pendente |
| Categoria genealógica | `PO`, `PC`, `PA` ou `UNREGISTERED`; esta última é explicitamente informada. | Implementado |
| Genitor | Referência local por `BovineId` ou externa por número de registro; para PO/PC exige pai e mãe. | Regra local implementada; consulta externa pendente |
| Titularidade | Relação temporal entre animal e fazenda, ainda sem entidade, tabela ou fluxo. | Proposto |
| Registro oficial / SISBOV | Identificação externa distinta de `BovineId` e do código interno. | Estratégia pendente; SISBOV não é universalmente obrigatório |

As categorias genealógicas, as regras de titularidade e os futuros módulos veterinários precisam de validação de negócio antes de serem ampliados. Não se pressupõem prazos, índices zootécnicos ou obrigações regulatórias.
