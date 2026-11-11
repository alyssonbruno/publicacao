import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

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
public class LogAppendOnly implements AutoCloseable {
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
