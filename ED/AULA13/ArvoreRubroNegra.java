import java.util.ArrayList;
import java.util.List;

public class ArvoreRubroNegra {
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
