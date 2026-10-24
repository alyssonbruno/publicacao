# Aula 12 — Código para o JDoodle

Programa usado no laboratório da Aula 12 de Estrutura de Dados
(árvores balanceadas — AVL, fator de balanceamento, rotações e o `TreeMap` do Java).

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
basta executar.

## Arquivo

| Arquivo | O que demonstra |
|---|---|
| `ArvoreAVLApp.java` | Insere os valores da árvore da Aula 11 em ordem crescente (`20, 30, ..., 80`) numa BST simples e numa AVL e compara as duas (altura 6 × 2; busca do 80 com 7 × 3 nós); mostra os quatro casos de rotação (LL, RR, LR e RL) numa árvore que começa por `50, 30, 70`; traz uma linha "Sua vez" para testar outras ordens; e usa o `TreeMap`, a árvore rubro-negra do Java. |

O arquivo é **autossuficiente**: reúne todas as classes necessárias — `ArvoreAVL` e a mesma
`ArvoreBuscaBinaria` da Aula 11 — e **não declara `package`**, para poder ser colado no JDoodle sem
nenhuma alteração.

**Como ler o desenho:** a árvore aparece deitada, como na Aula 11. A raiz fica encostada na margem
esquerda; cada nível a mais anda quatro espaços para a direita; os maiores ficam acima do pai e os
menores, abaixo. Na AVL, cada nó mostra a sua altura (`h`) e o seu fator de balanceamento (`fb`).

## Desafios

- Na linha da seção "Sua vez", troque `{30, 20, 10}` por `{10, 20, 30}`, `{30, 10, 20}` e
  `{10, 30, 20}`. Antes de executar, diga qual é o caso e qual rotação vai acontecer.
- Ache uma ordem para os valores `10, 20, 30, 40, 50, 60, 70` em que a AVL **não faz nenhuma
  rotação**. Depois, insira-os em ordem crescente e conte as rotações.
- Apague o corpo do método `rotacaoEsquerda`, menos a primeira linha (`rotacoes.add(...)`), e
  reescreva-o olhando o `rotacaoDireita`: troque `esquerdo` por `direito` e vice-versa. A saída tem
  de continuar idêntica.

## E se eu tiver computador?

O exemplo também existe como projeto Maven, com as classes separadas no pacote `br.unitins.ed`.
O resultado é o mesmo do JDoodle — muda apenas a organização dos arquivos.
