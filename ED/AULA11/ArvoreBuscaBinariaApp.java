import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Aula 11 — Hands-on: ÁRVORE BINÁRIA DE BUSCA (BST).
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>as três travessias (pré-ordem, em-ordem, pós-ordem) sobre a mesma árvore;</li>
 *   <li>a travessia em-ordem devolve os valores já ordenados;</li>
 *   <li>a remoção de um nó com dois filhos troca o valor pelo sucessor;</li>
 *   <li>a árvore degenerada tem altura igual ao número de nós menos um — vira uma lista.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class ArvoreBuscaBinariaApp {
    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        ArvoreBuscaBinaria arvore = new ArvoreBuscaBinaria();
        int[] valores = {50, 30, 70, 20, 40, 60, 80};

        for (int valor : valores) {
            arvore.inserir(valor);
        }

        out.println("Pré-ordem: " + arvore.preOrdem());
        out.println("Em-ordem: " + arvore.emOrdem());
        out.println("Pós-ordem: " + arvore.posOrdem());
        out.println("Altura: " + arvore.altura());
        out.println("Contém 60? " + arvore.contem(60));
        out.println("Contém 90? " + arvore.contem(90));

        out.println("\nRemoções:");
        imprimirAposRemover(out, arvore, 20);
        imprimirAposRemover(out, arvore, 30);
        imprimirAposRemover(out, arvore, 50);

        ArvoreBuscaBinaria degenerada = new ArvoreBuscaBinaria();
        int[] ordenados = {10, 20, 30, 40, 50};
        for (int valor : ordenados) {
            degenerada.inserir(valor);
        }
        out.println("\nÁrvore degenerada em-ordem: " + degenerada.emOrdem());
        out.println("Altura da árvore degenerada: " + degenerada.altura());
    }

    private static void imprimirAposRemover(PrintStream out, ArvoreBuscaBinaria arvore, int valor) {
        arvore.remover(valor);
        out.println("Após remover " + valor + ": " + arvore.emOrdem());
    }
}

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

    public void inserir(int valor) {
        raiz = inserir(raiz, valor);
    }

    private No inserir(No no, int valor) {
        if (no == null) {
            return new No(valor);
        }
        if (valor < no.valor) {
            no.esquerdo = inserir(no.esquerdo, valor);
        } else if (valor > no.valor) {
            no.direito = inserir(no.direito, valor);
        }
        return no;
    }

    public boolean contem(int valor) {
        return contem(raiz, valor);
    }

    private boolean contem(No no, int valor) {
        if (no == null) {
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

    public void remover(int valor) {
        raiz = remover(raiz, valor);
    }

    private No remover(No no, int valor) {
        if (no == null) {
            return null;
        }
        if (valor < no.valor) {
            no.esquerdo = remover(no.esquerdo, valor);
        } else if (valor > no.valor) {
            no.direito = remover(no.direito, valor);
        } else {
            if (no.esquerdo == null) {
                return no.direito;
            }
            if (no.direito == null) {
                return no.esquerdo;
            }
            No sucessor = menor(no.direito);
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

    public List<Integer> emOrdem() {
        List<Integer> valores = new ArrayList<>();
        emOrdem(raiz, valores);
        return valores;
    }

    private void emOrdem(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }
        emOrdem(no.esquerdo, valores);
        valores.add(no.valor);
        emOrdem(no.direito, valores);
    }

    public List<Integer> preOrdem() {
        List<Integer> valores = new ArrayList<>();
        preOrdem(raiz, valores);
        return valores;
    }

    private void preOrdem(No no, List<Integer> valores) {
        if (no == null) {
            return;
        }
        valores.add(no.valor);
        preOrdem(no.esquerdo, valores);
        preOrdem(no.direito, valores);
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
        posOrdem(no.esquerdo, valores);
        posOrdem(no.direito, valores);
        valores.add(no.valor);
    }

    public int altura() {
        return altura(raiz);
    }

    private int altura(No no) {
        if (no == null) {
            return -1;
        }
        return 1 + Math.max(altura(no.esquerdo), altura(no.direito));
    }
}
