# Aula 11 — Código para o JDoodle

Programa usado na prática da Aula 11 de Estrutura de Dados
(árvore binária de busca — inserção, busca, remoção e travessias).

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
| `ArvoreBuscaBinariaApp.java` | Monta a árvore com `{50, 30, 70, 20, 40, 60, 80}`, imprime as três travessias, busca valores, remove nós com zero, um e dois filhos, e termina montando a árvore degenerada. |

O arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado no JDoodle sem nenhuma alteração.

## Desafios

- Insira os valores em ordem crescente (`10, 20, 30, 40, 50`) e compare a altura com a da
  árvore original. Quantos passos a busca passa a gastar?
- Remova o `50` (a raiz) antes das outras remoções e confira qual valor toma o lugar dela.
- Acrescente um valor repetido, por exemplo `inserir(40)` duas vezes, e descubra pela
  travessia em-ordem o que esta implementação faz com duplicatas.

## E se eu tiver computador?

A pasta `../codigo/` traz o mesmo exemplo organizado como projeto Maven, com as classes no
pacote `br.unitins.ed`. O resultado é o mesmo do JDoodle — muda apenas a organização dos
arquivos.
