# Aula 06 — Códigos para o JDoodle

Programas usados nas práticas da Aula 06 de Estrutura de Dados
(listas ordenadas, pilhas e filas).

## Como executar

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. escolha a linguagem **Java** e a versão **JDK 25**;
3. apague o exemplo que aparece na tela;
4. cole **um arquivo inteiro** desta pasta;
5. clique em **Execute**.

Funciona no computador e também no navegador do celular. Nenhum dos três programas
pede digitação: basta executar.

## Arquivos

| Arquivo | O que demonstra |
|---|---|
| `ListaOrdenadaApp.java` | Inserção que empurra os vizinhos — O(n) — e busca binária — O(log n). Ao final, compara busca linear e binária em uma lista de 1.000 elementos. |
| `PilhaApp.java` | TAD Pilha (`push`, `pop`, `peek`), verificação de parênteses balanceados e a pilha de chamadas em uma recursão. |
| `FilaApp.java` | TAD Fila com array circular (`enqueue`, `dequeue`, `peek`) e simulação de atendimento por senha. |

Cada arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado no JDoodle sem nenhuma alteração.

## Desafios

- **`ListaOrdenadaApp`** — troque a ordem de chegada de `{40, 10, 25, 60, 15}` para
  `{10, 15, 25, 40, 60}` e compare o número de empurrões.
- **`PilhaApp`** — acrescente `"((a + b)"` ao vetor `testes` e confirme que dá **ERRO**.
- **`FilaApp`** — enfileire uma sexta pessoa com a fila cheia e observe a exceção.

## E se eu tiver computador?

A pasta `../codigo/` traz os mesmos exemplos organizados como projeto Maven, com as classes no
pacote `br.unitins.ed` e o `pom.xml` configurado para o **JDK 25**. O resultado é o mesmo do
JDoodle — muda apenas a organização dos arquivos.
