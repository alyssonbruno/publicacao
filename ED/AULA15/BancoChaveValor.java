import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class BancoChaveValor implements SGBD {
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
