/**
 * Aula 8 — Hands-on: MERGE SORT passo a passo.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>cada linha impressa é um trecho do vetor no momento em que vai ser mesclado;</li>
 *   <li>os trechos menores aparecem primeiro — a recursão desce antes de mesclar;</li>
 *   <li>acompanhe como dois pedaços ordenados viram um pedaço maior ordenado;</li>
 *   <li>a última linha entre colchetes é o vetor inteiro ainda partido ao meio;
 *       a linha <b>Ordenado:</b> só aparece depois da última mesclagem.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class PassoAPassoMergeApp {

    static private String toStr(int[] arr){
        String res = "";
        for (int idx = 0; idx < arr.length; idx++) {
            res += arr[idx];
            if (idx < arr.length-1) res += ", ";
        }
        return res;
    }

    static private void imprime (int[] arr, int esq, int dir){
        System.out.print("[");
        for(int i=esq; i<= dir; i++){
            
            System.out.print(arr[i]);
            if(i<dir){
                System.out.print(",");
            }
        }
        System.out.print("]");

    }

    static private void merge(int[] arr, int indice_esquerdo, int indice_meio, int indice_direito){
                // 1. Cria cópias temporárias das duas metades
        int primeira_metade = indice_meio - indice_esquerdo + 1;
        int segunda_metade = indice_direito - indice_meio;
        int[] lado_esquerdo = new int[primeira_metade];
        int[] lado_direito = new int[segunda_metade];
        for (int i = 0; i < primeira_metade; i++) lado_esquerdo[i] = arr[indice_esquerdo + i];
        for (int j = 0; j < segunda_metade; j++) lado_direito[j] = arr[indice_meio + 1 + j];

        // 2. Intercala os elementos em ordem crescente
        int i = 0, j = 0, k = indice_esquerdo;
        while (i < primeira_metade && j < segunda_metade)
            arr[k++] = (lado_esquerdo[i] <= lado_direito[j]) ? lado_esquerdo[i++] : lado_direito[j++];

        // 3. Copia os elementos restantes (de uma das metades)
        while (i < primeira_metade) arr[k++] = lado_esquerdo[i++];
        while (j < segunda_metade) arr[k++] = lado_direito[j++];
    }

    static private void mergeSort(int[] arr, int iesquerdo, int idireito){
        if(iesquerdo>=idireito) { 
            return; // para quando o esquerdo for maior ou igual o direito 
        }
        
        int meio = (idireito + iesquerdo ) / 2;
        mergeSort(arr, iesquerdo, meio);
        mergeSort(arr, meio+1, idireito);
        imprime(arr, iesquerdo, idireito);
        System.out.println("");
        merge(arr, iesquerdo, meio, idireito);
        return;
    }

    static public void main(String[] args) {
        int[] numeros = { 48, 12, 0, 3, 45, 15, 23, 8, 1, 22, 36 };
        System.out.println( "Original: " + toStr(numeros));
        mergeSort(numeros, 0, numeros.length-1);
        System.out.println( "Ordenado: " + toStr(numeros));
    }
}
