import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Aula 12 — Laboratório: ÁRVORE AVL — fator de balanceamento e rotações.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 21</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>Como ler o desenho: a árvore está deitada, como na Aula 11 — a raiz fica
 * encostada na margem esquerda e os maiores ficam acima do pai. Na AVL, cada
 * nó mostra a sua altura (h) e o seu fator de balanceamento (fb).</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>com os valores da Aula 11 em ordem crescente, a BST simples vira uma
 *       lista (altura 6; a busca do 80 visita 7 nós), e a AVL, depois de
 *       4 rotações simples à esquerda, chega sozinha à árvore da Aula 11
 *       (altura 2; a busca do 80 visita 3 nós);</li>
 *   <li>LL e RR se resolvem com uma rotação; LR e RL, com duas, em sentidos
 *       opostos (a rotação dupla);</li>
 *   <li>no caso LL, o 40 troca de pai: era filho direito do 30 e passa a filho
 *       esquerdo do 50 — é a "subárvore do meio" da rotação;</li>
 *   <li>em todo desenho da AVL, todo nó tem fb igual a -1, 0 ou +1;</li>
 *   <li>o TreeMap devolve as chaves em ordem sem que ninguém escreva uma
 *       rotação: ele é uma árvore rubro-negra pronta.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 2.0
 */
public class ArvoreAVLApp {
    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        // 1. Os valores da árvore da Aula 11, chegando em ordem crescente
        int[] crescente = {20, 30, 40, 50, 60, 70, 80};
        out.println("=== 1. BST x AVL, inserindo " + Arrays.toString(crescente));
        ArvoreBuscaBinaria bst = new ArvoreBuscaBinaria();
        for (int valor : crescente) {
            bst.inserir(valor);
        }
        out.println("BST simples (Aula 11):");
        out.print(bst.desenhar());
        out.println("Altura: " + bst.altura());
        mostrarBusca(out, bst.caminho(80));
        out.println();
        out.println("AVL:");
        ArvoreAVL avl = montar(out, crescente);
        out.print(avl.desenhar());
        out.println("Altura: " + avl.altura());
        mostrarBusca(out, avl.caminho(80));

        // 2. Os quatro casos: o último valor desequilibra o 50
        out.println();
        out.println("=== 2. Os quatro casos, a partir de 50, 30, 70");
        mostrarCaso(out, "LL", new int[] {50, 30, 70, 20, 40, 10});
        mostrarCaso(out, "RR", new int[] {50, 30, 70, 60, 80, 90});
        mostrarCaso(out, "LR", new int[] {50, 30, 70, 20, 40, 45});
        mostrarCaso(out, "RL", new int[] {50, 30, 70, 60, 80, 55});

        // 3. Sua vez: troque os valores e preveja antes de executar
        out.println("=== 3. Sua vez");
        mostrarCaso(out, "seu", new int[] {30, 20, 10});

        // 4. O TreeMap do Java: uma árvore rubro-negra pronta
        out.println("=== 4. TreeMap (árvore rubro-negra do Java)");
        TreeMap<Integer, String> protocolos = new TreeMap<>();
        for (int numero : new int[] {10, 20, 30, 40, 50}) {   // em ordem crescente
            protocolos.put(numero, "processo " + numero);
        }
        out.println("Chaves em ordem: " + protocolos.keySet());
        out.println("get(30): " + protocolos.get(30));
        out.println("Primeira: " + protocolos.firstKey() + " | última: " + protocolos.lastKey());
        protocolos.remove(20);
        out.println("Depois de remove(20): " + protocolos.keySet());
    }

    /** Insere os valores numa AVL nova e conta cada rotação que acontecer. */
    private static ArvoreAVL montar(PrintStream out, int[] valores) {
        ArvoreAVL arvore = new ArvoreAVL();
        for (int valor : valores) {
            int antes = arvore.rotacoes().size();
            arvore.inserir(valor);
            List<String> todas = arvore.rotacoes();
            List<String> novas = todas.subList(antes, todas.size());
            if (!novas.isEmpty()) {
                out.println("Insere " + valor + ": " + tipo(novas));
                out.println("  rotações: " + String.join(", ", novas));
            }
        }
        return arvore;
    }

    /** Uma rotação é simples; duas, em sentidos opostos, formam a dupla. */
    private static String tipo(List<String> novas) {
        if (novas.size() == 1) {
            return novas.get(0).startsWith("direita") ? "simples à direita" : "simples à esquerda";
        }
        return novas.get(0).startsWith("esquerda") ? "dupla esquerda-direita" : "dupla direita-esquerda";
    }

    private static void mostrarCaso(PrintStream out, String nome, int[] valores) {
        out.println("Caso " + nome + ", inserindo " + Arrays.toString(valores) + ":");
        ArvoreAVL arvore = montar(out, valores);
        out.print(arvore.desenhar());
        out.println("Pré-ordem: " + arvore.preOrdem() + " | altura " + arvore.altura());
        out.println();
    }

    private static void mostrarBusca(PrintStream out, List<Integer> caminho) {
        out.println("Busca do 80: visitou " + caminho + " = " + caminho.size() + " nós");
    }
}

