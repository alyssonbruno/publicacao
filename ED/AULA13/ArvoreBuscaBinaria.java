import java.util.ArrayList;
import java.util.List;

public class ArvoreBuscaBinaria {
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
