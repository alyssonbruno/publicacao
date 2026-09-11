import java.util.*;

/**
 * Versão autocontida do jogo 21 simplificado para execução no JDoodle.
 *
 * <p>Cole todo o arquivo no editor, selecione JDK 17 ou superior e ative o
 * modo interativo. A regra é didática: Ás vale 14 ou 1 e as figuras valem
 * 11, 12 e 13.</p>
 *
 * @author Alysson M. Bruno
 * @version 1.0
 * @since 1.0
 */
class Main {
    /** Impede a criação de instâncias da classe utilitária. */
    private Main() { }

    /** Naipes de um baralho francês. */
    enum Naipe {
        /** Naipe de copas. */ COPAS,
        /** Naipe de paus. */ PAUS,
        /** Naipe de ouros. */ OUROS,
        /** Naipe de espadas. */ ESPADAS
    }

    /**
     * Contrato mínimo de uma carta usada no jogo.
     *
     * @invariant {@code 1 <= valor() && valor() <= 13}
     */
    interface Carta {
        /**
         * Informa o valor nominal da carta.
         *
         * @return valor nominal, entre 1 e 13
         */
        int valor();

        /**
         * Informa a pontuação inicial da carta na regra didática.
         *
         * @return 14 para o Ás ou o valor nominal para as demais cartas
         */
        int pontos();
    }

    /** Implementação imutável de {@link Carta}. */
    static final class CartaImpl implements Carta {
        private final int valor; private final Naipe naipe;

        /**
         * Cria uma carta.
         *
         * @param valor valor entre 1 e 13, inclusive
         * @param naipe naipe da carta; não pode ser {@code null}
         * @throws IllegalArgumentException se o valor estiver fora do intervalo
         * @throws NullPointerException se o naipe for nulo
         * @pre {@code 1 <= valor && valor <= 13 && naipe != null}
         */
        CartaImpl(int valor, Naipe naipe) {
            if (valor < 1 || valor > 13) throw new IllegalArgumentException("Valor inválido.");
            this.valor = valor; this.naipe = Objects.requireNonNull(naipe);
        }
        public int valor() { return valor; }
        public int pontos() { return valor == 1 ? 14 : valor; }
        public String toString() {
            String[] nomes = {"", "Ás", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Valete", "Dama", "Rei"};
            return nomes[valor] + " de " + naipe;
        }
    }

    /**
     * Baralho francês cujo topo é o último elemento da lista interna.
     *
     * @invariant {@code 0 <= cartas.size() && cartas.size() <= 52}
     */
    static final class Baralho {
        private final List<Carta> cartas = new ArrayList<>();

        /**
         * Cria e embaralha um conjunto completo de 52 cartas.
         *
         * @pos o baralho contém 52 cartas em ordem aleatória
         */
        Baralho() {
            for (Naipe n : Naipe.values()) for (int v = 1; v <= 13; v++) cartas.add(new CartaImpl(v, n));
            Collections.shuffle(cartas);
        }

        /**
         * Retira a próxima carta do topo.
         *
         * @return carta retirada, nunca {@code null}
         * @throws NoSuchElementException se não houver cartas
         * @pre o baralho não está vazio
         * @pos a quantidade de cartas diminui em uma unidade
         */
        Carta distribuir() {
            if (cartas.isEmpty()) throw new NoSuchElementException("Baralho vazio.");
            return cartas.remove(cartas.size() - 1);
        }
    }

    /**
     * Calcula a pontuação ajustada de uma mão.
     *
     * @param mao cartas da mão; não pode ser {@code null} nem conter valores nulos
     * @return pontuação ajustada; 0 para uma mão vazia
     * @throws NullPointerException se a mão ou uma de suas cartas for nula
     * @pre {@code mao != null} e a lista não contém cartas nulas
     * @pos a mão recebida não é modificada
     */
    static int pontos(List<Carta> mao) {
        int total = 0, ases = 0;
        for (Carta c : mao) { total += c.pontos(); if (c.valor() == 1) ases++; }
        while (total > 21 && ases > 0) { total -= 13; ases--; }
        return total;
    }

    /**
     * Executa uma rodada interativa.
     *
     * @param args argumentos de linha de comando; não são utilizados
     */
    public static void main(String[] args) {
        Baralho baralho = new Baralho(); List<Carta> mao = new ArrayList<>(); Scanner in = new Scanner(System.in);
        String resposta;
        do {
            mao.add(baralho.distribuir());
            System.out.println("Mão: " + mao + " | pontos: " + pontos(mao));
            if (pontos(mao) >= 21) break;
            System.out.print("Outra carta? (s/n): "); resposta = in.nextLine().trim();
        } while (resposta.equalsIgnoreCase("s"));
        System.out.println(pontos(mao) > 21 ? "Você ultrapassou 21." : "Fim da rodada.");
    }
}
