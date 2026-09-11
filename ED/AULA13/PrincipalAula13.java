import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class PrincipalAula13 {
    public static void main(String[] args) {
        //demonstrarArvoreBinaria();
        // demonstrarBST();
        demonstrarCasosAVL();
        // compararEstruturasBalanceadas();
        // demonstrarTreeMap();
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
        // demonstrarCasoAVL("LL", 30, 20, 10);
        // demonstrarCasoAVL("RR", 10, 20, 30);
        // demonstrarCasoAVL("LR", 30, 10, 20);
        // demonstrarCasoAVL("RL", 10, 30, 20);
        demonstrarCasoAVL("AULA", 100,50,150,300,80,58,53);
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
