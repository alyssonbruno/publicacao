import java.util.Arrays;
import java.util.Random;

/**
 * Aula 8 — Hands-on: MERGE × QUICK — medindo o tempo.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>com 100.000 números aleatórios, os dois terminam em frações de segundo;</li>
 *   <li>com o vetor JÁ ORDENADO, o Quick Sort desta versão cai no pior caso, O(n²);</li>
 *   <li>o Merge Sort não muda de comportamento com a entrada ordenada;</li>
 *   <li>é por isso que a escolha do pivô importa tanto no Quick Sort.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class ComparaTemposApp {
    public static void main(String[] args) {
        // --- Teste com dados aleatórios ---
        int n = 100_000;
        Random rng = new Random(42);
        int[] base = new int[n];
        for (int i = 0; i < n; i++) base[i] = rng.nextInt(1_000_000);

        int[] m = Arrays.copyOf(base, n);
        long ini = System.nanoTime();
        Ordenacao.mergeSort(m, 0, m.length - 1);
        System.out.printf("Merge Sort  (n=%,d, aleatório): %,10d ns%n", n, System.nanoTime() - ini);

        int[] q = Arrays.copyOf(base, n);
        ini = System.nanoTime();
        Ordenacao.quickSort(q, 0, q.length - 1);
        System.out.printf("Quick Sort  (n=%,d, aleatório): %,10d ns%n", n, System.nanoTime() - ini);

        // --- Teste com array já ordenado (pior caso do Quick Sort) ---
        // No pior caso o Quick Sort desce um nível de recursão por elemento.
        // O texto-base usa 10.000; aqui são 3.000, porque a pilha de chamadas
        // do JDoodle é menor que a de um computador e estoura (StackOverflowError)
        // antes de terminar. Aumente aos poucos e veja até onde a pilha aguenta.
        int[] ordenado = new int[3_000];
        for (int i = 0; i < ordenado.length; i++) ordenado[i] = i;

        int[] qOrd = Arrays.copyOf(ordenado, ordenado.length);
        ini = System.nanoTime();
        Ordenacao.quickSort(qOrd, 0, qOrd.length - 1);
        System.out.printf("Quick Sort  (n=%,d, ordenado): %,10d ns%n",
                           ordenado.length, System.nanoTime() - ini);

        int[] mOrd = Arrays.copyOf(ordenado, ordenado.length);
        ini = System.nanoTime();
        Ordenacao.mergeSort(mOrd, 0, mOrd.length - 1);
        System.out.printf("Merge Sort  (n=%,d, ordenado): %,10d ns%n",
                           ordenado.length, System.nanoTime() - ini);
    }
}

class Ordenacao {

    /**
     * Ordena o subarray arr[esq..dir] usando Merge Sort.
     * Chamada inicial: mergeSort(arr, 0, arr.length - 1)
     */
    public static void mergeSort(int[] arr, int esq, int dir) {
        // caso base: subarray de 0 ou 1 elemento já está ordenado
        if (esq >= dir) return;

        int meio = (esq + dir) / 2;
        mergeSort(arr, esq, meio);           // ordena a metade esquerda
        mergeSort(arr, meio + 1, dir);       // ordena a metade direita
        merge(arr, esq, meio, dir);          // mescla as duas metades ordenadas
    }

    /**
     * Mescla os subarrays arr[esq..meio] e arr[meio+1..dir], ambos já ordenados.
     */
    private static void merge(int[] arr, int esq, int meio, int dir) {
        // Passo 1: cria cópias temporárias das duas metades
        int n1 = meio - esq + 1;
        int n2 = dir - meio;
        int[] L = new int[n1];
        int[] R = new int[n2];

        for (int i = 0; i < n1; i++) L[i] = arr[esq + i];
        for (int j = 0; j < n2; j++) R[j] = arr[meio + 1 + j];

        // Passo 2: intercala os elementos em ordem crescente
        int i = 0, j = 0, k = esq;
        while (i < n1 && j < n2) {
            // L[i] <= R[j] garante estabilidade (iguais mantêm ordem original)
            if (L[i] <= R[j]) arr[k++] = L[i++];
            else               arr[k++] = R[j++];
        }

        // Passo 3: copia elementos restantes (somente uma das metades terá sobra)
        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    /**
     * Ordena o subarray arr[esq..dir] usando Quick Sort.
     * Chamada inicial: quickSort(arr, 0, arr.length - 1)
     */
    public static void quickSort(int[] arr, int esq, int dir) {
        // caso base: subarray de 0 ou 1 elemento já está ordenado
        if (esq >= dir) return;

        int p = partition(arr, esq, dir);     // posição definitiva do pivô
        quickSort(arr, esq, p - 1);           // ordena elementos à esquerda do pivô
        quickSort(arr, p + 1, dir);           // ordena elementos à direita do pivô
    }

    /**
     * Partição de Lomuto: escolhe arr[dir] como pivô.
     * Retorna a posição definitiva do pivô.
     */
    private static int partition(int[] arr, int esq, int dir) {
        int pivot = arr[dir];   // pivô é o último elemento
        int i = esq - 1;        // i aponta para o último "elemento pequeno" encontrado

        for (int j = esq; j < dir; j++) {
            if (arr[j] <= pivot) {
                i++;
                // troca arr[i] e arr[j] — move elemento pequeno para a esquerda
                int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
            }
        }
        // coloca o pivô na posição correta: entre os menores e os maiores
        int tmp = arr[i + 1]; arr[i + 1] = arr[dir]; arr[dir] = tmp;
        return i + 1;
    }
}
