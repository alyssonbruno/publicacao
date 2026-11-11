import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aula 15 — Hands-on: BANCO CHAVE-VALOR com log append-only.
 *
 * <p>As classes SGBD, TipoOperacao, RegistroLog, LogAppendOnly e BancoChaveValor
 * deste arquivo são as do projeto-base da A2 (2026/2), sem o package: é o mesmo
 * núcleo que os grupos usam, num arquivo só, para rodar no celular.</p>
 *
 * <p>Como usar: no OneCompiler (https://onecompiler.com/java) roda como está:
 * apague o exemplo da tela, cole este arquivo inteiro e clique em <b>Run</b>.
 * No JDoodle (https://www.jdoodle.com/online-java-compiler-ide), deixe a
 * versão mais nova e troque <b>public class BancoChaveValorApp</b> por
 * <b>public class MyClass</b>; depois, clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>toda alteração vira uma linha nova no fim do log: nada é apagado nem reescrito;</li>
 *   <li>a memória é um HashMap; para listar em ordem, o banco ordena as chaves na hora;</li>
 *   <li>o valor "João|Palmas|TO" vai para o log como João\|Palmas\|TO: a barra é escapada;</li>
 *   <li>o segundo delete da mesma chave devolve false e não grava nada no log;</li>
 *   <li>a remoção grava uma linha DEL; a linha antiga do PUT continua no arquivo;</li>
 *   <li>na sessão 2, o estado volta porque o banco relê o log do começo ao fim.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 2.0 (2026/2)
 */
public class BancoChaveValorApp {

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

/**
 * TAD do SGBD chave-valor da A2: diz <em>o que</em> o banco faz, sem dizer
 * <em>como</em> ele guarda os dados.
 *
 * <p>Este é o <strong>núcleo comum</strong>, igual para os sete grupos. Cada grupo
 * cria a própria interface, que estende esta e acrescenta as operações do seu
 * caso. Exemplo:</p>
 *
 * <pre>
 * public interface SGBDBiblioteca extends SGBD {
 *     List&lt;String&gt; listarPorTitulo();
 * }
 * </pre>
 *
 * <p><strong>Invariantes</strong> (valem depois de qualquer operação):</p>
 * <ol>
 *   <li>cada chave tem no máximo um valor atual;</li>
 *   <li>toda alteração ({@code put} ou {@code delete} que remove algo) já está
 *       gravada no log quando o método termina;</li>
 *   <li>fechar e reabrir o banco sobre o mesmo arquivo de log devolve o mesmo
 *       estado de antes de fechar.</li>
 * </ol>
 *
 * <p><strong>Regras de chave e valor</strong>, válidas em todas as operações: a
 * chave não pode ser nula nem vazia (só espaços conta como vazia); o valor não
 * pode ser nulo. Depois de {@link #fechar()}, qualquer operação lança
 * {@link IllegalStateException}.</p>
 */
interface SGBD extends AutoCloseable {

    /**
     * Salva o valor da chave. Se a chave já existe, atualiza o valor.
     *
     * <p>Pré: chave válida e valor não nulo.<br>
     * Pós: {@code get(chave)} devolve {@code valor}, e a operação já está no log.</p>
     *
     * @param chave chave não nula e não vazia
     * @param valor valor não nulo (pode ser texto vazio)
     * @throws NullPointerException se a chave ou o valor forem nulos
     * @throws IllegalArgumentException se a chave for vazia
     */
    void put(String chave, String valor);

    /**
     * Recupera o valor atual da chave.
     *
     * @param chave chave não nula e não vazia
     * @return o valor atual, ou {@code null} se a chave não existe
     */
    String get(String chave);

    /**
     * Remove a chave e o seu valor.
     *
     * <p>Pós: {@code get(chave)} devolve {@code null}. Se a chave não existia,
     * nada muda e nada é gravado no log.</p>
     *
     * @param chave chave não nula e não vazia
     * @return {@code true} se a chave existia e foi removida
     */
    boolean delete(String chave);

    /**
     * Diz se a chave existe no banco.
     *
     * @param chave chave não nula e não vazia
     * @return {@code true} se há um valor atual para a chave
     */
    default boolean contem(String chave) {
        return get(chave) != null;
    }

    /**
     * Quantidade de chaves guardadas agora.
     *
     * @return número de pares chave-valor
     */
    int tamanho();

    /**
     * Lista as chaves atuais em ordem crescente (a ordem de {@link String#compareTo}).
     *
     * <p>O custo depende da estrutura: numa árvore de busca basta o percurso em
     * ordem; numa tabela hash é preciso ordenar as chaves antes.</p>
     *
     * @return nova lista com as chaves em ordem crescente
     */
    List<String> listarChavesEmOrdem();

    /**
     * Fecha o banco e libera o arquivo de log. Chamar duas vezes não dá erro.
     */
    void fechar();

    /** Permite usar o banco em {@code try (SGBD banco = ...) { ... }}. */
    @Override
    default void close() {
        fechar();
    }
}

/**
 * Tipos de operação gravados no log.
 *
 * <p>{@code PUT} salva ou atualiza um valor; {@code DEL} remove uma chave.
 * Um grupo pode acrescentar tipos novos, desde que explique no relatório como
 * a reconstrução trata cada um.</p>
 */
enum TipoOperacao {
    PUT,
    DEL
}

/**
 * Uma linha do arquivo de log: a operação, a chave e o valor.
 *
 * <p>Formato da linha: {@code OPERACAO|chave|valor}. Exemplos:</p>
 *
 * <pre>
 * PUT|produto:001|Lápis preto\|1.20\|300
 * DEL|produto:003|
 * </pre>
 *
 * <p>Como a barra vertical separa os campos, ela é "escapada" dentro da chave e
 * do valor. As trocas feitas na gravação são: {@code \} vira {@code \\},
 * {@code |} vira {@code \|}, quebra de linha vira {@code \n} e retorno de carro
 * vira {@code \r}. A leitura desfaz as trocas. Assim, qualquer texto pode ser
 * chave ou valor sem quebrar o arquivo.</p>
 */
final class RegistroLog {
    private final TipoOperacao operacao;
    private final String chave;
    private final String valor;

    public RegistroLog(TipoOperacao operacao, String chave, String valor) {
        this.operacao = Objects.requireNonNull(operacao, "operação não pode ser nula");
        this.chave = Objects.requireNonNull(chave, "chave não pode ser nula");
        this.valor = valor == null ? "" : valor;
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

    /** Transforma o registro numa linha de texto, sem a quebra de linha final. */
    public String paraLinha() {
        return operacao.name() + "|" + escapar(chave) + "|" + escapar(valor);
    }

    /**
     * Lê uma linha do log e devolve o registro correspondente.
     *
     * @throws IllegalArgumentException se a linha não tiver os três campos
     *         ou se a operação for desconhecida
     */
    public static RegistroLog deLinha(String linha) {
        List<String> campos = separarCampos(linha);
        if (campos.size() != 3) {
            throw new IllegalArgumentException("linha de log inválida: " + linha);
        }
        TipoOperacao operacao = TipoOperacao.valueOf(campos.get(0));
        return new RegistroLog(operacao, campos.get(1), campos.get(2));
    }

    private static String escapar(String texto) {
        StringBuilder saida = new StringBuilder(texto.length() + 8);
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '\\' -> saida.append("\\\\");
                case '|' -> saida.append("\\|");
                case '\n' -> saida.append("\\n");
                case '\r' -> saida.append("\\r");
                default -> saida.append(c);
            }
        }
        return saida.toString();
    }

    /** Divide a linha nas barras que não estão escapadas, desfazendo os escapes. */
    private static List<String> separarCampos(String linha) {
        List<String> campos = new ArrayList<>(3);
        StringBuilder atual = new StringBuilder();
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '\\' && i + 1 < linha.length()) {
                char seguinte = linha.charAt(++i);
                switch (seguinte) {
                    case 'n' -> atual.append('\n');
                    case 'r' -> atual.append('\r');
                    default -> atual.append(seguinte);   // \\ e \|
                }
            } else if (c == '|') {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos;
    }

    @Override
    public String toString() {
        return paraLinha();
    }
}

/**
 * Persistência do banco: um arquivo em que só se acrescenta (append-only).
 *
 * <p>Cada {@code put} e cada {@code delete} viram uma linha nova no fim do
 * arquivo. Nada é apagado nem reescrito. Para recuperar o estado, o banco relê
 * o arquivo do começo ao fim e refaz as operações na mesma ordem.</p>
 *
 * <p>Depois de gravar, {@link FileChannel#force(boolean)} pede ao sistema
 * operacional que leve os bytes para o disco antes de o método voltar.</p>
 */
class LogAppendOnly implements AutoCloseable {
    private final Path arquivo;
    private final FileChannel canal;
    private boolean fechado;

    /**
     * Abre o log, criando o arquivo (e a pasta) se ainda não existirem.
     *
     * @param arquivo caminho do arquivo de log
     */
    public LogAppendOnly(Path arquivo) {
        this.arquivo = arquivo;
        try {
            Path pasta = arquivo.toAbsolutePath().getParent();
            if (pasta != null) {
                Files.createDirectories(pasta);
            }
            this.canal = FileChannel.open(arquivo,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("não foi possível abrir o log: " + arquivo, e);
        }
    }

    /**
     * Acrescenta o registro no fim do arquivo e força a gravação no disco.
     *
     * @param registro operação a gravar
     */
    public synchronized void registrar(RegistroLog registro) {
        if (fechado) {
            throw new IllegalStateException("o log já foi fechado");
        }
        byte[] bytes = (registro.paraLinha() + "\n").getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        try {
            while (buffer.hasRemaining()) {
                canal.write(buffer);
            }
            canal.force(true);
        } catch (IOException e) {
            throw new IllegalStateException("falha ao gravar no log", e);
        }
    }

    /**
     * Lê todas as linhas do arquivo, na ordem em que foram gravadas.
     *
     * @return os registros do log; lista vazia se o arquivo estiver vazio
     * @throws IllegalStateException se alguma linha estiver corrompida
     */
    public List<RegistroLog> lerTodos() {
        List<RegistroLog> registros = new ArrayList<>();
        try (BufferedReader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            String linha;
            int numero = 0;
            while ((linha = leitor.readLine()) != null) {
                numero++;
                if (linha.isBlank()) {
                    continue;
                }
                try {
                    registros.add(RegistroLog.deLinha(linha));
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException(
                            "linha " + numero + " do log está corrompida: " + linha, e);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("falha ao ler o log", e);
        }
        return registros;
    }

    /** Tamanho atual do arquivo de log, em bytes. */
    public long tamanhoEmBytes() {
        try {
            return Files.size(arquivo);
        } catch (IOException e) {
            throw new IllegalStateException("falha ao medir o log", e);
        }
    }

    public Path getArquivo() {
        return arquivo;
    }

    /** Fecha o arquivo. Chamar duas vezes não dá erro. */
    @Override
    public synchronized void close() {
        if (fechado) {
            return;
        }
        fechado = true;
        try {
            canal.close();
        } catch (IOException e) {
            throw new IllegalStateException("falha ao fechar o log", e);
        }
    }
}

/**
 * Classe de exemplo: um SGBD chave-valor com {@link HashMap} na memória e
 * {@link LogAppendOnly} no disco.
 *
 * <p>Ela serve de ponto de partida. O grupo copia esta classe, troca o nome
 * para o do seu caso (por exemplo, {@code Biblioteca}) e troca ou complementa
 * o {@code HashMap} pela estrutura que o caso pede.</p>
 *
 * <p><strong>Ordem das coisas em cada alteração:</strong> primeiro grava no
 * log, depois muda a memória. Se a gravação falhar, a memória fica como
 * estava, e o banco não "lembra" de algo que o disco não guardou.</p>
 *
 * <p><strong>Custos desta versão</strong> (n = número de chaves):
 * {@code get}, {@code put} e {@code delete} custam O(1) em média na memória,
 * mais uma gravação no disco nas alterações; {@code listarChavesEmOrdem}
 * custa O(n log n), porque o {@code HashMap} não guarda ordem e é preciso
 * ordenar; abrir o banco custa O(m), sendo m o número de linhas do log.</p>
 */
class BancoChaveValor implements SGBD {
    private final Map<String, String> dados = new HashMap<>();
    private final LogAppendOnly log;
    private boolean fechado;

    /**
     * Abre o banco sobre o arquivo de log e reconstrói o estado a partir dele.
     *
     * @param arquivoLog caminho do arquivo de log (criado se não existir)
     */
    public BancoChaveValor(Path arquivoLog) {
        this.log = new LogAppendOnly(arquivoLog);
        reconstruirEstado();
    }

    @Override
    public synchronized void put(String chave, String valor) {
        validarChave(chave);
        Objects.requireNonNull(valor, "valor não pode ser nulo");
        garantirAberto();

        log.registrar(new RegistroLog(TipoOperacao.PUT, chave, valor));
        dados.put(chave, valor);
    }

    @Override
    public synchronized String get(String chave) {
        validarChave(chave);
        garantirAberto();
        return dados.get(chave);
    }

    @Override
    public synchronized boolean delete(String chave) {
        validarChave(chave);
        garantirAberto();
        if (!dados.containsKey(chave)) {
            return false;                       // nada a remover: não suja o log
        }
        log.registrar(new RegistroLog(TipoOperacao.DEL, chave, ""));
        dados.remove(chave);
        return true;
    }

    @Override
    public synchronized boolean contem(String chave) {
        validarChave(chave);
        garantirAberto();
        return dados.containsKey(chave);
    }

    @Override
    public synchronized int tamanho() {
        garantirAberto();
        return dados.size();
    }

    @Override
    public synchronized List<String> listarChavesEmOrdem() {
        garantirAberto();
        List<String> chaves = new ArrayList<>(dados.keySet());
        Collections.sort(chaves);               // O(n log n)
        return chaves;
    }

    @Override
    public synchronized void fechar() {
        if (fechado) {
            return;
        }
        fechado = true;
        log.close();
    }

    /** Relê o log do começo ao fim e refaz cada operação na memória. */
    private void reconstruirEstado() {
        for (RegistroLog registro : log.lerTodos()) {
            if (registro.getOperacao() == TipoOperacao.PUT) {
                dados.put(registro.getChave(), registro.getValor());
            } else {
                dados.remove(registro.getChave());
            }
        }
    }

    private void validarChave(String chave) {
        Objects.requireNonNull(chave, "chave não pode ser nula");
        if (chave.isBlank()) {
            throw new IllegalArgumentException("chave não pode ser vazia");
        }
    }

    private void garantirAberto() {
        if (fechado) {
            throw new IllegalStateException("o banco já foi fechado");
        }
    }
}
