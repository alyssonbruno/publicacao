import java.util.Arrays;
import java.util.Random;

/**
 * Aula 7 — Hands-on: OS TRÊS ALGORITMOS QUADRÁTICOS lado a lado.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>a tabela mostra o tempo de cada algoritmo para n = 100, 1.000, 5.000 e 10.000;</li>
 *   <li>ao multiplicar n por 10, o tempo cresce muito mais que 10 vezes — é a marca do O(n²);</li>
 *   <li>os três recebem exatamente o mesmo vetor de entrada (mesma semente);</li>
 *   <li>o Insertion Sort costuma ser o mais rápido dos três nesse teste.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class OrdenacaoDemo {

    // --- Bubble Sort ---
    static void bubbleSort(int[] v) {
        int n = v.length;
        for (int i = 0; i < n - 1; i++) {
            boolean trocou = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (v[j] > v[j + 1]) {
                    int tmp = v[j]; v[j] = v[j+1]; v[j+1] = tmp;
                    trocou = true;
                }
            }
            if (!trocou) break;
        }
    }

    // --- Selection Sort ---
    static void selectionSort(int[] v) {
        int n = v.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++)
                if (v[j] < v[minIdx]) minIdx = j;
            if (minIdx != i) {
                int tmp = v[i]; v[i] = v[minIdx]; v[minIdx] = tmp;
            }
        }
    }

    // --- Insertion Sort ---
    static void insertionSort(int[] v) {
        int n = v.length;
        for (int i = 1; i < n; i++) {
            int chave = v[i], j = i - 1;
            while (j >= 0 && v[j] > chave) { v[j+1] = v[j]; j--; }
            v[j+1] = chave;
        }
    }

    // --- Utilitário ---
    static int[] gerarAleatorio(int n, long seed) {
        var rng = new Random(seed);
        return rng.ints(n, 0, n * 10).toArray();
    }

    static long medir(int[] dados, Runnable algoritmo) {
        long inicio = System.nanoTime();
        algoritmo.run();
        return System.nanoTime() - inicio;
    }

    public static void main(String[] args) {
        int[] tamanhos = {100, 1_000, 5_000, 10_000};

        System.out.printf("%-8s %-15s %-15s %-15s%n",
            "n", "Bubble (µs)", "Selection (µs)", "Insertion (µs)");
        System.out.println("-".repeat(55));

        for (int n : tamanhos) {
            // Clonar para garantir mesma entrada
            var base = gerarAleatorio(n, 42L);
            var b = base.clone();
            var s = base.clone();
            var ins = base.clone();

            long tb  = medir(b,   () -> bubbleSort(b));
            long ts  = medir(s,   () -> selectionSort(s));
            long ti  = medir(ins, () -> insertionSort(ins));

            System.out.printf("%-8d %-15.1f %-15.1f %-15.1f%n",
                n, tb/1_000.0, ts/1_000.0, ti/1_000.0);
        }
    }
}