/**
 * Árvore AVL de números inteiros: uma árvore binária de busca que se
 * reequilibra sozinha a cada inserção.
 *
 * <p>Invariante da BST (Aula 11): em todo nó, os valores da subárvore esquerda
 * são menores que o valor do nó, e os da subárvore direita são maiores. Valores
 * repetidos são ignorados.</p>
 *
 * <p>Invariante da AVL: em todo nó, o fator de balanceamento (a altura da
 * subárvore esquerda menos a altura da subárvore direita) é -1, 0 ou +1.</p>
 *
 * <p>Convenção de altura (a mesma da Aula 11): uma folha tem altura 0, e a
 * árvore vazia, -1.</p>
 */
class ArvoreAVL {
    private No raiz;
    private final List<String> rotacoes = new ArrayList<>();

    private static class No {
        int valor;
        int altura;                        // guardada no nó; o nó novo é folha: 0
        No esquerdo;
        No direito;

        No(int valor) {
            this.valor = valor;
        }
    }

    // ---------- altura e fator de balanceamento ----------

    private int altura(No no) {
        if (no == null) {                  // árvore vazia
            return -1;
        }
        return no.altura;                  // lida do nó: não percorre a subárvore
    }

    private void atualizarAltura(No no) {
        no.altura = 1 + Math.max(altura(no.esquerdo), altura(no.direito));
    }

    private int fatorBalanceamento(No no) {
        return altura(no.esquerdo) - altura(no.direito);
    }

    // ---------- inserção ----------

    public void inserir(int valor) {
        raiz = inserir(raiz, valor);
    }

    private No inserir(No no, int valor) {
        if (no == null) {                  // achou o lugar vazio
            return new No(valor);
        }
        if (valor < no.valor) {
            no.esquerdo = inserir(no.esquerdo, valor);
        } else if (valor > no.valor) {
            no.direito = inserir(no.direito, valor);
        }                                  // igual: repetido, ignora
        return rebalancear(no);            // a linha nova: confere na volta
    }

    // ---------- rebalanceamento ----------

    private No rebalancear(No no) {
        atualizarAltura(no);
        int fb = fatorBalanceamento(no);
        if (fb > 1) {                                  // pesado à esquerda
            if (fatorBalanceamento(no.esquerdo) < 0) { // LR: o filho pende à direita
                no.esquerdo = rotacaoEsquerda(no.esquerdo);
            }
            return rotacaoDireita(no);                 // LL (ou o fim do LR)
        }
        if (fb < -1) {                                 // pesado à direita
            if (fatorBalanceamento(no.direito) > 0) {  // RL: o filho pende à esquerda
                no.direito = rotacaoDireita(no.direito);
            }
            return rotacaoEsquerda(no);                // RR (ou o fim do RL)
        }
        return no;                                     // fb -1, 0 ou +1: nada a fazer
    }

    // ---------- rotações ----------

    private No rotacaoDireita(No no) {
        rotacoes.add("direita no " + no.valor);
        No filho = no.esquerdo;            // o filho esquerdo sobe
        No meio = filho.direito;           // a subárvore do meio troca de pai
        filho.direito = no;                // o nó desce para a direita do filho
        no.esquerdo = meio;
        atualizarAltura(no);               // primeiro quem desceu
        atualizarAltura(filho);            // depois quem subiu
        return filho;                      // a nova raiz deste trecho
    }

