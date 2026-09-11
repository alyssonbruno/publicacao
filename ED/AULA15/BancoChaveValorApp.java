import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

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
/**
 * Aula 15 — Hands-on: BANCO CHAVE-VALOR com log append-only.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>toda escrita vira uma linha nova no log — nada é apagado no lugar;</li>
 *   <li>o índice fica em memória: um HashMap para achar a chave e um TreeMap para listá-las em ordem;</li>
 *   <li>ao reabrir o banco, o estado é reconstruído lendo o log do início ao fim;</li>
 *   <li>a remoção grava uma marca (tombstone), não apaga a linha antiga;</li>
 *   <li>no fim, o conteúdo do arquivo de log é impresso na tela.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class BancoChaveValorApp {

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

/**
 * TAD de um banco de dados chave-valor didatico.
 *
 * <p>Este contrato especifica o que o banco faz, sem expor como ele
 * persiste ou organiza internamente os dados.</p>
 *
 * <p><strong>Invariante:</strong> para toda chave valida, no maximo um valor
 * atual esta associado a ela no estado corrente do banco.</p>
 */
interface SGBD extends AutoCloseable {

    /**
     * Insere ou atualiza o valor associado a uma chave.
     *
     * @param chave chave nao nula e nao vazia
     * @param valor valor nao nulo
     * @throws NullPointerException se chave ou valor forem nulos
     * @throws IllegalArgumentException se a chave for vazia
     * Post: get(chave) devolve valor.
     */
    void put(String chave, String valor);

    /**
     * Consulta o valor associado a uma chave.
     *
     * @param chave chave nao nula e nao vazia
     * @return valor associado ou null se nao existir
     * @throws NullPointerException se chave for nula
     * @throws IllegalArgumentException se a chave for vazia
     */
    String get(String chave);

    /**
     * Remove a chave do banco.
     *
     * @param chave chave nao nula e nao vazia
     * @throws NullPointerException se chave for nula
     * @throws IllegalArgumentException se a chave for vazia
     * Post: get(chave) devolve null.
     */
    void delete(String chave);

    /**
     * Lista as chaves em ordem crescente.
     *
     * @return lista ordenada das chaves atuais
     */
    List<String> listarChavesEmOrdem();

    /**
     * Informa a quantidade de chaves atualmente armazenadas.
     *
     * @return numero de pares chave-valor presentes
     */
    int tamanho();

    /**
     * Fecha o banco e libera recursos de persistencia.
     */
    void fechar();

    @Override
    default void close() {
        fechar();
    }
}

/**
 * Tipos de operacao persistidos no log.
 */
enum TipoOperacao {
    PUT,
    DEL
}

/**
 * Representa uma linha do arquivo de log.
 */
class RegistroLog {
    private final TipoOperacao operacao;
    private final String chave;
    private final String valor;

    public RegistroLog(TipoOperacao operacao, String chave, String valor) {
        this.operacao = operacao;
        this.chave = chave;
        this.valor = valor;
    }

    public TipoOperacao getOperacao() {
        return operacao;
    }

    public String getChave() {
        return chave;
    }

    public String getValor() {
        return valor;
    }

    public String paraLinha() {
        String valorSeguro = valor == null ? "" : valor.replace("\n", "\\n").replace("|", "\\|");
        return operacao + "|" + chave + "|" + valorSeguro;
    }

    public static RegistroLog deLinha(String linha) {
        String[] partes = linha.split("(?<!\\\\)\\|", 3);
        if (partes.length < 2) {
            throw new IllegalArgumentException("Linha de log invalida: " + linha);
        }

        TipoOperacao operacao = TipoOperacao.valueOf(partes[0]);
        String chave = partes[1];
        String valor = partes.length == 3 ? partes[2].replace("\\|", "|").replace("\\n", "\n") : "";

        return new RegistroLog(operacao, chave, valor);
    }

    @Override
    public String toString() {
        return paraLinha();
    }
}

/**
 * Camada de persistencia baseada em arquivo append-only.
 */
class LogAppendOnly implements AutoCloseable {
    private final Path arquivo;
    private final FileChannel canal;

    public LogAppendOnly(Path arquivo) {
        try {
            this.arquivo = arquivo;
            Path pastaPai = arquivo.getParent();
            if (pastaPai != null) {
                Files.createDirectories(pastaPai);
            }
            this.canal = FileChannel.open(
                    arquivo,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel abrir o log: " + arquivo, e);
        }
    }

    public synchronized void registrar(RegistroLog registro) {
        try {
            canal.position(canal.size());
            byte[] bytes = (registro.paraLinha() + System.lineSeparator())
                    .getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) {
                canal.write(buffer);
            }
            canal.force(true);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gravar no log", e);
        }
    }

    public List<RegistroLog> lerTodos() {
        List<RegistroLog> registros = new ArrayList<>();
        try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (!linha.isBlank()) {
                    registros.add(RegistroLog.deLinha(linha));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler o log", e);
        }
        return registros;
    }

    public Path getArquivo() {
        return arquivo;
    }

    @Override
    public void close() {
        try {
            canal.close();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao fechar o log", e);
        }
    }
}

/**
 * Implementacao didatica de um banco chave-valor com:
 * - mapa principal em memoria;
 * - indice ordenado secundario por chave;
 * - persistencia em append-only log;
 * - reconstrucao do estado na inicializacao.
 */
class BancoChaveValor implements SGBD {
    private final Map<String, String> memoria = new HashMap<>();
    private final NavigableMap<String, String> indiceOrdenado = new TreeMap<>();
    private final LogAppendOnly log;

    public BancoChaveValor(Path arquivoLog) {
        this.log = new LogAppendOnly(arquivoLog);
        reconstruirEstado();
    }

    @Override
    public synchronized void put(String chave, String valor) {
        validarChave(chave);
        Objects.requireNonNull(valor, "valor nao pode ser nulo");

        RegistroLog registro = new RegistroLog(TipoOperacao.PUT, chave, valor);
        log.registrar(registro);

        memoria.put(chave, valor);
        indiceOrdenado.put(chave, valor);
    }

    @Override
    public synchronized String get(String chave) {
        validarChave(chave);
        return memoria.get(chave);
    }

    @Override
    public synchronized void delete(String chave) {
        validarChave(chave);

        RegistroLog registro = new RegistroLog(TipoOperacao.DEL, chave, "");
        log.registrar(registro);

        memoria.remove(chave);
        indiceOrdenado.remove(chave);
    }

    @Override
    public synchronized List<String> listarChavesEmOrdem() {
        return new ArrayList<>(indiceOrdenado.keySet());
    }

    @Override
    public synchronized int tamanho() {
        return memoria.size();
    }

    @Override
    public synchronized void fechar() {
        log.close();
    }

    private void reconstruirEstado() {
        for (RegistroLog registro : log.lerTodos()) {
            aplicarEmMemoria(registro);
        }
    }

    private void aplicarEmMemoria(RegistroLog registro) {
        if (registro.getOperacao() == TipoOperacao.PUT) {
            memoria.put(registro.getChave(), registro.getValor());
            indiceOrdenado.put(registro.getChave(), registro.getValor());
            return;
        }
        memoria.remove(registro.getChave());
        indiceOrdenado.remove(registro.getChave());
    }

    private void validarChave(String chave) {
        Objects.requireNonNull(chave, "chave nao pode ser nula");
        if (chave.isBlank()) {
            throw new IllegalArgumentException("chave nao pode ser vazia");
        }
    }
}
