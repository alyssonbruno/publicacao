import java.util.*;

record Produto(String nome, double preco, int estoque) {}

/**
 * Aula 7 — Hands-on: ORDENAR OBJETOS com Comparator.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>ordenar não é só para números: aqui a lista é de produtos;</li>
 *   <li>o critério de comparação fica no Comparator, fora do algoritmo;</li>
 *   <li>thenComparing resolve o empate — Teclado e Webcam custam o mesmo;</li>
 *   <li>reversed() inverte o critério sem reescrever a comparação.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class OrdenaProdutos {
    public static void main(String[] args) {
        var produtos = new ArrayList<>(List.of(
            new Produto("Teclado",    149.90, 50),
            new Produto("Monitor",   899.00,  8),
            new Produto("Mouse",      79.90, 120),
            new Produto("Headset",   249.90, 15),
            new Produto("Webcam",    149.90, 30)
        ));

        // 1. Ordenar por preço crescente; se igual, por nome alfabético
        produtos.sort(
            Comparator.comparingDouble(Produto::preco)
                      .thenComparing(Produto::nome)
        );

        System.out.println("=== Por preço (crescente) ===");
        produtos.forEach(p ->
            System.out.printf("%-12s R$ %7.2f%n", p.nome(), p.preco()));

        // 2. Ordenar por estoque decrescente
        produtos.sort(Comparator.comparingInt(Produto::estoque).reversed());

        System.out.println("\n=== Por estoque (decrescente) ===");
        produtos.forEach(p ->
            System.out.printf("%-12s %d un.%n", p.nome(), p.estoque()));
    }
}
