import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Aula 13 — Hands-on: REVISÃO — árvore binária, BST, AVL e rubro-negra no mesmo programa.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>na árvore binária comum cada nó é pendurado na mão: quem escolhe a posição é quem monta;</li>
 *   <li>na BST, a travessia em-ordem sai ordenada;</li>
 *   <li>os quatro casos de rotação da AVL e as rotações que cada um dispara;</li>
 *   <li>com os mesmos valores, compare a altura da BST, da AVL e da rubro-negra;</li>
 *   <li>quanto menor a altura, menos passos a busca gasta: 5 na BST contra 3 na AVL e na rubro-negra.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class ArvoresApp {
    public static void main(String[] args) {
        demonstrarArvoreBinaria();
        demonstrarBST();
        demonstrarCasosAVL();
        compararEstruturasBalanceadas();
        demonstrarTreeMap();
    }

    private static void demonstrarArvoreBinaria() {
        System.out.println("=== ARVORE BINARIA ===");
        ArvoreBinaria arvore = new ArvoreBinaria();
        arvore.definirRaiz(40);
        ArvoreBinaria.No n20 = arvore.adicionarEsquerdo(arvore.getRaiz(), 20);
        ArvoreBinaria.No n60 = arvore.adicionarDireito(arvore.getRaiz(), 60);
        arvore.adicionarEsquerdo(n20, 10);
        arvore.adicionarDireito(n20, 30);
        arvore.adicionarEsquerdo(n60, 50);
        arvore.adicionarDireito(n60, 70);

        System.out.println("Pre-ordem: " + arvore.preOrdem());
        System.out.println("Em-ordem: " + arvore.emOrdem());
        System.out.println("Pos-ordem: " + arvore.posOrdem());
        System.out.println("Altura: " + arvore.altura());
        System.out.println("Quantidade de nos: " + arvore.contarNos());
        System.out.println();
    }

    private static void demonstrarBST() {
        System.out.println("=== BST ===");
        ArvoreBuscaBinaria bst = new ArvoreBuscaBinaria();
        int[] valores = {40, 20, 60, 10, 30, 50, 70};
        for (int valor : valores) {
            bst.inserir(valor);
        }

        System.out.println("Em-ordem: " + bst.emOrdem());
        System.out.println("Pre-ordem: " + bst.preOrdem());
        System.out.println("Pos-ordem: " + bst.posOrdem());
        System.out.println("Altura: " + bst.altura());
        System.out.println("Buscar 50: " + bst.contem(50) + ", passos = " + bst.passosBusca(50));
        System.out.println("Buscar 99: " + bst.contem(99) + ", passos = " + bst.passosBusca(99));
        bst.remover(20);
        System.out.println("Apos remover 20: " + bst.emOrdem());
        System.out.println();
    }

    private static void demonstrarCasosAVL() {
        System.out.println("=== AVL - CASOS CLASSICOS ===");
        demonstrarCasoAVL("LL", 30, 20, 10);
        demonstrarCasoAVL("RR", 10, 20, 30);
        demonstrarCasoAVL("LR", 30, 10, 20);
        demonstrarCasoAVL("RL", 10, 30, 20);
        demonstrarCasoAVL("AULA", 100, 50, 150, 300, 80, 58, 53);
        System.out.println();
    }

    private static void demonstrarCasoAVL(String nome, int... valores) {
        ArvoreAVL avl = new ArvoreAVL();
        for (int valor : valores) {
            avl.inserir(valor);
        }

        System.out.println("Caso " + nome + " com " + Arrays.toString(valores));
        System.out.println("Rotacoes: " + avl.getRotacoes());
        System.out.println("Pre-ordem: " + avl.preOrdem());
        System.out.println("Em-ordem: " + avl.emOrdem());
        System.out.println("Altura: " + avl.altura());
    }

    private static void compararEstruturasBalanceadas() {
        System.out.println();
        System.out.println("=== COMPARACAO BST, AVL E RUBRO-NEGRA ===");
        List<Integer> valores = Arrays.asList(50, 40, 30, 20, 10, 60, 70, 80);

        ArvoreBuscaBinaria bst = new ArvoreBuscaBinaria();
        ArvoreAVL avl = new ArvoreAVL();
        ArvoreRubroNegra rb = new ArvoreRubroNegra();

        for (int valor : valores) {
            bst.inserir(valor);
            avl.inserir(valor);
            rb.inserir(valor);
        }

        System.out.println("BST em-ordem: " + bst.emOrdem());
        System.out.println("BST altura: " + bst.altura());
        System.out.println("BST passos busca 10: " + bst.passosBusca(10));
        System.out.println();

        System.out.println("AVL em-ordem: " + avl.emOrdem());
        System.out.println("AVL altura: " + avl.altura());
        System.out.println("AVL passos busca 10: " + avl.passosBusca(10));
        System.out.println("AVL rotacoes: " + avl.getRotacoes());
        System.out.println();

        System.out.println("Rubro-negra em-ordem: " + rb.emOrdem());
        System.out.println("Rubro-negra altura: " + rb.altura());
        System.out.println("Rubro-negra passos busca 10: " + rb.passosBusca(10));
        System.out.println("Rubro-negra valida: " + rb.propriedadesValidas());
        System.out.println();
    }

    private static void demonstrarTreeMap() {
        System.out.println("=== TREEMAP ===");
        TreeMap<Integer, String> mapa = new TreeMap<>();
        mapa.put(50, "cinquenta");
        mapa.put(40, "quarenta");
        mapa.put(30, "trinta");
        mapa.put(20, "vinte");
        mapa.put(10, "dez");
        mapa.put(60, "sessenta");

        System.out.println("Chaves ordenadas: " + mapa.keySet());
        System.out.println("Primeira chave: " + mapa.firstKey());
        System.out.println("Ultima chave: " + mapa.lastKey());
        System.out.println("Contem 30? " + mapa.containsKey(30));
    }
}

