/**
 * Aula 06 — Hands-on 3: FILA (FIFO) com array circular.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>quem chega primeiro é atendido primeiro (FIFO);</li>
 *   <li>o índice do fim "dá a volta" no array em vez de estourar;</li>
 *   <li>por isso a fila cabe em um vetor pequeno e nunca desperdiça espaço.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 2.0
 */
public class FilaApp {

    // =====================================================================
    // TAD Fila com array circular — entra no FIM, sai pela FRENTE
    // =====================================================================
    static class Fila {

        private String[] dados;
        private int frente = 0;   // onde está o próximo a ser atendido
        private int fim = 0;      // onde o próximo que chegar vai ficar
        private int tamanho = 0;  // quantos estão esperando agora

        Fila(int capacidade) {
            dados = new String[capacidade];
        }

        /** Coloca alguém no fim da fila. Custo O(1). */
        void enqueue(String nome) {
            if (tamanho == dados.length) {
                throw new IllegalStateException("Fila cheia.");
            }
            dados[fim] = nome;
            fim = (fim + 1) % dados.length;   // o % faz dar a volta no array
            tamanho++;
        }

        /** Atende (remove) quem está na frente. Custo O(1). */
        String dequeue() {
            if (vazia()) {
                throw new IllegalStateException("Fila vazia.");
            }
            String nome = dados[frente];
            dados[frente] = null;                 // limpa só para a saída ficar clara
            frente = (frente + 1) % dados.length; // o % faz dar a volta no array
            tamanho--;
            return nome;
        }

        /** Vê quem é o próximo sem atender. Custo O(1). */
        String peek() {
            if (vazia()) {
                throw new IllegalStateException("Fila vazia.");
            }
            return dados[frente];
        }

        boolean vazia() {
            return tamanho == 0;
        }

        int tamanho() {
            return tamanho;
        }

        /** Mostra o array por dentro, para enxergar a volta circular. */
        void imprimirArray() {
            System.out.print("   array: ");
            for (int i = 0; i < dados.length; i++) {
                System.out.print("[" + (dados[i] == null ? "-" : dados[i]) + "]");
            }
            System.out.println("   frente=" + frente + "  fim=" + fim + "  tamanho=" + tamanho);
        }
    }

    // =====================================================================
    // Programa principal
    // =====================================================================
    public static void main(String[] args) {

        System.out.println("=== 1) FIFO: quem chega primeiro é atendido primeiro ===");
        System.out.println("Guichê com capacidade para 4 pessoas na espera.");
        System.out.println();

        Fila guiche = new Fila(4);

        System.out.println("Chegaram Ana, Bruno e Carla:");
        guiche.enqueue("Ana");
        guiche.enqueue("Bruno");
        guiche.enqueue("Carla");
        guiche.imprimirArray();

        System.out.println("Próximo da fila (peek): " + guiche.peek());
        System.out.println("Atendendo (dequeue): " + guiche.dequeue());
        guiche.imprimirArray();

        System.out.println("Atendendo (dequeue): " + guiche.dequeue());
        guiche.imprimirArray();

        System.out.println();
        System.out.println("=== 2) O TRUQUE DO ARRAY CIRCULAR ===");
        System.out.println("Duas posições do começo ficaram livres. Vamos reaproveitá-las.");
        System.out.println();

        System.out.println("Chegou Diego:");
        guiche.enqueue("Diego");
        guiche.imprimirArray();

        System.out.println("Chegou Eva — repare que ela volta para a posição 0:");
        guiche.enqueue("Eva");
        guiche.imprimirArray();

        System.out.println();
        System.out.println("Sem o operador %, o índice fim passaria de 3 e estouraria o array,");
        System.out.println("mesmo com posições vazias sobrando no começo.");

        System.out.println();
        System.out.println("=== 3) SIMULAÇÃO DE ATENDIMENTO ===");
        System.out.println("Atendendo todo mundo, na ordem de chegada:");

        int senha = 1;
        while (!guiche.vazia()) {
            System.out.printf("   senha %d -> %s   (ainda esperando: %d)%n",
                    senha, guiche.dequeue(), guiche.tamanho());
            senha++;
        }
        guiche.imprimirArray();

        System.out.println();
        System.out.println("Fila vazia. Tentar atender agora dá erro — e isso é proposital:");
        try {
            guiche.dequeue();
        } catch (IllegalStateException e) {
            System.out.println("   erro capturado: " + e.getMessage());
        }

        System.out.println();
        System.out.println("Resumo: enqueue e dequeue são O(1) porque a fila");
        System.out.println("só mexe nas pontas — nunca precisa empurrar ninguém.");
    }
}
