import java.util.Arrays;

/**
 * Aula 8 — Hands-on: MERGE SORT e QUICK SORT no mesmo vetor.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>os dois algoritmos recebem uma cópia do mesmo vetor original;</li>
 *   <li>os dois chegam ao mesmo resultado por caminhos diferentes;</li>
 *   <li>o Merge Sort divide sempre ao meio e depois mescla;</li>
 *   <li>o Quick Sort escolhe um pivô e separa menores e maiores.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class OrdenacaoApp {
    public static void main(String[] args) {
        int[] original = {64, 34, 25, 12, 22, 11, 90};

        // Merge Sort
        int[] m = Arrays.copyOf(original, original.length);
        Ordenacao.mergeSort(m, 0, m.length - 1);
        System.out.println("Merge Sort : " + Arrays.toString(m));

        // Quick Sort
        int[] q = Arrays.copyOf(original, original.length);
        Ordenacao.quickSort(q, 0, q.length - 1);
        System.out.println("Quick Sort : " + Arrays.toString(q));
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
