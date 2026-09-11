import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Aula 12 — Hands-on: ÁRVORE AVL — os quatro casos de rotação.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>os casos LL e RR resolvem-se com uma única rotação;</li>
 *   <li>os casos LR e RL precisam de duas rotações;</li>
 *   <li>com valores em ordem decrescente, a BST simples degenera e a AVL não;</li>
 *   <li>com os mesmos oito valores, a BST fica com altura 4 e a AVL com altura 3;</li>
 *   <li>o TreeMap do Java é uma árvore balanceada pronta para uso.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class ArvoreAVLApp {
    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        demonstrarCasosBasicos(out);
        compararComBST(out);
        demonstrarTreeMap(out);
    }

    private static void demonstrarCasosBasicos(PrintStream out) {
        executarCaso(out, "LL", new int[]{30, 20, 10});
        executarCaso(out, "RR", new int[]{10, 20, 30});
        executarCaso(out, "LR", new int[]{30, 10, 20});
        executarCaso(out, "RL", new int[]{10, 30, 20});
    }

    private static void executarCaso(PrintStream out, String nome, int[] valores) {
        ArvoreAVL arvore = new ArvoreAVL();
        for (int valor : valores) {
            arvore.inserir(valor);
        }

        out.println("Caso " + nome + ":");
        out.println("Rotações: " + arvore.getRotacoes());
        out.println("Pré-ordem: " + arvore.preOrdem());
        out.println("Em-ordem: " + arvore.emOrdem());
        out.println("Altura: " + arvore.altura());
        out.println();
    }

    private static void compararComBST(PrintStream out) {
        int[] valores = {50, 40, 30, 20, 10, 60, 70, 80};

        ArvoreBuscaBinariaSimples bst = new ArvoreBuscaBinariaSimples();
        ArvoreAVL avl = new ArvoreAVL();

        for (int valor : valores) {
            bst.inserir(valor);
            avl.inserir(valor);
        }

        out.println("Comparação BST simples x AVL:");
        out.println("BST em-ordem: " + bst.emOrdem());
        out.println("AVL em-ordem: " + avl.emOrdem());
        out.println("Altura BST: " + bst.altura());
        out.println("Altura AVL: " + avl.altura());
        out.println("Passos para buscar 80 na BST: " + bst.passosBusca(80));
        out.println("Passos para buscar 80 na AVL: " + avl.passosBusca(80));
        out.println();
    }

    private static void demonstrarTreeMap(PrintStream out) {
        TreeMap<Integer, String> mapa = new TreeMap<>();
        int[] chaves = {10, 20, 30, 40, 50, 60};

        for (int chave : chaves) {
            mapa.put(chave, "valor-" + chave);
        }

        out.println("TreeMap em uso:");
        out.println("Chaves ordenadas: " + mapa.keySet());
        out.println("containsKey(40): " + mapa.containsKey(40));
        out.println("get(50): " + mapa.get(50));
        out.println("firstKey(): " + mapa.firstKey());
        out.println("lastKey(): " + mapa.lastKey());
    }
}

class ArvoreAVL {
    private No raiz;
    private final List<String> rotacoes = new ArrayList<>();

    private static class No {
        int valor;
        int altura;
        No esquerdo;
        No direito;

        No(int valor) {
            this.valor = valor;
            this.altura = 0;
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
        } else {
            return no;
        }

        atualizarAltura(no);
        int fb = fatorBalanceamento(no);

        if (fb > 1 && valor < no.esquerdo.valor) {
            return rotacaoDireita(no);
        }
        if (fb < -1 && valor > no.direito.valor) {
            return rotacaoEsquerda(no);
        }
        if (fb > 1 && valor > no.esquerdo.valor) {
            no.esquerdo = rotacaoEsquerda(no.esquerdo);
            return rotacaoDireita(no);
        }
        if (fb < -1 && valor < no.direito.valor) {
            no.direito = rotacaoDireita(no.direito);
            return rotacaoEsquerda(no);
        }

        return no;
    }

    private No rotacaoDireita(No y) {
        rotacoes.add("direita(" + y.valor + ")");
        No x = y.esquerdo;
        No t2 = x.direito;

        x.direito = y;
        y.esquerdo = t2;

        atualizarAltura(y);
        atualizarAltura(x);
        return x;
    }

    private No rotacaoEsquerda(No x) {
        rotacoes.add("esquerda(" + x.valor + ")");
        No y = x.direito;
        No t2 = y.esquerdo;

        y.esquerdo = x;
        x.direito = t2;

        atualizarAltura(x);
        atualizarAltura(y);
        return y;
    }

    private int altura(No no) {
        return no == null ? -1 : no.altura;
    }

    private void atualizarAltura(No no) {
        no.altura = 1 + Math.max(altura(no.esquerdo), altura(no.direito));
    }

    private int fatorBalanceamento(No no) {
        return altura(no.esquerdo) - altura(no.direito);
    }

    public boolean contem(int valor) {
        No atual = raiz;
        while (atual != null) {
            if (valor == atual.valor) {
                return true;
            }
            atual = valor < atual.valor ? atual.esquerdo : atual.direito;
        }
        return false;
    }

    public int passosBusca(int valor) {
        int passos = 0;
        No atual = raiz;
        while (atual != null) {
            passos++;
            if (valor == atual.valor) {
                return passos;
            }
            atual = valor < atual.valor ? atual.esquerdo : atual.direito;
        }
        return passos;
    }

    public int altura() {
        return altura(raiz);
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

    public List<String> getRotacoes() {
        return new ArrayList<>(rotacoes);
    }
}

class ArvoreBuscaBinariaSimples {
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

    public int altura() {
        return altura(raiz);
    }

    private int altura(No no) {
        if (no == null) {
            return -1;
        }
        return 1 + Math.max(altura(no.esquerdo), altura(no.direito));
    }

    public int passosBusca(int valor) {
        int passos = 0;
        No atual = raiz;
        while (atual != null) {
            passos++;
            if (valor == atual.valor) {
                return passos;
            }
            atual = valor < atual.valor ? atual.esquerdo : atual.direito;
        }
        return passos;
    }
}