class ArvoreBinaria {
    private No raiz;

    static final class No {
        int valor;
        No esquerdo;
        No direito;

        No(int valor) {
            this.valor = valor;
        }
    }

    public void definirRaiz(int valor) {
        raiz = new No(valor);
    }

    public No getRaiz() {
        return raiz;
    }

    public No adicionarEsquerdo(No pai, int valor) {
        pai.esquerdo = new No(valor);
        return pai.esquerdo;
    }

    public No adicionarDireito(No pai, int valor) {
        pai.direito = new No(valor);
        return pai.direito;
    }

    public int contarNos() {
        return contarNos(raiz);
    }

    private int contarNos(No no) {
        if (no == null) {
            return 0;
        }
        return 1 + contarNos(no.esquerdo) + contarNos(no.direito);
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
}

class ArvoreBuscaBinaria {
    private No raiz;

    private static final class No {
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

    public int altura() {
        return altura(raiz);
    }

    private int altura(No no) {
        if (no == null) {
            return -1;
        }
        return 1 + Math.max(altura(no.esquerdo), altura(no.direito));
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
}

class ArvoreAVL {
    private No raiz;
    private final List<String> rotacoes = new ArrayList<>();

    private static final class No {
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

    public List<String> getRotacoes() {
        return new ArrayList<>(rotacoes);
    }
}

class ArvoreRubroNegra {
    private static final boolean VERMELHO = true;
    private static final boolean PRETO = false;

    private No raiz;

    private static final class No {
        int valor;
        boolean cor;
        No esquerdo;
        No direito;
        No pai;

        No(int valor, boolean cor, No pai) {
            this.valor = valor;
            this.cor = cor;
            this.pai = pai;
        }
    }

