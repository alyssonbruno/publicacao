import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Aula 11 — Laboratório: ÁRVORE BINÁRIA DE BUSCA (BST).
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>Como ler o desenho: a raiz fica encostada na margem esquerda; cada nível
 * a mais anda quatro espaços para a direita; os maiores ficam acima do pai e
 * os menores, abaixo. Incline a cabeça para a esquerda e a árvore fica de pé.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>o percurso em ordem devolve os valores já ordenados;</li>
 *   <li>a busca desce por um caminho só: 3 nós visitados para achar o 60 e
 *       3 para descobrir que o 90 não está, numa árvore de 7 nós;</li>
 *   <li>ao remover o 50, que tem dois filhos, o sucessor 60 passa a ser a raiz;</li>
 *   <li>os mesmos cinco valores dão altura 2 numa ordem e altura 4 na outra:
 *       inseridos em ordem crescente, viram uma lista, e a busca do 50 passa
 *       de 2 para 5 nós visitados.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 2.0
 */
public class ArvoreBuscaBinariaApp {
    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        // 1. A árvore da aula
        int[] valores = {50, 30, 70, 20, 40, 60, 80};
        ArvoreBuscaBinaria arvore = montar(valores);
        out.println("=== 1. Árvore da aula, inserindo " + Arrays.toString(valores));
        out.print(arvore.desenhar());
        out.println("Pré-ordem: " + arvore.preOrdem());
        out.println("Em ordem:  " + arvore.emOrdem());
        out.println("Pós-ordem: " + arvore.posOrdem());
        out.println("Altura:    " + arvore.altura());

        // 2. A busca desce por um caminho só
        out.println();
        out.println("=== 2. Busca");
        mostrarBusca(out, arvore, 60);
        mostrarBusca(out, arvore, 90);

        // 3. Os três casos da remoção
        out.println();
        out.println("=== 3. Remoção");
        mostrarRemocao(out, arvore, 20);   // folha
        mostrarRemocao(out, arvore, 30);   // um filho: o 40
        mostrarRemocao(out, arvore, 50);   // dois filhos: o sucessor 60 sobe

        // 4. Os mesmos valores, em ordens diferentes
        out.println("=== 4. Mesmos valores, ordens diferentes");
        mostrarOrdem(out, new int[] {30, 10, 50, 20, 40});
        mostrarOrdem(out, new int[] {10, 20, 30, 40, 50});
    }

    private static ArvoreBuscaBinaria montar(int[] valores) {
        ArvoreBuscaBinaria arvore = new ArvoreBuscaBinaria();
        for (int valor : valores) {
            arvore.inserir(valor);
        }
        return arvore;
    }

    private static void mostrarBusca(PrintStream out, ArvoreBuscaBinaria arvore, int valor) {
        List<Integer> caminho = arvore.caminho(valor);
        String resultado = arvore.contem(valor) ? "achou" : "não achou";
        out.println("Busca do " + valor + ": " + resultado
                + " | visitou " + caminho + " = " + caminho.size() + " nós");
    }

    private static void mostrarRemocao(PrintStream out, ArvoreBuscaBinaria arvore, int valor) {
        arvore.remover(valor);
        out.println("Depois de remover o " + valor + ":");
        out.print(arvore.desenhar());
        out.println("Em ordem: " + arvore.emOrdem());
        out.println();
    }

    private static void mostrarOrdem(PrintStream out, int[] valores) {
        ArvoreBuscaBinaria arvore = montar(valores);
        out.println("Inserindo " + Arrays.toString(valores) + ":");
        out.print(arvore.desenhar());
        out.println("Em ordem: " + arvore.emOrdem() + " | altura " + arvore.altura());
        mostrarBusca(out, arvore, 50);
        out.println();
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
