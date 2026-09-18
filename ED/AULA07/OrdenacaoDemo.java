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
 *   <li>a tabela mostra o tempo de cada algoritmo para n = 1.000, 2.000, 4.000 e
 *       8.000 — tamanhos que <b>dobram</b> de uma linha para a outra;</li>
 *   <li>a última coluna divide o tempo do Bubble Sort pelo da linha anterior:
 *       o valor fica perto de <b>4x</b>, que é a marca do O(n²) — dobrar a
 *       entrada quadruplica o trabalho;</li>
 *   <li>os três recebem exatamente o mesmo vetor de entrada (mesma semente);</li>
 *   <li>o Insertion Sort é o mais rápido dos três neste teste, e o Bubble Sort
 *       o mais lento — os três são O(n²), mas com constantes bem diferentes;</li>
 *   <li>antes de medir, o programa executa um laço de aquecimento: a JVM só
 *       traduz o código para linguagem de máquina depois de rodá-lo algumas
 *       vezes, e sem isso as primeiras linhas sairiam distorcidas.</li>
 * </ol>
 *
 * <p>Os tempos em milissegundos mudam de máquina para máquina e entre duas
 * execuções seguidas; o que se repete é a <b>proporção</b> entre as linhas.
 * Na última linha o valor às vezes passa de 4x, porque o vetor já não cabe
 * inteiro na memória rápida do processador.</p>
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
        // Aquecimento. A JVM só traduz o código para linguagem de máquina depois
        // de executá-lo algumas vezes. Sem este laço, as primeiras medições saem
        // infladas e a tabela não mostra o crescimento quadrático.
        for (int i = 0; i < 5; i++) {
            var aquece = gerarAleatorio(3_000, 7L);
            bubbleSort(aquece.clone());
            selectionSort(aquece.clone());
            insertionSort(aquece.clone());
        }

        // Tamanhos que DOBRAM: é o que permite conferir se o tempo quadruplica.
        int[] tamanhos = {1_000, 2_000, 4_000, 8_000};

        System.out.printf("%-8s %-14s %-14s %-14s %s%n",
            "n", "Bubble (ms)", "Selection (ms)", "Insertion (ms)", "Bubble/anterior");
        System.out.println("-".repeat(72));

        double anterior = 0;
        for (int n : tamanhos) {
            // Clonar para garantir mesma entrada
            var base = gerarAleatorio(n, 42L);
            var b = base.clone();
            var s = base.clone();
            var ins = base.clone();

            double tb  = medir(b,   () -> bubbleSort(b))     / 1_000_000.0;
            double ts  = medir(s,   () -> selectionSort(s))  / 1_000_000.0;
            double ti  = medir(ins, () -> insertionSort(ins)) / 1_000_000.0;

            System.out.printf("%-8d %-14.2f %-14.2f %-14.2f %s%n",
                n, tb, ts, ti,
                anterior == 0 ? "-" : String.format("%.1fx", tb / anterior));
            anterior = tb;
        }
    }
}