    public void inserir(int valor) {
        if (raiz == null) {
            raiz = new No(valor, PRETO, null);
            return;
        }

        No atual = raiz;
        No pai = null;
        while (atual != null) {
            pai = atual;
            if (valor < atual.valor) {
                atual = atual.esquerdo;
            } else if (valor > atual.valor) {
                atual = atual.direito;
            } else {
                return;
            }
        }

        No novo = new No(valor, VERMELHO, pai);
        if (valor < pai.valor) {
            pai.esquerdo = novo;
        } else {
            pai.direito = novo;
        }

        corrigirInsercao(novo);
    }

    private void corrigirInsercao(No no) {
        while (no != raiz && cor(no.pai) == VERMELHO) {
            if (no.pai == avo(no).esquerdo) {
                No tio = avo(no).direito;
                if (cor(tio) == VERMELHO) {
                    no.pai.cor = PRETO;
                    tio.cor = PRETO;
                    avo(no).cor = VERMELHO;
                    no = avo(no);
                } else {
                    if (no == no.pai.direito) {
                        no = no.pai;
                        rotacaoEsquerda(no);
                    }
                    no.pai.cor = PRETO;
                    avo(no).cor = VERMELHO;
                    rotacaoDireita(avo(no));
                }
            } else {
                No tio = avo(no).esquerdo;
                if (cor(tio) == VERMELHO) {
                    no.pai.cor = PRETO;
                    tio.cor = PRETO;
                    avo(no).cor = VERMELHO;
                    no = avo(no);
                } else {
                    if (no == no.pai.esquerdo) {
                        no = no.pai;
                        rotacaoDireita(no);
                    }
                    no.pai.cor = PRETO;
                    avo(no).cor = VERMELHO;
                    rotacaoEsquerda(avo(no));
                }
            }
        }
        raiz.cor = PRETO;
    }

    private void rotacaoEsquerda(No x) {
        No y = x.direito;
        x.direito = y.esquerdo;
        if (y.esquerdo != null) {
            y.esquerdo.pai = x;
        }
        y.pai = x.pai;
        if (x.pai == null) {
            raiz = y;
        } else if (x == x.pai.esquerdo) {
            x.pai.esquerdo = y;
        } else {
            x.pai.direito = y;
        }
        y.esquerdo = x;
        x.pai = y;
    }

    private void rotacaoDireita(No y) {
        No x = y.esquerdo;
        y.esquerdo = x.direito;
        if (x.direito != null) {
            x.direito.pai = y;
        }
        x.pai = y.pai;
        if (y.pai == null) {
            raiz = x;
        } else if (y == y.pai.esquerdo) {
            y.pai.esquerdo = x;
        } else {
            y.pai.direito = x;
        }
        x.direito = y;
        y.pai = x;
    }

    private No avo(No no) {
        return no != null && no.pai != null ? no.pai.pai : null;
    }

    private boolean cor(No no) {
        return no != null && no.cor == VERMELHO;
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

    private int altura(No no) {
        if (no == null) {
            return -1;
        }
        return 1 + Math.max(altura(no.esquerdo), altura(no.direito));
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

    public boolean propriedadesValidas() {
        return raiz == null
            || (!cor(raiz) && semVermelhoConsecutivo(raiz) && alturaPreta(raiz) != -1);
    }

    private boolean semVermelhoConsecutivo(No no) {
        if (no == null) {
            return true;
        }
        if (cor(no) && (cor(no.esquerdo) || cor(no.direito))) {
            return false;
        }
        return semVermelhoConsecutivo(no.esquerdo) && semVermelhoConsecutivo(no.direito);
    }

    private int alturaPreta(No no) {
        if (no == null) {
            return 1;
        }
        int esquerda = alturaPreta(no.esquerdo);
        int direita = alturaPreta(no.direito);
        if (esquerda == -1 || direita == -1 || esquerda != direita) {
            return -1;
        }
        return esquerda + (no.cor == PRETO ? 1 : 0);
    }
}
