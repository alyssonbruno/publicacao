/**
 * Aula 06 — Hands-on 2: PILHA (LIFO) e verificação de parênteses.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>o último que entra é o primeiro que sai (LIFO);</li>
 *   <li>a pilha "lembra" qual parêntese ainda está aberto;</li>
 *   <li>expressão certa termina com a pilha vazia.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 2.0
 */
public class PilhaApp {

    // =====================================================================
    // TAD Pilha de caracteres — só mexe no TOPO
    // =====================================================================
    static class Pilha {

        private char[] dados;
        private int topo = -1;   // -1 significa pilha vazia

        Pilha(int capacidade) {
            dados = new char[capacidade];
        }

        /** Coloca um item no topo. Custo O(1). */
        void push(char c) {
            if (topo == dados.length - 1) {
                throw new IllegalStateException("Pilha cheia.");
            }
            topo++;
            dados[topo] = c;
        }

        /** Tira o item do topo e devolve. Custo O(1). */
        char pop() {
            if (vazia()) {
                throw new IllegalStateException("Pilha vazia.");
            }
            char c = dados[topo];
            topo--;
            return c;
        }

        /** Espia o topo sem tirar. Custo O(1). */
        char peek() {
            if (vazia()) {
                throw new IllegalStateException("Pilha vazia.");
            }
            return dados[topo];
        }

        boolean vazia() {
            return topo == -1;
        }

        int tamanho() {
            return topo + 1;
        }

        /** Mostra a pilha da base para o topo. */
        void imprimir() {
            System.out.print("base -> ");
            for (int i = 0; i <= topo; i++) {
                System.out.print(dados[i] + " ");
            }
            System.out.println("<- topo");
        }
    }

    // =====================================================================
    // Aplicação: os parênteses estão bem fechados?
    // =====================================================================

    /**
     * Verifica se ( ), [ ] e { } estão corretamente emparelhados.
     *
     * <p>Regra: abriu, empilha; fechou, tem de casar com o topo.
     * No fim, a pilha precisa estar vazia.</p>
     */
    static boolean balanceada(String expressao) {
        Pilha pilha = new Pilha(expressao.length() + 1);

        for (int i = 0; i < expressao.length(); i++) {
            char c = expressao.charAt(i);

            if (c == '(' || c == '[' || c == '{') {
                pilha.push(c);                       // guardou o que abriu
            } else if (c == ')' || c == ']' || c == '}') {
                if (pilha.vazia()) {
                    return false;                    // fechou sem ter aberto
                }
                char aberto = pilha.pop();           // o último que abriu
                if (!combina(aberto, c)) {
                    return false;                    // fechou com o par errado
                }
            }
        }
        return pilha.vazia();                        // sobrou algo aberto?
    }

    /** Diz se o par de símbolos combina: ( com ), [ com ], { com }. */
    static boolean combina(char aberto, char fechado) {
        return (aberto == '(' && fechado == ')')
            || (aberto == '[' && fechado == ']')
            || (aberto == '{' && fechado == '}');
    }

    // =====================================================================
    // Programa principal
    // =====================================================================
    public static void main(String[] args) {

        System.out.println("=== 1) LIFO: o último a entrar é o primeiro a sair ===");
        System.out.println("Imagine uma pilha de pratos na pia.");

        Pilha pratos = new Pilha(5);
        pratos.push('A');
        pratos.push('B');
        pratos.push('C');
        System.out.print("depois de push(A), push(B), push(C):  ");
        pratos.imprimir();

        System.out.println("peek() = " + pratos.peek() + "   (só espiou, não tirou)");
        System.out.println("pop()  = " + pratos.pop()  + "   (tirou o C)");
        System.out.println("pop()  = " + pratos.pop()  + "   (tirou o B)");
        System.out.print("como ficou:                           ");
        pratos.imprimir();
        System.out.println("tamanho = " + pratos.tamanho());

        System.out.println();
        System.out.println("=== 2) APLICAÇÃO: parênteses balanceados ===");
        System.out.println("É assim que o editor de código acusa parêntese esquecido.");
        System.out.println();

        String[] testes = {
            "(a + b) * (c - d)",   // certo
            "{[()]}",              // certo
            "((x + y)",            // faltou fechar
            "{[(])}",              // fechou na ordem errada
            "a + b)"               // fechou sem abrir
        };

        for (String teste : testes) {
            System.out.printf("%-20s -> %s%n",
                    teste, balanceada(teste) ? "OK" : "ERRO");
        }

        System.out.println();
        System.out.println("=== 3) A PILHA QUE VOCÊ JÁ USAVA SEM SABER ===");
        contagemRegressiva(3);
        System.out.println("Cada chamada de método vira um quadro na pilha da JVM.");
        System.out.println("Quando o método termina, o quadro sai (pop).");
        System.out.println("Recursão sem parada enche essa pilha: StackOverflowError.");
    }

    /** Recursão simples: mostra a pilha de chamadas na prática. */
    static void contagemRegressiva(int n) {
        if (n == 0) {                                  // caso base
            System.out.println("   fim!");
            return;
        }
        System.out.println("   entrando com n = " + n);
        contagemRegressiva(n - 1);                     // empilha outra chamada
        System.out.println("   voltando  com n = " + n);
    }
}
