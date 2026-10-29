# Aula 14 — Códigos para o compilador on-line

Programas usados na prática da Aula 14 de Estrutura de Dados (06/11/2026): grafos em lista de
adjacência, busca em largura (BFS), busca em profundidade (DFS), componentes e detecção de ciclo.

## Como executar

**Caminho principal — OneCompiler.**

1. abra <https://onecompiler.com/java>;
2. apague o exemplo que aparece na tela;
3. cole **um arquivo inteiro** desta pasta, **sem mudar nada** (nem o nome da classe);
4. clique em **Run**.

**Caminho alternativo — JDoodle.** O JDoodle mudou em outubro de 2026: sem login, só roda a
versão mais nova do Java e o número de execuções acaba depressa ("You've used your runs for this
session"). Se quiser usá-lo:

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. deixe a versão que já vem escolhida, a mais nova (as anteriores pedem login);
3. apague o exemplo que aparece na tela e cole um arquivo inteiro;
4. troque o nome da classe por `MyClass`: o JDoodle grava o código num arquivo chamado
   `MyClass.java`, e em Java a classe pública tem o nome do arquivo.
   - no `GrafosApp.java`, troque `public class GrafosApp` por `public class MyClass`;
   - no `DeteccaoCicloApp.java`, troque `public class DeteccaoCicloApp` por
     `public class MyClass`;
   - no `DetectorDependencias.java`, o nome aparece em **três linhas**: troque
     `DetectorDependencias` por `MyClass` na linha `public class DetectorDependencias` e nas
     duas linhas do `main` que criam o detector, onde ele aparece **duas vezes** em cada uma:
     `MyClass sistemaSeguro = new MyClass();` e `MyClass sistemaCiclico = new MyClass();`;
5. clique em **Execute**. Se as execuções acabarem, use o OneCompiler ou entre com uma conta
   gratuita do JDoodle.

Funciona no computador e também no navegador do celular. Nenhum dos programas pede digitação:
basta executar.

## Arquivos

| Arquivo | O que demonstra |
|---|---|
| `GrafosApp.java` | O programa principal do laboratório. Monta a **rede do exemplo** (oito equipamentos, A a H, e sete cabos) em **lista de adjacência**, mostra a mesma rede como **matriz**, faz a **BFS** a partir de A passo a passo (a fila a cada passo), calcula as **distâncias** e o **caminho com menos saltos** (A, B, D, E, F), faz a **DFS** recursiva (com o rastro "entra em" / "volta de") e com **pilha explícita**, e acha os **componentes** ({A a F} e {G, H}). Seções 4 a 8 e 10 do texto-base. |
| `DetectorDependencias.java` | Detecção de ciclo num grafo **direcionado** de dependências com os **três estados** (não visitado, visitando, visitado). Roda dois cenários: um sem ciclo e outro com `ModuloA → ModuloB → ModuloC → ModuloA`. Seção 8.3 do texto-base. |
| `DeteccaoCicloApp.java` | A mesma detecção escrita com **dois conjuntos** (`visitando` e `visitado`), sobre um grafo de dependências maior. Nas duas perguntas a resposta é `false`: este grafo não tem ciclo. Para quem quiser comparar as duas escritas. |

Cada arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado num compilador on-line (no OneCompiler, sem nenhuma
alteração).

## Desafios

- **`GrafosApp`** — logo acima do `main`, troque `caboNovoCE = false` por `true` e execute de
  novo: a distância de E cai para 2, a de F para 3, e o caminho com menos saltos até F passa a
  ser A, C, E, F.
- **`DetectorDependencias`** — no cenário seguro, logo depois da linha
  `sistemaSeguro.adicionarDependencia("Database", "Utils");`, acrescente
  `sistemaSeguro.adicionarDependencia("Utils", "App");` e execute: o programa aponta a conexão
  `Utils -> App` como a que fecha o ciclo.
- **`DeteccaoCicloApp`** — acrescente `grafo.add("List", "App");` antes das perguntas e
  execute de novo: as duas respostas viram `true`. Desenhe no papel o ciclo que essa única
  aresta fechou.
- Compare os dois detectores: por que o conjunto `visitado` (ou o estado `VISITADO`) é
  necessário? Apague-o de um deles e veja o que acontece com grafos maiores.
