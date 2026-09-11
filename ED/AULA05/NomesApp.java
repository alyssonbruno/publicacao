import java.util.ArrayList;
import java.util.Scanner;

/**
 * Hands-on 1 — Cadastro de nomes com ArrayList.
 *
 * <p>Demonstra o crescimento automático do ArrayList conforme
 * novos nomes são inseridos via console.</p>
 *
 * <p>No JDoodle, cole este arquivo inteiro e ative o Interactive Mode.</p>
 *
 * @author Alysson M. Bruno
 * @version 1.0
 */
public class NomesApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ArrayList<String> nomes = new ArrayList<>();

        System.out.println("=== Cadastro de Nomes (ArrayList) ===");
        System.out.println("Digite nomes (linha em branco para encerrar):");

        while (true) {
            System.out.print("> ");
            String nome = sc.nextLine().trim();
            if (nome.isEmpty()) break;
            nomes.add(nome);
            System.out.println("  Lista tem agora " + nomes.size() + " elemento(s).");
        }

        System.out.println("\n--- Nomes cadastrados ---");
        for (int i = 0; i < nomes.size(); i++) {
            System.out.println((i + 1) + ". " + nomes.get(i));
        }
        sc.close();
    }
}
