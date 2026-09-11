# Aula 14 — Códigos para o JDoodle

Programas usados na prática da Aula 14 de Estrutura de Dados
(grafos: representação por lista de adjacência e detecção de ciclo com DFS).

## Como executar

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. escolha a linguagem **Java** e a versão **JDK 25**;
3. apague o exemplo que aparece na tela;
4. cole **um arquivo inteiro** desta pasta;
5. clique em **Execute**.

Funciona no computador e também no navegador do celular. Nenhum dos dois programas pede
digitação: basta executar.

## Arquivos

| Arquivo | O que demonstra |
|---|---|
| `DetectorDependencias.java` | Detecção de ciclo com as **três cores** (não visitado, visitando, visitado). Roda dois cenários: um sem ciclo e outro com `ModuloA → ModuloB → ModuloC → ModuloA`. É o código da seção 8.2 do texto-base. |
| `DeteccaoCicloApp.java` | A mesma detecção escrita com **dois conjuntos** (`visitando` e `visitado`), sobre um grafo de dependências maior. Nas duas perguntas a resposta é `false`: este grafo não tem ciclo. |

Cada arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado no JDoodle sem nenhuma alteração.

## Desafios

- **`DeteccaoCicloApp`** — acrescente `grafo.add("List", "App");` antes das perguntas e
  execute de novo: as duas respostas viram `true`. Desenhe no papel o ciclo que essa única
  aresta fechou.
- **`DetectorDependencias`** — no cenário seguro, acrescente
  `sistemaSeguro.adicionarDependencia("Utils", "App");` e confira qual aresta o programa
  aponta como culpada.
- Compare os dois arquivos: por que o conjunto `visitado` (ou a cor PRETA) é necessário?
  Apague-o de um deles e veja o que acontece com grafos maiores.
