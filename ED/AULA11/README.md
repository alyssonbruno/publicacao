# Aula 11 — Código para o JDoodle

Programa usado no laboratório da Aula 11 de Estrutura de Dados
(árvore binária de busca — inserção, busca, remoção, percursos e altura).

## Como executar

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. escolha a linguagem **Java** e a versão **JDK 25**;
3. apague o exemplo que aparece na tela;
4. cole o arquivo inteiro;
5. clique em **Execute**.

**Caminho alternativo — OneCompiler.** Se o JDoodle não abrir ou estiver lento, abra
<https://onecompiler.com/java>, apague o exemplo, cole o arquivo inteiro **sem mudar nada** (nem o
nome da classe) e clique em **Run**.

Funciona no computador e também no navegador do celular. O programa não pede digitação:
basta executar.

## Arquivo

| Arquivo | O que demonstra |
|---|---|
| `ArvoreBuscaBinariaApp.java` | Monta a árvore da aula com `50, 30, 70, 20, 40, 60, 80`, desenha a árvore, imprime os três percursos e a altura, mostra o caminho percorrido por duas buscas, remove um nó de cada caso (folha, um filho, dois filhos) e compara os mesmos cinco valores inseridos em duas ordens diferentes. |

O arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado no JDoodle sem nenhuma alteração.

**Como ler o desenho:** a árvore aparece deitada. A raiz fica encostada na margem esquerda; cada
nível a mais anda quatro espaços para a direita; os maiores ficam acima do pai e os menores, abaixo.
Incline a cabeça para a esquerda e a árvore fica de pé.

## Desafios

- Troque os valores das duas buscas (`mostrarBusca`) por 20 e 45 e preveja, no papel, o caminho
  de cada uma antes de executar.
- Troque a última ordem de inserção por `{50, 40, 30, 20, 10}`. Qual é a altura? Quantos nós a
  busca do 50 visita — e quantos visitaria a busca do 10?
- Ache uma ordem para os valores `10, 20, 30, 40, 50, 60, 70` que deixe a árvore com altura 2.
- Escreva o método `contarFolhas`, que conta os nós sem filhos. Na árvore da aula, o resultado é 4.

## E se eu tiver computador?

O exemplo também existe como projeto Maven, com as classes separadas no pacote `br.unitins.ed`.
O resultado é o mesmo do JDoodle — muda apenas a organização dos arquivos.
