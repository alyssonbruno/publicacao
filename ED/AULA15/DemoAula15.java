import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DemoAula15 {

    public static void main(String[] args) throws IOException {
        Path arquivoLog = Path.of("aula15-demo.log");
        Files.deleteIfExists(arquivoLog);   // começa do zero a cada execução

        System.out.println("=== Aula 15 - Banco chave-valor (o núcleo do projeto-base da A2) ===");

        primeiraSessao(arquivoLog);
        segundaSessao(arquivoLog);
        mostrarArquivoGerado(arquivoLog);
    }

    // Sessão 1: grava, consulta, atualiza e remove. Toda alteração vai primeiro para o log.
    private static void primeiraSessao(Path arquivoLog) {
        System.out.println("\n--- Sessão 1: gravando dados ---");

        try (SGBD banco = new BancoChaveValor(arquivoLog)) {
            banco.put("cliente:003", "Carla");
            banco.put("cliente:001", "Ana");
            banco.put("cliente:002", "Bruno");
            banco.put("cliente:004", "João|Palmas|TO");   // valor com três campos

            System.out.println("cliente:001 -> " + banco.get("cliente:001"));
            System.out.println("cliente:004 -> " + banco.get("cliente:004"));
            System.out.println("Chaves em ordem: " + banco.listarChavesEmOrdem());

            banco.put("cliente:002", "Bruno Silva");      // atualização
            boolean removeu = banco.delete("cliente:003");
            boolean removeuDeNovo = banco.delete("cliente:003");

            System.out.println("delete(cliente:003) = " + removeu + "; de novo = " + removeuDeNovo);
            System.out.println("Tamanho ao fechar a sessão 1: " + banco.tamanho());
        }
    }

    // Sessão 2: abre o mesmo arquivo. O estado volta porque o banco relê o log.
    private static void segundaSessao(Path arquivoLog) {
        System.out.println("\n--- Sessão 2: reabrindo e reconstruindo pelo log ---");

        try (SGBD banco = new BancoChaveValor(arquivoLog)) {
            System.out.println("Tamanho recuperado: " + banco.tamanho());
            System.out.println("cliente:002 -> " + banco.get("cliente:002"));
            System.out.println("cliente:003 -> " + banco.get("cliente:003"));
            System.out.println("cliente:004 -> " + banco.get("cliente:004"));
            System.out.println("Chaves em ordem: " + banco.listarChavesEmOrdem());
        }
    }

    // O log é texto: dá para ler cada operação que o banco gravou.
    private static void mostrarArquivoGerado(Path arquivoLog) throws IOException {
        List<String> linhas = Files.readAllLines(arquivoLog, StandardCharsets.UTF_8);
        System.out.println("\n--- Conteúdo do log (" + linhas.size() + " linhas, "
                + Files.size(arquivoLog) + " bytes) ---");
        for (String linha : linhas) {
            System.out.println(linha);
        }
    }
}
