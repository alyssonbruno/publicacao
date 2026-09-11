import java.util.ArrayList;
import java.util.List;

public class ArvoreAVL {
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
