/**
 * Aula 06 — Hands-on 1: LISTA ORDENADA com array.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>a lista continua ordenada depois de cada inserção;</li>
 *   <li>para abrir espaço, a inserção <b>empurra</b> os vizinhos maiores;</li>
 *   <li>a busca binária corta a lista ao meio e acha o valor em poucos passos.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 2.0
 */
public class ListaOrdenadaApp {

    // =====================================================================
    // TAD Lista Ordenada
    // Invariante: os valores estão SEMPRE em ordem crescente.
    // =====================================================================
    static class ListaOrdenada {

        private int[] dados;      // vetor interno
        private int tamanho;      // quantos valores já foram guardados
        private int empurroes;    // quantos elementos a última inserção mexeu

        /** Cria a lista com uma capacidade máxima fixa. */
        ListaOrdenada(int capacidade) {
            dados = new int[capacidade];
            tamanho = 0;
        }

        /**
         * Insere o valor no lugar certo, mantendo a ordem crescente.
         * Custo O(n): no pior caso empurra todos os elementos.
         */
        void inserir(int valor) {
            if (tamanho == dados.length) {
                throw new IllegalStateException("Lista cheia.");
            }
            empurroes = 0;
            int pos = tamanho;
            // Enquanto o vizinho da esquerda for MAIOR, empurre-o para a direita.
            while (pos > 0 && dados[pos - 1] > valor) {
                dados[pos] = dados[pos - 1];
                pos--;
                empurroes++;
            }
            dados[pos] = valor;   // agora o buraco está no lugar certo
            tamanho++;
        }

        /**
         * Busca binária: só funciona porque a lista está ordenada.
         * Custo O(log n) — a cada passo, metade da lista é descartada.
         */
        boolean buscar(int valor) {
            int esquerda = 0;
            int direita = tamanho - 1;
            int passos = 0;
            while (esquerda <= direita) {
                int meio = (esquerda + direita) / 2;
                passos++;
                System.out.printf("   passo %d: olhando a posição %d (valor %d)%n",
                        passos, meio, dados[meio]);
                if (dados[meio] == valor) {
                    System.out.println("   -> achei em " + passos + " passo(s)");
                    return true;
                }
                if (dados[meio] < valor) {
                    esquerda = meio + 1;   // o valor só pode estar à direita
                } else {
                    direita = meio - 1;    // o valor só pode estar à esquerda
                }
            }
            System.out.println("   -> não existe (" + passos + " passo(s))");
            return false;
        }

        /** Busca linear, para comparar o esforço com a busca binária. */
        int buscaLinearPassos(int valor) {
            int passos = 0;
            for (int i = 0; i < tamanho; i++) {
                passos++;
                if (dados[i] == valor) break;
            }
            return passos;
        }

        int empurroesDaUltimaInsercao() {
            return empurroes;
        }

        int tamanho() {
            return tamanho;
        }

        /** Mostra a lista no formato [10, 20, 30]. */
        void imprimir() {
            System.out.print("[");
            for (int i = 0; i < tamanho; i++) {
                System.out.print(dados[i]);
                if (i < tamanho - 1) System.out.print(", ");
            }
            System.out.println("]");
        }
    }

    // =====================================================================
    // Programa principal
    // =====================================================================
    public static void main(String[] args) {

        System.out.println("=== 1) INSERINDO E MANTENDO A ORDEM ===");
        System.out.println("Notas da turma, na ordem em que o professor as recebeu.");

        ListaOrdenada notas = new ListaOrdenada(10);
        int[] chegada = {40, 10, 25, 60, 15};

        for (int nota : chegada) {
            notas.inserir(nota);
            System.out.printf("inserir(%2d) -> empurrou %d elemento(s) -> ",
                    nota, notas.empurroesDaUltimaInsercao());
            notas.imprimir();
        }

        System.out.println();
        System.out.println("Repare: o 10 chegou depois do 40 e mesmo assim ficou na frente.");
        System.out.println("O preço disso é o empurrão — por isso a inserção é O(n).");

        System.out.println();
        System.out.println("=== 2) BUSCA BINÁRIA (só funciona em lista ordenada) ===");
        System.out.println("Lista atual: ");
        System.out.print("   ");
        notas.imprimir();

        System.out.println("buscar(15):");
        notas.buscar(15);

        System.out.println("buscar(99):");
        notas.buscar(99);

        System.out.println();
        System.out.println("=== 3) POR QUE ISSO IMPORTA ===");
        ListaOrdenada grande = new ListaOrdenada(1000);
        for (int i = 1; i <= 1000; i++) {
            grande.inserir(i);       // já chegam em ordem: nenhum empurrão
        }
        int passosLinear = grande.buscaLinearPassos(1000);
        int passosBinaria = 0;
        for (int n = grande.tamanho(); n > 0; n = n / 2) {
            passosBinaria++;         // quantas vezes dá para dividir 1000 por 2
        }
        System.out.println("Lista com " + grande.tamanho() + " elementos, procurando o último:");
        System.out.println("   busca linear  : cerca de " + passosLinear + " passos  -> O(n)");
        System.out.println("   busca binária : cerca de " + passosBinaria + " passos    -> O(log n)");
        System.out.println();
        System.out.println("Conclusão: vale a pena manter a lista ordenada quando");
        System.out.println("a gente busca MUITO mais do que insere.");
    }
}
