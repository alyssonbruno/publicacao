# Aula 08 — Códigos para o JDoodle

Programas usados no laboratório da Aula 08 de Estrutura de Dados
(Merge Sort e Quick Sort — algoritmos de dividir para conquistar).

## Como executar

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. escolha a linguagem **Java** e a versão **JDK 25**;
3. apague o exemplo que aparece na tela;
4. cole **um arquivo inteiro** desta pasta;
5. clique em **Execute**.

**Caminho alternativo — OneCompiler.** Se o JDoodle não abrir ou estiver lento, abra
<https://onecompiler.com/java>, apague o exemplo, cole o arquivo inteiro **sem mudar nada** (nem o
nome da classe) e clique em **Run**. Os três arquivos rodam lá sem alteração, com os mesmos
resultados.

Funciona no computador e também no navegador do celular. Nenhum dos três programas
pede digitação: basta executar.

## Arquivos

| Arquivo | O que demonstra |
|---|---|
| `OrdenacaoApp.java` | Merge Sort e Quick Sort testados em seis vetores: o da aula, `[6, 3, 8, 1, 7, 2, 5, 4]`, e os casos de borda (já ordenado, ao contrário, com repetidos, um elemento, vazio). É o código das seções 6.4, 7.3 e 10.2 do texto-base, num arquivo só. |
| `PassoAPassoMergeApp.java` | O Merge Sort imprimindo cada trecho do vetor no momento em que ele vai ser intercalado. Serve para acompanhar a recursão descendo e voltando. |
| `ComparaTemposApp.java` | Merge × Quick com o tamanho dobrando: com números sorteados (os tempos ficam perto de 2x a cada linha) e com o vetor **já ordenado**, o pior caso do Quick Sort desta versão (4x a cada linha). É o programa da seção 10.3 do texto-base. |

Cada arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado no JDoodle sem nenhuma alteração.

> **Por que o vetor ordenado de `ComparaTemposApp` vai só até 3.000?** No pior caso, o
> Quick Sort empilha uma chamada recursiva por elemento, e a pilha de chamadas do JDoodle
> estoura poucos milhares depois disso (`StackOverflowError`). Descobrir onde isso acontece
> é um dos desafios abaixo.

## Antes de rodar: veja o algoritmo andando

O visualizador executa o Merge Sort e o Quick Sort **passo a passo**, com a linha de código
em execução, as variáveis e a pilha de chamadas. Abre no celular, sem conta e sem instalar
nada:

<https://alyssonbruno.github.io/publicacao/ED/AULA08/visualizador-ed-aula08.html>

Primeiro entender o movimento; depois rodar o código.

## Desafios

- **`OrdenacaoApp`** — troque o `<=` do `merge` por `<` e rode de novo. O resultado dos
  números muda? E o que mudaria se, em vez de números, fossem pedidos com o mesmo valor?
- **`PassoAPassoMergeApp`** — conte quantas linhas entre colchetes são impressas para os
  11 elementos. Cada uma é uma intercalação: quantas seriam para 16 elementos?
- **`ComparaTemposApp`** — troque o `3_000` do segundo laço por `6_000`, depois `12_000`,
  e descubra em que tamanho aparece o `StackOverflowError`.
- **`ComparaTemposApp`** — acrescente o pivô aleatório no início do `particionar` (seção
  7.6 do texto-base) e rode de novo: o que acontece com a coluna `Quick/anterior` da
  Parte 2?

## E se eu tiver computador?

A pasta `codigo/` da aula traz o Merge Sort organizado como projeto Maven, com as classes
no pacote `br.unitins.ed`. O resultado é o mesmo do JDoodle — muda apenas a organização dos
arquivos.
