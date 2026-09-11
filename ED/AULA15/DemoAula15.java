import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Demonstracao completa para a Aula 15.
 *
 * <p>Fluxo:
 * 1. limpa o arquivo de log anterior;
 * 2. abre uma primeira "sessao" do banco;
 * 3. grava, consulta e remove registros;
 * 4. fecha o banco;
 * 5. reabre o mesmo arquivo para provar a recuperacao do estado.</p>
 */
public class DemoAula15 {

    public static void main(String[] args) throws IOException {
        Path arquivoLog = Path.of("aula15-demo.log");
        Files.deleteIfExists(arquivoLog);

        System.out.println("=== Aula 15 - Demo de Banco Chave-Valor ===");
        System.out.println("Arquivo de log: " + arquivoLog.toAbsolutePath());

        primeiraSessao(arquivoLog);
        segundaSessao(arquivoLog);
        mostrarArquivoGerado(arquivoLog);
    }

    private static void primeiraSessao(Path arquivoLog) {
        System.out.println("\n--- Sessao 1: gravando dados ---");

        try (SGBD banco = new BancoChaveValor(arquivoLog)) {
            banco.put("cliente:003", "Carla");
            banco.put("cliente:001", "Ana");
            banco.put("cliente:002", "Bruno");
             banco.put("cliente:004", "João");

            System.out.println("cliente:001 -> " + banco.get("cliente:001"));
            System.out.println("cliente:002 -> " + banco.get("cliente:002"));
            System.out.println("Chaves em ordem: " + banco.listarChavesEmOrdem());

            banco.put("cliente:002", "Bruno Silva");
            banco.delete("cliente:003");

            System.out.println("Tamanho ao fechar a sessao 1: " + banco.tamanho());
            System.out.println("Chaves finais da sessao 1: " + banco.listarChavesEmOrdem());
        }
    }

    private static void segundaSessao(Path arquivoLog) {
        System.out.println("\n--- Sessao 2: reabrindo e recuperando do log ---");

        try (SGBD banco = new BancoChaveValor(arquivoLog)) {
            System.out.println("Tamanho recuperado: " + banco.tamanho());
            System.out.println("cliente:001 -> " + banco.get("cliente:001"));
            System.out.println("cliente:002 -> " + banco.get("cliente:002"));
            System.out.println("cliente:003 -> " + banco.get("cliente:003"));
            System.out.println("Chaves em ordem apos reconstruir: " + banco.listarChavesEmOrdem());
        }
    }

    private static void mostrarArquivoGerado(Path arquivoLog) throws IOException {
        System.out.println("\n--- Conteudo do log gerado ---");
        List<String> linhas = Files.readAllLines(arquivoLog, StandardCharsets.UTF_8);
        for (String linha : linhas) {
            System.out.println(linha);
        }
    }
}
