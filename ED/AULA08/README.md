# Aula 08 — Códigos para o JDoodle

Programas usados nas práticas da Aula 08 de Estrutura de Dados
(Merge Sort e Quick Sort — algoritmos de divisão e conquista).

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
| `OrdenacaoApp.java` | Merge Sort e Quick Sort ordenando uma cópia do mesmo vetor. É o código das seções 4.3 e 5 do texto-base, reunido em um arquivo só. |
| `PassoAPassoMergeApp.java` | O Merge Sort imprimindo cada trecho do vetor no momento em que ele vai ser mesclado. Serve para acompanhar a recursão descendo e voltando. |
| `ComparaTemposApp.java` | Merge × Quick com 100.000 números aleatórios e, depois, com o vetor **já ordenado** — o pior caso do Quick Sort desta versão. |

Cada arquivo é **autossuficiente**: reúne todas as classes necessárias e **não declara
`package`**, para poder ser colado no JDoodle sem nenhuma alteração.

> **Sobre o tamanho do vetor ordenado em `ComparaTemposApp`.** O texto-base usa 10.000
> elementos. Aqui são 3.000, porque no pior caso o Quick Sort desce um nível de recursão
> por elemento e a pilha de chamadas do JDoodle estoura antes do fim
> (`StackOverflowError`). Aumentar esse número aos poucos é, por si só, uma boa
> demonstração do limite da recursão.

## Desafios

- **`OrdenacaoApp`** — troque o vetor `original` por um vetor já ordenado e confira que os
  dois algoritmos continuam devolvendo o mesmo resultado.
- **`PassoAPassoMergeApp`** — conte quantas linhas são impressas para 11 elementos e
  compare com o número de chamadas recursivas previsto na aula.
- **`ComparaTemposApp`** — suba o vetor ordenado de 3.000 para 5.000, depois 10.000, e
  descubra em que tamanho o `StackOverflowError` aparece.
- **`ComparaTemposApp`** — no `partition`, troque o pivô do último elemento para o do meio
  e veja o que acontece com o tempo do caso ordenado.

## E se eu tiver computador?

A pasta `../codigo/` traz o Merge Sort organizado como projeto Maven, com as classes no
pacote `br.unitins.ed`. O resultado é o mesmo do JDoodle — muda apenas a organização dos
arquivos.
