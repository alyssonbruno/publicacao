/**
 * Hands-on 2 — Lista encadeada simples de cidades do Tocantins.
 *
 * <p>Demonstra inserção no início, inserção no fim, busca,
 * remoção e percurso de uma lista encadeada simples genérica.</p>
 *
 * <p>No JDoodle, cole este arquivo inteiro e selecione Execute.</p>
 *
 * @author Alysson M. Bruno
 * @version 1.0
 */
public class ListaApp {

    // ----------------------------------------------------------
    // Nó da lista encadeada simples
    // ----------------------------------------------------------
    static class No<T> {
        T dado;
        No<T> proximo;

        No(T dado) {
            this.dado = dado;
        }
    }

    // ----------------------------------------------------------
    // Lista encadeada simples genérica
    // ----------------------------------------------------------
    static class ListaEncadeada<T> {
        No<T> cabeca;
        No<T> cauda;
        int tamanho;

        /** Insere no início — O(1). */
        void inserirNoInicio(T dado) {
            No<T> novo   = new No<>(dado);
            novo.proximo = cabeca;
            cabeca       = novo;
            if (cauda == null) cauda = novo;
            tamanho++;
        }

        /** Insere no fim via referência à cauda — O(1). */
        void inserirNoFim(T dado) {
            No<T> novo = new No<>(dado);
            if (cauda == null) {
                cabeca = cauda = novo;
            } else {
                cauda.proximo = novo;
                cauda         = novo;
            }
            tamanho++;
        }

        /** Remove a primeira ocorrência de {@code dado} — O(n). */
        boolean remover(T dado) {
            if (cabeca == null) return false;
            if (cabeca.dado.equals(dado)) {
                cabeca = cabeca.proximo;
                if (cabeca == null) cauda = null;
                tamanho--;
                return true;
            }
            No<T> ant = cabeca;
            while (ant.proximo != null) {
                if (ant.proximo.dado.equals(dado)) {
                    if (ant.proximo == cauda) cauda = ant;
                    ant.proximo = ant.proximo.proximo;
                    tamanho--;
                    return true;
                }
                ant = ant.proximo;
            }
            return false;
        }

        /** Verifica se {@code dado} está na lista — O(n). */
        boolean contem(T dado) {
            No<T> atual = cabeca;
            while (atual != null) {
                if (atual.dado.equals(dado)) return true;
                atual = atual.proximo;
            }
            return false;
        }

        /** Imprime todos os elementos no formato: cabeca → [x] → ... → null. */
        void imprimir() {
            No<T> atual = cabeca;
            System.out.print("cabeca → ");
            while (atual != null) {
                System.out.print("[" + atual.dado + "] → ");
                atual = atual.proximo;
            }
            System.out.println("null  (tamanho=" + tamanho + ")");
        }
    }

    // ----------------------------------------------------------
    // Demonstração
    // ----------------------------------------------------------
    public static void main(String[] args) {
        ListaEncadeada<String> lista = new ListaEncadeada<>();

        System.out.println("=== Lista Encadeada Simples — Cidades do Tocantins ===\n");

        // Inserções no fim
        lista.inserirNoFim("Palmas");
        lista.inserirNoFim("Gurupi");
        lista.inserirNoFim("Araguaína");
        System.out.print("Após inserir 3 cidades no fim:          ");
        lista.imprimir();

        // Inserção no início
        lista.inserirNoInicio("Porto Nacional");
        System.out.print("Após inserir 'Porto Nacional' no início: ");
        lista.imprimir();

        // Busca
        System.out.println("Contém 'Gurupi'?      " + lista.contem("Gurupi"));
        System.out.println("Contém 'Miracema'?    " + lista.contem("Miracema"));

        // Remoção
        lista.remover("Gurupi");
        System.out.print("Após remover 'Gurupi':                  ");
        lista.imprimir();

        // Inserção adicional para demonstrar cauda
        lista.inserirNoFim("Paraíso do Tocantins");
        System.out.print("Após inserir 'Paraíso do Tocantins':    ");
        lista.imprimir();
    }
}
