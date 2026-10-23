import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/**
 * Aula 13 — Laboratório: QUANTO CUSTA ACHAR UMA CHAVE? Lista, BST, AVL e HashMap.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O programa guarda as chaves 1, 2, ..., n em quatro estruturas — uma lista
 * (ArrayList), a BST simples da Aula 11, a AVL da Aula 12 e um HashMap — e mede
 * o custo de achar uma chave de dois jeitos: contando os passos de cada busca
 * e cronometrando 10.000 buscas com o System.nanoTime.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>com as chaves em ordem crescente, a lista e a BST simples gastam os
 *       mesmos passos — em média (n + 1) / 2, no pior caso n —, porque a BST
 *       virou uma lista (altura n - 1);</li>
 *   <li>a AVL gasta em média cerca de log2(n) - 1 passos (8,0 para n = 500),
 *       a altura dela fica abaixo de log2(n) e, quando o n dobra, ela ganha só
 *       um passo;</li>
 *   <li>no tempo, quando o n dobra, as colunas da lista e da BST mais ou menos
 *       dobram, a da AVL quase não muda e a do HashMap fica parada; a BST é
 *       mais lenta que a lista, embora as duas sejam O(n): o O(...) esconde as
 *       constantes;</li>
 *   <li>com "embaralhar = true", a BST simples fica baixa (altura 22 para
 *       n = 500, contra 499 em ordem) e passa a gastar poucos passos: a ordem
 *       de chegada é o que estraga a BST;</li>
 *   <li>"estourou" na coluna da BST quer dizer StackOverflowError: a inserção
 *       recursiva numa BST que virou lista desce n chamadas (Aula 03).</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class ExperimentoBuscaApp {

    // ===== Sua vez: mude só estas duas linhas =====
    static int[] tamanhos = {500, 1_000, 2_000, 4_000};   // o n dobra a cada linha
    static boolean embaralhar = false;                     // true: chaves fora de ordem
    // ===============================================

    static final int BUSCAS = 10_000;                      // buscas cronometradas
    static final Locale BR = Locale.forLanguageTag("pt-BR");

    // As quatro estruturas, sempre com as mesmas chaves 1, 2, ..., n
    static List<Integer> lista;
    static ArvoreBuscaBinaria bst;                         // fica null se a pilha estourar
    static ArvoreAVL avl;
    static Map<Integer, String> mapa;

    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("Lista x BST x AVL x HashMap, com as chaves 1 a n inseridas "
                + (embaralhar ? "FORA DE ORDEM" : "EM ORDEM CRESCENTE"));

        aquecer();

        StringBuilder passos = new StringBuilder();
        StringBuilder alturas = new StringBuilder();
        StringBuilder tempos = new StringBuilder();
        for (int n : tamanhos) {
            montar(n);

            // 1. Passos: busca cada chave de 1 a n e guarda a média e o pior caso
            long[] naLista = contarPassos(n, chave -> posicoesOlhadas(chave));
            long[] naBst = bst == null ? null : contarPassos(n, chave -> bst.caminho(chave).size());
            long[] naAvl = contarPassos(n, chave -> avl.caminho(chave).size());
            passos.append(String.format(BR, "%-6d %14s %14s %10s%n", n,
                    mediaEPior(naLista, n), mediaEPior(naBst, n), mediaEPior(naAvl, n)));

            // 2. Alturas: na BST, a altura é o pior caminho menos 1
            alturas.append(String.format(BR, "%-6d %10s %10d %10.1f%n", n,
                    naBst == null ? "estourou" : String.valueOf(naBst[1] - 1),
                    avl.altura(), Math.log(n) / Math.log(2)));

            // 3. Tempo de BUSCAS buscas sorteadas, igual para as quatro estruturas
            int[] sorteadas = sortear(n);
            tempos.append(String.format(BR, "%-6d %10.3f %10s %10.3f %10.3f%n", n,
                    medir(chave -> lista.contains(chave), sorteadas),
                    bst == null ? "estourou"
                            : String.format(BR, "%.3f", medir(chave -> bst.contem(chave), sorteadas)),
                    medir(chave -> avl.contem(chave), sorteadas),
                    medir(chave -> mapa.containsKey(chave), sorteadas)));
        }

        out.println();
        out.println("=== 1. Passos para achar uma chave: média (pior caso)");
        out.println("lista: posições olhadas; árvores: nós visitados");
        out.printf("%-6s %14s %14s %10s%n", "n", "lista", "BST", "AVL");
        out.print(passos);

        out.println();
        out.println("=== 2. Altura das árvores");
        out.printf("%-6s %10s %10s %10s%n", "n", "BST", "AVL", "log2(n)");
        out.print(alturas);

        out.println();
        out.println("=== 3. Tempo de " + String.format(BR, "%,d", BUSCAS)
                + " buscas, em milissegundos");
        out.printf("%-6s %10s %10s %10s %10s%n", "n", "lista", "BST", "AVL", "HashMap");
        out.print(tempos);
        out.println();
        out.println("Os tempos mudam a cada execução: compare como cada coluna cresce");
        out.println("quando o n dobra, e não os números exatos.");
    }

    /** Monta as quatro estruturas com as chaves 1, 2, ..., n. */
    static void montar(int n) {
        int[] chaves = new int[n];
        for (int i = 0; i < n; i++) {
            chaves[i] = i + 1;
        }
        if (embaralhar) {
            misturar(chaves);
        }
        lista = new ArrayList<>();
        avl = new ArvoreAVL();
        mapa = new HashMap<>();
        for (int chave : chaves) {
            lista.add(chave);
            avl.inserir(chave);
            mapa.put(chave, "valor " + chave);
        }
        bst = new ArvoreBuscaBinaria();
        try {
            for (int chave : chaves) {
                bst.inserir(chave);
            }
        } catch (StackOverflowError erro) {
            bst = null;   // recursão funda demais (Aula 03): a BST em ordem virou lista
        }
    }

    /** Busca sequencial (Aula 05): olha uma posição de cada vez. Devolve quantas olhou. */
    static int posicoesOlhadas(int chave) {
        int olhadas = 0;
        for (int valor : lista) {
            olhadas++;
            if (valor == chave) {
                return olhadas;
            }
        }
        return olhadas;                                    // não achou: olhou todas
    }

    /** Busca cada chave de 1 a n; devolve {soma dos passos, pior caso}. */
    static long[] contarPassos(int n, IntUnaryOperator buscar) {
        long soma = 0;
        long pior = 0;
        for (int chave = 1; chave <= n; chave++) {
            int passos = buscar.applyAsInt(chave);
            soma += passos;
            pior = Math.max(pior, passos);
        }
        return new long[] {soma, pior};
    }

    static String mediaEPior(long[] resultado, int n) {
        if (resultado == null) {
            return "estourou";
        }
        return String.format(BR, "%.1f (%d)", (double) resultado[0] / n, resultado[1]);
    }

    /** Tempo, em milissegundos, de todas as buscas. Repete 3 vezes e fica com o menor. */
    static double medir(IntPredicate busca, int[] chaves) {
        long melhor = Long.MAX_VALUE;
        for (int vez = 0; vez < 3; vez++) {
            int achadas = 0;
            long inicio = System.nanoTime();
            for (int chave : chaves) {
                if (busca.test(chave)) {
                    achadas++;
                }
            }
            long tempo = System.nanoTime() - inicio;
            if (achadas != chaves.length) {
                throw new IllegalStateException("alguma busca não achou a chave");
            }
            melhor = Math.min(melhor, tempo);
        }
        return melhor / 1_000_000.0;
    }

    /** BUSCAS chaves sorteadas entre 1 e n. A semente fixa repete o sorteio. */
    static int[] sortear(int n) {
        return new Random(42).ints(BUSCAS, 1, n + 1).toArray();
    }

    /** Mistura o vetor (Fisher-Yates), sempre do mesmo jeito. */
    static void misturar(int[] v) {
        Random sorteio = new Random(7);
        for (int i = v.length - 1; i > 0; i--) {
            int j = sorteio.nextInt(i + 1);
            int troca = v[i];
            v[i] = v[j];
            v[j] = troca;
        }
    }

    /** Aquecimento da JVM (Aula 07): mede uma vez sem mostrar, para as linhas saírem justas. */
    static void aquecer() {
        montar(1_000);
        int[] sorteadas = sortear(1_000);
        for (int vez = 0; vez < 3; vez++) {
            medir(chave -> lista.contains(chave), sorteadas);
            medir(chave -> avl.contem(chave), sorteadas);
            medir(chave -> mapa.containsKey(chave), sorteadas);
            if (bst != null) {
                medir(chave -> bst.contem(chave), sorteadas);
            }
        }
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
