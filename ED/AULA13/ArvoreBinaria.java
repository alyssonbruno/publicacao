import java.util.ArrayList;
import java.util.List;

public class ArvoreBinaria {
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
