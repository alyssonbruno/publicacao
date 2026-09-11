/**
 * Questão Prática 1 — Vetor dinâmico com log de redimensionamento.
 *
 * <p>Demonstra a estratégia de dobrar a capacidade e o custo amortizado
 * de inserções em um vetor dinâmico. Imprime mensagem a cada
 * redimensionamento e após cada inserção.</p>
 *
 * <p>No JDoodle, cole este arquivo inteiro e selecione Execute.</p>
 *
 * @author Alysson M. Bruno
 * @version 1.0
 */
public class VetorComLog {

    // ----------------------------------------------------------
    // Vetor dinâmico com log
    // ----------------------------------------------------------
    static class VetorDinamico<T> {
        private Object[] dados;
        private int tamanho   = 0;
        private int capacidade;

        VetorDinamico(int capInicial) {
            this.capacidade = capInicial;
            this.dados      = new Object[capacidade];
        }

        void adicionar(T elemento) {
            if (tamanho == capacidade) {
                int capAnterior = capacidade;
                capacidade     *= 2;
                Object[] novo   = new Object[capacidade];
                for (int i = 0; i < tamanho; i++) novo[i] = dados[i];
                dados = novo;
                System.out.println("  [RESIZE] capacidade: " + capAnterior + " → " + capacidade
                        + "  (copiados " + tamanho + " elemento(s))");
            }
            dados[tamanho++] = elemento;
            System.out.println("  Adicionado: " + elemento
                    + " | tamanho: " + tamanho
                    + " | capacidade: " + capacidade);
        }

        int tamanho()    { return tamanho; }
        int capacidade() { return capacidade; }
    }

    // ----------------------------------------------------------
    // Demonstração
    // ----------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Vetor Dinâmico com Log (capacidade inicial = 2) ===\n");

        VetorDinamico<Integer> v = new VetorDinamico<>(2);
        for (int i = 1; i <= 10; i++) {
            v.adicionar(i);
        }

        System.out.println("\n--- Resumo ---");
        System.out.println("Total de elementos: " + v.tamanho());
        System.out.println("Capacidade final:   " + v.capacidade());
        System.out.println("\nObserve: redimensionamentos ocorreram nas inserções 3, 5 e 9.");
        System.out.println("Total de cópias: 2 + 4 + 8 = 14  (< 2 × 10 = 20)");
        System.out.println("Custo amortizado por inserção ≈ O(1).");
    }
}