    private No rotacaoEsquerda(No no) {
        rotacoes.add("esquerda no " + no.valor);
        No filho = no.direito;             // o filho direito sobe
        No meio = filho.esquerdo;          // a subárvore do meio troca de pai
        filho.esquerdo = no;               // o nó desce para a esquerda do filho
        no.direito = meio;
        atualizarAltura(no);               // primeiro quem desceu
        atualizarAltura(filho);            // depois quem subiu
        return filho;                      // a nova raiz deste trecho
    }

    /** As rotações feitas até agora, na ordem: "direita no 30", "esquerda no 10"... */
    public List<String> rotacoes() {
        return new ArrayList<>(rotacoes);
    }

    // ---------- busca ----------

    public boolean contem(int valor) {
        return contem(raiz, valor);
    }

    private boolean contem(No no, int valor) {
        if (no == null) {                  // o caminho acabou: não está
            return false;
        }
        if (valor == no.valor) {
            return true;
        }
        if (valor < no.valor) {
            return contem(no.esquerdo, valor);
        }
        return contem(no.direito, valor);
    }

    /** Os valores dos nós visitados pela busca, da raiz até onde ela parou. */
    public List<Integer> caminho(int valor) {
        List<Integer> visitados = new ArrayList<>();
        No atual = raiz;
        while (atual != null) {
            visitados.add(atual.valor);
            if (valor == atual.valor) {
                break;
            }
            if (valor < atual.valor) {
                atual = atual.esquerdo;
            } else {
                atual = atual.direito;
            }
        }
        return visitados;
    }

    // ---------- percursos ----------

    public List<Integer> preOrdem() {
        List<Integer> valores = new ArrayList<>();
        preOrdem(raiz, valores);
        return valores;
    }

    private void preOrdem(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }
        valores.add(no.valor);             // 1. raiz
        preOrdem(no.esquerdo, valores);    // 2. esquerda
        preOrdem(no.direito, valores);     // 3. direita
    }

    public List<Integer> emOrdem() {
        List<Integer> valores = new ArrayList<>();
        emOrdem(raiz, valores);
        return valores;
    }

    private void emOrdem(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }
        emOrdem(no.esquerdo, valores);     // 1. esquerda
        valores.add(no.valor);             // 2. raiz
        emOrdem(no.direito, valores);      // 3. direita
    }

    // ---------- altura da árvore e desenho ----------

    public int altura() {
        return altura(raiz);
    }

    /** Desenha a árvore deitada, com a altura (h) e o fator (fb) de cada nó. */
    public String desenhar() {
        StringBuilder desenho = new StringBuilder();
        desenhar(raiz, 0, desenho);
        return desenho.toString();
    }

    private void desenhar(No no, int nivel, StringBuilder desenho) {
        if (no == null) {
            return;
        }
        desenhar(no.direito, nivel + 1, desenho);
        desenho.append("    ".repeat(nivel)).append(no.valor)
               .append(" (h=").append(no.altura)
               .append(", fb=").append(comSinal(fatorBalanceamento(no))).append(")\n");
        desenhar(no.esquerdo, nivel + 1, desenho);
    }

    private String comSinal(int numero) {
        if (numero > 0) {
            return "+" + numero;
        }
        return String.valueOf(numero);
    }
}

/**
 * Árvore binária de busca (BST) de números inteiros.
 *
 * <p>Invariante: em todo nó, os valores da subárvore esquerda são menores que
 * o valor do nó, e os da subárvore direita são maiores. Valores repetidos são
 * ignorados.</p>
 *
 * <p>Convenção de altura: uma folha tem altura 0, e a árvore vazia, -1.</p>
 */
class ArvoreBuscaBinaria {
    private No raiz;

    private static class No {
        int valor;
        No esquerdo;
        No direito;

        No(int valor) {
            this.valor = valor;
        }
    }

    // ---------- inserção ----------

    public void inserir(int valor) {
        raiz = inserir(raiz, valor);
    }

