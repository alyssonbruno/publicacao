import java.util.Arrays;

/**
 * Aula 8 — MERGE SORT e QUICK SORT testados em seis vetores.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>o primeiro vetor é o da aula, [6, 3, 8, 1, 7, 2, 5, 4], o mesmo dos slides
 *       e do visualizador;</li>
 *   <li>cada algoritmo recebe a sua própria cópia do vetor original, que por isso
 *       aparece intacto na linha "original";</li>
 *   <li>os dois chegam ao mesmo resultado nos seis casos, inclusive nos casos de
 *       borda: vetor com repetidos, com um elemento só e vazio;</li>
 *   <li>o vetor vazio não quebra nada: a chamada é mergeSort(v, 0, -1) e o caso
 *       base (esq >= dir) devolve na hora.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 2.0
 */
public class OrdenacaoApp {

    static void testar(String nome, int[] original) {
        int[] m = original.clone();              // cada algoritmo recebe a sua cópia
        int[] q = original.clone();
        Ordenacao.mergeSort(m, 0, m.length - 1);
        Ordenacao.quickSort(q, 0, q.length - 1);
        System.out.println(nome);
        System.out.println("  original   : " + Arrays.toString(original));
        System.out.println("  Merge Sort : " + Arrays.toString(m));
        System.out.println("  Quick Sort : " + Arrays.toString(q));
    }

    public static void main(String[] args) {
        testar("Vetor da aula",   new int[] {6, 3, 8, 1, 7, 2, 5, 4});
        testar("Já ordenado",     new int[] {1, 2, 3, 4, 5});
        testar("Ao contrário",    new int[] {5, 4, 3, 2, 1});
        testar("Com repetidos",   new int[] {3, 1, 3, 2, 1});
        testar("Um elemento",     new int[] {42});
        testar("Vazio",           new int[] {});
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
