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
 * Camada de persistencia baseada em arquivo append-only.
 */
public class LogAppendOnly implements AutoCloseable {
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