    private No inserir(No no, int valor) {
        if (no == null) {                  // achou o lugar vazio
            return new No(valor);
        }
        if (valor < no.valor) {
            no.esquerdo = inserir(no.esquerdo, valor);
        } else if (valor > no.valor) {
            no.direito = inserir(no.direito, valor);
        }                                  // igual: repetido, ignora
        return no;
    }

    // ---------- busca ----------

    public boolean contem(int valor) {
        return contem(raiz, valor);
    }

    private boolean contem(No no, int valor) {
        if (no == null) {                  // o caminho acabou: não está
            return false;
        }
        if (valor == no.valor) {
            return true;
        }
        if (valor < no.valor) {
            return contem(no.esquerdo, valor);
        }
        return contem(no.direito, valor);
    }

    /** Os valores dos nós visitados pela busca, da raiz até onde ela parou. */
    public List<Integer> caminho(int valor) {
        List<Integer> visitados = new ArrayList<>();
        No atual = raiz;
        while (atual != null) {
            visitados.add(atual.valor);
            if (valor == atual.valor) {
                break;
            }
            if (valor < atual.valor) {
                atual = atual.esquerdo;
            } else {
                atual = atual.direito;
            }
        }
        return visitados;
    }

    // ---------- percursos ----------

    public List<Integer> preOrdem() {
        List<Integer> valores = new ArrayList<>();
        preOrdem(raiz, valores);
        return valores;
    }

    private void preOrdem(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }
        valores.add(no.valor);             // 1. raiz
        preOrdem(no.esquerdo, valores);    // 2. esquerda
        preOrdem(no.direito, valores);     // 3. direita
    }

    public List<Integer> emOrdem() {
        List<Integer> valores = new ArrayList<>();
        emOrdem(raiz, valores);
        return valores;
    }

    private void emOrdem(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }
        emOrdem(no.esquerdo, valores);     // 1. esquerda
        valores.add(no.valor);             // 2. raiz
        emOrdem(no.direito, valores);      // 3. direita
    }

    public List<Integer> posOrdem() {
        List<Integer> valores = new ArrayList<>();
        posOrdem(raiz, valores);
        return valores;
    }

    private void posOrdem(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }
        posOrdem(no.esquerdo, valores);    // 1. esquerda
        posOrdem(no.direito, valores);     // 2. direita
        valores.add(no.valor);             // 3. raiz
    }

    // ---------- altura e desenho ----------

    public int altura() {
        return altura(raiz);
    }

    private int altura(No no) {
        if (no == null) {                  // árvore vazia
            return -1;
        }
        return 1 + Math.max(altura(no.esquerdo), altura(no.direito));
    }

    /** Desenha a árvore deitada: raiz à esquerda, maiores em cima. */
    public String desenhar() {
        StringBuilder desenho = new StringBuilder();
        desenhar(raiz, 0, desenho);
        return desenho.toString();
    }

    private void desenhar(No no, int nivel, StringBuilder desenho) {
        if (no == null) {
            return;
        }
        desenhar(no.direito, nivel + 1, desenho);
        desenho.append("    ".repeat(nivel)).append(no.valor).append('\n');
        desenhar(no.esquerdo, nivel + 1, desenho);
    }

    // ---------- remoção ----------

    public void remover(int valor) {
        raiz = remover(raiz, valor);
    }

    private No remover(No no, int valor) {
        if (no == null) {                  // o valor não está na árvore
            return null;
        }
        if (valor < no.valor) {
            no.esquerdo = remover(no.esquerdo, valor);
        } else if (valor > no.valor) {
            no.direito = remover(no.direito, valor);
        } else {
            if (no.esquerdo == null) {     // folha ou só filho direito
                return no.direito;
            }
            if (no.direito == null) {      // só filho esquerdo
                return no.esquerdo;
            }
            No sucessor = menor(no.direito);                    // dois filhos
            no.valor = sucessor.valor;
            no.direito = remover(no.direito, sucessor.valor);
        }
        return no;
    }

    private No menor(No no) {
        while (no.esquerdo != null) {
            no = no.esquerdo;
        }
        return no;
    }
}
