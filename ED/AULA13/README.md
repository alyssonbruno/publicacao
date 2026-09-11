# Aula 13 — Códigos da revisão

Esta pasta tem **duas formas do mesmo conteúdo**: os arquivos separados, que os alunos
montam passo a passo durante a prática (`../pratica-ed-aula13.md`), e um arquivo único
pronto para colar no JDoodle.

## Para colar no JDoodle

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. escolha a linguagem **Java** e a versão **JDK 25**;
3. apague o exemplo que aparece na tela;
4. cole o `ArvoresApp.java` inteiro;
5. clique em **Execute**.

| Arquivo | O que demonstra |
|---|---|
| `ArvoresApp.java` | As quatro estruturas em um programa só: árvore binária comum, BST, AVL (os quatro casos clássicos de rotação mais o exemplo usado na aula) e rubro-negra, terminando com a comparação de alturas e o `TreeMap`. |

O arquivo é **autossuficiente**: reúne as cinco classes e **não declara `package`**, para
poder ser colado no JDoodle sem nenhuma alteração. Ele é gerado a partir dos arquivos
separados desta pasta — ao alterar um deles, refaça a fusão para as duas versões não
divergirem.

## Para a prática em sala

| Arquivo | Papel |
|---|---|
| `ArvoreBinaria.java` | Árvore binária comum, montada nó a nó. |
| `ArvoreBuscaBinaria.java` | BST com inserção, busca, remoção e travessias. |
| `ArvoreAVL.java` | AVL com fator de balanceamento e as quatro rotações. |
| `ArvoreRubroNegra.java` | Rubro-negra com a verificação das propriedades. |
| `PrincipalAula13.java` | Programa de demonstração, com os blocos comentados para serem habilitados um a um durante a aula. |

No `PrincipalAula13.java` as chamadas ficam comentadas de propósito: a ideia é habilitar
uma por vez, conforme o assunto avança. No `ArvoresApp.java` todas estão habilitadas.

## Desafios

- Na comparação final, a BST gasta **5 passos** para achar o `10`, contra **3** na AVL e na
  rubro-negra. Insira mais valores em ordem decrescente e veja essa distância crescer.
- No caso `AULA` (`100, 50, 150, 300, 80, 58, 53`), confira no papel as duas rotações que o
  programa informa e desenhe a árvore resultante.
- Troque a ordem de inserção da rubro-negra e verifique se `propriedadesValidas()` continua
  devolvendo `true`.
