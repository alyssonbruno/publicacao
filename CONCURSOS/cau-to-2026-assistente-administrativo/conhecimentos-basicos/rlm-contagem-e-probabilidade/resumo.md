# Resumo — Contagem e probabilidade

## O essencial

> [!IMPORTANT]
> **Trocar a ordem muda o resultado?** Monte um resultado com nomes (Ana, Bruno, Carla), troque a ordem e veja. **Virou outro** (cargos, senha, fila) → **arranjo**. **Continua o mesmo** (comissão, grupo, dupla) → **combinação** = arranjo ÷ *r*!. De 8 servidores: 3 cargos, 8 × 7 × 6 = 336; comissão de 3, 336 ÷ 6 = 56.

| Situação | Conta |
|---|---|
| Etapas em sequência ("e depois") | multiplique as opções |
| Casos que se excluem ("ou um, ou outro") | some os casos |
| Todos em fila (*n* diferentes) | *n*! |
| Anagrama com letras repetidas | *n*! ÷ (fatorial de cada repetição): PALMAS = 6!/2! = 360 |
| Alguns, com ordem (arranjo) | *n* × (*n* − 1) × … (*r* fatores) |
| Alguns, sem ordem (combinação) | arranjo ÷ *r*! |
| Probabilidade | favoráveis ÷ possíveis, contados do mesmo jeito |
| "Pelo menos um" | 1 − P(nenhum) |

## Contas à mão

- Corte antes de multiplicar: 9!/7! = 9 × 8 = 72; C(9, 3) = (9 × 8 × 7)/6 = 84.
- C(*n*, *r*) = C(*n*, *n* − *r*): C(10, 8) = C(10, 2) = 45.
- Fração × limite: 3/7 > 2/5 porque 3 × 5 = 15 > 2 × 7 = 14.

## Distinções que mais confundem

> [!CAUTION]
> A banca erra o item por **arranjo no lugar de combinação**, por **algarismos trocados** (495 × 459), por **"ou" lido como "exatamente um"** e por **limites** ("superior a 40%").

- Divide-se por *r*! (3 escolhidos: por 6), e não por *r*.
- "Pelo menos 1 arquiteto" não é "1 garantido × o resto": isso conta o mesmo grupo várias vezes. Use o total menos "nenhum".
- "Ana ou Bruno" inclui os dois juntos.
- Sem reposição, a segunda fração muda: 4/10 × 3/9, e não 4/10 × 4/10.
- 1/6 + 1/6 não é "pelo menos um 6" em dois lançamentos: é 11/36.

## O que rever em cada revisão

- **24 horas:** refaça sem olhar o quadro dos 8 servidores (336 e 56) e diga, para cada caso, se a ordem importa: senha, comissão, torneio, presidente e vice.
- **7 dias:** de 9 servidores (5 arquitetos e 4 administradores), conte as comissões de 3 com exatamente 1 arquiteto (30) e com pelo menos 1 (80).
- **30 dias:** julgue: "de 7 setores, em que cada um enfrenta cada outro uma vez, saem 42 partidas" (errado: 21).
