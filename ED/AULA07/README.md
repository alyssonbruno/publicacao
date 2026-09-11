# Aula 07 — Códigos para o JDoodle

Programas usados nas práticas da Aula 07 de Estrutura de Dados
(Bubble Sort, Selection Sort, Insertion Sort e ordenação de objetos).

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
| `OrdenacaoDemo.java` | Os três algoritmos quadráticos medidos lado a lado, com n = 100, 1.000, 5.000 e 10.000. É o exemplo da seção 6.2 do texto-base. |
| `OrdenaProdutos.java` | Ordenação de objetos com `Comparator`: por preço, com desempate por nome, e depois por estoque decrescente. |
| `BubbleSortListaApp.java` | Bubble Sort sobre uma **lista encadeada**, e não sobre um vetor. A lista é impressa a cada passagem completa. |

Cada arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado no JDoodle sem nenhuma alteração.

## Antes de rodar: veja o algoritmo andando

A página `../visualizador-ed-aula07.html` executa os três algoritmos **passo a passo**, com a
barra colorida mostrando o que o Java está fazendo em cada linha. Abre no celular, sem conta e
sem instalar nada. Use-a antes do JDoodle: primeiro entender o movimento, depois rodar o código.

## Desafios

- **`OrdenacaoDemo`** — acrescente `50_000` ao vetor `tamanhos` e veja quanto tempo o
  Bubble Sort passa a gastar. Compare com o Insertion Sort.
- **`OrdenacaoDemo`** — troque a semente `42L` por outro número: a ordem relativa entre os
  três algoritmos muda?
- **`OrdenaProdutos`** — inverta o critério de preço com `.reversed()` e confira o que
  acontece com o desempate entre Teclado e Webcam.
- **`BubbleSortListaApp`** — insira os números já em ordem crescente e conte quantas
  passagens a lista imprime antes de parar.

## E se eu tiver computador?

A pasta `../codigo/` traz o Bubble Sort sobre lista encadeada organizado como projeto Maven,
com as classes no pacote `br.unitins.bubblesort`. O resultado é o mesmo do JDoodle — muda
apenas a organização dos arquivos.
