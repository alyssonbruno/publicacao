# Aula 12 — Código para o JDoodle

Programa usado na prática da Aula 12 de Estrutura de Dados
(árvores balanceadas: AVL, rotações e `TreeMap`).

## Como executar

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. escolha a linguagem **Java** e a versão **JDK 25**;
3. apague o exemplo que aparece na tela;
4. cole o arquivo inteiro;
5. clique em **Execute**.

Funciona no computador e também no navegador do celular. O programa não pede digitação:
basta executar.

## Arquivos

| Arquivo | O que demonstra |
|---|---|
| `ArvoreAVLApp.java` | Os quatro casos clássicos de desbalanceamento (LL, RR, LR e RL) com as rotações que cada um dispara; a comparação entre BST simples e AVL com os mesmos oito valores; e o `TreeMap` do Java em uso. |

O arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado no JDoodle sem nenhuma alteração.

## Desafios

- Nos quatro casos, todos terminam com a mesma árvore final (`20, 10, 30` em pré-ordem).
  Explique por que os caminhos foram diferentes e o destino, o mesmo.
- Em `compararComBST`, insira os oito valores em ordem crescente e veja como ficam as duas
  alturas.
- Acrescente valores à comparação até que a diferença de passos entre BST e AVL apareça —
  com oito valores as duas ainda empatam na busca por `80`.
- Troque o `TreeMap` por `HashMap` no último bloco e observe o que acontece com a ordem
  das chaves.

## E se eu tiver computador?

A pasta `../codigo/` traz o mesmo exemplo organizado como projeto Maven, com as classes no
pacote `br.unitins.ed`. O resultado é o mesmo do JDoodle — muda apenas a organização dos
arquivos.
