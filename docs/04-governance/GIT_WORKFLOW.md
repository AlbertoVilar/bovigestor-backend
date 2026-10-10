# Fluxo Git

## Origem e integração das branches

- `feat/*`, `fix/*`, `refactor/*` e `chore/*` partem normalmente de `develop` e são integradas a `develop` por Pull Request.
- `hotfix/*` parte excepcionalmente de `main` para corrigir um problema urgente da versão publicada. Após integrar o hotfix em `main`, sincronize a mesma correção com `develop`, preferencialmente por Pull Request, e confirme que os históricos contêm a correção nos dois destinos.
- A promoção regular ocorre de `develop` para `main` por Pull Request.

## Proteções e limpeza

- `main` e `develop` exigem Pull Request e o check estrito `Build and test`.
- Force-push e exclusão dessas branches são bloqueados; admins também estão sujeitos às regras.
- Merge commits são o único método habilitado; a branch de origem é removida automaticamente após merge.
- Exclua branches temporárias somente depois de confirmar a integração no branch de destino (PR concluída e commit integrado). A exclusão remota automática pode ocorrer após o merge; quando a branch local existir, remova-a com `git branch -d` após confirmar que está integrada. Não force a exclusão de branches não integradas.
- Commits e comentários de código devem ser escritos em inglês.
- Nenhuma integração ocorre sem CI verde e gate aplicável.
