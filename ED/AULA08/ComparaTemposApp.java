import java.util.Arrays;
import java.util.Random;

/**
 * Aula 8 — MERGE × QUICK: a previsão do Big-O no relógio.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho, em poucos segundos.</p>
 *
 * <p>O que observar na saída (os milissegundos mudam de máquina para máquina;
 * olhe as colunas de razão):</p>
 * <ol>
 *   <li>PARTE 1, números sorteados: quando n dobra, os dois tempos ficam perto
 *       do dobro (entre 1,6x e 2,6x) — é o O(n log n). Na Aula 07, o Bubble Sort
 *       quadruplicava;</li>
 *   <li>na PARTE 1 o Quick Sort gasta menos que o Merge Sort: mesmo crescimento,
 *       constante menor, porque ele não copia o vetor;</li>
 *   <li>PARTE 2, vetor já ordenado: o Quick Sort quadruplica a cada linha (4,0x) —
 *       com o último elemento como pivô, esse é o pior caso dele, O(n²);</li>
 *   <li>na PARTE 2 o Merge Sort continua rápido: ele não tem pior caso.</li>
 * </ol>
 *
 * <p>Desafio: troque o 3_000 do segundo laço por 6_000, depois 12_000, e
 * descubra em que tamanho aparece o StackOverflowError.</p>
 *
 * @author Prof. Alysson M. Bruno
 * @version 2.0
 */
public class ComparaTemposApp {

    // n números sorteados. A semente fixa (42) faz o sorteio sair sempre igual.
    static int[] aleatorio(int n) {
        return new Random(42).ints(n, 0, n * 10).toArray();
    }

    // 0, 1, 2, ..., n - 1: já em ordem.
    static int[] crescente(int n) {
        int[] v = new int[n];
        for (int i = 0; i < n; i++) v[i] = i;
        return v;
    }

    // Tempo para ordenar uma cópia do vetor, em milissegundos. Repete cinco vezes
    // e fica com o menor tempo, o menos atrapalhado pelo resto da máquina.
    static double medir(int[] dados, boolean merge) {
        long melhor = Long.MAX_VALUE;
        for (int r = 0; r < 5; r++) {
            int[] v = dados.clone();
            long inicio = System.nanoTime();
            if (merge) Ordenacao.mergeSort(v, 0, v.length - 1);
            else       Ordenacao.quickSort(v, 0, v.length - 1);
            melhor = Math.min(melhor, System.nanoTime() - inicio);
        }
        return melhor / 1_000_000.0;
    }

    static String razao(double agora, double antes) {
        return antes == 0 ? "-" : String.format("%.1fx", agora / antes);
    }

    public static void main(String[] args) {
        // Aquecimento da JVM, como na Aula 07: sem ele, as primeiras linhas saem infladas.
        for (int r = 0; r < 20; r++) {
            medir(aleatorio(20_000), true);
            medir(aleatorio(20_000), false);
            medir(crescente(500), false);
        }

        System.out.println("PARTE 1 - números sorteados (n dobra a cada linha)");
        System.out.printf("%-9s %-11s %-11s %-15s %-15s%n",
                "n", "Merge (ms)", "Quick (ms)", "Merge/anterior", "Quick/anterior");
        double mAnt = 0, qAnt = 0;
        for (int n = 100_000; n <= 800_000; n *= 2) {
            int[] dados = aleatorio(n);
            double m = medir(dados, true), q = medir(dados, false);
            System.out.printf("%-9d %-11.2f %-11.2f %-15s %-15s%n",
                    n, m, q, razao(m, mAnt), razao(q, qAnt));
            mAnt = m;
            qAnt = q;
        }

        System.out.println();
        System.out.println("PARTE 2 - vetor JÁ ORDENADO (pior caso do Quick Sort)");
        System.out.printf("%-9s %-11s %-11s %-15s%n", "n", "Merge (ms)", "Quick (ms)", "Quick/anterior");
        qAnt = 0;
        for (int n = 750; n <= 3_000; n *= 2) {
            int[] dados = crescente(n);
            double m = medir(dados, true), q = medir(dados, false);
            System.out.printf("%-9d %-11.3f %-11.3f %-15s%n", n, m, q, razao(q, qAnt));
            qAnt = q;
        }
    }
}

class Ordenacao {

    /**
     * Ordena o trecho v[esq..dir] com Merge Sort.
     * Para ordenar o vetor inteiro: mergeSort(v, 0, v.length - 1)
     */
    public static void mergeSort(int[] v, int esq, int dir) {
        if (esq >= dir) return;          // 0 ou 1 elemento
        int meio = (esq + dir) / 2;
        mergeSort(v, esq, meio);         // metade esquerda
        mergeSort(v, meio + 1, dir);     // metade direita
        merge(v, esq, meio, dir);        // intercala as duas
    }

    /** Intercala v[esq..meio] e v[meio+1..dir], já ordenados. */
    static void merge(int[] v, int esq, int meio, int dir) {
        int[] L = Arrays.copyOfRange(v, esq, meio + 1);
        int[] R = Arrays.copyOfRange(v, meio + 1, dir + 1);
        int i = 0, j = 0, k = esq;
        while (i < L.length && j < R.length) {
            if (L[i] <= R[j]) v[k++] = L[i++];  // "<=": estável
            else              v[k++] = R[j++];
        }
        while (i < L.length) v[k++] = L[i++];   // sobras de L
        while (j < R.length) v[k++] = R[j++];   // sobras de R
    }

    /**
     * Ordena o trecho v[esq..dir] com Quick Sort.
     * Para ordenar o vetor inteiro: quickSort(v, 0, v.length - 1)
     */
    public static void quickSort(int[] v, int esq, int dir) {
        if (esq >= dir) return;              // 0 ou 1 elemento
        int p = particionar(v, esq, dir);    // pivô vai para p
        quickSort(v, esq, p - 1);            // ordena os menores
        quickSort(v, p + 1, dir);            // ordena os maiores
    }

    /** Partição de Lomuto: o último elemento é o pivô. */
    static int particionar(int[] v, int esq, int dir) {
        int pivo = v[dir];
        int i = esq - 1;                     // fim da zona dos pequenos
        for (int j = esq; j < dir; j++) {
            if (v[j] <= pivo) {
                i++;
                trocar(v, i, j);             // v[j] entra na zona
            }
        }
        trocar(v, i + 1, dir);               // pivô entre as zonas
        return i + 1;
    }

    static void trocar(int[] v, int a, int b) {
        int tmp = v[a];
        v[a] = v[b];
        v[b] = tmp;
    }
}
