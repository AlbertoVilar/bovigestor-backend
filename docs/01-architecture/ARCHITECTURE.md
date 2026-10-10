# Arquitetura

O BoviGestor adota arquitetura hexagonal pragmática com DDD leve. A separação entre domínio, aplicação e adapters será introduzida somente quando houver casos de uso concretos.

- O domínio concentra comportamento e invariantes; não depende de Spring, JPA, HTTP ou persistência.
- Entidades possuem comportamento quando esse comportamento pertence à entidade; não criamos métodos artificiais para evitar anemismo por definição.
- Value Objects e DTOs imutáveis usam records quando apropriado.
- Application Services coordenam casos de uso; Domain Services existem apenas quando o comportamento não pertence naturalmente a uma entidade.
- Ports isolam fronteiras reais; adapters implementam detalhes externos.
- Não introduzir interfaces, camadas ou abstrações apenas por formalismo.

Construtores, factories e operações de domínio preservam invariantes reais. Atributos obrigatórios nulos, textos obrigatórios em branco, identificadores inválidos, limites numéricos e transições inválidas devem ser tratados quando o contrato de domínio os definir.

## Convenções de idioma

Nomes de classes, métodos, variáveis e pacotes Java, comentários de código e mensagens de commit são escritos em inglês. Mensagens de exceções de domínio, erros de validação e erros de negócio apresentados ao usuário são escritos em português brasileiro (pt-BR). A documentação técnica e arquitetural é escrita em português brasileiro por padrão; instruções operacionais para agentes podem ser escritas em inglês quando apropriado.

Essa convenção não altera identificadores Java nem contratos técnicos de APIs. Ela também não se aplica a mensagens padrão de exceções de frameworks Java ou bibliotecas externas que o projeto não controla.

Lombok é seletivo; evitar `@Data`, setters irrestritos e igualdade gerada sem semântica explícita em entidades. MapStruct serve para mapeamentos estruturais repetitivos, nunca para contornar construtores, factories ou operações de domínio. Mapeamentos pequenos ou sensíveis permanecem manuais.
