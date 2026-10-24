# Aula 13 — Código para o JDoodle

Programa usado no laboratório da Aula 13 de Estrutura de Dados
(análise de complexidade aplicada: quanto custa achar uma chave numa lista, numa BST simples, numa
AVL e num `HashMap`, com n crescente).

## Como executar

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. escolha a linguagem **Java** e a versão **JDK 21** (serve qualquer uma a partir da 17);
3. apague o exemplo que aparece na tela;
4. cole o arquivo inteiro;
5. clique em **Execute**.

**Caminho alternativo — OneCompiler.** Se o JDoodle não abrir ou estiver lento, abra
<https://onecompiler.com/java>, apague o exemplo, cole o arquivo inteiro **sem mudar nada** (nem o
nome da classe) e clique em **Run**.

Funciona no computador e também no navegador do celular. O programa não pede digitação:
basta executar. Ele leva menos de um segundo num computador comum; num site on-line, alguns
segundos.

## Arquivo

| Arquivo | O que demonstra |
|---|---|
| `ExperimentoBuscaApp.java` | Guarda as chaves 1, 2, ..., n numa lista (`ArrayList`), na BST simples da Aula 11, na AVL da Aula 12 e num `HashMap`, com n = 500, 1.000, 2.000 e 4.000. Mede o custo de achar uma chave de dois jeitos: **contando os passos** de cada busca (posições olhadas na lista, nós visitados nas árvores) e **cronometrando** 10.000 buscas com o `System.nanoTime`. Mostra também a altura das duas árvores ao lado de log₂ n. |

O arquivo é **autossuficiente**: reúne a classe do experimento, a mesma `ArvoreAVL` da Aula 12 e a
mesma `ArvoreBuscaBinaria` da Aula 11, e **não declara `package`**, para poder ser colado no
JDoodle sem nenhuma alteração.

**Como ler a saída:** a parte 1 traz a média de passos de n buscas e, entre parênteses, o pior
caso; a parte 2, a altura das árvores; a parte 3, o tempo de 10.000 buscas em milissegundos. Os
passos são sempre os mesmos; os tempos mudam a cada execução — compare como cada coluna cresce
quando o n dobra, e não os números exatos.

## Desafios

- Logo acima do `main`, troque `embaralhar = false` por `embaralhar = true`. Antes de executar,
  diga o que vai acontecer com os passos e a altura da BST simples.
- Troque os tamanhos por `{4_000, 8_000, 16_000, 32_000}`. Se aparecer `estourou` na coluna da
  BST, a inserção recursiva desceu fundo demais e estourou a pilha (`StackOverflowError`, Aula 03):
  a BST com as chaves em ordem virou uma lista. Num computador comum isso aconteceu com
  n = 32.000; num site on-line, com menos memória, pode acontecer antes.
- Escreva, no papel, quantos passos a busca binária num vetor ordenado (Aula 06) gastaria no pior
  caso para cada n da tabela. Com qual coluna ela se parece?

## E se eu tiver computador?

O exemplo também existe como projeto Maven, com as classes separadas no pacote `br.unitins.ed`.
O resultado é o mesmo do JDoodle — muda apenas a organização dos arquivos.
